package org.example.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.common.entity.BaseEntity;

import java.time.Instant;

@Entity
@Table(name = "cms_logs")
@Getter
@Setter
public class CmsLog extends BaseEntity {

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "endpoint", length = 255, nullable = false)
    private String endpoint;

    @Column(name = "http_method", length = 20, nullable = false)
    private String httpMethod;

    @Column(name = "action_name", length = 100)
    private String actionName;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "result_status", nullable = false)
    private Integer resultStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "duration_ms")
    private Integer durationMs;
}