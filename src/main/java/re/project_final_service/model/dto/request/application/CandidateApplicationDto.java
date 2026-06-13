package re.project_final_service.model.dto.request.application;

import lombok.Builder;
import lombok.Data;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;

@Builder
@Data
public class CandidateApplicationDto {

    private Long applicationId;

    private String jobTitle;

    private ApplicationStatusEnum status;

    private String employerFeedback;
}