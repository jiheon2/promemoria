package kopo.videochatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class VideoChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(VideoChatServiceApplication.class);
        application.run(args);
    }
}
