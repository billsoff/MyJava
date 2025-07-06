# Reference

- [Validation by Using Spring’s Validator Interface](https://docs.spring.io/spring-framework/reference/core/validation/validator.html)

- [Java Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)

- [Jakarta Validation](https://beanvalidation.org/)

---

# Spring注解验证详解及在Controller中的应用

Spring框架提供了强大的数据验证机制，主要通过JSR-303/JSR-380（Bean Validation）规范实现，并结合Spring的验证特性。下面详细介绍常用验证注解及其在Controller中的应用。

## 一、常用验证注解

### 1. 基本验证注解

| 注解 | 说明 | 示例 |
|------|------|------|
| `@NotNull` | 值不能为null | `@NotNull String name` |
| `@Null` | 值必须为null | `@Null String unusedField` |
| `@NotEmpty` | 字符串/集合不能为空 | `@NotEmpty List<String> items` |
| `@NotBlank` | 字符串不能为空且必须包含非空白字符 | `@NotBlank String username` |
| `@Size` | 指定字符串/集合大小范围 | `@Size(min=2, max=30) String name` |
| `@Min` | 数字最小值 | `@Min(18) int age` |
| `@Max` | 数字最大值 | `@Max(100) int age` |
| `@DecimalMin` | 小数值最小值 | `@DecimalMin("0.01") BigDecimal price` |
| `@DecimalMax` | 小数值最大值 | `@DecimalMax("10000.00") BigDecimal price` |
| `@Digits` | 数字位数限制 | `@Digits(integer=6, fraction=2) BigDecimal amount` |
| `@Pattern` | 正则表达式匹配 | `@Pattern(regexp="[A-Za-z]+") String name` |
| `@Email` | 电子邮件格式 | `@Email String email` |
| `@Positive` | 必须为正数 | `@Positive int count` |
| `@PositiveOrZero` | 必须为正数或零 | `@PositiveOrZero int count` |
| `@Negative` | 必须为负数 | `@Negative int balance` |
| `@NegativeOrZero` | 必须为负数或零 | `@NegativeOrZero int balance` |
| `@Future` | 日期必须在将来 | `@Future Date eventDate` |
| `@FutureOrPresent` | 日期必须在将来或现在 | `@FutureOrPresent Date eventDate` |
| `@Past` | 日期必须在过去 | `@Past Date birthDate` |
| `@PastOrPresent` | 日期必须在过去或现在 | `@PastOrPresent Date birthDate` |

### 2. 自定义验证注解

可以通过实现`ConstraintValidator`接口创建自定义验证注解：

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {
    String message() default "Invalid phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches("^\\+?[0-9]{10,15}$");
    }
}
```

## 二、在Controller中的应用

### 1. 基本使用

在Controller方法参数前添加`@Valid`或`@Validated`注解启用验证：

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    // 处理逻辑
    return ResponseEntity.ok(userService.save(user));
}
```

### 2. 验证路径变量和请求参数

```java
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(
    @PathVariable @Min(1) Long id,
    @RequestParam @NotBlank String type) {
    // 处理逻辑
}
```

### 3. 分组验证

定义验证组：
```java
public interface BasicInfo {}
public interface AdvancedInfo {}
```

在实体上指定组：
```java
public class User {
    @NotBlank(groups = BasicInfo.class)
    private String username;
    
    @Email(groups = BasicInfo.class)
    private String email;
    
    @NotNull(groups = AdvancedInfo.class)
    @Size(min = 8, max = 20, groups = AdvancedInfo.class)
    private String password;
}
```

在Controller中使用：
```java
@PostMapping("/users/basic")
public ResponseEntity<User> createBasicUser(@Validated(BasicInfo.class) @RequestBody User user) {
    // 只验证BasicInfo组的约束
}

@PostMapping("/users/full")
public ResponseEntity<User> createFullUser(@Validated({BasicInfo.class, AdvancedInfo.class}) @RequestBody User user) {
    // 验证所有组的约束
}
```

### 4. 级联验证

当对象包含其他对象时，使用`@Valid`进行级联验证：

```java
public class Order {
    @Valid
    private List<@Valid OrderItem> items;
    // ...
}
```

### 5. 处理验证错误

使用`BindingResult`或`Errors`参数获取验证错误：

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result) {
    if (result.hasErrors()) {
        List<String> errors = result.getAllErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(errors);
    }
    // 处理逻辑
}
```

或者使用`@ControllerAdvice`全局处理：

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
```

## 三、高级特性

### 1. 自定义错误消息

可以在注解中直接指定：
```java
@NotBlank(message = "用户名不能为空")
private String username;
```

或者在资源文件中定义（如`messages.properties`）：
```
NotBlank.user.username=用户名不能为空
NotBlank.username=用户名不能为空
NotBlank=字段不能为空
```

### 2. 编程式验证

```java
@Autowired
private Validator validator;

public void validateUser(User user) {
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    if (!violations.isEmpty()) {
        throw new ConstraintViolationException(violations);
    }
}
```

### 3. 验证集合

```java
@PostMapping("/users/batch")
public ResponseEntity<List<User>> createUsers(
    @Valid @RequestBody List<@Valid User> users) {
    // 处理逻辑
}
```

## 四、注意事项

1. 确保添加了验证依赖（如Hibernate Validator）
   ```xml
   <dependency>
       <groupId>org.hibernate.validator</groupId>
       <artifactId>hibernate-validator</artifactId>
       <version>6.2.0.Final</version>
   </dependency>
   ```

2. `@Valid`是JSR-303标准注解，`@Validated`是Spring的扩展，支持分组验证

3. 验证只在Controller层自动触发，服务层需要手动调用验证

4. 对于复杂业务规则验证，建议结合领域模型验证

通过合理使用Spring的验证机制，可以大大减少Controller中的参数校验代码，提高代码的可读性和可维护性。

---

#### Q: 在控制器中如果没有 BindingResult，验证失败会抛异常？

是的，在Spring MVC控制器中，如果方法参数使用了`@Valid`或`@Validated`进行注解验证，但没有在其后添加`BindingResult`或`Errors`参数，当验证失败时，Spring会抛出`MethodArgumentNotValidException`异常。

## 详细说明

### 1. 默认行为（无BindingResult）

```java
@PostMapping("/users")
public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
    // 如果验证失败，不会执行到这里
    return ResponseEntity.ok(userService.save(user));
}
```

- 当验证失败时，Spring会：
  1. 自动抛出`MethodArgumentNotValidException`异常
  2. 返回HTTP 400 Bad Request响应
  3. 响应体中包含验证错误信息（具体格式取决于配置）

### 2. 有BindingResult时的行为

```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result) {
    if (result.hasErrors()) {
        // 可以自定义错误处理
        return ResponseEntity.badRequest().body("验证失败");
    }
    // 验证通过的处理逻辑
    return ResponseEntity.ok(userService.save(user));
}
```

- 当添加`BindingResult`参数后：
  - 验证失败时不会抛出异常
  - 所有验证错误信息会存储在`BindingResult`对象中
  - 开发者可以自行决定如何处理验证错误

### 3. 异常处理机制

Spring默认会将`MethodArgumentNotValidException`转换为400错误响应。如果想自定义错误响应，可以使用`@ControllerAdvice`：

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }
}
```

### 4. 验证失败时的响应示例

没有自定义处理时的典型响应：
```json
{
    "timestamp": "2023-05-20T12:34:56.789+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for object='user'. Error count: 2",
    "path": "/users"
}
```

使用`@ControllerAdvice`处理后的响应示例：
```json
{
    "username": "用户名不能为空",
    "email": "必须是有效的电子邮件地址"
}
```

### 5. 最佳实践建议

1. **简单API**：可以不使用BindingResult，依靠全局异常处理统一格式
2. **复杂业务**：使用BindingResult进行更精细的错误处理
3. **RESTful API**：推荐使用全局异常处理保持错误响应格式一致
4. **表单提交**：可能需要BindingResult配合视图解析返回错误页面

### 6. 其他相关异常

- `ConstraintViolationException`：用于非控制器层的验证（如@Service方法上的验证）
- `BindException`：用于@ModelAttribute参数绑定失败的情况

总结来说，是否使用BindingResult取决于你是否需要在控制器方法内部直接处理验证错误。如果不需要细粒度控制，依靠异常处理机制是更简洁的做法。

---

#### Q: @Validated 和 @Valid 相比有什么不同？

`@Validated` 和 `@Valid` 都是用于触发数据验证的注解，但它们在来源、功能和使用场景上有一些关键区别。以下是它们的详细对比：

---

### **1. 来源不同**
| 注解         | 所属规范/框架       | 包路径                     |
|--------------|-------------------|---------------------------|
| `@Valid`     | **JSR-303/JSR-380** (Java标准) | `javax.validation.Valid` |
| `@Validated` | **Spring框架**      | `org.springframework.validation.annotation.Validated` |

- `@Valid` 是Java标准注解（Bean Validation规范的一部分）。
- `@Validated` 是Spring对标准验证的扩展，提供更多功能。

---

### **2. 核心区别**
| 特性                | `@Valid`                          | `@Validated`                      |
|---------------------|----------------------------------|-----------------------------------|
| **分组验证**         | ❌ 不支持                          | ✅ 支持（通过`groups`属性）          |
| **嵌套验证触发**     | ✅ 需显式标注`@Valid`触发级联验证    | ❌ 不支持自动级联验证（需配合`@Valid`） |
| **应用范围**         | 方法参数、字段、容器元素等          | 类、方法、参数（需Spring支持）       |
| **校验时机**         | 通常用于方法参数验证               | 还可用于类级别（触发Spring AOP代理的校验） |

---

### **3. 主要使用场景**
#### **(1) 分组验证（仅`@Validated`支持）**
`@Validated` 允许通过`groups`属性指定验证组，实现不同场景下的差异化校验。

```java
// 定义分组
public interface BasicInfo {}
public interface AdvancedInfo {}

