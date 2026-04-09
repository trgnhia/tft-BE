package org.example.core.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.CmsLog;
import org.example.repositories.CmsLogRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmsLogWorker {

    private final InternalLogQueue logQueue;
    private final CmsLogRepository cmsLogRepository;

    @Scheduled(fixedDelay = 5000)
    public void processLogs() {
        if (logQueue.getQueue().isEmpty()) return;

        List<CmsLog> batchLogs = new ArrayList<>();
        logQueue.getQueue().drainTo(batchLogs, 100);

        try {
            cmsLogRepository.saveAll(batchLogs);
            log.info("Batch Insert thành công {} log.", batchLogs.size());
        } catch (DataIntegrityViolationException e) {
            log.error("Lỗi dữ liệu (Tràn cột/NULL). Không thể cứu.");
            batchLogs.forEach(failedLog -> {
                failedLog.setErrorMessage("Data Error: " + e.getMessage());
                logQueue.pushToRetry(new RetryCmslog(failedLog, 2)); // Đánh dấu 2 để in Terminal ngay
            });
        } catch (TransientDataAccessException e) {
            log.warn(" Lỗi mạng/DB tạm thời. Đưa vào phòng hồi sức.");
            batchLogs.forEach(failedLog -> {
                failedLog.setErrorMessage("Transient Error: " + e.getMessage());
                logQueue.pushToRetry(new RetryCmslog(failedLog, 0));
            });
        } catch (Exception e) {
            log.error(" Lỗi không xác định: {}", e.getMessage());
            batchLogs.forEach(failedLog -> logQueue.pushToRetry(new RetryCmslog(failedLog, 0)));
        }
    }

    @Scheduled(fixedDelay = 15000)
    public void processRetryLogs() {
        if (logQueue.getRetryQueue().isEmpty()) return;

        List<RetryCmslog> retryBatch = new ArrayList<>();
        logQueue.getRetryQueue().drainTo(retryBatch, 50);

        List<CmsLog> logsToSave = retryBatch.stream().map(RetryCmslog::getCmsLog).toList();
        try {
            cmsLogRepository.saveAll(logsToSave);
            log.info(" Cứu sống thành công {} log từ Retry Queue.", logsToSave.size());
        } catch (Exception e) {
            retryBatch.forEach(item -> {
                item.getCmsLog().setErrorMessage("Retry Error: " + e.getMessage());
                logQueue.pushToRetry(item);
            });
        }
    }
}