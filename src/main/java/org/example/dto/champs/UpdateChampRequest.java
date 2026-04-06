package org.example.dto.champs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.constant.Constants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChampRequest {
    @Size(max = 100, message = "{" + Constants.MessageKey.CHAMP_SLUG_SIZE + "}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{" + Constants.MessageKey.CHAMP_SLUG_PATTERN + "}")
    private String slug;

    @Size(max = 255, message = "{" + Constants.MessageKey.CHAMP_NAME_SIZE + "}")
    private String name;

    @Size(max = 500, message = "{" + Constants.MessageKey.CHAMP_IMAGE_URL_SIZE + "}")
    private String imageUrl;

    @Min(value = 1, message = "{" + Constants.MessageKey.CHAMP_COST_MIN + "}")
    @Max(value = 5, message = "{" + Constants.MessageKey.CHAMP_COST_MAX + "}")
    private Integer cost;

    @Valid
    private ChampStatsRequest stats;
}
