package org.example.dto.set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SetRequest {
    private Boolean isActive;
    @NotBlank(message = "{set.name.not_blank}")
    @Size(max = 100, message = "{set.name.size}")
    private String name;
}
