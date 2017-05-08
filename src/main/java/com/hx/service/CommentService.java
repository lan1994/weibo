package com.hx.service;

import com.hx.dao.CommentDAO;
import com.hx.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentDAO commentDAO;

    @Autowired
    SensitiveService sensitiveService;

    public List<Comment> getCommentsByEntity(int entityId, int entityType, int offset, int limit) {
        return commentDAO.selectCommentByEntity(entityId, entityType ,offset,limit);
    }

    public int addComment(Comment comment) {
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
    }

    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }

    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }

    public boolean deleteComment(int commentId) {
        return commentDAO.deleteComment(commentId) > 0;
    }

    public boolean deleteCommentbyQuestion(int commentId) {
        return commentDAO.deleteCommentbyQuestion(commentId) > 0;
    }



    public int updateComment(Comment comment) {
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDAO.updateComment(comment) > 0 ? comment.getId() : 0;
    }

    public int updateCommentCount(Comment comment) {
        return commentDAO.updateCommentCount(comment) > 0 ? comment.getId() : 0;
    }

    public Comment getCommentById(int id) {
        return commentDAO.getCommentById(id);
    }

    public List<Comment> getCommentChild(int parent) {
        return commentDAO.getCommentChild(parent);
    }
}
