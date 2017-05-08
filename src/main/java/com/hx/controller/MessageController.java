package com.hx.controller;

import com.hx.model.HostHolder;
import com.hx.model.Message;
import com.hx.model.User;
import com.hx.model.ViewObject;
import com.hx.service.MessageService;
import com.hx.service.UserService;
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
public class MessageController {
    @Autowired
    HostHolder hostHolder;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;
    private static final int pageSize = 5;
    private static final int pageDisplay = 20;

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String getConversationListDefault(){
        return "redirect:/msg/list/1";
    }

    @RequestMapping(path = {"/msg/list/{page}"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String getConversationList(Model model,
                                      @PathVariable(value = "page") int page) {
        if (hostHolder.getUser() == null) {
            return "redirect:/reglogin";
        }
        int localUserId = hostHolder.getUser().getId();
        List<Message> conversationListCount = messageService.getConversationListCount(localUserId);

        if(conversationListCount.size()>0){
            try {
                Map<String, Object> pmap = PageUtil.execute(page, pageSize, conversationListCount.size(), pageDisplay);
                int offset = (Integer) pmap.get("offset");
                int limit = (Integer) pmap.get("limit");
                List<Message> conversationList = messageService.getConversationList(localUserId, offset, limit);
                List<ViewObject> conversations = new ArrayList<ViewObject>();
                for (Message message : conversationList) {
                    ViewObject vo = new ViewObject();
                    vo.set("message", message);
                    int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
                    vo.set("user", userService.getUser(targetId));
                    vo.set("unread", messageService.getConversationUnreadCount(localUserId, message.getConversationId()));
                    conversations.add(vo);
                }
                model.addAttribute("conversations", conversations);
                model.addAttribute("pageDisplayArray",pmap.get("pageArray"));
                model.addAttribute("nextPage",pmap.get("nextPage"));
            }catch (Exception e){
                logger.error("获取失败" + e.getMessage());
            }
        }
        return "letter";
    }

    @RequestMapping(path = {"/msg/detail/{page}"}, method = {RequestMethod.GET,RequestMethod.POST})
    public String getConversationDetail(Model model,
                                        @RequestParam("conversationId") String conversationId,
                                        @PathVariable(value = "page") int page) {
        int messageCount = messageService.getConversationCount(conversationId);
        if(messageCount>0){
            try {
                Map<String, Object> pmap = PageUtil.execute(page, pageSize, messageCount, pageDisplay);
                int offset = (Integer) pmap.get("offset");
                int limit = (Integer) pmap.get("limit");
                List<Message> messageList = messageService.getConversationDetail(conversationId, offset, limit);
                List<ViewObject> messages = new ArrayList<ViewObject>();
                String messageId = null;
                for (Message message : messageList) {
                    ViewObject vo = new ViewObject();
                    messageId = message.getConversationId();
                    vo.set("message", message);
                    vo.set("user", userService.getUser(message.getFromId()));
                    messages.add(vo);
                }
                model.addAttribute("conversationId", messageId);
                model.addAttribute("messages", messages);
                model.addAttribute("pageDisplayArray",pmap.get("pageArray"));
                model.addAttribute("nextPage",pmap.get("nextPage"));
            } catch (Exception e) {
                logger.error("获取详情失败" + e.getMessage());
            }
        }

        return "letterDetail";
    }

    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                             @RequestParam("content") String content) {
        try {
            if (hostHolder.getUser() == null) {
                return WeiBoUtil.getJSONString(999, "未登录");
            }

            User user = userService.selectByName(toName);
            if (user == null) {
                return WeiBoUtil.getJSONString(1, "用户不存在");
            }

            Message message = new Message();
            message.setCreatedDate(new Date());
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            message.setContent(content);
            messageService.addMessage(message);
            return WeiBoUtil.getJSONString(0);

        } catch (Exception e) {
            logger.error("发送消息失败" + e.getMessage());
            return WeiBoUtil.getJSONString(1, "发信失败");
        }
    }
}
