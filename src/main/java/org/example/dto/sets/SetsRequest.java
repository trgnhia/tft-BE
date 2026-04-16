package org.example.dto.sets;

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
public class SetsRequest {
    @NotBlank(message = "{sets.name.not_blank}")
    @Size(max = 100, message = "{sets.name.size}")
    private String name;
    @NotBlank(message = "{sets.description.not_blank}")
    @Size(max = 100, message = "{sets.description.size}")
    private String description;
    private boolean deleted;
}
