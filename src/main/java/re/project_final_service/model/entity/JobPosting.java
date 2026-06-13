package re.project_final_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;

import static re.project_final_service.model.entity.myEnum.JobStatusEnum.DRAFT;
import static re.project_final_service.model.entity.myEnum.JobStatusEnum.PENDING_APPROVAL;

@Entity
@Table(name = "job_postings")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
// Bảng tin tuyển dụng
public class JobPosting {

    /**
     * Mã định danh duy nhất.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tiêu đề tin.
     */
    private String title;

    /**
     * Mô tả chi tiết.
     */
    private String description;

    /**
     * Khoảng lương.
     */
    private String salaryRange;

    /**
     * Trạng thái tin, ví dụ: đang đăng, đã đóng.
     */
    @Builder.Default
    private JobStatusEnum status = DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;
}
