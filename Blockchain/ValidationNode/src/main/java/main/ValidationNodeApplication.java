package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"model"})
@EnableJpaRepositories(basePackages = {"repository"})
@ComponentScan(basePackages = {"controller", "config"})
@SpringBootApplication
public class ValidationNodeApplication {
  public static void main(String[] args) {
    SpringApplication.run(ValidationNodeApplication.class, args);
  }
}
