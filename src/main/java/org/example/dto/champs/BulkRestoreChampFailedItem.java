package org.example.dto.champs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRestoreChampFailedItem {
    private Long champId;
    private String reason;
}
