Got it\! You want to achieve the same Spring Boot REST AOP performance monitoring, but using **Gradle** instead of Maven. No problem at all, the core Java code for the AOP aspect, annotations, and service/repository/controller layers remains exactly the same. The only change is in your build configuration file, `build.gradle`.

Here's how you'd set up your `build.gradle` for this project.

-----

## Setting Up Your Spring Boot Project with Gradle

First, create a new Spring Boot project using Spring Initializr ([https://start.spring.io/](https://start.spring.io/)) and select **Gradle Project** as your build tool. Make sure to include the **Spring Web** and **Spring AOP** dependencies.

Your `build.gradle` file should look like this:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.1' // Use a recent stable version
    id 'io.spring.dependency-management' version '1.1.5' // Ensure this matches your Boot version
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17) // Or your desired Java version
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-aop' // AOP dependency
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.springframework.boot:spring-boot-devtools' // Optional: for faster development
}

tasks.named('test') {
    useJUnitPlatform()
}
```

-----

## Core Java Code (Remains Unchanged)

As mentioned, the Java code for your application's logic, the custom annotation, and the AOP aspect will be identical to the Maven example. You can simply copy these files into your Gradle project's `src/main/java` directory, respecting the package structure.

Here's a quick recap of the files you'll need:

1.  **`TrackExecutionTime.java`** (in `com.example.performancemonitor.annotation` package)

    ```java
    // src/main/java/com/example/performancemonitor/annotation/TrackExecutionTime.java
    package com.example.performancemonitor.annotation;

    import java.lang.annotation.ElementType;
    import java.lang.annotation.Retention;
    import java.lang.annotation.RetentionPolicy;
    import java.lang.annotation.Target;

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TrackExecutionTime {
    }
    ```

2.  **`PerformanceAspect.java`** (in `com.example.performancemonitor.aop` package)

    ```java
    // src/main/java/com/example/performancemonitor/aop/PerformanceAspect.java
    package com.example.performancemonitor.aop;

    import org.aspectj.lang.ProceedingJoinPoint;
    import org.aspectj.lang.annotation.Around;
    import org.aspectj.lang.annotation.Aspect;
    import org.aspectj.lang.annotation.Pointcut;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.stereotype.Component;

    @Aspect
    @Component
    public class PerformanceAspect {

        private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

        @Pointcut("@annotation(com.example.performancemonitor.annotation.TrackExecutionTime)")
        public void trackExecutionTimePointcut() { }

        @Pointcut("execution(* com.example.performancemonitor.controller.*.*(..))")
        public void controllerMethodsPointcut() { }

        @Pointcut("execution(* com.example.performancemonitor.service.*.*(..))")
        public void serviceMethodsPointcut() { }

        @Pointcut("execution(* com.example.performancemonitor.repository.*.*(..)) || execution(* com.example.performancemonitor.dao.*.*(..))")
        public void daoMethodsPointcut() { }

        @Around("trackExecutionTimePointcut() || controllerMethodsPointcut() || serviceMethodsPointcut() || daoMethodsPointcut()")
        public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
            long startTime = System.currentTimeMillis();
            Object result = null;

            try {
                result = joinPoint.proceed();
            } finally {
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;

                String className = joinPoint.getSignature().getDeclaringTypeName();
                String methodName = joinPoint.getSignature().getName();

                logger.info("Execution time of {}.{} :: {} ms", className, methodName, executionTime);
            }
            return result;
        }
    }
    ```

    **Remember to adjust the package names in the `execution` pointcut expressions to match your project's structure if they are different from `com.example.performancemonitor`\!**

3.  **Model, Repository, Service, and Controller classes** (e.g., `User.java`, `UserRepository.java`, `UserService.java`, `UserController.java` in their respective `model`, `repository`, `service`, and `controller` packages).

      * **`User.java`**: (Simple POJO)
        ```java
        package com.example.performancemonitor.model;
        // ... (rest of the User class)
        ```
      * **`UserRepository.java`**: (Dao Layer)
        ```java
        package com.example.performancemonitor.repository;
        // ... (rest of the UserRepository class)
        ```
      * **`UserService.java`**: (Service Layer - note the `@TrackExecutionTime` on `getAllUsers()`)
        ```java
        package com.example.performancemonitor.service;
        // ... (rest of the UserService class)
        ```
      * **`UserController.java`**: (Controller Layer)
        ```java
        package com.example.performancemonitor.controller;
        // ... (rest of the UserController class)
        ```

4.  **`PerformanceMonitorApplication.java`** (your main Spring Boot application class)

    ```java
    package com.example.performancemonitor;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;

    @SpringBootApplication
    public class PerformanceMonitorApplication {

        public static void main(String[] args) {
            SpringApplication.run(PerformanceMonitorApplication.class, args);
        }
    }
    ```

-----

## Running the Application with Gradle

1.  **Build the Project:** Open your terminal in the root directory of your project (where `build.gradle` is located) and run:

    ```bash
    ./gradlew build
    ```

    (On Windows, use `gradlew build`)

2.  **Run the Spring Boot Application:**

    ```bash
    ./gradlew bootRun
    ```

3.  **Access Endpoints and Observe Logs:**

      * Access `http://localhost:8080/api/users`
      * Access `http://localhost:8080/api/users/1`
      * Just like with Maven, you'll see the execution time logs in your console output, demonstrating that AOP is working correctly with your Gradle setup.

