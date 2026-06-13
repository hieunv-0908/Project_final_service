package re.project_final_service.model.dto.request.application;

import lombok.Data;
import re.project_final_service.model.entity.myEnum.ApplicationStatusEnum;

@Data
public class UpdateApplicationStatusDto {

    private ApplicationStatusEnum status;
}
