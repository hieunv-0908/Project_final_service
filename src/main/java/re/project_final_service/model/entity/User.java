package re.project_final_service.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.project_final_service.model.entity.myEnum.Role;

import java.util.List;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", unique = true, nullable = false)
    private String username;
    @Column(unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String passwordHash;
    private Role role;
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActiveLegacy = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return isActive != null && isActive;
    }

    public void setActive(boolean value) {
        this.isActive = value;
    }

    public void setActive(Boolean value) {
        this.isActive = value;
    }
}
