# 📖 HƯỚNG DẪN CHI TIẾT CHẠY UNIT TESTS

## I. OVERVIEW - GIỚI THIỆU

Dự án hiện có **12 Unit Tests** được viết sẵn cho:
- **5 Service Tests** (Lớp xử lý logic)
- **4 Controller Tests** (Lớp API endpoints)

```
📂 src/test/java/re/project_final_service/
  ├── service/impl/
  │   ├── UserServiceImplTest.java           (5 tests)
  │   ├── JobPostingServiceImplTest.java     (2 tests)
  │   ├── RefreshTokenServiceTest.java       (5 tests)
  │   ├── PasswordResetServiceImplTest.java  (5 tests)
  │   └── RedisBlacklistServiceTest.java     (5 tests)
  │
  └── controller/
      ├── AuthControllerTest.java            (5 tests)
      ├── AdminControllerTest.java           (6 tests)
      ├── EmployerControllerTest.java        (5 tests)
      └── CandidateControllerTest.java       (6 tests)
```

**Tổng Cộng: 44 Unit Tests** ✅

---

## II. CÁC BƯỚC CHUẨN BỊ (Setup)

### 1. Đảm Bảo Dependencies Đã Có

File `build.gradle` đã chứa tất cả test dependencies cần thiết:

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
testImplementation 'org.springframework.boot:spring-boot-starter-security-test'
testImplementation 'org.springframework.boot:spring-boot-starter-validation-test'
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

### 2. Cài Đặt Test Profile (Optional)

Tạo file: `src/test/resources/application-test.properties`

```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:mysql://localhost:3306/project_final_service_test?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=hieu123456
spring.jpa.show-sql=false
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

## III. CHẠY TESTS

### Cách 1: Chạy TẤT CẢ Tests

```bash
# Sử dụng Gradle Wrapper
./gradlew test

# Hoặc trên Windows
gradlew.bat test
```

**Output Mong Đợi:**
```
BUILD SUCCESSFUL
44 tests passed
```

### Cách 2: Chạy Một Test Class Cụ Thể

```bash
# Chạy tất cả test của UserServiceImpl
./gradlew test --tests UserServiceImplTest

# Chạy tất cả test của AuthController
./gradlew test --tests AuthControllerTest

# Chạy tất cả test của Admin
./gradlew test --tests AdminControllerTest
```

### Cách 3: Chạy Một Test Method Cụ Thể

```bash
# Chạy riêng test "testRegisterSuccess"
./gradlew test --tests UserServiceImplTest.testRegisterSuccess

