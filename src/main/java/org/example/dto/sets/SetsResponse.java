package org.example.dto.sets;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetsResponse {
    private Long id;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdByName;
    private boolean deleted;
}
