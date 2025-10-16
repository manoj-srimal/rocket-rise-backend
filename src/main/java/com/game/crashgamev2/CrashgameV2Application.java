package com.game.crashgamev2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CrashgameV2Application {

    public static void main(String[] args) {
        SpringApplication.run(CrashgameV2Application.class, args);
    }

}