# Chạy riêng test "testLoginSuccess"
./gradlew test --tests AuthControllerTest.testLoginSuccess
```

### Cách 4: Chạy với IDE (IntelliJ IDEA)

```
1. Mở file test (ví dụ: UserServiceImplTest.java)
2. Click chuột phải vào method test
3. Chọn "Run 'testRegisterSuccess()'" hoặc Ctrl+Shift+F10
4. Xem kết quả trong tab "Run"
```

---

## IV. CẤU TRÚC TRONG SỬ DỤNG @Test

### Pattern Chuẩn: Arrange-Act-Assert (AAA)

```java
@Test
@DisplayName("✅ Mô tả test case")
void testSomething() {
    // 1. ARRANGE - Chuẩn bị dữ liệu & mock
    when(mockRepo.findById(1L)).thenReturn(Optional.of(user));
    
    // 2. ACT - Thực hiện hành động
    User result = userService.register(userDto);
    
    // 3. ASSERT - Kiểm tra kết quả
    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
}
```

---

## V. GIẢI THÍCH CÁC ANNOTATIONS

| Annotation | Ý Nghĩa | Ví Dụ |
|---|---|---|
| `@SpringBootTest` | Load toàn bộ Spring context | Dùng cho integration tests |
| `@AutoConfigureMockMvc` | Cấu hình MockMvc để test HTTP | Test Controllers |
| `@MockBean` | Mock (giả lập) bean Spring | Thay thế các service thực |
| `@BeforeEach` | Chạy trước mỗi test | Setup dữ liệu test |
| `@DisplayName` | Tên descriptive cho test | Hiển thị rõ hơn trong report |
| `@WithMockUser` | Mock authenticated user | Test API cần authentication |
| `@ActiveProfiles("test")` | Sử dụng test profile | Load application-test.properties |

---

## VI. GIẢI THÍCH UNIT TESTS CHÍNH

### A. SERVICE TESTS (UserServiceImplTest)

#### Test 1: `testRegisterSuccess()`
```java
@Test
void testRegisterSuccess() {
    // Mock: userRepo.existsByEmail() trả về false
    // Mock: userRepo.save() trả về saved user
    
    // Act: gọi userService.register()
    User result = userService.register(validUserDto);
    
    // Assert: kiểm tra user được tạo đúng
    assertEquals("testuser@example.com", result.getEmail());
}
```

**Kiểm Tra:**
- ✅ Email không trùng
- ✅ User được lưu vào DB
- ✅ Password được mã hóa

#### Test 2: `testRegisterWithDuplicateEmail()`
```java
@Test
void testRegisterWithDuplicateEmail() {
    // Mock: userRepo.existsByEmail() trả về true
    
    // Act & Assert: Throws RuntimeException
    assertThrows(RuntimeException.class, () -> 
        userService.register(validUserDto)
    );
}
```

**Kiểm Tra:**
- ✅ Khi email trùng, throw exception
- ✅ Không lưu user vào DB (verify never())

#### Test 3: `testChangePasswordSuccess()`
```java
@Test
void testChangePasswordSuccess() {
    // Mock: userRepo.save() trả về updated user
    
    // Act: gọi changePassword()
    User result = userService.changePassword(user, "NewPassword");
    
    // Assert: password được update
    assertNotEquals(oldPasswordHash, result.getPasswordHash());
}
```

---

### B. CONTROLLER TESTS (AuthControllerTest)

#### Test 1: `testRegisterSuccess()`
```java
@Test
void testRegisterSuccess() throws Exception {
    // Mock: userService.register()
    when(userService.register(any())).thenReturn(testUser);
    
    // Act: POST /api/v1/auth/register
    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserDto)))
        
    // Assert: kiểm tra HTTP response
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true));
}
```

**Kiểm Tra:**
- ✅ HTTP Status 201 Created
- ✅ Response JSON structure đúng
- ✅ Success field = true

#### Test 2: `testLoginSuccess()`
```java
@Test
void testLoginSuccess() throws Exception {
    // Mock: JWT token generation
    when(jwtTokenProvider.generateAccessToken(any()))
        .thenReturn("valid-token");
    
    // Act: POST /api/v1/auth/login
    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginDto)))
        
    // Assert
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.accessToken").exists());
}
```

---

### C. ADMIN CONTROLLER TESTS (AdminControllerTest)

#### Test 1: `testListUsersSuccess()`
```java
@Test
@WithMockUser(roles = "ADMIN")  // ✅ Giả lập ADMIN user
void testListUsersSuccess() throws Exception {
    // Mock: userRepo.findAll() trả về page
    Page<User> userPage = new PageImpl<>(
        Arrays.asList(testUser),
        PageRequest.of(0, 10),
        1
    );
    when(userRepo.findAll(any())).thenReturn(userPage);
    
    // Act: GET /api/v1/admin/users
    mockMvc.perform(get("/api/v1/admin/users?page=0&size=10"))
    
    // Assert
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].email").value("user@example.com"));
}
```

**Lưu Ý:**
- `@WithMockUser(roles = "ADMIN")` - Cung cấp authentication
- Không cần login thực sự
- Chỉ test authorization check

---

### D. CANDIDATE CONTROLLER TESTS (CandidateControllerTest)

#### Test 1: `testUploadCvSuccess()`
```java
@Test
@WithMockUser(username = "candidate@example.com", roles = "CANDIDATE")
void testUploadCvSuccess() throws Exception {
    // Arrange: Tạo mock file
    MockMultipartFile file = new MockMultipartFile(
        "file",
        "resume.pdf",
        "application/pdf",
        "PDF content".getBytes()
    );
    
    // Mock: cloudinaryService.uploadPdf()
    when(cloudinaryService.uploadPdf(file))
        .thenReturn("https://cloudinary.com/cv/resume.pdf");
    
    // Act: POST /api/v1/candidate/cv/upload
    mockMvc.perform(multipart("/api/v1/candidate/cv/upload")
        .file(file))
    
    // Assert
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.cvUrl").exists());
}
```

**Lưu Ý:**
- Sử dụng `MockMultipartFile` để test file upload
- Kiểm tra Cloudinary integration được gọi
- Verify cvUrl được lưu trong DB

#### Test 2: `testApplyJobSuccess()`
```java
@Test
void testApplyJobSuccess() throws Exception {
    // Arrange: Setup candidate với CV
    testCandidate.setCvUrl("https://cloudinary.com/cv/resume.pdf");
    
    // Mock: applicationRepo.findByCandidateAndJobPosting() = empty
    when(applicationRepo.findByCandidateAndJobPosting(any(), any()))
        .thenReturn(Optional.empty());
    
    // Act: POST /api/v1/candidate/applications
    mockMvc.perform(post("/api/v1/candidate/applications")
        .contentType(MediaType.APPLICATION_JSON)
        .content(applyJobJson))
    
    // Assert
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("PENDING"));
}
```

---

## VII. MOCKITO - MOCK OBJECTS

### Verify Method Calls

```java
// Kiểm tra method được gọi 1 lần
verify(userRepo, times(1)).save(any(User.class));

