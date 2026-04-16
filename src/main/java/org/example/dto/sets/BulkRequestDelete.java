package org.example.dto.sets;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BulkRequestDelete {
    @NotEmpty(message = "{common.ids.not.empty}")
    private List<Long> ids;
}
