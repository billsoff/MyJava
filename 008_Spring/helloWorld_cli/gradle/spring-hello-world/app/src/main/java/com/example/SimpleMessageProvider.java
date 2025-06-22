package com.example;

import org.springframework.stereotype.Component;

@Component
public class SimpleMessageProvider implements MessageProvider {
    @Override
    public String getMessage() {
        return "Hello World from Spring IoC Container!";
    }
}
