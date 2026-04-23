package org.example.common.constant;

public final class Constants {
    private Constants() {
    }

    public static final class Api {
        private Api() {
        }

        public static final String SUCCESS_CODE = "00";
        public static final String FAIL_CODE = "11";
        public static final String SUCCESS_MESSAGE = "SUCCESS";
        public static final String FAIL_MESSAGE = "FAIL";
    }

    public static final class MessageKey {
        private MessageKey() {
        }

        public static final String ERROR_ALREADY_EXISTS = "error.ALREADY_EXIST";
        public static final String ERROR_NOT_FOUND = "error.NOT_FOUND";


        public static final String ENTITY_SETS = "entity.sets";
        public static final String FIELD_ID = "field.id";
        public static final String FIELD_SETS_NAME = "field.sets.name";
        public static final String ENTITY_ITEM = "entity.item";
        public static final String FIELD_ITEM_NAME = "field.item.name";

        public static final String ERROR_INCOMPLETE_DATA = "error.INCOMPLETE_DATA";
        public static final String CHAMP_COST_NOT_NULL = "champ.cost.not_null";
        public static final String CHAMP_COST_MIN = "champ.cost.min";
        public static final String CHAMP_COST_MAX = "champ.cost.max";

        public static final String FIELD_MESSAGE_CONTENT = "field.message.content";
        public static final String CHAT_SELF_NOT_ALLOWED = "chat.self.not.allowed";
        public static final String CHAT_PARTICIPANT_REQUIRED = "chat.participant.required";
        public static final String CHAT_MESSAGE_CONTENT_BLANK = "chat.message.content.blank";
        public static final String INTERNAL_INSTANT_CONVERSION_FAILED = "internal.instant.conversion.failed";

        // entity name
        public static final String ENTITY_CHAMP = "entity.champ";
        public static final String ENTITY_CHAMP_ITEM_RECOMMEND = "entity.champ_item_recommend";
        public static final String ENTITY_TRAIT = "entity.trait";
        public static final String ENTITY_USER = "entity.user";
        public static final String ENTITY_CONVERSATION = "entity.conversation";
        public static final String ENTITY_TEAMS = "entity.teams";


        // field name
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
        public static final String TRAIT_SLUG_EXISTS = "trait.slug.already_exists";
        // Trait Validation & Error
        public static final String TRAIT_SET_ID_NOT_NULL = "trait.setId.not_null";
        public static final String TRAIT_SLUG_NOT_BLANK = "trait.slug.not_blank";
        public static final String TRAIT_SLUG_SIZE = "trait.slug.size";
        public static final String TRAIT_SLUG_PATTERN = "trait.slug.pattern";
        public static final String TRAIT_NAME_NOT_BLANK = "trait.name.not_blank";
        public static final String TRAIT_NAME_SIZE = "trait.name.size";
        public static final String TRAIT_TYPE_NOT_BLANK = "trait.type.not_blank";
        public static final String TRAIT_TYPE_SIZE = "trait.type.size";
        public static final String TRAIT_ICON_URL_SIZE = "trait.iconUrl.size";
        // Trait Breakpoint Validation
        public static final String TRAIT_BREAKPOINT_COUNT_NOT_NULL = "trait.breakpoint.count.not_null";
        public static final String TRAIT_BREAKPOINT_COUNT_MIN = "trait.breakpoint.count.min";
        public static final String TRAIT_BREAKPOINT_COLOR_NOT_BLANK = "trait.breakpoint.color.not_blank";
        public static final String TRAIT_BREAKPOINT_EFFECT_NOT_BLANK = "trait.breakpoint.effect.not_blank";

        //champ
        public static final String CHAMP_SLUG_NOT_FOUND = "champ.slug.not_found";
        public static final String CHAMP_SLUG_EXISTS = "champ.slug.already_exists";
        public static final String CHAMP_SET_ID_NOT_NULL = "champ.setId.not_null";
        public static final String CHAMP_SLUG_NOT_BLANK = "champ.slug.not_blank";
        public static final String CHAMP_SLUG_SIZE = "champ.slug.size";
        public static final String CHAMP_SLUG_PATTERN = "champ.slug.pattern";
        public static final String CHAMP_CODE_NOT_BLANK = "champ.code.not_blank";
        public static final String CHAMP_CODE_SIZE = "champ.code.size";
        public static final String CHAMP_CODE_PATTERN = "champ.code.pattern";
        public static final String CHAMP_CODE_EXISTS = "champ.code.already_exists";
        public static final String CHAMP_NAME_NOT_BLANK = "champ.name.not_blank";
        public static final String CHAMP_NAME_SIZE = "champ.name.size";
        public static final String CHAMP_IMAGE_URL_SIZE = "champ.imageUrl.size";

