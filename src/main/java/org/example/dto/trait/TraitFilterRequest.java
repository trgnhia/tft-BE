package org.example.dto.trait;

import lombok.Data;

import java.util.List;

@Data
public class TraitFilterRequest {
    private String keyword;
    private String type;
    private Long setId;
    private List<Long> setIds;
    private String status;
    private Boolean restorable;
    private Boolean isActive;
    private Boolean includeDeleted;
}
