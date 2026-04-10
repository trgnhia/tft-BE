package org.example.entities.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemStats {
    private Integer attackDamage;
    private Integer abilityPower;
    private Integer armor;
    private Integer magicResist;
    private Integer attackSpeed;
    private Integer health;
}
