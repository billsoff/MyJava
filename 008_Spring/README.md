# Spring Boot Hello World with Eclipse, Gradle & IoC

## Prerequisites
- Java 17+ installed
- Eclipse IDE with Spring Tools Suite (STS) plugin
- Command line access

## Step 1: Create Project Structure via CLI

```bash
# Create project directory
mkdir spring-hello-world
cd spring-hello-world

# Initialize Gradle project
gradle init --type java-application --dsl groovy --test-framework junit-jupiter --package com.example --project-name spring-hello-world
```

## Step 2: Configure build.gradle

Replace the generated `build.gradle` with:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

## Step 3: Create Spring Boot Application Class

Create `src/main/java/com/example/SpringHelloWorldApplication.java`:

```java
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
```

## Step 4: Create Service with IoC (Dependency Injection)

Create `src/main/java/com/example/HelloService.java`:

```java
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
```

## Step 5: Create Message Provider Interface & Implementation

Create `src/main/java/com/example/MessageProvider.java`:

```java
package com.example;

public interface MessageProvider {
    String getMessage();
}
```

Create `src/main/java/com/example/SimpleMessageProvider.java`:

```java
package com.example;

import org.springframework.stereotype.Component;

@Component
public class SimpleMessageProvider implements MessageProvider {
    @Override
    public String getMessage() {
        return "Hello World from Spring IoC Container!";
    }
}
```

## Step 6: Run from Command Line

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

Expected output:
```
Hello World from Spring IoC Container!
```

## Step 7: Import into Eclipse

1. Open Eclipse IDE
2. File → Import → Existing Gradle Project
3. Browse to your `spring-hello-world` directory
4. Click Finish
5. Eclipse will import and build the project automatically

## Step 8: Run from Eclipse

1. Right-click on `SpringHelloWorldApplication.java`
2. Run As → Java Application
3. Check the console for output

## IoC Concepts Demonstrated

### 1. **Inversion of Control (IoC)**
- Spring manages object creation and lifecycle
- Objects don't create their dependencies directly

### 2. **Dependency Injection**
- `HelloService` receives `MessageProvider` via constructor
- Spring automatically injects the `SimpleMessageProvider` implementation

### 3. **Annotations Used**
- `@SpringBootApplication`: Enables auto-configuration
- `@Service`: Marks business logic component
- `@Component`: Generic Spring-managed component
- `@Autowired`: Enables dependency injection

### 4. **Benefits**
- **Loose coupling**: `HelloService` depends on interface, not concrete class
- **Testability**: Easy to mock `MessageProvider` for testing
- **Flexibility**: Can swap implementations without changing `HelloService`

## Next Steps

Try modifying the code:
- Create multiple `MessageProvider` implementations
- Use `@Qualifier` to specify which implementation to inject
- Add configuration properties
- Create REST endpoints with `@RestController`