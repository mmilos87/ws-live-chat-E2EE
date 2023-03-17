package com.ws01.websocket.confing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ws01.websocket.util.LocalDateAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
public class Components {
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxBinaryMessageBufferSize(1024000);
        return container;
    }

    @Bean
    public Gson getGson(){
        return new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
                .create();
    }

    @Bean
    public Map<String, RequestBodyActionHandler>  gethandlersMap(ApplicationContext applicationContext){
        return  applicationContext.getBeansOfType(RequestBodyActionHandler.class);
    }

}
