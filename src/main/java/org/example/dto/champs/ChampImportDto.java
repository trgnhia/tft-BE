package org.example.dto.champs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.annotations.ImportColumn;
import org.example.common.constant.Constants;

@Getter
@Setter
@NoArgsConstructor
public class ChampImportDto {

    public static final String COL_SET_CODE = "Set Code";
    public static final String COL_CODE = "Code";
    public static final String COL_NAME = "Name";
    public static final String COL_SLUG = "Slug";
    public static final String COL_COST = "Cost";
    public static final String COL_HP = "HP";
    public static final String COL_AD = "AD";
    public static final String COL_ARMOR = "Armor";
    public static final String COL_RANGE = "Range";
    public static final String COL_MAGIC_RESIST = "Magic Resist";
    public static final String COL_ATTACK_SPEED = "Attack Speed";
    public static final String COL_CRIT_CHANCE = "Crit Chance";
    public static final String COL_TRAIT_SLUGS = "Trait Slugs";
    public static final String COL_TRAIT_NAMES = "Trait Names";
    public static final String COL_IMAGE_URL = "Image Url";

    @ImportColumn(name = COL_SET_CODE)
    @Size(max = 100, message = "{champ.import.set_code.size}")
    private String setCode;

    @ImportColumn(name = COL_CODE, required = true)
    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_CODE_NOT_BLANK + "}")
    @Size(max = 100, message = "{" + Constants.MessageKey.CHAMP_CODE_SIZE + "}")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "{" + Constants.MessageKey.CHAMP_CODE_PATTERN + "}")
    private String code;

    @ImportColumn(name = COL_NAME, required = true)
    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_NAME_NOT_BLANK + "}")
    @Size(max = 255, message = "{" + Constants.MessageKey.CHAMP_NAME_SIZE + "}")
    private String name;

    @ImportColumn(name = COL_SLUG, required = true)
    @NotBlank(message = "{" + Constants.MessageKey.CHAMP_SLUG_NOT_BLANK + "}")
    @Size(max = 100, message = "{" + Constants.MessageKey.CHAMP_SLUG_SIZE + "}")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "{" + Constants.MessageKey.CHAMP_SLUG_PATTERN + "}")
    private String slug;

    @ImportColumn(name = COL_COST, required = true)
    @NotNull(message = "{" + Constants.MessageKey.CHAMP_COST_NOT_NULL + "}")
    @Min(value = 1, message = "{" + Constants.MessageKey.CHAMP_COST_MIN + "}")
    @Max(value = 5, message = "{" + Constants.MessageKey.CHAMP_COST_MAX + "}")
    private Integer cost;

    @ImportColumn(name = COL_HP, required = true)
    @NotBlank(message = "{champ.import.hp.required}")
    private String hp;

    @ImportColumn(name = COL_AD, required = true)
    @NotBlank(message = "{champ.import.ad.required}")
    private String ad;

    @ImportColumn(name = COL_ARMOR, required = true)
    @NotNull(message = "{champ.import.armor.required}")
    @Min(value = 0, message = "{" + Constants.MessageKey.CHAMP_STATS_ARMOR_MIN + "}")
    private Integer armor;

    @ImportColumn(name = COL_RANGE, required = true)
    @NotNull(message = "{champ.import.range.required}")
    @Min(value = 1, message = "{" + Constants.MessageKey.CHAMP_STATS_RANGE_MIN + "}")
    private Integer range;

    @ImportColumn(name = COL_MAGIC_RESIST, required = true)
    @NotNull(message = "{champ.import.magic_resist.required}")
    @Min(value = 0, message = "{" + Constants.MessageKey.CHAMP_STATS_MAGIC_RESIST_MIN + "}")
    private Integer magicResist;

    @ImportColumn(name = COL_ATTACK_SPEED, required = true)
    @NotNull(message = "{champ.import.attack_speed.required}")
    @DecimalMin(value = "0.1", message = "{" + Constants.MessageKey.CHAMP_STATS_ATTACK_SPEED_MIN + "}")
    private Double attackSpeed;

    @ImportColumn(name = COL_CRIT_CHANCE, required = true)
    @NotNull(message = "{champ.import.crit_chance.required}")
    @DecimalMin(value = "0.0", message = "{" + Constants.MessageKey.CHAMP_STATS_CRIT_CHANCE_MIN + "}")
    @DecimalMax(value = "1.0", message = "{" + Constants.MessageKey.CHAMP_STATS_CRIT_CHANCE_MAX + "}")
    private Double critChance;

    @ImportColumn(name = COL_TRAIT_SLUGS)
    @Size(max = 1000, message = "{champ.import.trait_slugs.size}")
    private String traitSlugs;

    @ImportColumn(name = COL_TRAIT_NAMES)
    @Size(max = 1000, message = "{champ.import.trait_names.size}")
    private String traitNames;

    @ImportColumn(name = COL_IMAGE_URL)
    @Size(max = 500, message = "{" + Constants.MessageKey.CHAMP_IMAGE_URL_SIZE + "}")
    private String imageUrl;
}
