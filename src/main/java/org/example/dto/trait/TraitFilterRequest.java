package org.example.dto.trait;

import lombok.Data;

@Data
public class TraitFilterRequest {
    private String keyword;
    private String type;
    private Long setId;
    private Boolean isActive;
    private Boolean includeDeleted;
}
