package main;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;


@ComponentScan(basePackages = {"service", "config", "cli"})
@SpringBootApplication(exclude = {
  DataSourceAutoConfiguration.class,
  DataSourceTransactionManagerAutoConfiguration.class,
  HibernateJpaAutoConfiguration.class
})
public class ClientNodeApplication {
  public static void main(String[] args) {
    new SpringApplicationBuilder(ClientNodeApplication.class)
      .web(WebApplicationType.NONE)
      .run(args);
  }
}
