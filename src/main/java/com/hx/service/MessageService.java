package com.hx.service;

import com.hx.dao.MessageDAO;
import com.hx.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MessageService {

    @Autowired
    MessageDAO messageDAO;

    @Autowired
    SensitiveService sensitiveService;

    public int addMessage(Message message) {
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDAO.addMessage(message) > 0 ? message.getId() : 0;
    }

    public int getConversationCount(String conversationId) {
        return  messageDAO.getConversationCount(conversationId);
    }

    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return  messageDAO.getConversationDetail(conversationId, offset, limit);
    }

    public List<Message> getConversationList(int userId, int offset, int limit) {
        return  messageDAO.getConversationList(userId, offset, limit);
    }
    public List<Message> getConversationListCount(int userId) {
        return  messageDAO.getConversationListCount(userId);
    }
    public int getConversationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConversationUnreadCount(userId, conversationId);
    }

    public boolean deleteMessage(int id) {
        return messageDAO.deleteMessage(id) > 0;
    }
}