-----

The transition from Maven to Gradle for this particular use case is quite straightforward because the core Spring Boot and AOP functionalities are independent of the build tool. The main difference lies in how you declare your dependencies and plugins in the build script.

Maven
-----
You want to implement Aspect-Oriented Programming (AOP) in a Spring Boot application to intercept methods in your `Controller`, `Service`, and `Dao` layers and calculate their execution time. This is a common and very useful application of AOP for performance monitoring.

Let's break down how to achieve this with a complete code example.

-----

## Setting Up Your Spring Boot Project

First, ensure you have a basic Spring Boot project. If not, you can create one using Spring Initializr ([https://start.spring.io/](https://start.spring.io/)). Make sure to include the **Spring Web** and **Spring AOP** dependencies.

Your `pom.xml` should have entries similar to these:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.1</version> <relativePath/> </parent>
    <groupId>com.example</groupId>
    <artifactId>performance-monitor</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>performance-monitor</name>
    <description>Demo project for Spring Boot AOP performance monitoring</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```

-----

## Core Components for Performance Monitoring

Here's the complete code, broken down by component:

### 1\. The `@TrackExecutionTime` Annotation

This custom annotation will mark the methods you want to intercept.

```java
// src/main/java/com/example/performancemonitor/annotation/TrackExecutionTime.java
package com.example.performancemonitor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods for execution time tracking.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackExecutionTime {
}
```

-----

### 2\. The `PerformanceAspect`

This is the heart of our AOP implementation. It defines the pointcuts and the advice for measuring execution time.

```java
// src/main/java/com/example/performancemonitor/aop/PerformanceAspect.java
package com.example.performancemonitor.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for tracking the execution time of methods.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * Pointcut for methods annotated with @TrackExecutionTime.
     */
    @Pointcut("@annotation(com.example.performancemonitor.annotation.TrackExecutionTime)")
    public void trackExecutionTimePointcut() {
    }

    /**
     * Pointcut for methods in Controller classes.
     * Assumes controllers are in a package like 'com.example.performancemonitor.controller'.
     */
    @Pointcut("execution(* com.example.performancemonitor.controller.*.*(..))")
    public void controllerMethodsPointcut() {
    }

    /**
     * Pointcut for methods in Service classes.
     * Assumes services are in a package like 'com.example.performancemonitor.service'.
     */
    @Pointcut("execution(* com.example.performancemonitor.service.*.*(..))")
    public void serviceMethodsPointcut() {
    }

    /**
     * Pointcut for methods in Dao/Repository classes.
     * Assumes DAOs are in a package like 'com.example.performancemonitor.dao' or 'com.example.performancemonitor.repository'.
     */
    @Pointcut("execution(* com.example.performancemonitor.dao.*.*(..)) || execution(* com.example.performancemonitor.repository.*.*(..))")
    public void daoMethodsPointcut() {
    }

    /**
     * Around advice to measure the execution time of methods matching the defined pointcuts.
     *
     * @param joinPoint The join point representing the method execution.
     * @return The result of the method execution.
     * @throws Throwable If the method execution throws an exception.
     */
    @Around("trackExecutionTimePointcut() || controllerMethodsPointcut() || serviceMethodsPointcut() || daoMethodsPointcut()")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;

        try {
            result = joinPoint.proceed(); // Execute the original method
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();

            logger.info("Execution time of {}.{} :: {} ms", className, methodName, executionTime);
        }
        return result;
    }
}
```

**Explanation:**

  * `@Aspect`: Marks this class as an Aspect.
  * `@Component`: Makes this class a Spring component, so it's picked up by component scanning.
  * `@Pointcut`: Defines reusable pointcut expressions.
      * `trackExecutionTimePointcut()`: Matches any method annotated with `@TrackExecutionTime`. This gives you granular control.
      * `controllerMethodsPointcut()`, `serviceMethodsPointcut()`, `daoMethodsPointcut()`: These use `execution` expressions to target all public methods within specific packages (e.g., `com.example.performancemonitor.controller.*.*(..)`) for your Controller, Service, and Dao layers, respectively. **Adjust the package names to match your project structure.**
  * `@Around`: This is an "around" advice. It wraps the execution of the matched methods.
      * `ProceedingJoinPoint`: This object allows you to `proceed()` with the actual method execution.
      * We record the `startTime` before `joinPoint.proceed()` and `endTime` after, then log the difference.
      * The `finally` block ensures that the execution time is logged even if the intercepted method throws an exception.

-----

### 3\. Example Application Layers

Let's create some simple classes in our Controller, Service, and Dao layers to demonstrate the AOP in action.

#### a. `User` Model (Simple POJO)

```java
// src/main/java/com/example/performancemonitor/model/User.java
package com.example.performancemonitor.model;

public class User {
    private Long id;
    private String name;

