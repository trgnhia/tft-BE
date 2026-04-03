package org.example.core.logging; // Đổi lại theo đường dẫn bạn chọn

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.logging.InternalLogQueue;
import org.example.entities.CmsLog;
import org.example.repositories.CmsLogRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CmsLogWorker {

    private final InternalLogQueue logQueue;
    private final CmsLogRepository cmsLogRepository;

    // Chạy ngầm mỗi 5 giây
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processLogs() {
        if (logQueue.getQueue().isEmpty()) {
            return;
        }

        List<CmsLog> batchLogs = new ArrayList<>();

        logQueue.getQueue().drainTo(batchLogs, 100);

        if (!batchLogs.isEmpty()) {
            try {
                cmsLogRepository.saveAll(batchLogs);
                log.info("Batch Insert thành công {} bản ghi CMS Log.", batchLogs.size());
            } catch (Exception e) {
                log.error("Lỗi khi lưu batch CMS Log xuống DB: ", e);


            }
        }
    }
}