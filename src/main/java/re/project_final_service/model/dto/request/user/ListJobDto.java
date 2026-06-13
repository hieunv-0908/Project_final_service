package re.project_final_service.model.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.project_final_service.model.entity.myEnum.JobStatusEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListJobDto {

    private Long id;

    private String title;

    private String salaryRange;

    private JobStatusEnum status;

    private Long employerId;

    private String employerName;

    private String employerEmail;
}