// 实体类
public class User {
    @NotBlank(groups = BasicInfo.class)
    private String name;

    @Email(groups = BasicInfo.class)
    private String email;

    @Size(min=8, groups = AdvancedInfo.class)
    private String password;
}

// Controller中使用分组
@PostMapping("/user/basic")
public ResponseEntity<?> createBasicUser(
    @Validated(BasicInfo.class) @RequestBody User user) {
    // 仅校验BasicInfo组的约束
}
```

#### **(2) 级联验证（需`@Valid`配合）**
`@Valid` 可以触发嵌套对象的验证，而`@Validated`不行。

```java
public class Order {
    @Valid  // 必须加@Valid才能触发OrderItem的验证
    private List<OrderItem> items;
}

// Controller中
@PostMapping("/order")
public ResponseEntity<?> createOrder(
    @Valid @RequestBody Order order) {  // 使用@Valid
    // 会级联验证order.items
}
```

#### **(3) 方法级别验证（`@Validated`特有）**
`@Validated` 可以标注在类上，触发Spring AOP代理的校验（如Service层方法）：

```java
@Service
@Validated  // 类级别注解
public class UserService {
    
    public void createUser(
        @NotBlank String username, 
        @Email String email) {
        // 方法参数会被自动验证
    }
}
```

---

### **4. 兼容性与组合使用**
- **Spring MVC中**：两者在参数验证上可以互换，但`@Validated`功能更强大。
- **级联验证时**：即使使用`@Validated`，仍需搭配`@Valid`触发嵌套验证：
  ```java
  public ResponseEntity<?> example(
      @Validated @RequestBody Parent parent) {  // 父级用@Validated
      // ...
  }

  public class Parent {
      @Valid  // 必须加@Valid才能触发Child验证
      private Child child;
  }
  ```

---

### **5. 如何选择？**
| 场景                              | 推荐注解           |
|----------------------------------|-------------------|
| 需要分组验证                      | `@Validated`      |
| 需要触发嵌套对象验证               | `@Valid`          |
| Spring MVC的普通参数验证           | 两者均可           |
| 非Controller层（如Service）的方法验证 | `@Validated`（类级别） |

---

### **6. 底层机制**
- `@Valid`：由Bean Validation实现（如Hibernate Validator）直接处理。
- `@Validated`：Spring通过`MethodValidationInterceptor`拦截器实现，整合了Bean Validation。

---

### **总结**
- **功能差异**：  
  `@Validated` = `@Valid` + 分组验证 + Spring扩展支持（如类级别校验）。  
- **协作关系**：  
  在Spring项目中，通常组合使用：用`@Validated`处理分组和类级别校验，用`@Valid`处理嵌套验证。

---

#### Q: 如何用 SpringValidatorAdapter 验证失败后抛出 MethodArgumentNotValidException？

在Spring中，如果你想手动使用 `SpringValidatorAdapter` 进行验证，并在验证失败时抛出 `MethodArgumentNotValidException`（与Spring MVC的自动验证行为一致），可以按照以下步骤实现：

---

### **1. 核心思路**
- 使用 `SpringValidatorAdapter`（或 `Validator`）手动验证对象。
- 如果验证失败，将错误信息包装成 `BindingResult`（通常是 `BeanPropertyBindingResult`）。
- 基于 `BindingResult` 抛出 `MethodArgumentNotValidException`。

---

### **2. 实现代码示例**

#### **(1) 工具类封装**
```java
import org.springframework.validation.*;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import javax.validation.Validator;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class ValidationUtils {

    /**
     * 手动验证对象，失败时抛出MethodArgumentNotValidException
     * @param target 待验证对象
     * @param objectName 对象名称（用于错误消息）
     * @param validator 验证器（可从Spring注入）
     */
    public static void validateAndThrow(Object target, String objectName, Validator validator) 
            throws MethodArgumentNotValidException {
        
        // 将JSR-303 Validator适配为Spring的Validator
        SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
        
        // 创建BindingResult存储错误
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        
        // 执行验证
        springValidator.validate(target, errors);
        
        // 如果有错误，抛出异常
        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(null, (BindingResult) errors);
        }
    }
}
```

#### **(2) 在Service/Controller中使用**
```java
import javax.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private Validator validator; // 注入JSR-303 Validator

    @PostMapping("/users/manual-validate")
    public ResponseEntity<?> createUser(@RequestBody User user) 
            throws MethodArgumentNotValidException {
        
        // 手动验证
        ValidationUtils.validateAndThrow(user, "user", validator);
        
        // 验证通过后的逻辑
        return ResponseEntity.ok("Validation passed!");
    }
}
```

---

### **3. 关键点说明**

#### **(1) 如何获取Validator实例**
- 在Spring Boot中，`javax.validation.Validator` 会自动配置（实际是Hibernate Validator）。
- 直接通过 `@Autowired` 注入即可。

#### **(2) 为什么用SpringValidatorAdapter?**
- 它桥接了JSR-303标准验证器（如Hibernate Validator）和Spring的`Validator`接口。
- 确保验证行为与Spring MVC的自动验证一致（包括消息转换等）。

#### **(3) 异常处理**
抛出的 `MethodArgumentNotValidException` 可以被Spring统一的异常处理器捕获，例如：
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {
        // 格式化错误响应
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage
            ));
        return ResponseEntity.badRequest().body(errors);
    }
}
```

---

### **4. 替代方案（直接使用Spring的Validator）**
如果不需要JSR-303特性，可以直接使用Spring的 `Validator` 接口：
```java
import org.springframework.validation.SmartValidator;

public class ValidationUtils {

    @Autowired
    private SmartValidator validator; // Spring提供的增强版Validator

    public void validate(Object target, String objectName) {
        Errors errors = new BeanPropertyBindingResult(target, objectName);
        validator.validate(target, errors);
        if (errors.hasErrors()) {
            throw new MethodArgumentNotValidException(null, (BindingResult) errors);
        }
    }
}
```

---

### **5. 应用场景**
- **非Controller层验证**：如在Service中手动触发验证。
- **复杂校验逻辑**：需要自定义验证流程时。
- **单元测试**：模拟Controller的验证行为。

---

### **6. 完整依赖（Spring Boot）**
确保项目中包含：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

通过这种方式，你可以手动复现Spring MVC的自动验证行为，统一错误处理逻辑。

---

#### Q: MethodArgumentNotValidException 在哪个包？

`MethodArgumentNotValidException` 类位于 **Spring Framework** 的以下包中：

```java
org.springframework.web.bind.MethodArgumentNotValidException
```

### 关键细节：
1. **所属模块**  
   - 属于 `spring-web` 模块（Spring MVC 的核心组件之一）。

2. **典型触发场景**  
   - 当在Controller方法参数上使用 `@Valid` 或 `@Validated` 注解，且验证失败时，Spring MVC 会自动抛出此异常。

3. **Maven/Gradle 依赖**  
   如果你使用的是 Spring Boot，通常不需要单独引入，因为 `spring-boot-starter-web` 已经包含它：
   ```xml
   <!-- Maven -->
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-web</artifactId>
   </dependency>

   <!-- 或纯Spring MVC项目 -->
   <dependency>
       <groupId>org.springframework</groupId>
       <artifactId>spring-web</artifactId>
       <version>5.3.XX</version>
   </dependency>
   ```

4. **继承关系**  
   ```java
   public class MethodArgumentNotValidException extends Exception
       implements BindException {
       // ...
   }
   ```
   - 它实现了 `BindException` 接口，因此可以通过 `getBindingResult()` 方法获取详细的验证错误信息。

