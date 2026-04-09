package org.example.core.logging;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.example.entities.CmsLog;
import org.springframework.stereotype.Component;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
@Getter
public class InternalLogQueue {
    // Queue chính cho log mới (RAM)
    private final LinkedBlockingQueue<CmsLog> queue = new LinkedBlockingQueue<>(5000);
    // Queue dự phòng cho các ca khó (RAM)
    private final LinkedBlockingQueue<RetryCmslog> retryQueue = new LinkedBlockingQueue<>(1000);

    public void push(CmsLog logItem) {
        if (!this.queue.offer(logItem)) {
            log.warn("Main Queue đầy, chuyển log API [{}] sang hàng chờ Retry.", logItem.getEndpoint());
            pushToRetry(new RetryCmslog(logItem, 0));
        }
    }

    public void pushToRetry(RetryCmslog retryItem) {
        // Nếu đã thử lại quá 2 lần hoặc gặp lỗi dữ liệu vĩnh viễn (đánh dấu 99)
        if (retryItem.getRetryCount() >= 2) {
            outputToTerminal(retryItem.getCmsLog(), "Quá số lần thử hoặc lỗi dữ liệu vĩnh viễn");
            return;
        }
        retryItem.setRetryCount(retryItem.getRetryCount() + 1);
        if (!this.retryQueue.offer(retryItem)) {
            log.error(" Retry Queue cũng đã đầy! Buộc phải hủy bỏ log.");
            outputToTerminal(retryItem.getCmsLog(), "Retry Queue đầy");
        }
    }

    private void outputToTerminal(CmsLog deadLog, String reason) {
        log.error(" [DEAD LETTER] - Lý do: {}\n" +
                        "Action: [{}] | User: [{}] | API: [{}] | Status: [{}]\n" +
                        "Body: {}\n" +
                        "Lỗi chi tiết: {}",
                reason, deadLog.getActionName(), deadLog.getUsername(),
                deadLog.getEndpoint(), deadLog.getResultStatus(),
                deadLog.getRequestBody(), deadLog.getErrorMessage());
    }
}