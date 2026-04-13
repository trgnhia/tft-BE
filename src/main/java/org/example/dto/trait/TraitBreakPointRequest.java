package org.example.dto.trait;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.common.constant.Constants;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraitBreakPointRequest {

    @NotNull(message = "{" + Constants.MessageKey.TRAIT_BREAKPOINT_COUNT_NOT_NULL + "}")
    @Min(value = 1, message = "{" + Constants.MessageKey.TRAIT_BREAKPOINT_COUNT_MIN + "}")
    private Integer count;

    @NotBlank(message = "{" + Constants.MessageKey.TRAIT_BREAKPOINT_COLOR_NOT_BLANK + "}")
    private String color;

    @NotBlank(message = "{" + Constants.MessageKey.TRAIT_BREAKPOINT_EFFECT_NOT_BLANK + "}")
    private String effect;
}