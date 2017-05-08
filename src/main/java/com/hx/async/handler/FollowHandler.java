package com.hx.async.handler;

import com.hx.async.EventHandler;
import com.hx.async.EventModel;
import com.hx.async.EventType;
import com.hx.model.EntityType;
import com.hx.model.Message;
import com.hx.model.User;
import com.hx.service.MessageService;
import com.hx.service.UserService;
import com.hx.util.WeiBoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Component
public class FollowHandler implements EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(FollowHandler.class);
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        Message message = new Message();
        message.setFromId(WeiBoUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User user = userService.getUser(model.getActorId());
        if (model.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setContent("用户" + user.getName()
                    + "关注了你的问题,<a href=http://127.0.0.1:8080/question/" + model.getEntityId()+
                ">点击查看</a>"
            );
        } else if (model.getEntityType() == EntityType.ENTITY_USER) {
            message.setContent("用户" + user.getName()
                    + "关注了你,<a href=http://127.0.0.1:8080/user/" + model.getActorId()+
                    ">点击查看</a>"
            );
        }
        messageService.addMessage(message);
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW);
    }
}
