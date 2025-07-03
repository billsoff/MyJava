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