    public User(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

#### b. `UserRepository` (Dao Layer)

```java
// src/main/java/com/example/performancemonitor/repository/UserRepository.java
package com.example.performancemonitor.repository;

import com.example.performancemonitor.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Simple "DAO" layer for User data.
 */
@Repository
public class UserRepository {

    private final List<User> users = new ArrayList<>();

    public UserRepository() {
        users.add(new User(1L, "Alice"));
        users.add(new User(2L, "Bob"));
    }

    public List<User> findAll() {
        try {
            TimeUnit.MILLISECONDS.sleep(150); // Simulate network/DB delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>(users);
    }

    public Optional<User> findById(Long id) {
        try {
            TimeUnit.MILLISECONDS.sleep(80); // Simulate network/DB delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return users.stream().filter(user -> user.getId().equals(id)).findFirst();
    }
}
```

**Note:** For demonstration, I've used `TimeUnit.MILLISECONDS.sleep()` to simulate some work being done in the methods, so you can see meaningful execution times in the logs.

#### c. `UserService` (Service Layer)

```java
// src/main/java/com/example/performancemonitor/service/UserService.java
package com.example.performancemonitor.service;

import com.example.performancemonitor.annotation.TrackExecutionTime;
import com.example.performancemonitor.model.User;
import com.example.performancemonitor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service layer for User operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @TrackExecutionTime // This method will be specifically tracked by the annotation pointcut
    public List<User> getAllUsers() {
        try {
            TimeUnit.MILLISECONDS.sleep(50); // Simulate business logic delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        try {
            TimeUnit.MILLISECONDS.sleep(20); // Simulate business logic delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return userRepository.findById(id);
    }
}
```

#### d. `UserController` (Controller Layer)

```java
// src/main/java/com/example/performancemonitor/controller/UserController.java
package com.example.performancemonitor.controller;

import com.example.performancemonitor.model.User;
import com.example.performancemonitor.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for User operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

-----

### 4\. Main Application Class

```java
// src/main/java/com/example/performancemonitor/PerformanceMonitorApplication.java
package com.example.performancemonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PerformanceMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceMonitorApplication.class, args);
    }

}
```

-----

## Running the Application and Observing Output

1.  **Run the `PerformanceMonitorApplication`** from your IDE or using Maven:

    ```bash
    mvn spring-boot:run
    ```

2.  **Access the Endpoints:**

      * Open your browser or use a tool like Postman/curl to access:
          * `http://localhost:8080/api/users`
          * `http://localhost:8080/api/users/1`

3.  **Check Your Console Logs:**
    You will see output in your console similar to this (exact times will vary):

    ```
    INFO  c.e.p.r.UserRepository       : Execution time of com.example.performancemonitor.repository.UserRepository.findAll :: 155 ms
    INFO  c.e.p.s.UserService          : Execution time of com.example.performancemonitor.service.UserService.getAllUsers :: 207 ms
    INFO  c.e.p.c.UserController       : Execution time of com.example.performancemonitor.controller.UserController.getAllUsers :: 209 ms

    INFO  c.e.p.r.UserRepository       : Execution time of com.example.performancemonitor.repository.UserRepository.findById :: 81 ms
    INFO  c.e.p.s.UserService          : Execution time of com.example.performancemonitor.service.UserService.getUserById :: 102 ms
    INFO  c.e.p.c.UserController       : Execution time of com.example.performancemonitor.controller.UserController.getUserById :: 103 ms
    ```

As you can see, the aspect successfully intercepted the methods in each layer and logged their execution times. The `getAllUsers` method in `UserService` was also specifically tracked by the `@TrackExecutionTime` annotation.

-----

## Key Takeaways and Considerations

  * **Loose Coupling:** AOP allows you to add cross-cutting concerns (like logging, security, or performance monitoring) without modifying the core business logic. This keeps your code clean and maintainable.
  * **Pointcut Expressions:** Mastering pointcut expressions is crucial. They define *where* your advice will be applied. Common types include:
      * `execution()`: For method execution.
      * `@annotation()`: For methods with a specific annotation.
      * `within()`: For types within a certain package.
  * **Advice Types:**
      * `@Before`: Runs before the method.
      * `@After`: Runs after the method (regardless of success or failure).
      * `@AfterReturning`: Runs after the method returns successfully.
      * `@AfterThrowing`: Runs if the method throws an exception.
      * `@Around`: The most powerful, allowing you to completely control the method execution (like in our example).
  * **Performance Overhead:** While AOP is powerful, introducing aspects does add a slight performance overhead. For simple logging, it's usually negligible, but for very high-performance systems, be mindful of the complexity of your advice.
  * **Proxy Limitations:** Spring AOP works by creating proxies (either JDK dynamic proxies or CGLIB proxies). This means that AOP will only apply to method calls made from *outside* the advised object. If `this.someMethod()` is called from within the same class, the advice won't be applied.

This setup provides a robust foundation for monitoring method execution times across your Spring Boot application layers. You can extend this further by integrating with metrics systems (e.g., Micrometer, Prometheus) or logging to a dedicated performance monitoring system.
