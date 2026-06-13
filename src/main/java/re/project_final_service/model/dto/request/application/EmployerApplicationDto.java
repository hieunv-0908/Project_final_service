package re.project_final_service.model.dto.request.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployerApplicationDto {

    private Long applicationId;

    private String candidateName;

    private String candidateEmail;

    private String cvUrl;

    private String coverLetter;

    private ApplicationStatusEnum status;
}