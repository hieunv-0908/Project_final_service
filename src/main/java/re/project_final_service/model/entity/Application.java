package re.project_final_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
// Bảng hồ sơ ứng tuyển
public class Application {
    /**
     * id: Long: Định danh duy nhất của hồ sơ.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * coverLetter: String: Nội dung thư ngỏ/thư xin việc.
     */
    private String coverLetter;

    /**
     * cvUrl: String: Đường dẫn (URL) trỏ đến tệp tin CV.
     */
    private String cvUrl;

    /**
     * appliedAt: DateTime: Ngày giờ nộp hồ sơ.
     */
    private LocalDateTime appliedAt;

    /**
     * status: ApplicationStatusEnum: Trạng thái của hồ sơ (sử dụng một kiểu liệt kê - Enum).
     */
    private ApplicationStatusEnum status;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @ManyToOne
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;
}