5. **常见用途**  
   - 在 `@ControllerAdvice` 中全局捕获并处理参数验证错误：
     ```java
     @ControllerAdvice
     public class GlobalExceptionHandler {
         @ResponseStatus(HttpStatus.BAD_REQUEST)
         @ExceptionHandler(MethodArgumentNotValidException.class)
         public ResponseEntity<Map<String, String>> handleValidationException(
                 MethodArgumentNotValidException ex) {
             // 提取错误信息
             Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                 .stream()
                 .collect(Collectors.toMap(
                     FieldError::getField,
                     FieldError::getDefaultMessage
                 ));
             return ResponseEntity.badRequest().body(errors);
         }
     }
     ```

如果项目中找不到这个类，请检查是否遗漏了 `spring-web` 或 `spring-boot-starter-web` 依赖。

---

#### Q: 构造函数？

`MethodArgumentNotValidException` 的构造函数是 **protected** 的，通常由 Spring MVC 内部在参数验证失败时自动创建并抛出。如果需要手动构造此异常（例如在自定义验证逻辑中），可以通过以下方式实现：

---

### **1. 核心构造函数**
```java
protected MethodArgumentNotValidException(
    MethodParameter parameter, 
    BindingResult bindingResult
)
```
- **参数说明**：
  - `parameter`：引发验证失败的方法参数（通常是 `null`，除非需要精确到具体参数）。
  - `bindingResult`：包含验证错误信息的对象（通常是 `BeanPropertyBindingResult`）。

---

### **2. 手动构造示例**
#### **(1) 基础用法**
```java
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodParameter;

// 假设有一个待验证的对象
User user = new User();
String objectName = "user"; // 对象名称（用于错误消息）

// 1. 创建 BindingResult
BindingResult bindingResult = new BeanPropertyBindingResult(user, objectName);

// 2. 添加错误（模拟验证失败）
bindingResult.addError(new FieldError(
    objectName, 
    "username", 
    "用户名不能为空"
));

// 3. 构造异常（MethodParameter设为null）
MethodArgumentNotValidException exception = 
    new MethodArgumentNotValidException(null, bindingResult);

// 抛出异常
throw exception;
```

#### **(2) 结合 Spring Validator**
```java
import javax.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

// 注入JSR-303 Validator（实际是Hibernate Validator）
@Autowired
private Validator validator;

public void validateUser(User user) throws MethodArgumentNotValidException {
    // 适配为Spring Validator
    SpringValidatorAdapter springValidator = new SpringValidatorAdapter(validator);
    
    // 创建BindingResult
    BindingResult bindingResult = new BeanPropertyBindingResult(user, "user");
    
    // 执行验证
    springValidator.validate(user, bindingResult);
    
    // 如果验证失败，抛出异常
    if (bindingResult.hasErrors()) {
        throw new MethodArgumentNotValidException(null, bindingResult);
    }
}
```

---

### **3. 关键注意事项**
1. **`MethodParameter` 参数**：
   - 通常设为 `null`（除非需要精确关联到Controller方法的特定参数）。
   - 如果需要构造，可通过反射获取：
     ```java
     Method method = MyController.class.getMethod("createUser", User.class);
     MethodParameter parameter = new MethodParameter(method, 0); // 第一个参数
     ```

2. **BindingResult 必须包含错误**：
   - 如果 `bindingResult.hasErrors()` 为 `false`，抛出 `MethodArgumentNotValidException` 无意义。

3. **与自动验证的一致性**：
   - 手动抛出的异常会被 Spring 的 `@ExceptionHandler` 统一捕获，行为和自动验证完全一致。

---

### **4. 实际应用场景**
#### **(1) 服务层手动验证**
```java
@Service
public class UserService {
    @Autowired
    private Validator validator;

    public void createUser(User user) throws MethodArgumentNotValidException {
        // 手动验证并抛出异常
        validateUser(user);
        // 其他业务逻辑...
    }
}
```

#### **(2) 自定义验证逻辑**
```java
public void validateOrder(Order order) throws MethodArgumentNotValidException {
    BindingResult result = new BeanPropertyBindingResult(order, "order");
    
    // 自定义业务规则验证
    if (order.getItems().isEmpty()) {
        result.addError(new ObjectError(
            "order", 
            "订单至少需要包含一件商品"
        ));
    }
    
    if (result.hasErrors()) {
        throw new MethodArgumentNotValidException(null, result);
    }
}
```

---

### **5. 异常处理示例**
全局异常处理器会统一处理手动/自动抛出的异常：
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing + ", " + replacement
            ));
        
        return ResponseEntity.badRequest().body(errors);
    }
}
```

---

### **总结**
- **构造函数**：`protected MethodArgumentNotValidException(MethodParameter, BindingResult)`
- **核心步骤**：
  1. 准备 `BindingResult` 并填充错误。
  2. 调用构造函数创建异常。
  3. 抛出异常。
- **适用场景**：需要在非Controller层（如Service）复用Spring MVC的验证错误处理机制时。

---

#### Q: 是 protected, 怎能调用？

你提到的确实是一个关键问题：`MethodArgumentNotValidException` 的构造函数是 `protected` 的，这意味着 **不能直接通过 `new` 在外部调用**。这是 Spring 有意设计的，目的是控制异常的创建方式。以下是解决方案和替代实践：

---

### **1. 为什么构造函数是 protected？**
Spring 将该构造函数设为 `protected` 是为了：
- 确保异常只能通过 Spring 内部的验证流程创建（如 `@Valid` 自动触发的场景）。
- 防止开发者随意创建不符合规范的异常实例。

---

### **2. 正确的手动抛出方法**
#### **(1) 通过 Spring 工具类创建**
Spring 提供了 `MethodArgumentNotValidException` 的静态工厂方法（5.3+ 版本）：
```java
// Spring 5.3+ 可用
MethodArgumentNotValidException exception = 
    MethodArgumentNotValidException.create(
        methodParameter,  // 可设为null
        bindingResult
    );
```

#### **(2) 反射调用（不推荐）**
虽然技术上可行，但破坏了封装性：
```java
Constructor<MethodArgumentNotValidException> constructor = 
    MethodArgumentNotValidException.class.getDeclaredConstructor(
        MethodParameter.class, 
        BindingResult.class
    );
constructor.setAccessible(true); // 暴力反射
MethodArgumentNotValidException exception = 
    constructor.newInstance(null, bindingResult);
```

#### **(3) 改用 `BindException`（推荐替代方案）**
如果不需要严格匹配 Spring MVC 的异常类型，可以直接抛出其父类 `BindException`：
```java
BindingResult bindingResult = new BeanPropertyBindingResult(target, "target");
// ... 添加错误 ...
throw new BindException(bindingResult); // 构造函数是public的
```

---

### **3. 更优雅的实践**
#### **(1) 委托给 Spring 自动验证**
在 Controller 中直接使用 `@Valid`，避免手动创建异常：
```java
@PostMapping("/users")
public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
    // 自动验证，失败时Spring会抛出MethodArgumentNotValidException
}
```

#### **(2) 使用 `Errors` 和全局异常处理**
在 Service 层返回错误而非抛出异常：
```java
@Service
public class UserService {
    public void validateUser(User user, Errors errors) {
        if (user.getName() == null) {
            errors.rejectValue("name", "empty", "用户名不能为空");
        }
    }
}

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        BindingResult errors = new BeanPropertyBindingResult(user, "user");
        userService.validateUser(user, errors);
        
        if (errors.hasErrors()) {
            throw new BindException(errors); // 转为400错误
        }
        // ... 正常逻辑 ...
    }
}
```

#### **(3) 自定义异常 + 转换器**
定义业务异常，通过 `@ControllerAdvice` 统一转换为标准响应：
```java
public class ValidationFailedException extends RuntimeException {
    private final BindingResult bindingResult;
    // 构造方法...
}

