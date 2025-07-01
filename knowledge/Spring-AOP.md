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