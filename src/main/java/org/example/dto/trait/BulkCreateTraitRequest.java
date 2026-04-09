package org.example.dto.trait;

import lombok.Data;
import java.util.List;

@Data
public class BulkCreateTraitRequest {
    private List<CreateTraitRequest> traits;
}