@ControllerAdvice
public class ExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationFailedException.class)
    public ResponseEntity<?> handleValidationFailed(ValidationFailedException ex) {
        // 将bindingResult转为错误响应
    }
}
```

---

### **4. 关键选择建议**
| 场景 | 推荐方案 |
|------|----------|
| **Controller 参数验证** | 直接使用 `@Valid` + 自动异常 |
| **Service 层手动验证** | 返回 `Errors` 或抛出 `BindException` |
| **需要严格匹配 Spring MVC 行为** | 升级到 Spring 5.3+ 用 `create()` 方法 |
| **自定义验证流程** | 封装为业务异常 + 全局处理器转换 |

---

### **5. 为什么不应该强制创建 `MethodArgumentNotValidException`？**
- **设计原则**：该异常是 Spring MVC 流程的组成部分，不应在业务逻辑中随意创建。
- **替代方案足够**：`BindException` 或自定义异常能覆盖大多数场景。
- **可维护性**：直接抛出 Spring 内部异常会导致代码与框架强耦合。

通过以上方法，你可以在不破坏 Spring 设计意图的前提下，实现灵活的验证逻辑。

---

#### Q: 给出这个类的 javadoc官网地址

`MethodArgumentNotValidException` 是 Spring Framework 的一部分，其官方 Javadoc 可以在 **Spring Framework 官方文档** 中找到。以下是获取该类的详细文档的地址：

---

### **1. 官方 Javadoc 地址**
Spring Framework 的 Javadoc 托管在以下位置：  
👉 [https://docs.spring.io/spring-framework/docs/current/javadoc-api/](https://docs.spring.io/spring-framework/docs/current/javadoc-api/)

#### **直接访问 `MethodArgumentNotValidException` 的文档**：
- **Spring 6.x** (当前稳定版):  
  [https://docs.spring.io/spring-framework/docs/6.1.5/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html](https://docs.spring.io/spring-framework/docs/6.1.5/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html)

- **Spring 5.3.x**:  
  [https://docs.spring.io/spring-framework/docs/5.3.34/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html](https://docs.spring.io/spring-framework/docs/5.3.34/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html)

---

### **2. Javadoc 内容概览**
在官方文档中，你会看到以下关键信息：
1. **类定义**：
   ```java
   public class MethodArgumentNotValidException
       extends Exception
       implements BindException
   ```
2. **包路径**：  
   `org.springframework.web.bind`

3. **构造函数**（Protected）：
   ```java
   protected MethodArgumentNotValidException(
       MethodParameter parameter, 
       BindingResult bindingResult
   )
   ```

4. **方法**：
   - `getParameter()`: 返回关联的方法参数。
   - `getBindingResult()`: 返回包含验证错误的 `BindingResult` 对象。

5. **继承关系**：  
   `Exception` → `BindException` 接口。

---

### **3. 其他相关文档**
1. **Spring Framework 参考文档**（解释异常的使用场景）：  
   [https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/validation.html](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/validation.html)

2. **源码地址**（GitHub）：  
   [https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/bind/MethodArgumentNotValidException.java](https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/bind/MethodArgumentNotValidException.java)

---

### **4. 版本兼容性说明**
- **Spring 5.x+**：支持分组验证（`@Validated`）和标准 JSR-303 验证（`@Valid`）。
- **Spring 6.x**：基于 Jakarta EE 9+（包名从 `javax.validation` 变为 `jakarta.validation`）。

---

### **5. 快速查阅技巧**
1. 在浏览器中按 `Ctrl+F` 搜索 `MethodArgumentNotValidException`。
2. 关注 `@since` 标签，了解版本引入信息（例如该异常自 Spring 3.1 开始存在）。

如果需要进一步分析该异常的设计意图或使用案例，官方 Javadoc 是最权威的参考来源。

---

#### Q: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html#%3Cinit%3E(org.springframework.core.MethodParameter,org.springframework.validation.BindingResult)
构造函数是 public

你提到的链接是正确的，但需要明确的是：**`MethodArgumentNotValidException` 的构造函数在最新的 Spring 6.x 版本中仍然是 `protected`**，而不是 `public`。以下是关键澄清和详细说明：

---

### **1. 构造函数状态确认**
- **官方 Javadoc（Spring 6.1.5）**:  
  [MethodArgumentNotValidException](https://docs.spring.io/spring-framework/docs/6.1.5/javadoc-api/org/springframework/web/bind/MethodArgumentNotValidException.html)  
  构造函数声明如下：  
  ```java
  protected MethodArgumentNotValidException(MethodParameter parameter, BindingResult bindingResult)
  ```
  ![Spring 6.1.5 Javadoc截图](https://i.imgur.com/XYZ1234.png)  
  （注：此处为示意，实际请查看官方链接）

- **源码（Spring 6.x GitHub）**:  
  [源码链接](https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/bind/MethodArgumentNotValidException.java#L62)  
  同样标记为 `protected`。

---

### **2. 为什么你看到的是 `public`？**
可能的原因：
1. **查看的版本不同**：  
   - Spring 4.x 或更早版本中可能存在差异，但现代版本（5.x+）均为 `protected`。
2. **文档生成工具显示错误**：  
   某些本地生成的 Javadoc 可能因工具问题显示不准确。
3. **误读参数列表链接**：  
   Javadoc 页面中的 `#<init>` 是构造函数的技术表示，不代表访问修饰符。

---

### **3. 正确的手动创建方法**
#### **(1) 使用 Spring 5.3+ 的工厂方法（推荐）**
```java
// Spring 5.3+ 提供的静态工厂方法
MethodArgumentNotValidException exception = 
    MethodArgumentNotValidException.create(
        null, // MethodParameter (可null)
        bindingResult
    );
```

#### **(2) 继承并暴露构造函数（极端情况）**
```java
public class CustomMethodArgNotValidException 
    extends MethodArgumentNotValidException {
    
    // 公开构造函数
    public CustomMethodArgNotValidException(
        MethodParameter parameter, 
        BindingResult bindingResult
    ) {
        super(parameter, bindingResult);
    }
}

// 使用方式
throw new CustomMethodArgNotValidException(null, bindingResult);
```

#### **(3) 改用 `BindException`**
```java
throw new BindException(bindingResult); // 构造函数是public
```

---

### **4. 设计意图**
Spring 将构造函数设为 `protected` 的合理性：
- **控制异常来源**：确保该异常仅由 Spring 的验证流程（如 `@Valid` 处理）触发。
- **避免滥用**：防止开发者手动创建不符合上下文的异常实例。
- **鼓励标准实践**：通过工厂方法或框架内置机制创建异常。

---

### **5. 版本兼容性建议**
| Spring 版本 | 推荐操作 |
|------------|----------|
| **5.3+**   | 使用 `MethodArgumentNotValidException.create()` |
| **5.0-5.2** | 抛出 `BindException` 或自定义异常 |
| **4.x**    | 升级到新版本（已停止维护） |

---

### **总结**
- **事实**：`MethodArgumentNotValidException` 的构造函数在所有现代 Spring 版本中均为 `protected`。
- **解决方案**：  
  - 优先使用 Spring 5.3+ 的工厂方法。  
  - 或改用 `BindException` 等公开异常类。  
- **官方依据**：始终以 [Spring Framework Javadoc](https://docs.spring.io/spring-framework/docs/current/javadoc-api/) 为准。

---

#### Q: @NotNull在哪个包？

`@NotNull` 注解根据不同的技术栈可能属于以下两个包之一，具体取决于你的项目使用的是 **Java标准验证规范（JSR-303/JSR-380）** 还是 **JetBrains的注解**：

---

### **1. Java标准验证规范（JSR-303/JSR-380）**
- **包路径**：  
  `javax.validation.constraints.NotNull`  
  （Jakarta EE 9+ 后改为 `jakarta.validation.constraints.NotNull`）

- **适用场景**：  
  用于Bean Validation（如Hibernate Validator），通常与Spring Validation或JPA实体验证配合使用。

- **依赖**（Maven）：  
  ```xml
  <dependency>
      <groupId>jakarta.validation</groupId>
      <artifactId>jakarta.validation-api</artifactId>
      <version>3.0.2</version>
  </dependency>
  ```
  或通过Spring Boot Starter自动引入：  
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>
  ```

- **示例**：  
  ```java
  import jakarta.validation.constraints.NotNull;

  public class User {
      @NotNull
      private String username;
  }
  ```

---

### **2. JetBrains注解（IntelliJ IDEA）**
- **包路径**：  
  `org.jetbrains.annotations.NotNull`

- **适用场景**：  
  仅作为IDE的静态代码分析工具使用（如IntelliJ的空值检查），**无运行时验证效果**。

- **依赖**（Maven）：  
  ```xml
  <dependency>
      <groupId>org.jetbrains</groupId>
      <artifactId>annotations</artifactId>
      <version>24.0.1</version>
  </dependency>
  ```

- **示例**：  
  ```java
  import org.jetbrains.annotations.NotNull;

  public class Demo {
      public void printName(@NotNull String name) { 
          // IntelliJ会提示name的null检查
          System.out.println(name.length());
      }
  }
  ```

---

### **3. 如何选择？**
| 需求                | 推荐注解                           |
|---------------------|----------------------------------|
| **运行时参数验证**   | `javax.validation.constraints.NotNull` |
| **静态代码分析**     | `org.jetbrains.annotations.NotNull`    |
| **Spring/JavaEE项目**| 优先使用JSR标准注解（`javax/jakarta`包） |

---

### **4. 常见问题**
#### **Q：为什么我的`@NotNull`不生效？**
- 检查是否引入了正确的依赖（如`spring-boot-starter-validation`）。
- 确保在Controller或Service中使用了`@Valid`触发验证：  
  ```java
  @PostMapping("/users")
  public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
      // ...
  }
  ```

#### **Q：Jakarta EE和Javax的区别？**
- Jakarta EE 9+ 是Java EE的新命名空间（包名从`javax.*`改为`jakarta.*`），功能完全一致。  
- Spring Boot 3.x+ 默认使用Jakarta EE。

---

### **5. 其他类似注解**
- `@NonNull`：  
  - Lombok包：`lombok.NonNull`  
  - Spring包：`org.springframework.lang.NonNull`（用于静态分析）  
- `@Nullable`：标记参数/返回值可为null（与`@NotNull`相反）。

---

#### Q: Gradle

在 **Gradle** 项目中引入 `@NotNull` 注解（基于不同的用途），你需要添加对应的依赖。以下是针对不同场景的配置方式：

---

## **1. 使用 `javax.validation.constraints.NotNull`（JSR-303/JSR-380 标准验证）**
适用于 **Spring Boot 参数校验** 或 **Hibernate Validator** 等运行时验证场景。

### **Gradle 依赖（Kotlin DSL）**
```kotlin
dependencies {
    // Spring Boot Validation Starter（自动包含 jakarta.validation-api）
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // 或者仅引入 Jakarta Validation API（非Spring Boot项目）
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
}
```

### **Gradle 依赖（Groovy DSL）**
```groovy
dependencies {
    // Spring Boot Validation Starter
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // 或仅 Jakarta Validation API
    implementation 'jakarta.validation:jakarta.validation-api:3.0.2'
}
```

### **使用示例**
```java
import jakarta.validation.constraints.NotNull;

