package re.project_final_service.model.dto.request.Job;

import lombok.Data;

@Data
public class UpdateJobDto {

    private String title;

    private String description;

    private String salaryRange;
}
