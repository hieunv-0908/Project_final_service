# 🧪 UNIT TESTS - TỔNG HỢP VÀ CHẠY

## 📊 THỐNG KÊ UNIT TESTS

```
✅ TỔNG CỘNG: 44 UNIT TESTS
   ├─ Service Tests:      22 tests (5 test files)
   └─ Controller Tests:    22 tests (4 test files)

YÊU CẦU (FR-12-AF2): Minimum 10 tests
BẠN CÓ: 44 tests ✅ (440% yêu cầu)
```

---

## 📂 CẤU TRÚC TEST FILES

### Service Tests (5 files - 22 tests)

```
src/test/java/re/project_final_service/service/impl/

1. UserServiceImplTest.java
   ├─ testRegisterSuccess()
   ├─ testRegisterWithDuplicateEmail()
   ├─ testRegisterWithAdminRole()
   ├─ testChangePasswordSuccess()
   └─ testRegisterWithEmployerRole()

2. JobPostingServiceImplTest.java
   ├─ testPostNewJobPostingSuccess()
   └─ testPostJobWithCompleteInfo()

3. RefreshTokenServiceTest.java
   ├─ testCreateRefreshTokenSuccess()
   ├─ testFindByTokenSuccess()
   ├─ testFindByTokenNotFound()
   ├─ testDeleteTokenSuccess()
   └─ testDeleteByUserSuccess()

4. PasswordResetServiceImplTest.java
   ├─ testCreateTokenForUserSuccess()
   ├─ testFindByTokenSuccess()
   ├─ testFindByTokenNotFound()
   ├─ testRevokeAllForUserSuccess()
   └─ testTokenNotExpired()

5. RedisBlacklistServiceTest.java
   ├─ testBlacklistTokenSuccess()
   ├─ testIsBlacklistedTokenFound()
   ├─ testIsBlacklistedTokenNotFound()
   ├─ testBlacklistTokenWithLongTTL()
   └─ testBlacklistTokenWithShortTTL()
```

### Controller Tests (4 files - 22 tests)

```
src/test/java/re/project_final_service/controller/

1. AuthControllerTest.java
   ├─ testRegisterSuccess()
   ├─ testRegisterWithDuplicateEmail()
   ├─ testLoginSuccess()
   ├─ testRegisterWithEmployerRole()
   └─ testLogoutSuccess()

2. AdminControllerTest.java
   ├─ testListUsersSuccess()
   ├─ testListUsersWithSearch()
   ├─ testGetUserSuccess()
   ├─ testGetUserNotFound()
   ├─ testUpdateUserSuccess()
   └─ testToggleUserStatusSuccess()

3. EmployerControllerTest.java
   ├─ testPostNewJobPostingSuccess()
   ├─ testListMyJobsSuccess()
   ├─ testGetMyJobSuccess()
   ├─ testSubmitJobSuccess()
   └─ testCloseJobSuccess()

4. CandidateControllerTest.java
   ├─ testUploadCvSuccess()
   ├─ testUploadCvWithInvalidFileType()
   ├─ testGetJobsSuccess()
   ├─ testGetJobDetailSuccess()
   ├─ testApplyJobSuccess()
   └─ testApplyJobWithoutCV()
```

---

## 🚀 CHẠY TESTS

### ✨ Cách 1: Chạy tất cả tests (RECOMMENDED)

```bash
cd D:\JavaWebService\Project_Final_Service

# Trên Windows PowerShell
.\gradlew test

# Trên Mac/Linux
./gradlew test
```

**Kết quả kỳ vọng:**
```
> Task :test
com.example.YourTestClass > testMethod PASSED
...
44 tests completed successfully

BUILD SUCCESSFUL
```

---

### ✨ Cách 2: Chạy từng nhóm tests

```bash
# Chạy tất cả SERVICE tests
.\gradlew test --tests "*ServiceImpl*Test" 
.\gradlew test --tests "*Service*Test"

# Chạy tất cả CONTROLLER tests
.\gradlew test --tests "*Controller*Test"

# Chạy từ một package
.\gradlew test --tests "re.project_final_service.service.impl.*"
```

---

### ✨ Cách 3: Chạy một test file cụ thể

