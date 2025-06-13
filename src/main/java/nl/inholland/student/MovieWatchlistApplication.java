package nl.inholland.student;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MovieWatchlistApplication {

    public static void main(String[] args) {
        SpringApplication.run(MovieWatchlistApplication.class, args);
    }
}
