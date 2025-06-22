package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringHelloWorldApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(SpringHelloWorldApplication.class, args);
        
        // Get the HelloService bean from IoC container
        HelloService helloService = context.getBean(HelloService.class);
        System.out.println(helloService.sayHello());
    }
}
