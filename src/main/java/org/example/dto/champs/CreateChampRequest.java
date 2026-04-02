package org.example.dto.champs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChampRequest {

    @NotNull(message = "{champ.setId.not_null}")
    private Long setId;

    @NotBlank(message = "{champ.slug.not_blank}")
    @Size(max = 100, message = "{champ.slug.size}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{champ.slug.pattern}")
    private String slug;

    @NotBlank(message = "{champ.name.not_blank}")
    @Size(max = 255, message = "{champ.name.size}")
    private String name;

    @Size(max = 500, message = "{champ.imageUrl.size}")
    private String imageUrl;

    private String stats;
}
