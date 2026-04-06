package org.example.entities.trait;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraitBreakpoint implements Serializable {
    private Integer count;
    private String color;
    private String effect;
}
