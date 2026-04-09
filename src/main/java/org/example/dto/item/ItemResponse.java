package org.example.dto.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entities.item.ItemEffects;
import org.example.entities.item.ItemStats;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemResponse {
    private Long id;
    private Long setId;
    private String name;
    private String imageUrl;
    private String setName;
    private ItemEffects effects;
    private ItemStats stats;
    private Instant createdAt;
    private Instant updatedAt;
}
