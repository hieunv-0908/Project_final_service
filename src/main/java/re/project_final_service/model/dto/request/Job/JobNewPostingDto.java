package re.project_final_service.model.dto.request.Job;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class JobNewPostingDto {
    /**
     * Tiêu đề tin.
     */
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 5, max = 100,
            message = "Tiêu đề phải từ 5 đến 100 ký tự")
    private String title;

    /**
     * Mô tả chi tiết.
     */
    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 20, max = 2000,
            message = "Mô tả phải từ 20 đến 2000 ký tự")
    private String description;

    /**
     * Khoảng lương.
     */
    @NotBlank(message = "Khoảng lương không được để trống")
    @Size(max = 50,
            message = "Khoảng lương tối đa 50 ký tự")
    private String salaryRange;
}
