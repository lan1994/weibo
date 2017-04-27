package com.hx.async.handler;
import com.hx.async.EventHandler;
import com.hx.async.EventModel;
import com.hx.async.EventType;
import com.hx.service.SearchService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by xinxulan on 2017/4/25.
 */
@Component
public class RegisterHandler implements EventHandler{
    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);
    @Autowired
    SearchService searchService;

    @Override
    public void doHandle(EventModel model) {
        try {
            searchService.indexUser(model.getEntityId(), model.getExt("userName"),
                    model.getExt("headUrl"));
            logger.info("增加用户索引成功");
        }catch (Exception e){
            logger.error("增加用户名索引失败", e);
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.REGISTER);
    }
}