// Kiểm tra method KHÔNG bao giờ được gọi
verify(userRepo, never()).delete(any());

// Kiểm tra method được gọi ít nhất 1 lần
verify(userRepo, atLeastOnce()).findById(any());

// Kiểm tra method được gọi đúng 3 lần
verify(userRepo, times(3)).find(any());
```

### When-Then (Mock Return Values)

```java
// Mock trả về Optional
when(userRepo.findByEmail("test@example.com"))
    .thenReturn(Optional.of(testUser));

// Mock throw exception
when(userRepo.findById(999L))
    .thenThrow(new EntityNotFoundException("Not found"));

// Mock void method (chạy bình thường)
doNothing().when(redisBlacklistService)
    .blacklistToken(anyString(), anyLong());
```

---

## VIII. ASSERTIONS - KIỂM TRA KẾT QUẢ

```java
// Kiểm tra không null
assertNotNull(result);

// Kiểm tra bằng nhau
assertEquals("expected", actual);

// Kiểm tra true/false
assertTrue(result.isActive());
assertFalse(result.isDeleted());

// Kiểm tra không bằng nhau
assertNotEquals("old", "new");

// Throw exception
assertThrows(RuntimeException.class, () -> {
    userService.register(invalidDto);
});

// JSON path assertions (MockMvc)
.andExpect(jsonPath("$.success").value(true))
.andExpect(jsonPath("$.data.id").exists())
.andExpect(jsonPath("$.data.email").value("test@example.com"))
```

---

## IX. CHẠY TESTS TỪ COMMAND LINE

### Gradle Commands

```bash
# Chạy tất cả tests
./gradlew test

# Chạy tests với verbose output
./gradlew test --info

# Chạy tests và tạo report
./gradlew test --tests "*Test"

# Xem test report
open build/reports/tests/test/index.html  (Mac/Linux)
start build/reports/tests/test/index.html (Windows)
```

### Maven Commands (Nếu sử dụng Maven)

```bash
# Chạy tất cả tests
mvn test

# Chạy một test class
mvn test -Dtest=UserServiceImplTest

# Chạy một test method
mvn test -Dtest=UserServiceImplTest#testRegisterSuccess
```

---

## X. COVERAGE REPORT - BÁO CÁO PHỦ CODE

### Tạo Coverage Report với JaCoCo

Thêm vào `build.gradle`:

```gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.8"
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
}
```

Chạy:

```bash
./gradlew jacocoTestReport

