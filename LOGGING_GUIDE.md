# Hướng Dẫn Ghi Log Thời Gian Thực Hiện Chức Năng (FR-11 - AF1)

## Tổng Quan

Ứng dụng đã được cấu hình để tự động ghi log thời gian thực hiện của tất cả các chức năng mà không cần thêm code vào các service hay controller. Điều này được thực hiện bằng cách sử dụng **Aspect-Oriented Programming (AOP)** của Spring Framework.

## Kiến Trúc

### 1. LogExecutionTime Annotation (`annotation/LogExecutionTime.java`)
- Đây là custom annotation dùng để đánh dấu các methods cần tự động ghi log thời gian thực hiện
- Được áp dụng trên các public methods trong services và controllers
- Không có tham số, chỉ là một marker annotation

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}
```

### 2. ExecutionTimeAspect (`annotation/ExecutionTimeAspect.java`)
- Đây là AOP Aspect chịu trách nhiệm intercept tất cả các methods có `@LogExecutionTime` annotation
- Sử dụng `@Around` advice để:
  - Ghi log khi method bắt đầu thực hiện
  - Đo lường thời gian thực hiện (execution time)
  - Ghi log khi method kết thúc (bao gồm cả thời gian)
  - Xử lý exceptions nếu có

```
Luồng thực hiện:
START: ClassName.methodName
  ↓ (method execution)
END: ClassName.methodName - Thời gian thực hiện: Xms
```

## Cách Sử Dụng

### Thêm @LogExecutionTime vào Method

Đơn giản chỉ cần thêm `@LogExecutionTime` annotation trước method:

```java
@Service
public class UserServiceImpl implements UserService {
    
    @LogExecutionTime
    @Override
    public User register(UserDto userDto) {
        // Xử lý logic đăng ký...
    }
    
    @LogExecutionTime
    public Optional<User> findByEmail(String email) {
        // Xử lý logic tìm kiếm...
    }
}
```

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @LogExecutionTime
    @PostMapping("/login")
    public ResponseEntity<APIDataResponse<Object>> login(@Valid @RequestBody UserDtoLogin login) {
        // Xử lý logic đăng nhập...
    }
    
    @LogExecutionTime
    @PostMapping("/register")
    public ResponseEntity<APIDataResponse<User>> register(@Valid @RequestBody UserDto userDto) {
        // Xử lý logic đăng ký...
    }
}
```

## Các Methods Đã Được Áp Dụng

### Service Implementations:
- **UserServiceImpl**: register, changePassword, findByEmail
- **JobPostingServiceImpl**: postNewJobPosting
- **RefreshTokenService**: createRefreshToken, findByToken, delete, deleteByUser, revokeToken
- **PasswordResetServiceImpl**: createTokenForUser, findByToken, revokeAllForUser
- **BlackListServiceImpl**: blacklistToken, isBlacklisted

### Controllers:
- **AuthController**: register, login, logout, refresh, changePassword, forgotPassword, resetPassword
- **AdminController**: listUsers, updateUser, listJobs, approveJob
- **EmployerController**: postNewJobPosting, listMyJobs, updateJob, deleteJob, listApplications, updateApplicationStatus

## Output Log

Khi một method được gọi, bạn sẽ thấy trong logs:

```
INFO: START: UserServiceImpl.register
...
INFO: END: UserServiceImpl.register - Thời gian thực hiện: 234ms
```

Nếu có lỗi:

```
INFO: START: AuthController.login
...
ERROR: ERROR: AuthController.login - Thời gian thực hiện: 150ms - Exception: Invalid credentials
```

## Lợi Ích

1. **Tách Biệt Concerns**: Code ghi log hoàn toàn tách biệt khỏi code xử lý nghiệp vụ
2. **Không Xâm Lấn**: Không cần thêm code vào mỗi method
3. **Dễ Bảo Trì**: Nếu muốn thay đổi cách ghi log, chỉ cần sửa Aspect class
4. **Nhất Quán**: Tất cả các methods được ghi log theo cách nhất quán
5. **Hiệu Năng**: Có thể dễ dàng bật/tắt logging bằng cách xóa annotation

## Cơ Chế Hoạt Động

1. Spring AOP tạo proxy cho tất cả các beans
2. Khi method có `@LogExecutionTime` được gọi, proxy intercept lệnh gọi
3. ExecutionTimeAspect `@Around` advice được thực thi:
   - Ghi log START
   - Ghi lại thời gian bắt đầu
   - Thực thi method gốc
   - Ghi lại thời gian kết thúc
   - Tính toán execution time
   - Ghi log END với execution time
4. Return result từ method gốc

## Configuration Dependencies

Đảm bảo file `build.gradle` có dependency após:

```gradle
implementation 'org.springframework.boot:spring-boot-starter-aop:3.5.14'
```

Điều này đã được thêm vào project.

## Mở Rộng

Để thêm logging cho method mới:

```java
@LogExecutionTime
public void myNewMethod() {
    // Method implementation
}
```

Khi đó ExecutionTimeAspect sẽ tự động intercept và ghi log mà không cần thêm bất kỳ code nào khác.

## Troubleshooting

**Problem**: Logging không xuất hiện
- **Solution**: Đảm bảo method là public và có @LogExecutionTime annotation
- **Solution**: Kiểm tra logging level trong application.properties (INFO level trở lên)

**Problem**: Execution time không chính xác
- **Solution**: Thời gian này bao gồm thời gian của tất cả mọi thứ trong method, bao gồm cả database queries, external API calls, etc.

## Tài Liệu Tham Khảo

- Spring AOP Documentation: https://spring.io/guides/gs/aspect-oriented-programming/
- ExecutionTimeAspect: `src/main/java/re/project_final_service/annotation/ExecutionTimeAspect.java`
- LogExecutionTime: `src/main/java/re/project_final_service/annotation/LogExecutionTime.java`

