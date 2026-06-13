package re.project_final_service.model.dto.request.application;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;

@Data
public class UpdateApplicationResultDto {

    @NotNull
    private ApplicationStatusEnum status;

    private String feedback;
}