        //champstats
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


        public static final String DUPLICATE_USERNAME = "error.DUPLICATE_USERNAME";
        public static final String DUPLICATE_EMAIL = "error.DUPLICATE_EMAIL";


        public static final String USER_IMPORT_USERNAME_REQUIRED = "{error.USER_IMPORT_USERNAME_REQUIRED}";
        public static final String USER_IMPORT_USERNAME_SIZE = "{error.USER_IMPORT_USERNAME_SIZE}";
        public static final String USER_IMPORT_EMAIL_REQUIRED = "{error.USER_IMPORT_EMAIL_REQUIRED}";
        public static final String USER_IMPORT_EMAIL_INVALID = "{error.USER_IMPORT_EMAIL_INVALID}";
        public static final String USER_IMPORT_PASSWORD_REQUIRED = "{error.USER_IMPORT_PASSWORD_REQUIRED}";
        public static final String USER_IMPORT_ROLE_ID_REQUIRED = "{error.USER_IMPORT_ROLE_ID_REQUIRED}";
        public static final String USER_IMPORT_ENABLED_REQUIRED = "{error.USER_IMPORT_ENABLED_REQUIRED}";

        public static final String PERMISSION_NOT_FOUND = "{error.PERMISSION_NOT_FOUND}";

        // generic import messages
        public static final String IMPORT_FILE_EMPTY = "import.file.empty";
        public static final String IMPORT_SINGLE_FILE_REQUIRED = "import.single.file.required";
        public static final String IMPORT_FILE_SIZE_EXCEEDED = "import.file.size.exceeded";
        public static final String IMPORT_REQUEST_SIZE_EXCEEDED = "import.request.size.exceeded";
        public static final String IMPORT_MULTIPART_INVALID = "import.multipart.invalid";
        public static final String IMPORT_ROW_LIMIT_EXCEEDED = "import.row.limit.exceeded";
        public static final String IMPORT_UNSUPPORTED_FILE_TYPE = "import.unsupported.file.type";
        public static final String IMPORT_READ_FILE_FAILED = "import.read.file.failed";
        public static final String IMPORT_BUILD_ERROR_FILE_FAILED = "import.build.error.file.failed";
        public static final String IMPORT_MISSING_COLUMNS = "import.missing.columns";
        public static final String IMPORT_NO_COLUMNS_ANNOTATED = "import.no.columns.annotated";
        public static final String IMPORT_DTO_NO_NOARGS = "import.dto.no.noargs";
        public static final String IMPORT_INVALID_VALUE_FORMAT = "import.invalid.value.format";
        public static final String IMPORT_UNSUPPORTED_FIELD_TYPE = "import.unsupported.field.type";
        public static final String IMPORT_INVALID_ENUM_VALUE = "import.invalid.enum.value";
        public static final String IMPORT_INVALID_BOOLEAN_VALUE = "import.invalid.boolean.value";
        public static final String IMPORT_FILE_EXTENSION_MISSING = "import.file.extension.missing";
        public static final String IMPORT_COLUMN_REQUIRED = "import.column.required";
        public static final String IMPORT_COLUMN_INVALID_VALUE = "import.column.invalid.value";
        public static final String IMPORT_PERSIST_GENERIC_ERROR = "import.persist.generic.error";

        // upload messages
        public static final String UPLOAD_FILE_REQUIRED = "upload.file.required";
        public static final String UPLOAD_IMAGE_SIZE_EXCEEDED = "upload.image.size.exceeded";
        public static final String UPLOAD_IMAGE_INVALID_TYPE = "upload.image.invalid.type";
        public static final String UPLOAD_IMAGE_EXTENSION_UNSUPPORTED = "upload.image.extension.unsupported";
        public static final String UPLOAD_ORIGINAL_FILENAME_MISSING = "upload.original.filename.missing";
        public static final String UPLOAD_FILE_EXTENSION_MISSING = "upload.file.extension.missing";
        public static final String UPLOAD_FOLDER_REQUIRED = "upload.folder.required";
        public static final String UPLOAD_INVALID_PATH = "upload.invalid.path";
        public static final String UPLOAD_STORE_FAILED = "upload.store.failed";
        public static final String UPLOAD_URL_REQUIRED = "upload.url.required";
        public static final String UPLOAD_URL_INVALID = "upload.url.invalid";
        public static final String UPLOAD_FILE_NOT_FOUND = "upload.file.not.found";
        public static final String UPLOAD_DELETE_FAILED = "upload.delete.failed";

        //teamcomp
        public static final String ENTITY_TEAMS_STATE_LOCK = "entity.teams.state.lock";
    }
}
