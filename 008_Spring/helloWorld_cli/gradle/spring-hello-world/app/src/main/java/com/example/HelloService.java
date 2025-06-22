package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HelloService {
    
    private final MessageProvider messageProvider;
    
    // Constructor injection (recommended)
    @Autowired
    public HelloService(MessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }
    
    public String sayHello() {
        return messageProvider.getMessage();
    }
}
