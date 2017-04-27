package com.hx.controller;

import com.hx.model.*;
import com.hx.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;


@Controller
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    @Autowired
    SearchService searchService;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @RequestMapping(path = {"/search"}, method = {RequestMethod.GET})
    public String search(Model model, @RequestParam("q") String keyword,
                         @RequestParam(value = "offset", defaultValue = "0") int offset,
                         @RequestParam(value = "count", defaultValue = "10") int count) {
        try {
            List<Question> questionList = searchService.searchQuestion(keyword, offset, count,
                    "<em>", "</em>");
            List<ViewObject> vos = new ArrayList<>();
            for (Question question : questionList) {
                ViewObject vo = getViewObject(question);
                if(vo == null){
                    continue;
                }
                vos.add(vo);
            }
            List<User> userList = searchService.searchUser(keyword, offset, count,
                    "", "");
            List<Question> hotCommentQuestionList = questionService.getHotCommentQuestions(0, 20);
            List<ViewObject> hotCommentQuestionVos = new ArrayList<>();
            for (Question question:hotCommentQuestionList){
                ViewObject vo = getViewObject(question);
                if(vo == null){
                    continue;
                }
                hotCommentQuestionVos.add(vo);
            }
            model.addAttribute("vos", vos);
            model.addAttribute("users", userList);
            model.addAttribute("hotCommentQuestions", hotCommentQuestionVos);
            model.addAttribute("keyword", keyword);
        } catch (Exception e) {
            logger.error("搜索失败" + e.getMessage(), e);
        }
        return "result";
    }

    private ViewObject getViewObject(Question question) {
        Question q = questionService.getById(question.getId());
        int localUserId = hostHolder.getUser() != null ? hostHolder.getUser().getId() : 0;
        if (q == null) {
            return null;
        }
        ViewObject vo = new ViewObject();
        if (question.getContent() != null) {
            q.setContent(question.getContent());
        }
        if (question.getTitle() != null) {
            q.setTitle(question.getTitle());
        }
        vo.set("question", q);
        vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
        vo.set("followed",followService.isFollower(localUserId, EntityType.ENTITY_QUESTION, question.getId()));
        vo.set("user", userService.getUser(q.getUserId()));
        return vo;
    }
}
