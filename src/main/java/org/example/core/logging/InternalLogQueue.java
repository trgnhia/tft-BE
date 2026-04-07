package org.example.core.logging;
import lombok.Getter;
import org.example.entities.CmsLog;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;

@Component
@Getter
public class InternalLogQueue {
    // Giới hạn 5000 log trên RAM để chống tràn bộ nhớ (OOM)
    private final LinkedBlockingQueue<CmsLog> queue = new LinkedBlockingQueue<>(5000);

    public void push(CmsLog log) {
        this.queue.offer(log);
    }
}