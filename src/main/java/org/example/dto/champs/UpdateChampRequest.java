package org.example.dto.champs;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChampRequest {
    @Size(max = 100, message = "{champ.slug.size}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{champ.slug.pattern}")
    private String slug;

    @Size(max = 255, message = "{champ.name.size}")
    private String name;

    @Size(max = 500, message = "{champ.imageUrl.size}")
    private String imageUrl;

    private String stats;
}
