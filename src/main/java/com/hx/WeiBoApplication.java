package com.hx;

import com.hx.async.EventConsumer;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SpringBootApplication
public class WeiBoApplication extends SpringBootServletInitializer implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(WeiBoApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(WeiBoApplication.class, args);
        EventConsumer eventConsumer = applicationContext.getBean(EventConsumer.class);
        eventConsumer.consume();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
