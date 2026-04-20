package org.example.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.entities.item.ItemEffects;
import org.example.entities.item.ItemStats;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequest {
    @NotNull(message = "{item.setId.not_null}")
    private Long setId;
    @NotBlank(message = "{item.name.not_blank}")
    @Size(max = 100, message = "{item.name.size}")
    private String name;
    @Size(max = 255, message = "{item.imageUrl.size}")
    private String imageUrl;
    private ItemStats stats;
    private ItemEffects effects;
    @NotBlank(message = "{tier.name.not_blank}")
    @Size(max = 10, message = "{tier.name.size}")
    private String tier;
    private boolean deleted;

}
