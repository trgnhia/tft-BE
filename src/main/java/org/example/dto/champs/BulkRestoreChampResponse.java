package org.example.dto.champs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkRestoreChampResponse {
    @Builder.Default
    private List<Long> restored = new ArrayList<>();

    @Builder.Default
    private List<BulkRestoreChampFailedItem> failed = new ArrayList<>();
}
