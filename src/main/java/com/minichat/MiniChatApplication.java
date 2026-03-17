package com.minichat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class MiniChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniChatApplication.class, args);
    }

}

