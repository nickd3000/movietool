package com.physmo.movietool;

import com.physmo.movietool.domain.DataStore;
import com.physmo.movietool.domain.Genres;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);
    final DataStore dataStore;
    final Operations operations;

    final TMDBService tmdbService;

    public Application(DataStore dataStore, Operations operations,TMDBService tmdbService) {
        this.dataStore = dataStore;
        this.operations = operations;
        this.tmdbService = tmdbService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) {
        return args -> {
            operations.loadDataStore(dataStore);

        };
    }

}
