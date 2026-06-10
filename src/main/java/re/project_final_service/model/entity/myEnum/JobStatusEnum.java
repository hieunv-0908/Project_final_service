package re.project_final_service.model.entity.myEnum;

public enum JobStatusEnum {
        /** Bản nháp (chưa xuất bản).*/
        DRAFT,
        /** Đang chờ phê duyệt.*/
        PENDING_APPROVAL,
        /** Đã được phê duyệt/Đang hiển thị.*/
        APPROVED,
        /** Bị từ chối phê duyệt.*/
        REJECTED,
        /** Đã đóng (hết hạn hoặc đã tuyển đủ).*/
        CLOSED
}
