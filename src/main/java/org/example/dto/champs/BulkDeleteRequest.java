package org.example.dto.champs;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkDeleteRequest {
    @NotEmpty
    private List<Long> ids;
}
