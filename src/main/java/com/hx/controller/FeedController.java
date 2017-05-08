package com.hx.controller;

import com.hx.async.EventType;
import com.hx.model.*;
import com.hx.service.*;
import com.hx.util.JedisAdapter;
import com.hx.util.PageUtil;
import com.hx.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);
    private static final int pageSize = 5;
    private static final int pageDisplay = 5;
    @Autowired
    FeedService feedService;

    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @RequestMapping(path = {"/pushfeeds"})
    private String getPushFeedsDefault(){
        return "redirect:/pushfeeds/1";
    }
    @RequestMapping(path = {"/pushfeeds/{page}"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model, @PathVariable("page") int page) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        long feedsCount = 0;
        if (localUserId>0){
            feedsCount = jedisAdapter.llen(RedisKeyUtil.getTimelineKey(localUserId));
        }
        ViewObject result = new ViewObject();
        if (feedsCount>0){
            try {
                Map<String,Object> pmap = PageUtil.execute(page, pageSize, (int)feedsCount, pageDisplay);
                int start = (Integer) pmap.get("offset");
                int limit = (Integer)pmap.get("limit");
                int end = start+limit-1;
                List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), start, end);
                //List<Feed> feeds = new ArrayList<Feed>();
                List<ViewObject> vos = new ArrayList<ViewObject>();
                for (String feedId : feedIds) {
                    ViewObject vo;
                    Feed feed = feedService.getById(Integer.parseInt(feedId));
                    if (feed != null) {
                        vo = new ViewObject();
                        vo.set("type",feed.getType());
                        vo.set("questionId",feed.get("questionId"));
                        vo.set("questionTitle",feed.get("questionTitle"));
                        vo.set("userId",feed.get("userId"));
                        vo.set("userName",feed.get("userName"));
                        vo.set("userHead",feed.get("userHead"));
                        vo.set("createdDate",feed.getCreatedDate());
                        if(feed.getType()== EventType.ADD_QUESTION.getValue()){
                            Question question = questionService.getById(Integer.parseInt(feed.get("questionId")));
                            vo.set("commentCount",question.getCommentCount());
                            vo.set("content",question.getContent());
                            vo.set("followed",followService.isFollower(localUserId, EntityType.ENTITY_QUESTION, question.getId()));
                            vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
                        }
                        vos.add(vo);
                        //feeds.add(feed);
                    }
                }
                model.addAttribute("feeds", vos);
                model.addAttribute("pageDisplayArray", pmap.get("pageArray"));
                model.addAttribute("nextPage",pmap.get("nextPage"));
                model.addAttribute("type","pushfeeds");
                result.set("status",1);
                model.addAttribute("result",result);
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                result.set("status",-1);
                result.set("msg",e.getMessage());
                model.addAttribute("result",result);
            }
        }else {
            int questionCount = questionService.getQuestionCount(0);
            try {
                Map<String,Object> pmap = PageUtil.execute(page,pageSize,questionCount,pageDisplay);
                int offset = (Integer) pmap.get("offset");
                int limit = (Integer) pmap.get("limit");
                List<Question> hotCommentQuestions = questionService.getHotCommentQuestions(offset,limit);
                List<ViewObject> vos = new ArrayList<ViewObject>();
                for (Question q : hotCommentQuestions){
                    ViewObject vo = getVO(q);
                    vos.add(vo);
                }
                result.set("status",0);
                result.set("msg","没有关注信息，为您推荐热评微博");
                model.addAttribute("feeds", vos);
                model.addAttribute("result",result);
                model.addAttribute("pageDisplayArray", pmap.get("pageArray"));
                model.addAttribute("nextPage", pmap.get("nextPage"));
                model.addAttribute("type", "pushfeeds");
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                result.set("status",-1);
                result.set("msg",e.getMessage());
                model.addAttribute("result",result);
            }
        }


        return "feeds";
    }

    @RequestMapping(path = {"/pullfeeds"})
    private String getPullFeedsDefault(){
        return "redirect:/pullfeeds/1";
    }

    @RequestMapping(path = {"/pullfeeds/{page}"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPullFeeds(Model model, @PathVariable("page") int page) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<Integer>();
        ViewObject result = new ViewObject();
        if (localUserId != 0) {
            // 关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        //如果未登录或者没有关注的人，则返回10个没关联的feeds
        int count = feedService.getPullFeedsCount(followees, EventType.ADD_QUESTION);
        if (count>0) {
            try {
                Map<String, Object> pmap = PageUtil.execute(page, pageSize, count, pageDisplay);
                int offset = (Integer) pmap.get("offset");
                int limit = (Integer) pmap.get("limit");
                List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, EventType.ADD_QUESTION,offset, limit);
                List<ViewObject> vos = new ArrayList<ViewObject>();
                for (Feed feed : feeds) {
                    ViewObject vo = new ViewObject();
                    vo.set("type", feed.getType());
                    vo.set("questionId", feed.get("questionId"));
                    vo.set("questionTitle", feed.get("questionTitle"));
                    vo.set("userId", feed.get("userId"));
                    vo.set("userName", feed.get("userName"));
                    vo.set("userHead", feed.get("userHead"));
                    vo.set("createdDate", feed.getCreatedDate());
                    if (feed.getType() == EventType.ADD_QUESTION.getValue()) {
                        Question question = questionService.getById(Integer.parseInt(feed.get("questionId")));
                        if (question == null) {
                            continue;
                        }
                        vo.set("commentCount", question.getCommentCount());
                        vo.set("content", question.getContent());
                        vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_QUESTION, question.getId()));
                        vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
                    }
                    vos.add(vo);
                }
                result.set("status",1);
                model.addAttribute("feeds", vos);
                model.addAttribute("pageDisplayArray", pmap.get("pageArray"));
                model.addAttribute("nextPage", pmap.get("nextPage"));
                model.addAttribute("type", "pullfeeds");
            } catch (Exception e) {
                result.set("status",-1);
                result.set("msg",e.getMessage());
                logger.error(e.getMessage(),e);
            }
        }else {
            int questionCount = questionService.getQuestionCount(0);
            try {
                Map<String,Object> pmap = PageUtil.execute(page,pageSize,questionCount,pageDisplay);
                int offset = (Integer) pmap.get("offset");
                int limit = (Integer) pmap.get("limit");
                List<Question> hotCommentQuestions = questionService.getHotCommentQuestions(offset,limit);
                List<ViewObject> vos = new ArrayList<ViewObject>();
                for (Question q : hotCommentQuestions){
                    ViewObject vo = getVO(q);
                    vos.add(vo);
                }
                result.set("status",0);
                result.set("msg","没有关注信息，为您推荐热评微博");
                model.addAttribute("result",result);
                model.addAttribute("feeds", vos);
                model.addAttribute("pageDisplayArray", pmap.get("pageArray"));
                model.addAttribute("nextPage", pmap.get("nextPage"));
                model.addAttribute("type", "pullFeeds");
            }catch (Exception e){
                logger.error(e.getMessage(),e);
                result.set("status",-1);
                result.set("msg",e.getMessage());
                model.addAttribute("result",result);
            }
        }
        return "feeds";
    }

    private ViewObject getVO(Question question){
        User user = hostHolder.getUser();
        int localUserId = user!=null?user.getId():0;
        ViewObject vo = new ViewObject();
        vo.set("type", EventType.ADD_QUESTION.getValue());
        vo.set("questionId", question.getId());
        vo.set("questionTitle", question.getTitle());
        vo.set("userId", question.getUserId());
        User questionUser = userService.getUser(question.getUserId());
        vo.set("userName", questionUser.getName());
        vo.set("userHead", questionUser.getHeadUrl());
        vo.set("createdDate", question.getCreatedDate());

        vo.set("commentCount", question.getCommentCount());
        vo.set("content", question.getContent());
        vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_QUESTION, question.getId()));
        vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
        return vo;
    }
}
