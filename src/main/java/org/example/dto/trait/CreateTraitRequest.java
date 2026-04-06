package org.example.dto.trait;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.common.constant.Constants;
import org.example.entities.trait.TraitBreakpoint;

import java.util.List;

@Getter
@Setter

public class CreateTraitRequest {

    @NotNull(message = "{" + Constants.MessageKey.TRAIT_SET_ID_NOT_NULL + "}")
    private Long setId;

    @NotBlank(message = "{" + Constants.MessageKey.TRAIT_SLUG_NOT_BLANK + "}")
    @Size(max = 100, message = "{" + Constants.MessageKey.TRAIT_SLUG_SIZE + "}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{" + Constants.MessageKey.TRAIT_SLUG_PATTERN + "}")
    private String slug;

    @NotBlank(message = "{" + Constants.MessageKey.TRAIT_NAME_NOT_BLANK + "}")
    @Size(max = 100, message = "{" + Constants.MessageKey.TRAIT_NAME_SIZE + "}")
    private String name;

    @NotBlank(message = "{" + Constants.MessageKey.TRAIT_TYPE_NOT_BLANK + "}")
    @Size(max = 50, message = "{" + Constants.MessageKey.TRAIT_TYPE_SIZE + "}")
    private String type;

    @Size(max = 500, message = "{" + Constants.MessageKey.TRAIT_ICON_URL_SIZE + "}")
    private String iconUrl;

    private String description;

    @Valid
    @NotNull(message = "{" + Constants.MessageKey.ERROR_INCOMPLETE_DATA + "}")
    private List<TraitBreakpoint> breakpoints;
}
