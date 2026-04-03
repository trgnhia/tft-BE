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
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdByName;
}