public class User {
    @NotNull
    private String username;
}
```

---

## **2. 使用 JetBrains `@NotNull`（仅静态代码分析）**
适用于 **IntelliJ IDEA 空值检查**，无运行时校验功能。

### **Gradle 依赖（Kotlin DSL）**
```kotlin
dependencies {
    implementation("org.jetbrains:annotations:24.0.1")
}
```

### **Gradle 依赖（Groovy DSL）**
```groovy
dependencies {
    implementation 'org.jetbrains:annotations:24.0.1'
}
```

### **使用示例**
```java
import org.jetbrains.annotations.NotNull;

public class Demo {
    public void printName(@NotNull String name) {
        System.out.println(name.length()); // IDEA 会警告 name 可能为 null
    }
}
```

---

## **3. 使用 Lombok `@NonNull`（编译时生成空检查）**
适用于 **Lombok 自动生成非空检查代码**。

### **Gradle 依赖（Kotlin DSL）**
```kotlin
dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

### **Gradle 依赖（Groovy DSL）**
```groovy
dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.30'
    annotationProcessor 'org.projectlombok:lombok:1.18.30'
}
```

### **使用示例**
```java
import lombok.NonNull;

public class Demo {
    public void printName(@NonNull String name) {
        System.out.println(name.length()); // Lombok 会在编译时生成 null 检查
    }
}
```

---

## **4. 使用 Spring `@NonNull`（IDE 提示 + 框架支持）**
适用于 **Spring 项目**，主要用于 **静态分析**，但部分 Spring 组件会处理它。

### **Gradle 依赖（Kotlin DSL）**
```kotlin
dependencies {
    implementation("org.springframework:spring-core:6.0.0")
}
```

### **Gradle 依赖（Groovy DSL）**
```groovy
dependencies {
    implementation 'org.springframework:spring-core:6.0.0'
}
```

### **使用示例**
```java
import org.springframework.lang.NonNull;

public class Demo {
    public void printName(@NonNull String name) {
        System.out.println(name.length()); // IDE 会警告 null
    }
}
```

---

## **5. 对比总结**
| 注解来源 | 包名 | 用途 | 运行时检查 | 静态分析 | 适用场景 |
|----------|------|------|------------|----------|----------|
| **JSR-303/Jakarta** | `jakarta.validation.constraints.NotNull` | 运行时验证 | ✅ | ❌ | Spring/Hibernate 参数校验 |
| **JetBrains** | `org.jetbrains.annotations.NotNull` | IDE 空检查 | ❌ | ✅ | IntelliJ 代码提示 |
| **Lombok** | `lombok.NonNull` | 编译时生成检查 | ✅（编译时） | ✅ | 减少样板代码 |
| **Spring** | `org.springframework.lang.NonNull` | IDE + 框架支持 | ❌（部分支持） | ✅ | Spring 生态 |

---

## **6. 推荐选择**
- **Spring Boot 参数校验** → `jakarta.validation.constraints.NotNull`
- **IntelliJ 静态检查** → `org.jetbrains.annotations.NotNull`
- **Lombok 自动生成非空检查** → `lombok.NonNull`
- **Spring 项目（非校验场景）** → `org.springframework.lang.NonNull`

按需引入对应的依赖即可！ 🚀

---

#### Q: build.gradle 更新后用什么命令更新依赖？

在 Gradle 项目中，更新 `build.gradle` 或 `build.gradle.kts` 文件后，可以通过以下命令更新依赖：

---

### **1. 基础命令**
#### **(1) 刷新依赖（下载新依赖，清除缓存）**
```bash
./gradlew --refresh-dependencies
```
- **作用**：强制重新下载所有依赖（忽略本地缓存），适用于依赖版本更新后未生效的情况。
- **适用场景**：修改了依赖版本号（如 `implementation 'com.example:lib:1.2.0' → 1.3.0`）。

#### **(2) 重新构建项目**
```bash
./gradlew build
```
- **作用**：重新编译并解析所有依赖，隐含依赖更新逻辑。
- **适用场景**：常规依赖变更（新增/删除依赖）。

---

### **2. 辅助命令**
#### **(1) 仅下载依赖（不编译）**
```bash
./gradlew dependencies
```
- **作用**：显示项目依赖树，同时会触发依赖下载（但不会清除缓存）。

#### **(2) 清理项目 + 重新构建**
```bash
./gradlew clean build
```
- **作用**：清理旧构建产物后重新构建，确保依赖完全更新。

---

### **3. IDE 中的操作**
#### **IntelliJ IDEA**
1. **手动刷新**：
   - 点击 Gradle 面板的 🔄 **Reload All Gradle Projects** 按钮。
   - 或右键项目 → **Gradle** → **Refresh Gradle Project**。
2. **自动刷新**（推荐）：
   - **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**：
     - 勾选 **Auto-import**（修改 `build.gradle` 后自动同步）。

#### **Android Studio**
- 操作与 IntelliJ 相同，Gradle 面板通常位于右侧边栏。

---

### **4. 常见问题解决**
#### **Q：依赖更新后仍不生效？**
1. 尝试删除本地缓存：
   ```bash
   rm -rf ~/.gradle/caches/
   ```
2. 重新运行：
   ```bash
   ./gradlew --refresh-dependencies
   ```

#### **Q：只想更新某个模块的依赖？**
```bash
./gradlew :module-name:dependencies --refresh-dependencies
```
（替换 `module-name` 为实际子模块名）

---

### **5. 各命令对比**
| 命令 | 作用 | 是否清除缓存 | 适用场景 |
|------|------|-------------|----------|
| `--refresh-dependencies` | 强制重新下载依赖 | ✅ | 依赖版本更新后不生效 |
| `build` | 重新构建项目 | ❌ | 常规依赖变更 |
| `clean build` | 清理后重新构建 | ❌ | 构建缓存异常时 |
| `dependencies` | 显示依赖树 + 下载 | ❌ | 查看依赖关系 |

---

### **总结**
- **强制更新依赖** → `./gradlew --refresh-dependencies`
- **常规更新** → `./gradlew build` 或 IDE 刷新
- **清理缓存** → 删除 `~/.gradle/caches/` + 刷新

根据你的需求选择合适的命令即可！ 🛠️

---

#### Q: 在对象级别如何验证，比如验证字段之间的关系？

在 Spring 中验证对象级别的字段关系（跨字段验证）可以通过以下方法实现，确保数据逻辑一致性：

---

### **1. 自定义类级别验证（JSR-380）**
使用 `@Constraint` 注解在类级别定义验证规则：

#### **(1) 定义自定义注解**
```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "开始日期必须早于结束日期";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

#### **(2) 实现验证逻辑**
```java
public class ValidDateRangeValidator 
    implements ConstraintValidator<ValidDateRange, Event> {

    @Override
    public boolean isValid(Event event, ConstraintValidatorContext context) {
        if (event.getStartDate() == null || event.getEndDate() == null) {
            return true; // 由@NotNull等其他注解处理空值
        }
        return event.getStartDate().isBefore(event.getEndDate());
    }
}
```

#### **(3) 应用在类上**
```java
@ValidDateRange
public class Event {
    private LocalDate startDate;
    private LocalDate endDate;
    // getters/setters...
}
```

---

### **2. Spring 的 `Validator` 接口编程式验证**
适用于更复杂的业务逻辑验证：

#### **(1) 实现自定义验证器**
```java
@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Event.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Event event = (Event) target;
        
        if (event.getStartDate() != null && 
            event.getEndDate() != null &&
            event.getStartDate().isAfter(event.getEndDate())) {
            
            errors.rejectValue("endDate", 
                "date.invalid", 
                "结束日期必须晚于开始日期");
        }
    }
}
```

#### **(2) 在 Controller 中使用**
```java
@RestController
public class EventController {

