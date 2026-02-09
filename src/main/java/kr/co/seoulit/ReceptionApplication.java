package kr.co.seoulit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ReceptionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceptionApplication.class, args);
    }
}