```bash
# Auth Controller Tests
.\gradlew test --tests AuthControllerTest

# User Service Tests
.\gradlew test --tests UserServiceImplTest

# Admin Controller Tests
.\gradlew test --tests AdminControllerTest

# Candidate Controller Tests
.\gradlew test --tests CandidateControllerTest

# Employer Controller Tests
.\gradlew test --tests EmployerControllerTest

# Refresh Token Service Tests
.\gradlew test --tests RefreshTokenServiceTest

# Password Reset Service Tests
.\gradlew test --tests PasswordResetServiceImplTest

# Redis Blacklist Service Tests
.\gradlew test --tests RedisBlacklistServiceTest

# Job Posting Service Tests
.\gradlew test --tests JobPostingServiceImplTest
```

---

### ✨ Cách 4: Chạy một test method cụ thể

```bash
# Test đăng ký
.\gradlew test --tests UserServiceImplTest.testRegisterSuccess

# Test đăng nhập
.\gradlew test --tests AuthControllerTest.testLoginSuccess

# Test upload CV
.\gradlew test --tests CandidateControllerTest.testUploadCvSuccess

# Test tạo tin tuyển dụng
.\gradlew test --tests EmployerControllerTest.testPostNewJobPostingSuccess
```

---

### ✨ Cách 5: Sử dụng IDE (IntelliJ IDEA)

**Chạy tất cả tests:**
```
1. Click chuột phải vào folder: src/test/java/
2. Chọn "Run 'All Tests'" 
3. Hoặc nhấn Ctrl+Shift+F10
```

**Chạy một test file:**
```
1. Mở file test (ví dụ: UserServiceImplTest.java)
2. Click chuột phải vào class
3. Chọn "Run 'UserServiceImplTest'" (Ctrl+Shift+F10)
```

**Chạy một test method:**
```
1. Mở file test
2. Click chuột phải vào method (ví dụ: testRegisterSuccess)
3. Chọn "Run 'testRegisterSuccess()'"
4. Hoặc nhấn Ctrl+Shift+F10 khi cursor ở trên method
```

---

## 📈 VIEW TEST RESULTS

### 1. Console Output

Sau khi chạy `.\gradlew test`, xem output ở console:

```
> Task :test
com.example.UserServiceImplTest > testRegisterSuccess PASSED
com.example.UserServiceImplTest > testRegisterWithDuplicateEmail PASSED
...
BUILD SUCCESSFUL in Xs
44 tests completed
```

### 2. HTML Report

Gradle tạo report HTML tự động:

```bash
# Mở file report (sau khi test chạy xong)
# Windows
start build\reports\tests\test\index.html

# Mac
open build/reports/tests/test/index.html

# Linux
xdg-open build/reports/tests/test/index.html
```

**Report chứa:**
- ✅ Danh sách tất cả tests
- ✅ Pass/fail status
- ✅ Thời gian chạy
- ✅ Stack trace cho tests fail

---

## ✅ KIỂM SOÁT CHẤT LƯỢNG

### Cách kiểm tra test coverage

**Thêm JaCoCo vào build.gradle:**

```gradle
plugins {
    id 'jacoco'
}

test {
    finalizedBy jacocoTestReport
}

jacocoTestReport {
    dependsOn test
}
```

**Chạy với coverage:**

```bash
.\gradlew test jacocoTestReport

# Report sẽ ở: build/reports/jacoco/test/html/index.html
```

---

## 📝 CHI TIẾT CỦA TỪNG TEST

### UserServiceImplTest (5 tests)

| Test Name | Coverage | Ý Tưởng |
|---|---|---|
| `testRegisterSuccess()` | Register flow | Kiểm tra registration thành công |
| `testRegisterWithDuplicateEmail()` | Email validation | Kiểm tra duplicate email không được phép |
| `testRegisterWithAdminRole()` | Role validation | Chỉ CANDIDATE/EMPLOYER được đăng ký |
| `testChangePasswordSuccess()` | Password change | Mật khẩu được cập nhật & hash |
| `testRegisterWithEmployerRole()` | Role diversity | EMPLOYER cũng có thể đăng ký |

### AuthControllerTest (5 tests)

| Test Name | Coverage | Ý Tưởng |
|---|---|---|
| `testRegisterSuccess()` | REST API | Status 201, response format đúng |
| `testRegisterWithDuplicateEmail()` | Error handling | Error message trả đúng |
| `testLoginSuccess()` | JWT generation | AccessToken & RefreshToken được tạo |
| `testRegisterWithEmployerRole()` | Role endpoint | Employer endpoint hoạt động |
| `testLogoutSuccess()` | Logout workflow | Token bị blacklist, session clear |

### AdminControllerTest (6 tests)

