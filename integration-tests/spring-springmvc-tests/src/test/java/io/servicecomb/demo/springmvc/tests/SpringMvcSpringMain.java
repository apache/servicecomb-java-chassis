package io.servicecomb.demo.springmvc.tests;

import io.servicecomb.springboot.starter.provider.EnableServiceComb;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableServiceComb
public class SpringMvcSpringMain {

  public static void main(final String[] args) throws Exception {
    SpringApplication.run(SpringMvcSpringMain.class, args);
  }
}
