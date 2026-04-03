package org.example.dto.champs;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.constant.Constants;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateChampRequest {

    @NotNull(message = "{" + Constants.MessageKey.CHAMP_SET_ID_NOT_NULL + "}")
    private Long setId;

    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_SLUG_NOT_BLANK + "}")
    @Size(max = 100, message = "{" + Constants.MessageKey.CHAMP_SLUG_SIZE + "}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{" + Constants.MessageKey.CHAMP_SLUG_PATTERN + "}")
    private String slug;

    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_NAME_NOT_BLANK + "}")
    @Size(max = 255, message = "{" + Constants.MessageKey.CHAMP_NAME_SIZE + "}")
    private String name;

    @Size(max = 500, message = "{" + Constants.MessageKey.CHAMP_IMAGE_URL_SIZE + "}")
    private String imageUrl;

    @Valid
    @NotNull(message = "{" + Constants.MessageKey.ERROR_INCOMPLETE_DATA + "}")
    private ChampStatsRequest stats;
}
