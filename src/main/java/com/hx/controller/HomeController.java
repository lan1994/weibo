package com.hx.controller;

import com.hx.model.*;
import com.hx.service.CommentService;
import com.hx.service.FollowService;
import com.hx.service.QuestionService;
import com.hx.service.UserService;
import com.hx.util.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    private static final int pageSize = 5;
    private static final int pageDisplay = 20;
    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    FollowService followService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    private List<ViewObject> getQuestions(int userId, int offset, int limit) {
        List<Question> questionList = questionService.getLatestQuestions(userId, offset, limit);
        List<ViewObject> vos = new ArrayList<>();
        for (Question question : questionList) {
            ViewObject vo = getViewObject(question);
            vos.add(vo);
        }
        return vos;
    }

    private ViewObject getViewObject(Question question) {
        ViewObject vo = new ViewObject();
        vo.set("question", question);
        vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
        vo.set("user", userService.getUser(question.getUserId()));
        if (hostHolder.getUser()!=null)
            vo.set("followed",followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, question.getId()));
        return vo;
    }

    @RequestMapping(path = {"/", "/index"})
    public String index(){
        return "redirect:/home/1";
    }

    @RequestMapping(path = {"/home/{page}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String home(Model model,
                        @RequestParam(value = "pop", defaultValue = "0") int pop,
                        @PathVariable(value = "page") int page) {
        int localUserId = hostHolder.getUser() != null?hostHolder.getUser().getId():0;
        int questionCount = questionService.getQuestionCount(0);
        ViewObject result = new ViewObject();
        if (questionCount > 0) {
            try {
                Map<String, Object> pmap = PageUtil.execute(page, pageSize, questionCount, pageDisplay);
                int offset = (Integer) pmap.get("offset");
                int limit = (Integer) pmap.get("limit");
                List<ViewObject> vos = new ArrayList<>();
                for (Question question : questionService.getHotCommentQuestions(offset,limit)){
                    ViewObject vo = getViewObject(question);
                    vos.add(vo);
                }
                model.addAttribute("pageDisplayArray",pmap.get("pageArray"));
                model.addAttribute("vos", vos);
                model.addAttribute("nextPage",pmap.get("nextPage"));
                result.set("status",1);
                model.addAttribute("result", result);
            } catch (Exception e) {
                result.set("status",-1);
                result.set("msg", e.getMessage());
                model.addAttribute("result",result);
                logger.error(e.getMessage(), e);
            }
        }else {
            result.set("status",0);
            result.set("msg", "没有找到相关记录");
            model.addAttribute("result",result);
        }
        return "index";
    }
    @RequestMapping(path = {"/user/{userId}"})
    private String userIndexDefault(@PathVariable int userId){
        return String.format("redirect:/user/%d/1", userId);
    }

    @RequestMapping(path = {"/user/{userId}/{page}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model, @PathVariable("userId") int userId, @PathVariable int page) {
        int count = questionService.getQuestionCount(userId);
        try {
            Map<String,Object> pmap = PageUtil.execute(page,pageSize,count,pageDisplay);
            int offset = (Integer) pmap.get("offset");
            int limit = (Integer) pmap.get("limit");
            model.addAttribute("vos", getQuestions(userId, offset, limit));
            model.addAttribute("pageDisplayArray", pmap.get("pageArray"));
            model.addAttribute("nextPage", pmap.get("nextPage"));
            model.addAttribute("userId",userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        User user = userService.getUser(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        return "profile";
    }
}
