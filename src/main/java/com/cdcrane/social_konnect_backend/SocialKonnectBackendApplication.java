package com.cdcrane.social_konnect_backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SocialKonnectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialKonnectBackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(){
        return args -> {
            System.out.println("Command line runner \n---------------------\n");
        };
    }

}
