package re.project_final_service.model.entity.myEnum;

public enum ApplicationStatusEnum {
    /** Đang chờ xử lý.*/
    PENDING,
    /** Đang trong quá trình xem xét hồ sơ.*/
    REVIEWING,
    /** Đang trong giai đoạn phỏng vấn.*/
    INTERVIEWING,
    /** Đã trúng tuyển.*/
    ACCEPTED,
    /** Đã bị loại.*/
    REJECTED,
}
