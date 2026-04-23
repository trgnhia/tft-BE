package org.example.dto.trait;

import lombok.*;
import org.example.entities.trait.TraitBreakpoint;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraitResponse {
    private Long id;
    private Long setId;
    private String setName;
    private Boolean setDeleted;
    private Boolean deleted;
    private String slug;
    private String name;
    private String type;
    private String iconUrl;
    private String description;
    private Boolean canRestore;
    private String restoreBlockedReason;
    private List<TraitBreakpoint> breakpoints;
    private Instant createdAt;
    private Instant updatedAt;
    private Long createdBy;
    private Long updatedBy;
}
