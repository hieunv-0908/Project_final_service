package re.project_final_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_blacklist")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TokenBlacklist {

    /**
     * Khuôn mẫu dữ liệu cho bản ghi trong bảng danh sách đen token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Chuỗi JWT thực tế cần bị vô hiệu hóa.
     */
    @Column(name = "token_string", nullable = false)
    private String tokenString;

    /**
     * Thời điểm mà token đó bị đưa vào danh sách đen.
     */
    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
