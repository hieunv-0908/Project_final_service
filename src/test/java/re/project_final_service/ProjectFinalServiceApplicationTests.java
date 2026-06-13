package re.project_final_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot context initialization test
 * Đảm bảo Spring context được load thành công
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Project Final Service Application Tests")
class ProjectFinalServiceApplicationTests {

    @Test
    @DisplayName("✅ Spring context loads successfully")
    void contextLoads() {
        // Kiểm tra Spring context được load thành công
        // Nếu test này fail, có nghĩa Spring config có vấn đề
    }

}