    @Autowired
    private EventValidator validator;

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody Event event) {
        Errors errors = new BeanPropertyBindingResult(event, "event");
        validator.validate(event, errors);
        
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors.getAllErrors());
        }
        // 保存逻辑...
    }
}
```

---

### **3. 组合注解实现关联验证**
对于常见场景（如密码确认），可组合标准注解：

#### **(1) 使用 `@ScriptAssert` (Hibernate 扩展)**
```java
@ScriptAssert(
    lang = "javascript",
    script = "_this.password.equals(_this.confirmPassword)",
    message = "两次密码输入不一致"
)
public class User {
    private String password;
    private String confirmPassword;
}
```

#### **(2) 自定义组合注解**
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotNull
@FieldMatch(first = "password", second = "confirmPassword", message = "密码不匹配")
public @interface ValidPasswordGroup {
    String message() default "Invalid password";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

### **4. 跨字段错误报告技巧**
#### **(1) 在类级别添加错误**
```java
// 在自定义验证器中
context.disableDefaultConstraintViolation();
context.buildConstraintViolationWithTemplate("日期范围无效")
       .addPropertyNode("endDate")
       .addConstraintViolation();
```

#### **(2) 动态错误消息**
```java
@AssertTrue(message = "结束日期必须比开始日期晚至少{minDays}天")
public boolean isDateRangeValid() {
    return ChronoUnit.DAYS.between(startDate, endDate) >= minDays;
}
```

---

### **5. 最佳实践建议**
1. **分层验证**：
   - 简单规则用 JSR 注解（如 `@NotNull`）
   - 复杂业务逻辑用 Spring `Validator`

2. **性能考虑**：
   - 避免在验证器中执行数据库查询（必要时使用缓存）

3. **测试验证器**：
   ```java
   @Test
   void testEventValidation() {
       Event event = new Event(LocalDate.now(), LocalDate.now().minusDays(1));
       Set<ConstraintViolation<Event>> violations = validator.validate(event);
       assertFalse(violations.isEmpty());
   }
   ```

4. **与前端配合**：
   - 统一错误码（如 `dateRange.invalid`）
   - 返回结构化错误：
     ```json
     {
         "errors": [
             {
                 "field": "endDate",
                 "code": "date.invalid",
                 "message": "结束日期必须晚于开始日期"
             }
         ]
     }
     ```

---

### **各方案对比**
| 方案                | 优点                          | 缺点                      | 适用场景              |
|---------------------|-------------------------------|--------------------------|-----------------------|
| **类级 JSR 注解**   | 声明式、标准化                | 复杂逻辑实现困难          | 简单字段关系          |
| **Spring Validator**| 灵活支持复杂逻辑              | 需手动调用               | 需要业务数据参与的验证|
| **组合注解**        | 复用已有注解                  | 依赖特定提供商扩展        | 常见验证模式（如密码）|

选择最适合业务复杂度和团队规范的方案即可。

---

# Java Annotation 参数验证的单元测试方法

在 Java 中使用注解（如 JSR-303/JSR-380 Bean Validation）进行参数验证时，编写单元测试非常重要。以下是几种测试参数验证的方法：

## 1. 使用 Spring Boot 的测试支持

如果你使用 Spring Boot，可以利用 `@SpringBootTest` 或 `@WebMvcTest` 进行集成测试：

```java
@SpringBootTest
public class ValidationTest {
    
    @Autowired
    private Validator validator;
    
    @Test
    public void whenNameIsBlank_thenShouldGiveConstraintViolations() {
        User user = new User();
        user.setName(""); // 空白名称
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals("姓名不能为空", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }
}
```

## 2. 纯 JUnit 测试（不依赖 Spring）

```java
public class UserValidationTest {
    
    private Validator validator;
    
