package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.dto.champs.ChampImportDto;
import org.example.dto.champs.ChampImportTemplateFile;
import org.example.entities.Sets;
import org.example.entities.champ.Champ;
import org.example.entities.champ.ChampStats;
import org.example.entities.champ.ChampTrait;
import org.example.entities.trait.Trait;
import org.example.imports.model.ImportExecutionResult;
import org.example.imports.model.ImportRowOutcome;
import org.example.imports.service.GenericImportService;
import org.example.imports.strategy.ImportFileStrategy;
import org.example.imports.util.ImportHeaderUtils;
import org.example.repositories.ChampRepository;
import org.example.repositories.ChampTraitRepository;
import org.example.repositories.SetsRepository;
import org.example.repositories.TraitRepository;
import org.example.repositories.projection.ChampImportLookup;
import org.example.services.ChampImportService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.example.util.MessageUtils.getMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChampImportServiceImpl implements ChampImportService {

    private static final String TEMPLATE_FILE_NAME_DEFAULT = "champ-import-template";
    private static final String TEMPLATE_SHEET_NAME_DEFAULT = "Import Template";
    private static final String GUIDE_SHEET_NAME_DEFAULT = "Guide";
    private static final String REFERENCE_SHEET_NAME_DEFAULT = "Reference";
    private static final String TEMPLATE_COMMENT_AUTHOR = "TetraTactic CMS";

    private static final int MIN_TEMPLATE_COLUMN_WIDTH = 16 * 256;
    private static final int MAX_TEMPLATE_COLUMN_WIDTH = 40 * 256;
    private static final short TEMPLATE_HEADER_FONT_SIZE = 14;
    private static final short TEMPLATE_HEADER_ROW_HEIGHT = 24;

    private static final Set<String> HIGHLIGHT_REQUIRED_HEADERS = Set.of(
            ChampImportDto.COL_CODE,
            ChampImportDto.COL_NAME
    );

    private static final Map<String, String> TEMPLATE_HEADER_LABEL_KEYS = Map.ofEntries(
            Map.entry(ChampImportDto.COL_SET_CODE, "champ.import.template.columns.set_code"),
            Map.entry(ChampImportDto.COL_CODE, "champ.import.template.columns.code"),
            Map.entry(ChampImportDto.COL_NAME, "champ.import.template.columns.name"),
            Map.entry(ChampImportDto.COL_SLUG, "champ.import.template.columns.slug"),
            Map.entry(ChampImportDto.COL_COST, "champ.import.template.columns.cost"),
            Map.entry(ChampImportDto.COL_HP, "champ.import.template.columns.hp"),
            Map.entry(ChampImportDto.COL_AD, "champ.import.template.columns.ad"),
            Map.entry(ChampImportDto.COL_ARMOR, "champ.import.template.columns.armor"),
            Map.entry(ChampImportDto.COL_RANGE, "champ.import.template.columns.range"),
            Map.entry(ChampImportDto.COL_MAGIC_RESIST, "champ.import.template.columns.magic_resist"),
            Map.entry(ChampImportDto.COL_ATTACK_SPEED, "champ.import.template.columns.attack_speed"),
            Map.entry(ChampImportDto.COL_CRIT_CHANCE, "champ.import.template.columns.crit_chance"),
            Map.entry(ChampImportDto.COL_TRAIT_SLUGS, "champ.import.template.columns.trait_slugs"),
            Map.entry(ChampImportDto.COL_TRAIT_NAMES, "champ.import.template.columns.trait_names"),
            Map.entry(ChampImportDto.COL_IMAGE_URL, "champ.import.template.columns.image_url")
    );

    private static final Map<String, String> TEMPLATE_HEADER_COMMENT_KEYS = Map.of(
            ChampImportDto.COL_SET_CODE, "champ.import.template.comments.set_code",
            ChampImportDto.COL_CODE, "champ.import.template.comments.code",
            ChampImportDto.COL_NAME, "champ.import.template.comments.name"
    );

    private static final List<String> TEMPLATE_HEADERS = List.of(
            ChampImportDto.COL_SET_CODE,
            ChampImportDto.COL_CODE,
            ChampImportDto.COL_NAME,
            ChampImportDto.COL_SLUG,
            ChampImportDto.COL_COST,
            ChampImportDto.COL_HP,
            ChampImportDto.COL_AD,
            ChampImportDto.COL_ARMOR,
            ChampImportDto.COL_RANGE,
            ChampImportDto.COL_MAGIC_RESIST,
            ChampImportDto.COL_ATTACK_SPEED,
            ChampImportDto.COL_CRIT_CHANCE,
            ChampImportDto.COL_TRAIT_SLUGS,
            ChampImportDto.COL_TRAIT_NAMES,
            ChampImportDto.COL_IMAGE_URL
    );

    private static final List<TemplateGuideDefinition> TEMPLATE_GUIDE_DEFINITIONS = List.of(
            new TemplateGuideDefinition(ChampImportDto.COL_SET_CODE, false, "champ.import.template.guide.set_code.format", "Set business code from Reference sheet. If not found, champ is saved without set (warning).", "champ.import.template.guide.set_code.example", "set-10"),
            new TemplateGuideDefinition(ChampImportDto.COL_CODE, true, "champ.import.template.guide.code.format", "Unique champ code (upsert key), letters/numbers/_/-", "champ.import.template.guide.code.example", "ahri_tft14"),
            new TemplateGuideDefinition(ChampImportDto.COL_NAME, true, "champ.import.template.guide.name.format", "Champion display name", "champ.import.template.guide.name.example", "Ahri"),
            new TemplateGuideDefinition(ChampImportDto.COL_SLUG, true, "champ.import.template.guide.slug.format", "Unique slug: lowercase, number, hyphen only", "champ.import.template.guide.slug.example", "ahri"),
            new TemplateGuideDefinition(ChampImportDto.COL_COST, true, "champ.import.template.guide.cost.format", "Integer from 1 to 5", "champ.import.template.guide.cost.example", "2"),
            new TemplateGuideDefinition(ChampImportDto.COL_HP, true, "champ.import.template.guide.hp.format", "HP list separated by | , ;", "champ.import.template.guide.hp.example", "500|900|1620"),
            new TemplateGuideDefinition(ChampImportDto.COL_AD, true, "champ.import.template.guide.ad.format", "AD list (3 values) separated by | , ;", "champ.import.template.guide.ad.example", "40|60|90"),
            new TemplateGuideDefinition(ChampImportDto.COL_ARMOR, true, "champ.import.template.guide.armor.format", "Integer >= 0", "champ.import.template.guide.armor.example", "20"),
            new TemplateGuideDefinition(ChampImportDto.COL_RANGE, true, "champ.import.template.guide.range.format", "Integer >= 1", "champ.import.template.guide.range.example", "4"),
            new TemplateGuideDefinition(ChampImportDto.COL_MAGIC_RESIST, true, "champ.import.template.guide.magic_resist.format", "Integer >= 0", "champ.import.template.guide.magic_resist.example", "20"),
            new TemplateGuideDefinition(ChampImportDto.COL_ATTACK_SPEED, true, "champ.import.template.guide.attack_speed.format", "Decimal >= 0.1", "champ.import.template.guide.attack_speed.example", "0.75"),
            new TemplateGuideDefinition(ChampImportDto.COL_CRIT_CHANCE, true, "champ.import.template.guide.crit_chance.format", "Decimal from 0.0 to 1.0", "champ.import.template.guide.crit_chance.example", "0.25"),
            new TemplateGuideDefinition(ChampImportDto.COL_TRAIT_SLUGS, false, "champ.import.template.guide.trait_slugs.format", "Trait slugs separated by comma/semicolon/pipe", "champ.import.template.guide.trait_slugs.example", "mage,exalted"),
            new TemplateGuideDefinition(ChampImportDto.COL_TRAIT_NAMES, false, "champ.import.template.guide.trait_names.format", "Trait names separated by comma/semicolon/pipe", "champ.import.template.guide.trait_names.example", "Mage, Exalted"),
            new TemplateGuideDefinition(ChampImportDto.COL_IMAGE_URL, false, "champ.import.template.guide.image_url.format", "Public image URL, max 500 chars", "champ.import.template.guide.image_url.example", "https://cdn.example.com/champs/ahri.png")
    );

    private final GenericImportService genericImportService;
    private final ChampRepository champRepository;
    private final ChampTraitRepository champTraitRepository;
    private final SetsRepository setsRepository;
    private final TraitRepository traitRepository;
    private final List<ImportFileStrategy> importFileStrategies;

    @Override
    public ImportExecutionResult importChamps(MultipartFile file) {
        validateTemplateHeaders(file);
        ImportContext context = buildImportContext();

        ImportExecutionResult result = genericImportService.importFileWithOutcome(
                file,
                ChampImportDto.class,
                dto -> persistRow(dto, context)
        );

        return enrichResultWithUpsertSummary(result, context);
    }

    @Override
    public ChampImportTemplateFile downloadTemplate(String format) {
        TemplateFormat templateFormat = TemplateFormat.from(format);
        String templateBaseName = text("champ.import.template.file_name", TEMPLATE_FILE_NAME_DEFAULT);
        try {
            return switch (templateFormat) {
                case CSV -> new ChampImportTemplateFile(
                        buildCsvTemplate(),
                        templateBaseName + ".csv",
                        "text/csv"
                );
                case XLSX -> new ChampImportTemplateFile(
                        buildXlsxTemplate(),
                        templateBaseName + ".xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
            };
        } catch (IOException ex) {
            throw new IllegalStateException(text(
                    "champ.import.template.error.generate_failed",
                    "Unable to generate champ import template file."
            ), ex);
        }
    }

    private ImportExecutionResult enrichResultWithUpsertSummary(ImportExecutionResult result, ImportContext context) {
        String upsertSummary = text(
                "champ.import.summary.upsert",
                "Upsert summary: inserted %d, updated %d.",
                context.insertedCount().get(),
                context.updatedCount().get()
        );
        String baseMessage = result.message();
        String mergedMessage = (baseMessage == null || baseMessage.isBlank())
                ? upsertSummary
                : baseMessage + " " + upsertSummary;

        return new ImportExecutionResult(
                result.totalCount(),
                result.successCount(),
                result.failedCount(),
                result.warningCount(),
                mergedMessage,
                result.rowErrors(),
                result.rowReports(),
                result.errorFileContent(),
                result.errorFileName(),
                result.errorFileContentType()
        );
    }

    private ImportRowOutcome persistRow(ChampImportDto dto, ImportContext context) {
        String normalizedCode = normalize(dto.getCode());
        validateCodeUniquenessInFile(normalizedCode, dto.getCode(), context);

        ResolvedSet resolvedSet = resolveSet(dto.getSetCode(), context);
        List<Long> traitIds = resolveTraitIds(dto, resolvedSet.setId(), context);

        ExistingChampLookup existing = context.existingByCode().get(normalizedCode);
        if (existing != null) {
            Champ champ = champRepository.findById(existing.champId())
                    .orElseThrow(() -> new IllegalArgumentException(text(
                            "champ.import.error.champion_not_found_by_code",
                            "Champion not found by code: %s",
                            dto.getCode()
                    )));

            validateSlugForUpdate(dto.getSlug(), champ.getId());
            applyDtoToChamp(champ, dto, resolvedSet.set());
            Champ saved = champRepository.save(champ);
            syncChampTraits(saved, traitIds);

            context.updatedCount().incrementAndGet();
            context.existingByCode().put(normalizedCode, new ExistingChampLookup(saved.getId(), false));

            return buildOutcome(
                    "SUCCESS_UPDATE",
                    text("champ.import.row.message.updated_successfully", "Updated successfully"),
                    resolvedSet.warningMessage()
            );
        }

        validateSlugForInsert(dto.getSlug());

        Champ champ = new Champ();
        applyDtoToChamp(champ, dto, resolvedSet.set());
        Champ saved = champRepository.save(champ);
        syncChampTraits(saved, traitIds);

        context.insertedCount().incrementAndGet();
        context.existingByCode().put(normalizedCode, new ExistingChampLookup(saved.getId(), false));

        return buildOutcome(
                "SUCCESS_INSERT",
                text("champ.import.row.message.imported_successfully", "Imported successfully"),
                resolvedSet.warningMessage()
        );
    }

    private ImportRowOutcome buildOutcome(String successStatus, String successMessage, String warningMessage) {
        if (!StringUtils.hasText(warningMessage)) {
            return ImportRowOutcome.success(successStatus, successMessage);
        }
        String warningStatus = "SUCCESS_UPDATE".equalsIgnoreCase(successStatus)
                ? "WARNING_UPDATE"
                : "WARNING_INSERT";
        return new ImportRowOutcome(warningStatus, successMessage + ". " + warningMessage, false);
    }

    private void applyDtoToChamp(Champ champ, ChampImportDto dto, Sets targetSet) {
        champ.setCode(trimOrNull(dto.getCode()));
        champ.setName(trimOrNull(dto.getName()));
        champ.setSlug(trimOrNull(dto.getSlug()));
        champ.setCost(dto.getCost());
        champ.setImageUrl(trimOrNull(dto.getImageUrl()));
        champ.setStats(buildStats(dto));
        champ.setSets(targetSet);
        champ.setDeleted(false);
    }

    private ChampStats buildStats(ChampImportDto dto) {
        return ChampStats.builder()
                .hp(parseIntegerList(dto.getHp(), localizeTemplateHeader(ChampImportDto.COL_HP)))
                .ad(parseIntegerList(dto.getAd(), localizeTemplateHeader(ChampImportDto.COL_AD)))
                .armor(dto.getArmor())
                .range(dto.getRange())
                .magicResist(dto.getMagicResist())
                .attackSpeed(dto.getAttackSpeed())
                .critChance(dto.getCritChance())
                .build();
    }

    private void syncChampTraits(Champ champ, List<Long> traitIds) {
        champTraitRepository.deleteByChamp_Id(champ.getId());

        List<Long> normalizedTraitIds = traitIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (normalizedTraitIds.isEmpty()) {
            champ.setChampTraits(new ArrayList<>());
            return;
        }

        List<Trait> traits = traitRepository.findAllById(normalizedTraitIds);
        ensureAllTraitsExist(normalizedTraitIds, traits);

        List<ChampTrait> links = traits.stream()
                .map(trait -> ChampTrait.builder()
                        .champ(champ)
                        .trait(trait)
                        .build())
                .toList();

        List<ChampTrait> savedLinks = champTraitRepository.saveAll(links);
        champ.setChampTraits(new ArrayList<>(savedLinks));
    }

    private void ensureAllTraitsExist(List<Long> requestedTraitIds, List<Trait> traits) {
        Set<Long> foundIds = traits.stream()
                .map(Trait::getId)
                .collect(Collectors.toSet());
        List<Long> missingIds = requestedTraitIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new IllegalArgumentException(text(
                    "champ.import.error.trait_id_not_found",
                    "Trait id not found: %s",
                    missingIds
            ));
        }
    }

    private void validateSlugForInsert(String slug) {
        String normalizedSlug = trimOrNull(slug);
        if (!StringUtils.hasText(normalizedSlug)) {
            throw new IllegalArgumentException(text("champ.import.error.slug_required", "Slug is required"));
        }
        if (champRepository.existsBySlugIncludingDeleted(normalizedSlug)) {
            throw new IllegalArgumentException(text(
                    "champ.import.error.slug_already_exists",
                    "Slug already exists: %s",
                    normalizedSlug
            ));
        }
    }

    private void validateSlugForUpdate(String slug, Long champId) {
        String normalizedSlug = trimOrNull(slug);
        if (!StringUtils.hasText(normalizedSlug)) {
            throw new IllegalArgumentException(text("champ.import.error.slug_required", "Slug is required"));
        }
        if (champRepository.existsBySlugAndIdNotIncludingDeleted(normalizedSlug, champId)) {
            throw new IllegalArgumentException(text(
                    "champ.import.error.slug_already_exists",
                    "Slug already exists: %s",
                    normalizedSlug
            ));
        }
    }

    private void validateTemplateHeaders(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        String extension = extractExtension(file.getOriginalFilename());
        ImportFileStrategy strategy = resolveStrategy(extension);
        List<String> actualHeaders = parseHeaders(file, strategy);

        List<String> expectedNormalized = TEMPLATE_HEADERS.stream()
                .map(this::normalize)
                .toList();
        List<String> actualNormalized = actualHeaders.stream()
                .map(ImportHeaderUtils::normalizeHeader)
                .toList();

        List<String> missing = TEMPLATE_HEADERS.stream()
                .filter(header -> !actualNormalized.contains(normalize(header)))
                .toList();

        List<String> unexpected = actualHeaders.stream()
                .filter(header -> {
                    String normalized = ImportHeaderUtils.normalizeHeader(header);
                    return !expectedNormalized.contains(normalized) && !ImportHeaderUtils.isSystemResultColumn(header);
                })
                .toList();

        if (missing.isEmpty() && unexpected.isEmpty()) {
            return;
        }

        List<String> missingDisplay = missing.stream()
                .map(this::localizeTemplateHeader)
                .toList();
        StringBuilder message = new StringBuilder(text(
                "champ.import.error.invalid_header_format",
                "Invalid header format. Please use the official template."
        ));
        if (!missing.isEmpty()) {
            message.append(" ")
                    .append(text("champ.import.error.missing_columns", "Missing columns: %s.", String.join(", ", missingDisplay)));
        }
        if (!unexpected.isEmpty()) {
            message.append(" ")
                    .append(text("champ.import.error.unexpected_columns", "Unexpected columns: %s.", String.join(", ", unexpected)));
        }
        message.append(" ")
                .append(text(
                        "champ.import.error.expected_columns",
                        "Expected columns: %s.",
                        String.join(", ", localizedTemplateHeaders())
                ));
        throw new IllegalArgumentException(message.toString());
    }

    private List<String> parseHeaders(MultipartFile file, ImportFileStrategy strategy) {
        try {
            return strategy.parse(file.getInputStream()).headers();
        } catch (IOException ex) {
            throw new IllegalArgumentException(text(
                    "champ.import.error.read_headers_failed",
                    "Unable to read import file headers."
            ), ex);
        }
    }

    private ImportContext buildImportContext() {
        List<Sets> sets = setsRepository.findAll();
        List<SetCodeMapping> setCodeMappings = buildSetCodeMappings(sets);

        Map<String, Sets> activeSetsByCode = setCodeMappings.stream()
                .filter(mapping -> !mapping.set().isDeleted())
                .collect(Collectors.toMap(
                        mapping -> normalize(mapping.setCode()),
                        SetCodeMapping::set,
                        (left, right) -> left
                ));

        Set<String> inactiveSetCodes = setCodeMappings.stream()
                .filter(mapping -> mapping.set().isDeleted())
                .map(mapping -> normalize(mapping.setCode()))
                .collect(Collectors.toSet());

        List<Trait> traits = traitRepository.findAll();

        Map<String, Trait> traitBySlug = traits.stream()
                .collect(Collectors.toMap(
                        trait -> normalize(trait.getSlug()),
                        trait -> trait,
                        (left, right) -> left
                ));

        Map<String, List<Trait>> traitsBySetAndName = traits.stream()
                .collect(Collectors.groupingBy(
                        trait -> trait.getSets().getId() + "::" + normalize(trait.getName())
                ));

        Map<String, List<Trait>> traitsByName = traits.stream()
                .collect(Collectors.groupingBy(trait -> normalize(trait.getName())));

        Map<String, ExistingChampLookup> existingByCode = new HashMap<>();
        for (ChampImportLookup lookup : champRepository.findAllCodeLookupIncludingDeleted()) {
            if (!StringUtils.hasText(lookup.getCode()) || lookup.getChampId() == null) {
                continue;
            }
            existingByCode.put(
                    normalize(lookup.getCode()),
                    new ExistingChampLookup(lookup.getChampId(), Boolean.TRUE.equals(lookup.getDeleted()))
            );
        }

        return new ImportContext(
                activeSetsByCode,
                inactiveSetCodes,
                traitBySlug,
                traitsBySetAndName,
                traitsByName,
                existingByCode,
                new HashSet<>(),
                new AtomicInteger(0),
                new AtomicInteger(0)
        );
    }

    private void validateCodeUniquenessInFile(String normalizedCode, String rawCode, ImportContext context) {
        if (normalizedCode.isBlank()) {
            throw new IllegalArgumentException(text("champ.import.error.code_required", "Code is required"));
        }
        if (!context.seenCodesInFile().add(normalizedCode)) {
            throw new IllegalArgumentException(text(
                    "champ.import.error.duplicate_code_in_file",
                    "Duplicate code in import file: %s",
                    rawCode
            ));
        }
    }

    private ResolvedSet resolveSet(String setCode, ImportContext context) {
        String normalizedSetCode = normalize(setCode);
        if (!StringUtils.hasText(normalizedSetCode)) {
            return new ResolvedSet(
                    null,
                    text("champ.import.warning.set_code_empty_saved_without_set", "Set code is empty. Champion is saved without set.")
            );
        }

        Sets activeSet = context.activeSetsByCode().get(normalizedSetCode);
        if (activeSet != null) {
            return new ResolvedSet(activeSet, null);
        }

        if (context.inactiveSetCodes().contains(normalizedSetCode)) {
            return new ResolvedSet(
                    null,
                    text(
                            "champ.import.warning.set_code_inactive_saved_without_set",
                            "Set code '%s' is inactive. Champion is saved without set.",
                            setCode
                    )
            );
        }

        return new ResolvedSet(
                null,
                text(
                        "champ.import.warning.set_code_not_found_saved_without_set",
                        "Set code '%s' does not exist. Champion is saved without set.",
                        setCode
                )
        );
    }

    private List<Long> resolveTraitIds(ChampImportDto dto, Long setId, ImportContext context) {
        LinkedHashSet<Long> traitIds = new LinkedHashSet<>();

        List<String> slugs = splitTokens(dto.getTraitSlugs());
        for (String slug : slugs) {
            Trait trait = context.traitBySlug().get(normalize(slug));
            if (trait == null) {
                throw new IllegalArgumentException(text(
                        "champ.import.error.trait_slug_not_found",
                        "Trait slug not found: %s",
                        slug
                ));
            }
            if (setId != null && !Objects.equals(trait.getSets().getId(), setId)) {
                throw new IllegalArgumentException(text(
                        "champ.import.error.trait_slug_not_in_set",
                        "Trait slug '%s' does not belong to set code %s",
                        slug,
                        dto.getSetCode()
                ));
            }
            traitIds.add(trait.getId());
        }

        List<String> names = splitTokens(dto.getTraitNames());
        for (String name : names) {
            List<Trait> matchedTraits;
            if (setId != null) {
                matchedTraits = context.traitsBySetAndName()
                        .getOrDefault(setId + "::" + normalize(name), List.of());
            } else {
                matchedTraits = context.traitsByName().getOrDefault(normalize(name), List.of());
            }

            if (matchedTraits.isEmpty()) {
                throw new IllegalArgumentException(text(
                        "champ.import.error.trait_name_not_found",
                        "Trait name not found: %s",
                        name
                ));
            }
            if (matchedTraits.size() > 1) {
                throw new IllegalArgumentException(text(
                        "champ.import.error.trait_name_ambiguous_use_slug",
                        "Trait name is ambiguous: %s. Use Trait Slugs instead.",
                        name
                ));
            }

            traitIds.add(matchedTraits.get(0).getId());
        }

        return new ArrayList<>(traitIds);
    }

    private List<Integer> parseIntegerList(String raw, String columnName) {
        List<String> tokens = splitTokens(raw);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException(text(
                    "champ.import.error.column_required",
                    "%s is required",
                    columnName
            ));
        }

        List<Integer> values = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            try {
                values.add(Integer.parseInt(token));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(text(
                        "champ.import.error.column_invalid_number",
                        "%s contains invalid number: %s",
                        columnName,
                        token
                ));
            }
        }

        return values;
    }

    private List<String> splitTokens(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,;|]"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException(text("champ.import.error.file_extension_missing", "File extension is missing."));
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private ImportFileStrategy resolveStrategy(String extension) {
        return importFileStrategies.stream()
                .filter(strategy -> strategy.supports(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(text(
                        "champ.import.error.unsupported_file_type",
                        "Unsupported file type: %s",
                        extension
                )));
    }

    private List<String> localizedTemplateHeaders() {
        return TEMPLATE_HEADERS.stream()
                .map(this::localizeTemplateHeader)
                .toList();
    }

    private String localizeTemplateHeader(String canonicalHeader) {
        String key = TEMPLATE_HEADER_LABEL_KEYS.get(canonicalHeader);
        if (key == null) {
            return canonicalHeader;
        }
        String localizedLabel = text(key, canonicalHeader);
        return ImportHeaderUtils.composeLocalizedHeader(canonicalHeader, localizedLabel);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private byte[] buildCsvTemplate() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            csvPrinter.printRecord(localizedTemplateHeaders());
            csvPrinter.flush();
            return outputStream.toByteArray();
        }
    }

    private byte[] buildXlsxTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeTemplateSheet(workbook);
            writeGuideSheet(workbook);
            writeReferenceSheet(workbook);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void writeTemplateSheet(Workbook workbook) {
        Sheet templateSheet = workbook.createSheet(text("champ.import.template.sheet.template", TEMPLATE_SHEET_NAME_DEFAULT));
        Row headerRow = templateSheet.createRow(0);
        headerRow.setHeightInPoints(TEMPLATE_HEADER_ROW_HEIGHT);

        CellStyle defaultHeaderStyle = createTemplateHeaderStyle(workbook);
        CellStyle requiredHeaderStyle = createRequiredTemplateHeaderStyle(workbook);
        CreationHelper creationHelper = workbook.getCreationHelper();
        Drawing<?> drawing = templateSheet.createDrawingPatriarch();

        for (int col = 0; col < TEMPLATE_HEADERS.size(); col++) {
            String canonicalHeader = TEMPLATE_HEADERS.get(col);
            String localizedHeader = localizeTemplateHeader(canonicalHeader);
            Cell headerCell = headerRow.createCell(col);
            headerCell.setCellValue(localizedHeader);
            headerCell.setCellStyle(HIGHLIGHT_REQUIRED_HEADERS.contains(canonicalHeader) ? requiredHeaderStyle : defaultHeaderStyle);

            addHeaderCommentIfPresent(headerCell, canonicalHeader, drawing, creationHelper);
            applyTemplateColumnWidth(templateSheet, col, localizedHeader);
        }

        templateSheet.createFreezePane(0, 1);
        templateSheet.setAutoFilter(new CellRangeAddress(0, 0, 0, TEMPLATE_HEADERS.size() - 1));
    }

    private CellStyle createTemplateHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(TEMPLATE_HEADER_FONT_SIZE);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        return style;
    }

    private CellStyle createRequiredTemplateHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(createTemplateHeaderStyle(workbook));
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints(TEMPLATE_HEADER_FONT_SIZE);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);

        return style;
    }

    private void applyTemplateColumnWidth(Sheet sheet, int columnIndex, String headerName) {
        sheet.autoSizeColumn(columnIndex);
        int autoWidth = sheet.getColumnWidth(columnIndex);
        int preferredWidth = Math.max(autoWidth + (2 * 256), (headerName.length() + 4) * 256);
        preferredWidth = Math.max(preferredWidth, MIN_TEMPLATE_COLUMN_WIDTH);
        preferredWidth = Math.min(preferredWidth, MAX_TEMPLATE_COLUMN_WIDTH);
        sheet.setColumnWidth(columnIndex, preferredWidth);
    }

    private void addHeaderCommentIfPresent(
            Cell cell,
            String canonicalHeaderName,
            Drawing<?> drawing,
            CreationHelper creationHelper
    ) {
        String commentKey = TEMPLATE_HEADER_COMMENT_KEYS.get(canonicalHeaderName);
        String commentText = commentKey == null ? "" : text(commentKey, "");
        if (!StringUtils.hasText(commentText)) {
            return;
        }

        ClientAnchor anchor = creationHelper.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 3);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + 4);

        Comment comment = drawing.createCellComment(anchor);
        comment.setString(creationHelper.createRichTextString(commentText));
        comment.setAuthor(TEMPLATE_COMMENT_AUTHOR);
        cell.setCellComment(comment);
    }

    private void writeGuideSheet(Workbook workbook) {
        Sheet guideSheet = workbook.createSheet(text("champ.import.template.sheet.guide", GUIDE_SHEET_NAME_DEFAULT));
        Row header = guideSheet.createRow(0);
        header.createCell(0).setCellValue(text("champ.import.template.guide.header.column", "Column"));
        header.createCell(1).setCellValue(text("champ.import.template.guide.header.required", "Required"));
        header.createCell(2).setCellValue(text("champ.import.template.guide.header.format", "Format"));
        header.createCell(3).setCellValue(text("champ.import.template.guide.header.example", "Example"));

        int rowIndex = 1;
        for (TemplateGuideDefinition row : TEMPLATE_GUIDE_DEFINITIONS) {
            Row guideRow = guideSheet.createRow(rowIndex++);
            guideRow.createCell(0).setCellValue(localizeTemplateHeader(row.canonicalColumn()));
            guideRow.createCell(1).setCellValue(row.required()
                    ? text("champ.import.template.guide.required.yes", "Yes")
                    : text("champ.import.template.guide.required.no", "No"));
            guideRow.createCell(2).setCellValue(text(row.formatKey(), row.formatFallback()));
            guideRow.createCell(3).setCellValue(text(row.exampleKey(), row.exampleFallback()));
        }

        Row noteRow = guideSheet.createRow(rowIndex + 1);
        noteRow.createCell(0).setCellValue(text("champ.import.template.guide.note.title", "Note"));
        noteRow.createCell(1).setCellValue(text(
                "champ.import.template.guide.note.reimport_ignore_system_columns",
                "System columns Status/Message are ignored on re-import."
        ));

        guideSheet.setColumnWidth(0, 24 * 256);
        guideSheet.setColumnWidth(1, 12 * 256);
        guideSheet.setColumnWidth(2, 68 * 256);
        guideSheet.setColumnWidth(3, 40 * 256);
    }

    private void writeReferenceSheet(Workbook workbook) {
        Sheet referenceSheet = workbook.createSheet(text("champ.import.template.sheet.reference", REFERENCE_SHEET_NAME_DEFAULT));
        List<Sets> sets = setsRepository.findAll();
        List<SetCodeMapping> setCodeMappings = buildSetCodeMappings(sets);
        Map<Long, String> setCodeById = setCodeMappings.stream()
                .collect(Collectors.toMap(mapping -> mapping.set().getId(), SetCodeMapping::setCode));

        int rowIndex = 0;
        Row setTitle = referenceSheet.createRow(rowIndex++);
        setTitle.createCell(0).setCellValue(text("champ.import.template.reference.sets.title", "Sets"));

        Row setHeader = referenceSheet.createRow(rowIndex++);
        setHeader.createCell(0).setCellValue(text("champ.import.template.reference.sets.columns.set_code", "setCode"));
        setHeader.createCell(1).setCellValue(text("champ.import.template.reference.sets.columns.set_name", "setName"));
        setHeader.createCell(2).setCellValue(text("champ.import.template.reference.sets.columns.status", "status"));

        List<SetCodeMapping> sortedSetMappings = setCodeMappings.stream()
                .sorted(Comparator.comparing(mapping -> mapping.set().getName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
        for (SetCodeMapping mapping : sortedSetMappings) {
            Row row = referenceSheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(mapping.setCode());
            row.createCell(1).setCellValue(mapping.set().getName());
            row.createCell(2).setCellValue(mapping.set().isDeleted()
                    ? text("champ.import.template.reference.sets.status.inactive", "INACTIVE")
                    : text("champ.import.template.reference.sets.status.active", "ACTIVE"));
        }

        rowIndex += 2;

        Row traitTitle = referenceSheet.createRow(rowIndex++);
        traitTitle.createCell(0).setCellValue(text("champ.import.template.reference.traits.title", "Traits"));

        Row traitHeader = referenceSheet.createRow(rowIndex++);
        traitHeader.createCell(0).setCellValue(text("champ.import.template.reference.traits.columns.trait_slug", "traitSlug"));
        traitHeader.createCell(1).setCellValue(text("champ.import.template.reference.traits.columns.trait_name", "traitName"));
        traitHeader.createCell(2).setCellValue(text("champ.import.template.reference.traits.columns.set_code", "setCode"));
        traitHeader.createCell(3).setCellValue(text("champ.import.template.reference.traits.columns.set_name", "setName"));

        List<Trait> traits = traitRepository.findAll().stream()
                .sorted(Comparator.comparing(Trait::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        for (Trait trait : traits) {
            Row row = referenceSheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(trait.getSlug());
            row.createCell(1).setCellValue(trait.getName());
            row.createCell(2).setCellValue(setCodeById.getOrDefault(trait.getSets().getId(), ""));
            row.createCell(3).setCellValue(trait.getSets().getName());
        }

        referenceSheet.setColumnWidth(0, 24 * 256);
        referenceSheet.setColumnWidth(1, 30 * 256);
        referenceSheet.setColumnWidth(2, 24 * 256);
        referenceSheet.setColumnWidth(3, 30 * 256);
    }

    private List<SetCodeMapping> buildSetCodeMappings(List<Sets> sets) {
        Map<String, List<Sets>> grouped = sets.stream()
                .collect(Collectors.groupingBy(set -> deriveSetCode(set.getName())));

        List<SetCodeMapping> mappings = new ArrayList<>();
        for (Map.Entry<String, List<Sets>> entry : grouped.entrySet()) {
            String baseCode = entry.getKey();
            List<Sets> mappedSets = entry.getValue();
            if (mappedSets.size() == 1) {
                mappings.add(new SetCodeMapping(baseCode, mappedSets.get(0)));
                continue;
            }
            for (Sets set : mappedSets) {
                mappings.add(new SetCodeMapping(baseCode + "-" + set.getId(), set));
            }
        }

        return mappings;
    }

    private String deriveSetCode(String setName) {
        if (!StringUtils.hasText(setName)) {
            return "set";
        }

        String ascii = Normalizer.normalize(setName.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        String normalized = ascii
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return StringUtils.hasText(normalized) ? normalized : "set";
    }

    private String text(String key, String fallback, Object... args) {
        String resolved = getMessage(key, args);
        if (resolved == null || resolved.isBlank() || key.equals(resolved)) {
            if (args == null || args.length == 0) {
                return fallback;
            }
            return fallback.formatted(args);
        }
        return resolved;
    }

    private record ImportContext(
            Map<String, Sets> activeSetsByCode,
            Set<String> inactiveSetCodes,
            Map<String, Trait> traitBySlug,
            Map<String, List<Trait>> traitsBySetAndName,
            Map<String, List<Trait>> traitsByName,
            Map<String, ExistingChampLookup> existingByCode,
            Set<String> seenCodesInFile,
            AtomicInteger insertedCount,
            AtomicInteger updatedCount
    ) {
    }

    private record ExistingChampLookup(Long champId, boolean deleted) {
    }

    private record ResolvedSet(Sets set, String warningMessage) {
        Long setId() {
            return set == null ? null : set.getId();
        }
    }

    private record TemplateGuideDefinition(
            String canonicalColumn,
            boolean required,
            String formatKey,
            String formatFallback,
            String exampleKey,
            String exampleFallback
    ) {
    }

    private record SetCodeMapping(String setCode, Sets set) {
    }

    private enum TemplateFormat {
        CSV,
        XLSX;

        static TemplateFormat from(String format) {
            if (format == null || format.isBlank()) {
                return XLSX;
            }
            return switch (format.trim().toLowerCase(Locale.ROOT)) {
                case "csv" -> CSV;
                case "xlsx" -> XLSX;
                default -> {
                    String key = "champ.import.template.error.unsupported_format";
                    String resolved = getMessage(key, format);
                    if (key.equals(resolved)) {
                        resolved = "Unsupported template format: " + format;
                    }
                    throw new IllegalArgumentException(resolved);
                }
            };
        }
    }
}