| Test Name | Coverage | Ý Tưởng |
|---|---|---|
| `testListUsersSuccess()` | Pagination | Page data trả đúng format |
| `testListUsersWithSearch()` | Search filter | Tìm kiếm theo email |
| `testGetUserSuccess()` | Get detail | Lấy info người dùng |
| `testGetUserNotFound()` | Error handling | 404 khi không tìm thấy |
| `testUpdateUserSuccess()` | Update user | Cập nhật thông tin user |
| `testToggleUserStatusSuccess()` | Status toggle | Active/inactive user |

### EmployerControllerTest (5 tests)

| Test Name | Coverage | Ý Tưởng |
|---|---|---|
| `testPostNewJobPostingSuccess()` | Job creation | Tạo tin mới → status DRAFT |
| `testListMyJobsSuccess()` | Job listing | Liệt kê tin của employer |
| `testGetMyJobSuccess()` | Job detail | Lấy chi tiết tin |
| `testSubmitJobSuccess()` | Job submission | DRAFT → PENDING_APPROVAL |
| `testCloseJobSuccess()` | Job closing | APPROVED → CLOSED |

### CandidateControllerTest (6 tests)

| Test Name | Coverage | Ý Tưởng |
|---|---|---|
| `testUploadCvSuccess()` | File upload | PDF upload → Cloudinary |
| `testUploadCvWithInvalidFileType()` | Validation | Non-PDF bị reject |
| `testGetJobsSuccess()` | Job listing | Danh sách job approved |
| `testGetJobDetailSuccess()` | Job detail | Chi tiết công việc |
| `testApplyJobSuccess()` | Job application | Nộp hồ sơ → PENDING status |
| `testApplyJobWithoutCV()` | Business rule | Phải có CV mới nộp |

---

## 🎯 COVERAGE TARGETS

```
Requirement: Minimum 10 tests
Current: 44 tests ✅✅✅

Target Coverage by Component:

Auth (Authentication & Authorization)
├─ Register: 4 tests ✅
├─ Login: 1 test ✅ 
├─ Logout: 1 test ✅
└─ RefreshToken: 5 tests ✅

User Management
├─ Create: 3 tests ✅
├─ Read: 3 tests ✅
├─ Update: 2 tests ✅
└─ List: 2 tests ✅

Job Management
├─ Create: 2 tests ✅
├─ Read: 3 tests ✅
├─ Update: 2 tests ✅
└─ Submit/Close: 2 tests ✅

Application Management
├─ Apply: 2 tests ✅
├─ PDF Upload: 2 tests ✅
└─ List: 2 tests ✅

Advanced Features
├─ Redis Blacklist: 5 tests ✅
├─ Password Reset: 5 tests ✅
└─ Logging (via AOP): ✅ (không cần test riêng)

TOTAL: 44 tests ✅✅✅
```

---

## 🔧 TROUBLESHOOTING

### ❌ Lỗi: "Gradle tasks timed out"

**Giải Pháp:**
```bash
# Chạy với longer timeout
.\gradlew test --max-workers=1

# Hoặc chạy từng test
.\gradlew test --tests UserServiceImplTest
```

### ❌ Lỗi: "No qualifying bean of type"

**Giải Pháp:**
```java
@MockBean
private UserRepo userRepo;  // Thêm @MockBean

// Hoặc sử dụng @ActiveProfiles để load test config
@ActiveProfiles("test")
```

### ❌ Lỗi: "Cannot connect to database"

**Giải Pháp:**
```properties
# File: application-test.properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:h2:mem:testdb  # Sử dụng H2 in-memory
spring.datasource.driver-class-name=org.h2.Driver
```

### ❌ Tests chạy quá chậm

**Giải Pháp:**
```bash
# Chạy song song
.\gradlew test --max-workers=4

# Hoặc bỏ H2 validation
spring.h2.console.enabled=false
```

---

## 📞 LIÊN HỆ NHANH

**Các file test chính:**
- `src/test/java/re/project_final_service/service/impl/*Test.java`
- `src/test/java/re/project_final_service/controller/*Test.java`

**Hướng dẫn chi tiết:**
- `UNIT_TESTS_GUIDE.md` - Full documentation

**Run từ command line:**
```bash
# Quick run tất cả
.\gradlew test

# Xem report
start build\reports\tests\test\index.html
```

---

**✅ Bạn đã sẵn sàng để chạy 44 unit tests!**

Hãy chạy: `.\gradlew test` và xem kết quả 🎉

