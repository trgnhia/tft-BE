package org.example.dto.set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetResponse {
    private Long id;
    private String name;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdByName;

}
