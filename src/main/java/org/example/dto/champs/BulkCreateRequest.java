package org.example.dto.champs;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkCreateRequest {
    @NotEmpty
    private List<CreateChampRequest> champs;
}
