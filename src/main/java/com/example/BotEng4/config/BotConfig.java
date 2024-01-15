package com.example.BotEng4.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@Data
@PropertySource("application.properties")

public class BotConfig {

    public String getToken()
    {
        return this.token;
    }

    public String getBotName()
    {
        return this.botName;
    }

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;



}
