package org.example.services.implement;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.dto.champs.*;
import org.example.entities.Sets;
import org.example.entities.trait.Trait;
import org.example.imports.model.ImportExecutionResult;
import org.example.imports.service.GenericImportService;
import org.example.imports.strategy.ImportFileStrategy;
import org.example.repositories.ChampRepository;
import org.example.repositories.SetsRepository;
import org.example.repositories.TraitRepository;
import org.example.repositories.projection.ChampImportLookup;
import org.example.services.ChampImportService;
import org.example.services.ChampService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChampImportServiceImpl implements ChampImportService {

    private static final String TEMPLATE_FILE_NAME = "champ-import-template";
    private static final String TEMPLATE_SHEET_NAME = "Template";
    private static final String GUIDE_SHEET_NAME = "Guide";

    private static final List<String> TEMPLATE_HEADERS = List.of(
            ChampImportDto.COL_SET_ID,
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

    private static final List<TemplateGuideRow> TEMPLATE_GUIDE_ROWS = List.of(
            new TemplateGuideRow(ChampImportDto.COL_SET_ID, "Yes", "Numeric set id, must exist and active", "1"),
            new TemplateGuideRow(ChampImportDto.COL_CODE, "Yes", "Unique champ code (upsert key), letters/numbers/_/-", "ahri_tft14"),
            new TemplateGuideRow(ChampImportDto.COL_NAME, "Yes", "Champion display name", "Ahri"),
            new TemplateGuideRow(ChampImportDto.COL_SLUG, "Yes", "Unique slug: lowercase, number, hyphen only", "ahri"),
            new TemplateGuideRow(ChampImportDto.COL_COST, "Yes", "Integer from 1 to 5", "2"),
            new TemplateGuideRow(ChampImportDto.COL_HP, "Yes", "HP list separated by | , ;", "500|900|1620"),
            new TemplateGuideRow(ChampImportDto.COL_AD, "Yes", "AD list (3 values) separated by | , ;", "40|60|90"),
            new TemplateGuideRow(ChampImportDto.COL_ARMOR, "Yes", "Integer >= 0", "20"),
            new TemplateGuideRow(ChampImportDto.COL_RANGE, "Yes", "Integer >= 1", "4"),
            new TemplateGuideRow(ChampImportDto.COL_MAGIC_RESIST, "Yes", "Integer >= 0", "20"),
            new TemplateGuideRow(ChampImportDto.COL_ATTACK_SPEED, "Yes", "Decimal >= 0.1", "0.75"),
            new TemplateGuideRow(ChampImportDto.COL_CRIT_CHANCE, "Yes", "Decimal from 0.0 to 1.0", "0.25"),
            new TemplateGuideRow(ChampImportDto.COL_TRAIT_SLUGS, "No", "Trait slugs separated by comma/semicolon/pipe", "mage,exalted"),
            new TemplateGuideRow(ChampImportDto.COL_TRAIT_NAMES, "No", "Trait names separated by comma/semicolon/pipe", "Mage, Exalted"),
            new TemplateGuideRow(ChampImportDto.COL_IMAGE_URL, "No", "Public image URL, max 500 chars", "https://cdn.example.com/champs/ahri.png")
    );

    private final GenericImportService genericImportService;
    private final ChampService champService;
    private final ChampRepository champRepository;
    private final SetsRepository setsRepository;
    private final TraitRepository traitRepository;
    private final Validator validator;
    private final List<ImportFileStrategy> importFileStrategies;

    @Override
    public ImportExecutionResult importChamps(MultipartFile file) {
        validateTemplateHeaders(file);
        ImportContext context = buildImportContext();

        ImportExecutionResult result = genericImportService.importFile(
                file,
                ChampImportDto.class,
                dto -> persistRow(dto, context)
        );

        return enrichResultWithUpsertSummary(result, context);
    }

    @Override
    public ChampImportTemplateFile downloadTemplate(String format) {
        TemplateFormat templateFormat = TemplateFormat.from(format);
        try {
            return switch (templateFormat) {
                case CSV -> new ChampImportTemplateFile(
                        buildCsvTemplate(),
                        TEMPLATE_FILE_NAME + ".csv",
                        "text/csv"
                );
                case XLSX -> new ChampImportTemplateFile(
                        buildXlsxTemplate(),
                        TEMPLATE_FILE_NAME + ".xlsx",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                );
            };
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to generate champ import template file.", ex);
        }
    }

    private ImportExecutionResult enrichResultWithUpsertSummary(ImportExecutionResult result, ImportContext context) {
        String upsertSummary = "Upsert summary: inserted %d, updated %d."
                .formatted(context.insertedCount().get(), context.updatedCount().get());
        String baseMessage = result.message();
        String mergedMessage = (baseMessage == null || baseMessage.isBlank())
                ? upsertSummary
                : baseMessage + " " + upsertSummary;

        return new ImportExecutionResult(
                result.totalCount(),
                result.successCount(),
                result.failedCount(),
                mergedMessage,
                result.rowErrors(),
                result.errorFileContent(),
                result.errorFileName(),
                result.errorFileContentType()
        );
    }

    private void persistRow(ChampImportDto dto, ImportContext context) {
        String normalizedCode = normalize(dto.getCode());
        validateCodeUniquenessInFile(normalizedCode, dto.getCode(), context);

        Sets targetSet = resolveSet(dto.getSetId(), context);
        List<Long> traitIds = resolveTraitIds(dto, targetSet.getId(), context);

        ExistingChampLookup existing = context.existingByCode().get(normalizedCode);
        if (existing != null) {
            UpdateChampRequest request = buildUpdateRequest(dto, targetSet.getId(), traitIds);
            validateUpdateRequest(request);

            champService.updateForImport(existing.champId(), request);
            context.updatedCount().incrementAndGet();
            context.existingByCode().put(normalizedCode, new ExistingChampLookup(existing.champId(), false));
            return;
        }

        CreateChampRequest request = buildCreateRequest(dto, targetSet.getId(), traitIds);
        validateCreateRequest(request);

        ChampResponse created = champService.create(request);
        context.insertedCount().incrementAndGet();
        context.existingByCode().put(normalizedCode, new ExistingChampLookup(created.getId(), false));
    }

    private void validateTemplateHeaders(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        String extension = extractExtension(file.getOriginalFilename());
        ImportFileStrategy strategy = resolveStrategy(extension);
        List<String> actualHeaders = parseHeaders(file, strategy);

        List<String> expectedNormalized = TEMPLATE_HEADERS.stream().map(this::normalize).toList();
        List<String> actualNormalized = actualHeaders.stream().map(this::normalize).toList();

        if (actualNormalized.equals(expectedNormalized)) {
            return;
        }

        List<String> missing = TEMPLATE_HEADERS.stream()
                .filter(header -> !actualNormalized.contains(normalize(header)))
                .toList();
        List<String> unexpected = actualHeaders.stream()
                .filter(header -> !expectedNormalized.contains(normalize(header)))
                .toList();

        StringBuilder message = new StringBuilder("Invalid header format. Please use the official template.");
        if (!missing.isEmpty()) {
            message.append(" Missing columns: ").append(String.join(", ", missing)).append(".");
        }
        if (!unexpected.isEmpty()) {
            message.append(" Unexpected columns: ").append(String.join(", ", unexpected)).append(".");
        }
        message.append(" Expected columns: ").append(String.join(", ", TEMPLATE_HEADERS)).append(".");

        throw new IllegalArgumentException(message.toString());
    }

    private List<String> parseHeaders(MultipartFile file, ImportFileStrategy strategy) {
        try {
            return strategy.parse(file.getInputStream()).headers();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read import file headers.", ex);
        }
    }

    private ImportContext buildImportContext() {
        List<Sets> sets = setsRepository.findAll();
        Map<Long, Sets> activeSetsById = sets.stream()
                .filter(set -> !set.isDeleted())
                .collect(Collectors.toMap(Sets::getId, set -> set));
        Set<Long> deletedSetIds = sets.stream()
                .filter(Sets::isDeleted)
                .map(Sets::getId)
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
                activeSetsById,
                deletedSetIds,
                traitBySlug,
                traitsBySetAndName,
                existingByCode,
                new HashSet<>(),
                new AtomicInteger(0),
                new AtomicInteger(0)
        );
    }

    private void validateCodeUniquenessInFile(String normalizedCode, String rawCode, ImportContext context) {
        if (normalizedCode.isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }
        if (!context.seenCodesInFile().add(normalizedCode)) {
            throw new IllegalArgumentException("Duplicate code in import file: " + rawCode);
        }
    }

    private Sets resolveSet(Long setId, ImportContext context) {
        Sets set = context.activeSetsById().get(setId);
        if (set != null) {
            return set;
        }
        if (context.deletedSetIds().contains(setId)) {
            throw new IllegalArgumentException("Set is inactive: " + setId);
        }
        throw new IllegalArgumentException("Set not found: " + setId);
    }

    private List<Long> resolveTraitIds(ChampImportDto dto, Long setId, ImportContext context) {
        LinkedHashSet<Long> traitIds = new LinkedHashSet<>();

        List<String> slugs = splitTokens(dto.getTraitSlugs());
        for (String slug : slugs) {
            Trait trait = context.traitBySlug().get(normalize(slug));
            if (trait == null) {
                throw new IllegalArgumentException("Trait slug not found: " + slug);
            }
            if (!Objects.equals(trait.getSets().getId(), setId)) {
                throw new IllegalArgumentException("Trait slug '" + slug + "' does not belong to setId " + setId);
            }
            traitIds.add(trait.getId());
        }

        List<String> names = splitTokens(dto.getTraitNames());
        for (String name : names) {
            List<Trait> matchedTraits = context.traitsBySetAndName()
                    .getOrDefault(setId + "::" + normalize(name), List.of());

            if (matchedTraits.isEmpty()) {
                throw new IllegalArgumentException("Trait name not found in set " + setId + ": " + name);
            }
            if (matchedTraits.size() > 1) {
                throw new IllegalArgumentException("Trait name is ambiguous in set " + setId + ": " + name);
            }
            traitIds.add(matchedTraits.get(0).getId());
        }

        return new ArrayList<>(traitIds);
    }

    private CreateChampRequest buildCreateRequest(ChampImportDto dto, Long setId, List<Long> traitIds) {
        ChampStatsRequest stats = ChampStatsRequest.builder()
                .hp(parseIntegerList(dto.getHp(), ChampImportDto.COL_HP))
                .ad(parseIntegerList(dto.getAd(), ChampImportDto.COL_AD))
                .armor(dto.getArmor())
                .range(dto.getRange())
                .magicResist(dto.getMagicResist())
                .attackSpeed(dto.getAttackSpeed())
                .critChance(dto.getCritChance())
                .build();

        String imageUrl = dto.getImageUrl();
        if (imageUrl != null && imageUrl.isBlank()) {
            imageUrl = null;
        }

        return CreateChampRequest.builder()
                .setId(setId)
                .code(trimOrNull(dto.getCode()))
                .name(trimOrNull(dto.getName()))
                .slug(trimOrNull(dto.getSlug()))
                .cost(dto.getCost())
                .imageUrl(imageUrl)
                .stats(stats)
                .traitIds(traitIds)
                .build();
    }

    private UpdateChampRequest buildUpdateRequest(ChampImportDto dto, Long setId, List<Long> traitIds) {
        ChampStatsRequest stats = ChampStatsRequest.builder()
                .hp(parseIntegerList(dto.getHp(), ChampImportDto.COL_HP))
                .ad(parseIntegerList(dto.getAd(), ChampImportDto.COL_AD))
                .armor(dto.getArmor())
                .range(dto.getRange())
                .magicResist(dto.getMagicResist())
                .attackSpeed(dto.getAttackSpeed())
                .critChance(dto.getCritChance())
                .build();

        String imageUrl = dto.getImageUrl();
        if (imageUrl != null && imageUrl.isBlank()) {
            imageUrl = null;
        }

        return UpdateChampRequest.builder()
                .setId(setId)
                .code(trimOrNull(dto.getCode()))
                .name(trimOrNull(dto.getName()))
                .slug(trimOrNull(dto.getSlug()))
                .cost(dto.getCost())
                .imageUrl(imageUrl)
                .stats(stats)
                .traitIds(traitIds)
                .build();
    }

    private void validateCreateRequest(CreateChampRequest request) {
        Set<ConstraintViolation<CreateChampRequest>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }

        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        throw new IllegalArgumentException(message);
    }

    private void validateUpdateRequest(UpdateChampRequest request) {
        Set<ConstraintViolation<UpdateChampRequest>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return;
        }

        String message = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        throw new IllegalArgumentException(message);
    }

    private List<Integer> parseIntegerList(String raw, String columnName) {
        List<String> tokens = splitTokens(raw);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException(columnName + " is required");
        }
        List<Integer> values = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            try {
                values.add(Integer.parseInt(token));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(columnName + " contains invalid number: " + token);
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
            throw new IllegalArgumentException("File extension is missing.");
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private ImportFileStrategy resolveStrategy(String extension) {
        return importFileStrategies.stream()
                .filter(strategy -> strategy.supports(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file type: " + extension));
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
            csvPrinter.printRecord(TEMPLATE_HEADERS);
            csvPrinter.flush();
            return outputStream.toByteArray();
        }
    }

    private byte[] buildXlsxTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeTemplateSheet(workbook);
            writeGuideSheet(workbook);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void writeTemplateSheet(Workbook workbook) {
        Sheet templateSheet = workbook.createSheet(TEMPLATE_SHEET_NAME);
        Row headerRow = templateSheet.createRow(0);

        for (int col = 0; col < TEMPLATE_HEADERS.size(); col++) {
            headerRow.createCell(col).setCellValue(TEMPLATE_HEADERS.get(col));
            templateSheet.setColumnWidth(col, 20 * 256);
        }
    }

    private void writeGuideSheet(Workbook workbook) {
        Sheet guideSheet = workbook.createSheet(GUIDE_SHEET_NAME);
        Row header = guideSheet.createRow(0);
        header.createCell(0).setCellValue("Column");
        header.createCell(1).setCellValue("Required");
        header.createCell(2).setCellValue("Format");
        header.createCell(3).setCellValue("Example");

        int rowIndex = 1;
        for (TemplateGuideRow row : TEMPLATE_GUIDE_ROWS) {
            Row guideRow = guideSheet.createRow(rowIndex++);
            guideRow.createCell(0).setCellValue(row.column());
            guideRow.createCell(1).setCellValue(row.required());
            guideRow.createCell(2).setCellValue(row.format());
            guideRow.createCell(3).setCellValue(row.example());
        }

        guideSheet.setColumnWidth(0, 20 * 256);
        guideSheet.setColumnWidth(1, 12 * 256);
        guideSheet.setColumnWidth(2, 50 * 256);
        guideSheet.setColumnWidth(3, 35 * 256);
    }

    private record ImportContext(
            Map<Long, Sets> activeSetsById,
            Set<Long> deletedSetIds,
            Map<String, Trait> traitBySlug,
            Map<String, List<Trait>> traitsBySetAndName,
            Map<String, ExistingChampLookup> existingByCode,
            Set<String> seenCodesInFile,
            AtomicInteger insertedCount,
            AtomicInteger updatedCount
    ) {
    }

    private record ExistingChampLookup(Long champId, boolean deleted) {
    }

    private record TemplateGuideRow(String column, String required, String format, String example) {
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
                default -> throw new IllegalArgumentException("Unsupported template format: " + format);
            };
        }
    }
}
