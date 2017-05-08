package com.hx.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hx.async.EventModel;
import com.hx.async.EventProducer;
import com.hx.async.EventType;
import com.hx.model.Comment;
import com.hx.model.EntityType;
import com.hx.model.HostHolder;
import com.hx.model.ViewObject;
import com.hx.service.CommentService;
import com.hx.service.QuestionService;
import com.hx.service.UserService;
import com.hx.util.WeiBoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.*;
import static com.hx.util.WeiBoUtil.dateToString;


@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    QuestionService questionService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    UserService userService;

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST,RequestMethod.GET})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content,
                             @RequestParam("parentId") String parentId,
                             @RequestParam("toUserId") String toUserId) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCount(0);
            comment.setParent(Integer.parseInt(parentId));
            comment.setToUserId(Integer.parseInt(toUserId));
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(WeiBoUtil.ANONYMOUS_USERID);
                // return "redirect:/reglogin";
            }
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            if(Integer.parseInt(parentId)>0){
                int currentCount = commentService.getCommentById(Integer.parseInt(parentId)).getCount();
                comment.setCount(currentCount+1);
            }else{
                comment.setCount(0);
            }
            commentService.updateCommentCount(comment);

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                    .setEntityId(questionId));

        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
            e.printStackTrace();

        }
        return "redirect:/question/" + questionId+"/0";
    }


    @RequestMapping(path = {"/deleteComment"}, method = {RequestMethod.POST,RequestMethod.GET})
    public String deleteComment(@RequestParam("commentId") int commentId,
                                @RequestParam("questionId") int questionId,
                                @RequestParam("child") boolean child,
                                @RequestParam("parentId") String parentId) {
        try {
            Comment comment = new Comment();
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(WeiBoUtil.ANONYMOUS_USERID);
                // return "redirect:/reglogin";
            }
            int Commentcount = 0;
            if(Integer.parseInt(parentId)!=0){
                Commentcount = commentService.getCommentById(Integer.parseInt(parentId)).getCount();
            }

            comment.setId(commentId);
            comment.setParent(Integer.parseInt(parentId));
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.deleteComment(commentId);
            if(child){
                comment.setCount(Commentcount-1);
                commentService.updateCommentCount(comment);
            }

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                    .setEntityId(questionId));

        } catch (Exception e) {
            logger.error("删除评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId+"/0";
    }


    @RequestMapping(path = {"/editComment"}, method = {RequestMethod.POST,RequestMethod.GET})
    public String editComment(@RequestParam("commentId") int commentId,
                              @RequestParam("questionId") int questionId,
                              @RequestParam("content") String content) {
        try {
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                comment.setUserId(WeiBoUtil.ANONYMOUS_USERID);
                // return "redirect:/reglogin";
            }
            comment.setId(commentId);
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.updateComment(comment);

            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            questionService.updateCommentCount(comment.getEntityId(), count);

            eventProducer.fireEvent(new EventModel(EventType.COMMENT).setActorId(comment.getUserId())
                    .setEntityId(questionId));

        } catch (Exception e) {
            logger.error("修改评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId+"/0";
    }

    @RequestMapping(path = {"/getComment"}, method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String editComment(Model model, @RequestParam("commentId") int commentId,
                              @RequestParam("questionId") int questionId) {
        List<Comment> commentList = commentService.getCommentChild(commentId);
        JSONArray jsonArray = new JSONArray();
        List<ViewObject> comments = null;
        comments = new ArrayList<ViewObject>();
        for (Comment comment : commentList) {
            JSONObject json = new JSONObject();
            json.put("id",comment.getId());
            json.put("userId",comment.getUserId());
            json.put("username",userService.getUser(comment.getUserId()).getName());
            json.put("userurl",userService.getUser(comment.getUserId()).getHeadUrl());
            json.put("entityId",comment.getEntityId());
            json.put("content",comment.getContent());
            json.put("createdDate",dateToString(comment.getCreatedDate()));
            json.put("toUserId",comment.getToUserId());
            json.put("tousername",userService.getUser(comment.getToUserId()).getName());
            json.put("touserurl",userService.getUser(comment.getToUserId()).getHeadUrl());
            jsonArray.add(json);
        }
        return jsonArray.toJSONString();
    }
}
