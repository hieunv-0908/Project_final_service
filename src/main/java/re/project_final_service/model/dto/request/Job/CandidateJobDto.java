package re.project_final_service.model.dto.request.Job;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CandidateJobDto {

    private Long id;

    private String title;

    private String description;

    private String salaryRange;

    private String companyName;
}