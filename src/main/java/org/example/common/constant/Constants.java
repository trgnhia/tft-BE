package org.example.common.constant;

public final class Constants {
    private Constants(){}
    public static final class Api {
        private Api(){}
        public static final String SUCCESS_CODE = "00";
        public static final String FAIL_CODE = "11";
        public static final String SUCCESS_MESSAGE = "SUCCESS";
        public static final String FAIL_MESSAGE = "FAIL";
    }

    public static final class MessageKey {
        public static final String ERROR_INCOMPLETE_DATA = "error.INCOMPLETE_DATA";
        public static final String CHAMP_COST_NOT_NULL = "champ.cost.not_null";
        public static final String CHAMP_COST_MIN = "champ.cost.min";
        public static final String CHAMP_COST_MAX = "champ.cost.max";

        private MessageKey() {}
        public static final String ERROR_NOT_FOUND = "error.NOT_FOUND";
        public static final String ERROR_ALREADY_EXIST = "error.ALREADY_EXIST";


        // entity name
        public static final String ENTITY_SETS = "entity.sets";
        public static final String ENTITY_CHAMP = "entity.champ";
        public static final String ENTITY_TRAIT = "entity.trait";
        public static final String ENTITY_USER = "entity.user";

        // field name
        public static final String FIELD_SETS_NAME = "field.sets.name";
        public static final String FIELD_CHAMP_NAME = "field.champ.name";
        public static final String FIELD_CHAMP_SLUG = "field.champ.slug";
        public static final String FIELD_CHAMP_COST = "field.champ.cost";
        public static final String FIELD_CHAMP_HP = "field.champ.hp";
        public static final String FIELD_CHAMP_AD = "field.champ.ad";
        public static final String FIELD_CHAMP_ARMOR = "field.champ.armor";
        public static final String FIELD_CHAMP_RANGE = "field.champ.range";
        public static final String FIELD_CHAMP_CRIT_CHANCE = "field.champ.crit_chance";
        public static final String FIELD_CHAMP_IMAGE_URL = "field.champ.image_url";
        public static final String FIELD_CHAMP_MAGIC_RESIST = "field.champ.magic_resist";
        public static final String FIELD_CHAMP_ATTACK_SPEED = "field.champ.attack_speed";

        //trait
        public static final String TRAIT_NOT_FOUND = "trait.not_found";

        //champ
        public static final String CHAMP_NOT_FOUND = "champ.not_found";
        public static final String CHAMP_SLUG_NOT_FOUND = "champ.slug.not_found";
        public static final String CHAMP_SLUG_EXISTS = "champ.slug.already_exists";
        public static final String CHAMP_ID_NOT_NULL = "champ.id.not_null";
        public static final String CHAMP_ID_POSITIVE = "champ.id.positive";
        public static final String CHAMP_SET_ID_NOT_NULL = "champ.setId.not_null";
        public static final String CHAMP_SLUG_NOT_BLANK = "champ.slug.not_blank";
        public static final String CHAMP_SLUG_SIZE = "champ.slug.size";
        public static final String CHAMP_SLUG_PATTERN = "champ.slug.pattern";
        public static final String CHAMP_NAME_NOT_BLANK = "champ.name.not_blank";
        public static final String CHAMP_NAME_SIZE = "champ.name.size";
        public static final String CHAMP_IMAGE_URL_SIZE = "champ.imageUrl.size";

        //champstats
        public static final String CHAMP_STATS_COST_MIN = "champ.stats.cost.min";
        public static final String CHAMP_STATS_COST_MAX = "champ.stats.cost.max";
        public static final String CHAMP_STATS_HP_SIZE = "champ.stats.hp.size";
        public static final String CHAMP_STATS_AD_SIZE = "champ.stats.ad.size";
        public static final String CHAMP_STATS_HP_NOT_EMPTY = "champ.stats.hp.not_empty";
        public static final String CHAMP_STATS_HP_MIN = "champ.stats.hp.min";
        public static final String CHAMP_STATS_AD_NOT_EMPTY = "champ.stats.ad.not_empty";
        public static final String CHAMP_STATS_AD_MIN = "champ.stats.ad.min";
        public static final String CHAMP_STATS_ARMOR_MIN = "champ.stats.armor.min";
        public static final String CHAMP_STATS_RANGE_MIN = "champ.stats.range.min";
        public static final String CHAMP_STATS_MAGIC_RESIST_MIN = "champ.stats.magic_resist.min";
        public static final String CHAMP_STATS_ATTACK_SPEED_MIN = "champ.stats.attack_speed.min";
        public static final String CHAMP_STATS_CRIT_CHANCE_MIN = "champ.stats.crit_chance.min";
        public static final String CHAMP_STATS_CRIT_CHANCE_MAX = "champ.stats.crit_chance.max";

    }
}
