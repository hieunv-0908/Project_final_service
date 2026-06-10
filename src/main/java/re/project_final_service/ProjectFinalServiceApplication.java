package re.project_final_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ProjectFinalServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectFinalServiceApplication.class, args);
    }

    // PasswordEncoder bean is provided in SecurityConfig to centralize security configuration
}
