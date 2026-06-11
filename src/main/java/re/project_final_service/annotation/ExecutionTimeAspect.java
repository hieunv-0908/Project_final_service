package re.project_final_service.annotation;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect để tự động ghi log thời gian thực hiện của tất cả public methods
 * trong controllers (@RestController) và services (@Service)
 *
 * Không cần phải thêm @LogExecutionTime vào từng method - tự động apply cho all
 */
@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    /**
     * Log tất cả public methods trong @RestController
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint);
    }

    /**
     * Log tất cả public methods trong @Service
     */
    @Around("@within(org.springframework.stereotype.Service)")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecution(joinPoint);
    }

    /**
     * Helper method để ghi log thời gian thực hiện
     */
    private Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("START: {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("END: {}.{} - Thoi gian thuc hien: {}ms", className, methodName, executionTime);

            return result;
        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.error("ERROR: {}.{} - Thoi gian thuc hien: {}ms - Exception: {}",
                    className, methodName, executionTime, throwable.getMessage());

            throw throwable;
        }
    }
}
