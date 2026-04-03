package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.core.logging.InternalLogQueue;
import org.example.entities.CmsLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/spam")
@RequiredArgsConstructor
public class TestCmsLogController {

    private final InternalLogQueue internalLogQueue;

    @GetMapping
    public String spamLog(@RequestParam(defaultValue = "1") int count) {
        for (int i = 0; i < count; i++) {
            CmsLog mockLog = new CmsLog();
            mockLog.setUsername("mock_user_" + i);
            mockLog.setEndpoint("/api/v1/fake-endpoint");
            mockLog.setHttpMethod("POST");
            mockLog.setActionName("MOCK_INSERT_ACTION");
            mockLog.setIpAddress("127.0.0.1");
            mockLog.setRequestBody("{\"test\": \"data " + i + "\"}");
            mockLog.setResultStatus(200);
            mockLog.setStartTime(Instant.now().minusMillis(150));
            mockLog.setEndTime(Instant.now());
            mockLog.setDurationMs(150);
            internalLogQueue.push(mockLog);
        }
        return "Pushed " + count + " log messages to the queue.";

    }


}
