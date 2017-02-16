package com.hx.controller;

import com.hx.async.EventType;
import com.hx.model.*;
import com.hx.service.*;
import com.hx.util.JedisAdapter;
import com.hx.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;


@Controller
public class FeedController {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

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

    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<String> feedIds = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId), 0, 10);
        //List<Feed> feeds = new ArrayList<Feed>();
        List<ViewObject> vos = new ArrayList<>();
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
                    vo.set("followed",followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, question.getId()));
                    vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
                }
                vos.add(vo);
                //feeds.add(feed);
            }
        }
        model.addAttribute("feeds", vos);
        return "feeds";
    }

    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPullFeeds(Model model) {
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        List<Integer> followees = new ArrayList<>();
        if (localUserId != 0) {
            // 关注的人
            followees = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);
        }
        List<Feed> feeds = feedService.getUserFeeds(Integer.MAX_VALUE, followees, 10);
        List<ViewObject> vos = new ArrayList<>();
        for (Feed feed : feeds){
            ViewObject vo = new ViewObject();
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
                vo.set("followed",followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, question.getId()));
                vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            }
            vos.add(vo);
        }
        model.addAttribute("feeds", vos);
        return "feeds";
    }
}
