package org.example.dto.champs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.constant.Constants;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChampRequest {

    @NotNull(message = "{" + Constants.MessageKey.CHAMP_SET_ID_NOT_NULL + "}")
    private Long setId;

    @NotNull(message = "{" + Constants.MessageKey.CHAMP_COST_NOT_NULL + "}")
    @Min(value = 1, message = "{" + Constants.MessageKey.CHAMP_COST_MIN + "}")
    @Max(value = 5, message = "{" + Constants.MessageKey.CHAMP_COST_MAX + "}")
    private Integer cost;

    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_SLUG_NOT_BLANK + "}")
    @Size(max = 100, message = "{" + Constants.MessageKey.CHAMP_SLUG_SIZE + "}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{" + Constants.MessageKey.CHAMP_SLUG_PATTERN + "}")
    @NotNull(message = "{" + Constants.MessageKey.CHAMP_SLUG_NOT_FOUND + "}")
    private String slug;

    @Size(max = 100, message = "{" + Constants.MessageKey.CHAMP_CODE_SIZE + "}")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "{" + Constants.MessageKey.CHAMP_CODE_PATTERN + "}")
    private String code;

    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_NAME_NOT_BLANK + "}")
    @Size(max = 255, message = "{" + Constants.MessageKey.CHAMP_NAME_SIZE + "}")
    private String name;

    @Size(max = 500, message = "{" + Constants.MessageKey.CHAMP_IMAGE_URL_SIZE + "}")
    private String imageUrl;

    @Valid
    @NotNull(message = "{" + Constants.MessageKey.ERROR_INCOMPLETE_DATA + "}")
    private ChampStatsRequest stats;

    private List<Long> traitIds;
}
