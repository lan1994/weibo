package com.hx.async;

import com.alibaba.fastjson.JSON;
import com.hx.util.JedisAdapter;
import com.hx.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private Map<EventType, List<EventHandler>> config = new HashMap<EventType, List<EventHandler>>();
    private ApplicationContext applicationContext;
    private volatile boolean running = false;
    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }
        startConsume();

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true) {
//                    String key = RedisKeyUtil.getEventQueueKey();
//                    List<String> events = jedisAdapter.brpop(0, key);
//
//                    for (String message : events) {
//                        if (message.equals(key)) {
//                            continue;
//                        }
//
//                        EventModel eventModel = JSON.parseObject(message, EventModel.class);
//                        if (!config.containsKey(eventModel.getType())) {
//                            logger.error("不能识别的事件 type:"+eventModel.getType().getValue());
//                            continue;
//                        }
//
//                        for (EventHandler handler : config.get(eventModel.getType())) {
//                            handler.doHandle(eventModel);
//                        }
//                    }
//                }
//            }
//        });
//        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void startConsume(){
        this.running = true;
    }

    public void stopConsume(){
        this.running = false;
    }

    public void consume(){
        while (running){
            String key = RedisKeyUtil.getEventQueueKey();
            ExecutorService executorService = Executors.newFixedThreadPool(10);
            while(true) {
                List<String> events = jedisAdapter.brpop(0, key);
                Runnable runnable = new ConsumerRunnable(events);
                executorService.execute(runnable);

            }
        }
    }

    private class ConsumerRunnable implements Runnable{
        private  List<String> events = new ArrayList<String>();
        public ConsumerRunnable(){}
        private ConsumerRunnable(List<String> events) {
            this.events = events;
        }

        @Override
        public void run() {
            String key = RedisKeyUtil.getEventQueueKey();
            for (String message : events) {
                if (message.equals(key)) {
                    continue;
                }

                EventModel eventModel = JSON.parseObject(message, EventModel.class);
                if (!config.containsKey(eventModel.getType())) {
                    logger.error("不能识别的事件");
                    continue;
                }

                for (EventHandler handler : config.get(eventModel.getType())) {
                    handler.doHandle(eventModel);
                }
            }
        }
    }
}