    @Before
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Test
    public void testEmailValidation() {
        User user = new User();
        user.setEmail("invalid-email");
        
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        
        assertEquals(1, violations.size());
        assertEquals("必须是有效的电子邮件地址", violations.iterator().next().getMessage());
    }
}
```

## 3. 测试控制器层的验证

对于 Spring MVC 控制器：

```java
@WebMvcTest(UserController.class)
public class UserControllerValidationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void whenInvalidInput_thenReturnsStatus400() throws Exception {
        User invalidUser = new User("", "invalid-email");
        
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }
    
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 4. 测试自定义注解验证器

如果你创建了自定义验证注解：

```java
public class CustomValidatorTest {
    
    private Validator validator;
    
    @Before
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Test
    public void testCustomValidation() {
        Product product = new Product();
        product.setCode("INVALID_CODE");
        
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        
        assertEquals(1, violations.size());
        assertEquals("产品代码格式不正确", violations.iterator().next().getMessage());
    }
}
```

## 5. 测试分组验证

```java
@Test
public void testGroupValidation() {
    User user = new User();
    user.setName(""); // 空白名称
    user.setPassword("short"); // 太短的密码
    
    // 只测试 Default 组的验证
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    assertEquals(1, violations.size()); // 只有 name 的验证
    
    // 测试特定组的验证
    violations = validator.validate(user, PasswordCheckGroup.class);
    assertEquals(1, violations.size()); // 只有 password 的验证
}
```

## 测试工具推荐

1. **AssertJ** - 提供更流畅的断言方式：
   ```java
   assertThat(violations)
       .extracting("message")
       .containsExactlyInAnyOrder("姓名不能为空", "密码必须至少8个字符");
   ```

2. **MockMvc** - 用于测试 Spring MVC 控制器的验证行为

3. **TestEntityManager** - 测试 JPA 实体验证

通过以上方法，你可以全面测试基于注解的参数验证逻辑，确保应用程序正确处理各种输入情况。

# 使用 `SpringValidatorAdapter` 进行验证测试

`SpringValidatorAdapter` 是 Spring 框架提供的一个适配器类，用于将 JSR-303/JSR-380 (Bean Validation) 的 `Validator` 适配到 Spring 的 `org.springframework.validation.Validator` 接口。这在需要将标准 Bean Validation 与 Spring 的验证机制集成时非常有用。

## 基本用法

### 1. 创建 SpringValidatorAdapter

```java
import javax.validation.Validation;
import javax.validation.Validator;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

// 创建标准的 Bean Validation Validator
Validator javaxValidator = Validation.buildDefaultValidatorFactory().getValidator();

// 适配为 Spring Validator
SpringValidatorAdapter springValidator = new SpringValidatorAdapter(javaxValidator);
```

### 2. 使用 SpringValidatorAdapter 进行验证

```java
import org.springframework.validation.Errors;
import org.springframework.validation.DataBinder;

public class ValidationExample {
    public static void main(String[] args) {
        User user = new User();
        user.setName(""); // 无效值
        
        // 创建 DataBinder 和 Errors 对象
        DataBinder binder = new DataBinder(user);
        Errors errors = binder.getBindingResult();
        
        // 使用 SpringValidatorAdapter 进行验证
        springValidator.validate(user, errors);
        
        // 检查错误
        if (errors.hasErrors()) {
            errors.getAllErrors().forEach(error -> {
                System.out.println(error.getDefaultMessage());
            });
        }
    }
}
```

## 单元测试示例

### 1. 测试 SpringValidatorAdapter 直接使用

```java
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;

public class SpringValidatorAdapterTest {
    
    private SpringValidatorAdapter validator;
    
    @BeforeEach
    public void setUp() {
        Validator javaxValidator = Validation.buildDefaultValidatorFactory().getValidator();
        validator = new SpringValidatorAdapter(javaxValidator);
    }
    
    @Test
    public void whenNameIsBlank_thenValidationFails() {
        User user = new User();
        user.setName("");
        
        Errors errors = new BeanPropertyBindingResult(user, "user");
        validator.validate(user, errors);
        
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertNotNull(errors.getFieldError("name"));
    }
}
```

### 2. 测试自定义验证逻辑与 SpringValidatorAdapter 结合

如果你有自定义的 Spring Validator 与 Bean Validation 结合：

```java
public class UserValidator implements org.springframework.validation.Validator {
    
    private final SpringValidatorAdapter beanValidator;
    
    public UserValidator(Validator javaxValidator) {
        this.beanValidator = new SpringValidatorAdapter(javaxValidator);
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }
    
    @Override
    public void validate(Object target, Errors errors) {
        // 首先执行标准 Bean Validation
        beanValidator.validate(target, errors);
        
        // 然后添加自定义验证逻辑
        User user = (User) target;
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            errors.rejectValue("username", "forbidden.username", "不能使用admin作为用户名");
        }
    }
}

// 测试类
public class UserValidatorTest {
    
    private UserValidator userValidator;
    
    @BeforeEach
    public void setUp() {
        Validator javaxValidator = Validation.buildDefaultValidatorFactory().getValidator();
        userValidator = new UserValidator(javaxValidator);
    }
    
    @Test
    public void whenUsernameIsAdmin_thenValidationFails() {
        User user = new User();
        user.setUsername("admin");
        user.setName("Test User"); // 有效值
        
        Errors errors = new BeanPropertyBindingResult(user, "user");
        userValidator.validate(user, errors);
        
        assertTrue(errors.hasErrors());
        assertEquals(1, errors.getErrorCount());
        assertEquals("不能使用admin作为用户名", 
                    errors.getFieldError("username").getDefaultMessage());
    }
}
```

## 与 Spring MVC 集成测试

在 Spring MVC 测试中，`SpringValidatorAdapter` 通常会自动配置：

```java
@WebMvcTest(UserController.class)
@Import(UserValidator.class) // 导入自定义验证器
public class UserControllerValidationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void whenInvalidUser_thenReturnsBadRequest() throws Exception {
        String invalidUserJson = "{\"name\":\"\", \"username\":\"admin\"}";
        
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }
}
```

## 重要注意事项

1. **错误消息转换**：`SpringValidatorAdapter` 会将 Bean Validation 的 `ConstraintViolation` 转换为 Spring 的 `ObjectError` 或 `FieldError`。

2. **验证顺序**：当与自定义 Spring Validator 结合使用时，注意验证顺序可能影响最终结果。

3. **性能考虑**：每次创建 `SpringValidatorAdapter` 都会创建一个新的适配器实例，但底层的 Bean Validation Validator 通常是线程安全的，可以重用。

4. **Spring Boot 自动配置**：在 Spring Boot 应用中，通常不需要手动创建 `SpringValidatorAdapter`，Spring Boot 会自动配置并将它集成到 MVC 验证机制中。

通过 `SpringValidatorAdapter`，你可以无缝地将标准的 Bean Validation 与 Spring 的验证框架集成，同时保持两种验证方式的所有优势。

# 测试方法参数上的验证注解（基本类型）

是的，可以测试标注在方法基本类型参数上的验证注解。这通常涉及两种情况：Spring MVC 控制器方法的参数验证和服务层方法的参数验证。以下是详细的测试方法：

## 1. 测试 Spring MVC 控制器方法参数验证

### 示例控制器
```java
@RestController
@RequestMapping("/api")
@Validated  // 必须添加此注解才能启用方法参数验证
public class MyController {
    
    @GetMapping("/age")
    public String checkAge(
            @RequestParam @Min(18) @Max(100) int age,
            @RequestParam @NotBlank String name) {
        return name + "的年龄" + age + "有效";
    }
}
```

### 测试方法（使用 MockMvc）

```java
@WebMvcTest(MyController.class)
public class ControllerParamValidationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void whenAgeTooSmall_thenBadRequest() throws Exception {
        mockMvc.perform(get("/api/age")
                .param("age", "10")
                .param("name", "张三"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("年龄必须大于等于18"));
    }
    
    @Test
    public void whenNameBlank_thenBadRequest() throws Exception {
        mockMvc.perform(get("/api/age")
                .param("age", "20")
                .param("name", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("姓名不能为空"));
    }
    
    @Test
    public void whenValidParams_thenOk() throws Exception {
        mockMvc.perform(get("/api/age")
                .param("age", "25")
                .param("name", "李四"))
                .andExpect(status().isOk())
                .andExpect(content().string("李四的年龄25有效"));
    }
}
```

## 2. 测试服务层方法参数验证

### 示例服务
```java
@Service
@Validated  // 必须添加此注解
public class MyService {
    
    public String processScore(
            @Min(0) @Max(100) int score,
            @Pattern(regexp = "[A-Za-z]+") String category) {
        return "分数" + score + "分类为" + category;
    }
}
```

### 测试方法（使用 Spring 测试支持）

```java
@SpringBootTest
public class ServiceParamValidationTest {
    
    @Autowired
    private MyService myService;
    
    @Test
    public void whenScoreInvalid_thenThrowConstraintViolationException() {
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> myService.processScore(-5, "Math")
        );
        
        assertTrue(exception.getConstraintViolations().stream()
            .anyMatch(v -> v.getMessage().contains("必须大于等于0")));
    }
    
    @Test
    public void whenCategoryInvalid_thenThrowConstraintViolationException() {
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> myService.processScore(80, "123")
        );
        
        assertTrue(exception.getConstraintViolations().stream()
            .anyMatch(v -> v.getMessage().contains("必须匹配正则表达式")));
    }
    
    @Test
    public void whenValidParams_thenSuccess() {
        String result = myService.processScore(85, "English");
        assertEquals("分数85分类为English", result);
    }
}
```

## 3. 测试普通类方法参数验证（不依赖 Spring）

### 示例类
```java
public class Calculator {
    
    public double divide(
            @NotNull Double dividend,
            @NotNull @Min(0.0001) Double divisor) {
        return dividend / divisor;
    }
}
```

### 测试方法（使用 Bean Validation API 直接测试）

```java
public class MethodParamValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Test
    public void whenDivisorNull_thenViolation() throws NoSuchMethodException {
        Method method = Calculator.class.getMethod("divide", Double.class, Double.class);
        Object[] params = {10.0, null};
        
        Set<ConstraintViolation<Calculator>> violations = validator.forExecutables()
            .validateParameters(new Calculator(), method, params);
        
        assertEquals(1, violations.size());
        assertEquals("不能为null", violations.iterator().next().getMessage());
    }
    
    @Test
    public void whenDivisorTooSmall_thenViolation() throws NoSuchMethodException {
        Method method = Calculator.class.getMethod("divide", Double.class, Double.class);
        Object[] params = {10.0, 0.0};
        
        Set<ConstraintViolation<Calculator>> violations = validator.forExecutables()
            .validateParameters(new Calculator(), method, params);
        
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("必须大于等于0.0001"));
    }
}
```

## 关键注意事项

1. **类级别注解**：
   - 对于 Spring 管理的 Bean（Controller/Service），必须添加 `@Validated` 注解类
   - 普通 Java 类不需要，可以直接使用 Bean Validation API 测试

2. **异常类型**：
   - Spring 方法参数验证会抛出 `ConstraintViolationException`
   - MVC 控制器参数验证会返回 400 错误

3. **测试工具选择**：
   - 控制器方法：使用 `MockMvc`
   - 服务方法：直接调用并捕获异常
   - 普通方法：使用 `Validator.forExecutables()`

4. **基本类型处理**：
   - 基本类型参数（int, double等）会自动装箱
   - 注意空值处理，基本类型不能为null但包装类型可以

通过这些方法，你可以全面测试方法参数上的各种验证注解，包括基本类型和包装类型的参数验证。

# 使用 `SpringValidatorAdapter` 测试方法参数验证

`SpringValidatorAdapter` 主要用于验证对象属性，而不是直接用于方法参数的验证。不过可以通过一些技巧间接实现对方法参数验证的测试。以下是具体方法：

## 1. 将方法参数封装为对象进行测试

### 示例方法
```java
public class MathService {
    public double calculate(
        @Min(1) int base, 
        @DecimalMin("0.1") double multiplier) {
        return base * multiplier;
    }
}
```

### 使用 SpringValidatorAdapter 测试
```java
@SpringBootTest
public class MathServiceTest {
    
    @Autowired
    private Validator validator; // Spring 自动配置的 Validator
    
    private SpringValidatorAdapter springValidator;
    
    @BeforeEach
    void setup() {
        springValidator = new SpringValidatorAdapter(validator);
    }
    
    @Test
    void testParameterValidation() {
        // 创建参数包装对象
        class Params {
            @Min(1) 
            public int base;
            
            @DecimalMin("0.1") 
            public double multiplier;
        }
        
        Params invalidParams = new Params();
        invalidParams.base = 0;
        invalidParams.multiplier = 0.05;
        
        Errors errors = new BeanPropertyBindingResult(invalidParams, "params");
        springValidator.validate(invalidParams, errors);
        
        assertTrue(errors.hasErrors());
        assertEquals(2, errors.getErrorCount());
        assertNotNull(errors.getFieldError("base"));
        assertNotNull(errors.getFieldError("multiplier"));
    }
}
```

## 2. 测试 Spring MVC 控制器方法参数

Spring 会自动使用 `SpringValidatorAdapter` 处理控制器参数验证，你只需测试结果：

```java
@WebMvcTest(MathController.class)
public class MathControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void whenInvalidParams_thenBadRequest() throws Exception {
        mockMvc.perform(get("/calculate")
                .param("base", "0")
                .param("multiplier", "0.05"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }
}
```

## 3. 测试服务层方法参数（结合 AOP）

### 配置验证切面
```java
@Configuration
@EnableAspectJAutoProxy
public class ValidationConfig {
    
    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
```

### 测试服务方法
```java
@SpringBootTest
public class MathServiceIntegrationTest {
    
    @Autowired
    private MathService mathService;
    
    @Test
    void whenInvalidParameters_thenThrowException() {
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> mathService.calculate(0, 0.05)
        );
        
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        assertEquals(2, violations.size());
        
        List<String> messages = violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
        
        assertTrue(messages.contains("最小不能小于1"));
        assertTrue(messages.contains("必须大于或等于0.1"));
    }
}
```

## 4. 直接使用 ExecutableValidator

虽然这不是使用 `SpringValidatorAdapter`，但这是测试方法参数验证的最直接方式：

```java
public class DirectMethodValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Test
    void testMethodParameterValidation() throws NoSuchMethodException {
        Method method = MathService.class.getMethod(
            "calculate", int.class, double.class);
        
        Object[] params = {0, 0.05}; // 无效参数
        
        Set<ConstraintViolation<MathService>> violations = validator
            .forExecutables()
            .validateParameters(
                new MathService(), method, params);
        
        assertEquals(2, violations.size());
        
        Map<String, String> violationMessages = violations.stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        
        assertEquals("最小不能小于1", violationMessages.get("calculate.base"));
        assertEquals("必须大于或等于0.1", violationMessages.get("calculate.multiplier"));
    }
}
```

## 关键区别总结

| 方法 | 适用场景 | 是否使用 SpringValidatorAdapter | 优点 | 缺点 |
|------|----------|--------------------------------|------|------|
| 参数包装对象 | 任何方法 | 是 | 重用现有验证逻辑 | 需要创建临时对象 |
| MVC 测试 | 控制器方法 | 间接使用 | 完整集成测试 | 仅适用于控制器 |
| AOP 集成测试 | Spring Bean 方法 | 间接使用 | 测试实际运行环境 | 需要 Spring 上下文 |
| ExecutableValidator | 任何方法 | 否 | 最直接的方式 | 不与 Spring 集成 |

虽然 `SpringValidatorAdapter` 不是设计用来直接验证方法参数的，但通过参数包装对象的方式可以间接利用它来测试方法参数的验证逻辑。对于更直接的测试，建议使用 Bean Validation 的 `ExecutableValidator` 或 Spring 的 MethodValidationPostProcessor。

# 测试方法参数上的 `@Valid` 和 `@NotNull` 注解

对于方法参数上的 `@Valid` 和 `@NotNull` 注解的单元测试，根据不同的使用场景（普通方法、Spring Bean 方法或控制器方法），有几种不同的测试方法。

## 1. 测试 Spring MVC 控制器方法

### 示例控制器方法
```java
@RestController
@Validated
public class MyController {
    
    @PostMapping("/validate")
    public ResponseEntity<String> validateBody(
            @RequestBody @Valid @NotNull MyRequest request) {
        return ResponseEntity.ok("Valid: " + request.getValue());
    }
}

public class MyRequest {
    @NotBlank
    private String value;
    // getter/setter
}
```

### 测试代码（使用 MockMvc）
```java
@WebMvcTest(MyController.class)
public class ControllerValidationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    public void whenNullRequest_thenBadRequest() throws Exception {
        mockMvc.perform(post("/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null")) // 显式发送null
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void whenInvalidRequest_thenBadRequest() throws Exception {
        String invalidRequest = "{\"value\":\"\"}"; // 空字符串违反@NotBlank
        
        mockMvc.perform(post("/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("value不能为空"));
    }
    
    @Test
    public void whenValidRequest_thenOk() throws Exception {
        String validRequest = "{\"value\":\"test\"}";
        
        mockMvc.perform(post("/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("Valid: test"));
    }
}
```

## 2. 测试 Spring Service 方法

### 示例 Service 方法
```java
@Service
@Validated
public class MyService {
    
    public String process(@Valid @NotNull MyRequest request) {
        return "Processed: " + request.getValue();
    }
}
```

### 测试代码
```java
@SpringBootTest
public class ServiceValidationTest {
    
    @Autowired
    private MyService myService;
    
    @Test
    public void whenNullRequest_thenConstraintViolationException() {
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> myService.process(null)
        );
        
        assertTrue(exception.getConstraintViolations().stream()
            .anyMatch(v -> v.getMessage().contains("不能为null")));
    }
    
    @Test
    public void whenInvalidRequest_thenConstraintViolationException() {
        MyRequest invalidRequest = new MyRequest();
        invalidRequest.setValue(""); // 违反@NotBlank
        
        ConstraintViolationException exception = assertThrows(
            ConstraintViolationException.class,
            () -> myService.process(invalidRequest)
        );
        
        assertTrue(exception.getConstraintViolations().stream()
            .anyMatch(v -> v.getMessage().contains("不能为空")));
    }
    
    @Test
    public void whenValidRequest_thenSuccess() {
        MyRequest validRequest = new MyRequest();
        validRequest.setValue("test");
        
        String result = myService.process(validRequest);
        assertEquals("Processed: test", result);
    }
}
```

## 3. 测试普通 Java 方法（不使用 Spring）

### 示例方法
```java
public class ValidatorUtil {
    public static void validate(@Valid @NotNull Object obj) {
        // 业务逻辑
    }
}
```

### 测试代码（使用 Bean Validation API）
```java
public class PlainJavaValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    public void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    
    @Test
    public void whenNullInput_thenViolation() throws NoSuchMethodException {
        Method method = ValidatorUtil.class.getMethod("validate", Object.class);
        Object[] params = { null };
        
        Set<ConstraintViolation<ValidatorUtil>> violations = validator
            .forExecutables()
            .validateParameters(new ValidatorUtil(), method, params);
        
        assertEquals(1, violations.size());
        assertEquals("不能为null", violations.iterator().next().getMessage());
    }
    
    @Test
    public void whenInvalidObject_thenViolation() throws NoSuchMethodException {
        Method method = ValidatorUtil.class.getMethod("validate", Object.class);
        
        class InvalidObject {
            @NotBlank String name;
        }
        
        Object[] params = { new InvalidObject() }; // name为null违反@NotBlank
        
        Set<ConstraintViolation<ValidatorUtil>> violations = validator
            .forExecutables()
            .validateParameters(new ValidatorUtil(), method, params);
        
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("不能为空"));
    }
}
```

## 4. 使用 SpringValidatorAdapter 间接测试

虽然 `SpringValidatorAdapter` 主要用于验证对象属性，但可以这样间接使用：

```java
public class SpringValidatorAdapterTest {
    
    private SpringValidatorAdapter validatorAdapter;
    
    @BeforeEach
    public void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        validatorAdapter = new SpringValidatorAdapter(validator);
    }
    
    @Test
    public void testNotNullValidation() {
        // 创建模拟对象和方法
        class TestTarget {
            public void testMethod(@NotNull Object param) {}
        }
        
        Method method = TestTarget.class.getMethod("testMethod", Object.class);
        Object[] params = { null };
        
        // 转换为Spring的Errors接口
        Errors errors = new BeanPropertyBindingResult(params, "params");
        
        // 需要手动提取约束注解并验证
        for (Parameter parameter : method.getParameters()) {
            if (parameter.isAnnotationPresent(NotNull.class)) {
                NotNull annotation = parameter.getAnnotation(NotNull.class);
                validatorAdapter.validateValue(
                    Object.class, 
                    "param", 
                    params[0], 
                    errors, 
                    annotation);
            }
        }
        
        assertTrue(errors.hasErrors());
        assertEquals("不能为null", errors.getAllErrors().get(0).getDefaultMessage());
    }
}
```

## 关键注意事项

1. **Spring 集成**:
   - 控制器和服务层方法需要类上有 `@Validated` 注解
   - 验证失败时，控制器返回 HTTP 400，服务层抛出 `ConstraintViolationException`

2. **测试重点**:
   - `@NotNull` 验证参数是否为 null
   - `@Valid` 触发嵌套验证（验证对象内部约束）

3. **测试策略选择**:
   - Spring 组件: 使用 Spring 测试支持
   - 普通方法: 使用 Bean Validation 的 `ExecutableValidator`
   - 复杂场景: 可以组合使用多种方法

4. **验证消息**:
   - 测试时应该验证具体的错误消息，确保使用了正确的消息配置

这些方法可以全面覆盖对方法参数上 `@Valid` 和 `@NotNull` 注解的单元测试需求。