# Report sẽ ở: build/reports/jacoco/test/index.html
```

---

## XI. DEBUGGING TESTS

### 1. Thêm Print Statements

```java
@Test
void testRegisterSuccess() {
    System.out.println("🔍 Starting test...");
    System.out.println("Email: " + validUserDto.getEmail());
    
    User result = userService.register(validUserDto);
    
    System.out.println("✅ Result: " + result);
    assertNotNull(result);
}
```

Chạy với:
```bash
./gradlew test --tests UserServiceImplTest -i
```

### 2. Sử Dụng Debugger

```
IntelliJ IDEA:
1. Đặt breakpoint (click vào dòng)
2. Click chuột phải → "Debug 'testMethod()'"
3. Sử dụng Step Over (F10), Step Into (F11)
```

### 3. Assert Messages

```java
assertEquals(expected, actual, "Expected [x] but got [y]");
assertTrue(condition, "❌ Condition failed: " + condition);
```

---

## XII. BEST PRACTICES

### ✅ DO - Nên Làm

```java
// 1. Sử dụng descriptive test names
@Test
void testUserRegistrationSuccess() { ... }

// 2. Một test = một behavior
@Test
void testRegisterWithDuplicateEmail() { ... }

// 3. Sử dụng @DisplayName
@Test
@DisplayName("✅ Thành công: Đăng ký người dùng mới")
void testRegisterSuccess() { ... }

// 4. Mock external dependencies
@MockBean
private UserRepo userRepo;

// 5. Verify method calls
verify(userRepo, times(1)).save(any());
```

### ❌ DON'T - Không Nên

```java
// 1. Test cả database
@Test
void testWithRealDatabase() { ... } // ❌

// 2. Multiple assertions xáo trộn
@Test
void testEverything() { // ❌
    // Kiểm tra 10 cái khác nhau
}

// 3. Sử dụng thực tế khi có thể mock
when(realJwtProvider.generateToken())  // ❌

// 4. Hardcode secrets/passwords
"encrypted_password_123"  // ❌

// 5. Test implementation, không test behavior
// Test private methods  // ❌
```

---

## XIII. TROUBLESHOOTING

### ❌ Lỗi: "No qualifying bean of type 'X' found"

**Nguyên Nhân:** Service chưa được mock  
**Giải Pháp:**
```java
@MockBean
private UserRepo userRepo;  // Thêm này
```

### ❌ Lỗi: "NullPointerException"

**Nguyên Nhân:** Mock object chưa được setup  
**Giải Pháp:**
```java
@BeforeEach
void setUp() {
    validUserDto = new UserDto(...);  // Initialize trước
}
```

### ❌ Lỗi: "Status expected <201> but was <400>"

**Nguyên Nhân:** Validation failed  
**Giải Pháp:**
```java
// Kiểm tra request body JSON đúng format
mockMvc.perform(post("/api/v1/auth/register")
    .content(objectMapper.writeValueAsString(validUserDto)));
```

### ❌ Test chạy chậm

**Giải Pháp:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
// Hoặc sử dụng @DataJpaTest cho repo tests
```

---

## XIV. CI/CD INTEGRATION (GitHub Actions)

Tạo file: `.github/workflows/test.yml`

```yaml
name: Run Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 21
        uses: actions/setup-java@v2
        with:
          java-version: '21'
      - name: Run tests
        run: ./gradlew test
      - name: Generate coverage report
        run: ./gradlew jacocoTestReport
```

---

## XV. NEXT STEPS

1. ✅ Chạy `./gradlew test` xem tất cả tests pass
2. ✅ Xem coverage report: `open build/reports/tests/test/index.html`
3. ✅ Thêm tests mới cho các features khác
4. ✅ Setup CI/CD để auto-run tests

---

**Liên Hệ:** Nếu có câu hỏi, hãy xem documentation trong từng file test!

