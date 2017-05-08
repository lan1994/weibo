package com.hx.controller;

import com.hx.async.EventModel;
import com.hx.async.EventProducer;
import com.hx.async.EventType;
import com.hx.model.*;
import com.hx.service.*;
import com.hx.util.PageUtil;
import com.hx.util.WeiBoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private static final int pageSize = 5;
    private static final int pageDisplay = 20;
    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    FollowService followService;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(value = "/question/{qid}", method = {RequestMethod.GET,RequestMethod.POST})
    public String questionDefault(@PathVariable("qid") int qid){
        return String.format("redirect:/question/%d/0", qid);
    }

    @RequestMapping(value = "/question/{qid}/{page}", method = {RequestMethod.GET,RequestMethod.POST})
    public String questionDetail(Model model,
                                 @PathVariable("qid") int qid,
                                 @PathVariable(value = "page") int page) {
        Question question = questionService.getById(qid);
        question.setUserImga(userService.getUser(question.getUserId()).getHeadUrl());
        model.addAttribute("question", question);
        List<Comment> commentList = null;
        List<ViewObject> comments = null;
        Map<String, Object> pmap = null;
        int commentCount = commentService.getCommentCount(qid, EntityType.ENTITY_QUESTION);

        try {
            pmap = PageUtil.execute(page, pageSize, commentCount, pageDisplay);
            int offset = (Integer) pmap.get("offset");
            int limit = (Integer) pmap.get("limit");

            commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION,offset,limit);
            comments = new ArrayList<ViewObject>();
            for (Comment comment : commentList) {
                ViewObject vo = new ViewObject();
                vo.set("comment", comment);
                if (hostHolder.getUser() == null) {
                    vo.set("liked", 0);
                } else {
                    vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
                }

                vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));
                vo.set("user", userService.getUser(comment.getUserId()));
                comments.add(vo);
            }
        }catch (Exception e){

        }
        model.addAttribute("pageDisplayArray",pmap.get("pageArray"));
        model.addAttribute("nextPage",pmap.get("nextPage"));
        model.addAttribute("comments", comments);
        model.addAttribute("commentss", null);
        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.getUser(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }

        return "detail";
    }

    @RequestMapping(value = "/question/add", method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title, @RequestParam("content") String content) {
        try {
            Question question = new Question();
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setTitle(title);
            if (hostHolder.getUser() == null) {
                question.setUserId(WeiBoUtil.ANONYMOUS_USERID);
                // return WendaUtil.getJSONString(999);
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            if (questionService.addQuestion(question) > 0) {
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                        .setActorId(question.getUserId()).setEntityId(question.getId())
                .setExt("title", question.getTitle()).setExt("content", question.getContent()));
                return WeiBoUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("增加题目失败" + e.getMessage());
        }
        return WeiBoUtil.getJSONString(1, "失败");
    }

    @RequestMapping(value = "/question/delete", method = {RequestMethod.POST,RequestMethod.GET})
    public String deleteQuestion(
            @RequestParam("id") int id,
            @RequestParam("userid") String userid) {
        try {
            Question question = new Question();
            if (hostHolder.getUser() == null) {
                question.setUserId(WeiBoUtil.ANONYMOUS_USERID);
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }
            boolean flag = questionService.deleteQuestion(id);
            if(flag){
                commentService.deleteCommentbyQuestion(id);
            }
        } catch (Exception e) {
            logger.error("增加题目失败" + e.getMessage());
        }
        return "redirect:"+userid;
    }
}
