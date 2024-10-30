package kopo.analyzeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class AnalyzeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzeServiceApplication.class, args);
    }

}
