package org.example.repositories.teamcomp;

import lombok.RequiredArgsConstructor;
import org.example.dto.teamcomp.TeamCompFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.example.repositories.teamcomp.custom.TeamCompRepositoryCustom;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TeamCompRepositoryImpl implements TeamCompRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Page<Long> filterTeamCompIds(TeamCompFilter filter, Pageable pageable) {
        filter.setDeleted(false);
        filter.setSetDeleted(false);
        return executeDynamicQuery(filter, pageable);
    }

    @Override
    public Page<Long> filterTeamCompIdsCms(TeamCompFilter filter, Pageable pageable) {
        return executeDynamicQuery(filter, pageable);
    }

    private Page<Long> executeDynamicQuery(TeamCompFilter filter, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT tc.id FROM team_comp tc ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(DISTINCT tc.id) FROM team_comp tc ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 1. JOIN
        buildJoins(sql, countSql, filter);

        // 2. WHERE
        StringBuilder where = buildWhereClause(filter, params);
        sql.append(where);
        countSql.append(where);

        // 3. COUNT
        Long total = jdbcTemplate.queryForObject(countSql.toString(), params, Long.class);
        if (total == null || total == 0) {
            return Page.empty(pageable);
        }

        // 4. GROUP BY
        sql.append(" GROUP BY tc.id ");

        // 5. ORDER BY
        buildOrderByV2(sql, pageable.getSort());

        // 6. LIMIT & OFFSET
        sql.append(" LIMIT :limit OFFSET :offset ");
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        List<Long> ids = jdbcTemplate.queryForList(sql.toString(), params, Long.class);
        return new PageImpl<>(ids, pageable, total);
    }

    private void buildJoins(StringBuilder sql, StringBuilder countSql, TeamCompFilter filter) {
        if (filter.getChampionId() != null) {
            String joinChamp = " INNER JOIN team_comp_champ tcc ON tc.id = tcc.team_comp_id ";
            sql.append(joinChamp);
            countSql.append(joinChamp);
        }

        if (filter.getSetDeleted() != null) {
            String joinSet = " LEFT JOIN \"set\" s ON tc.set_id = s.id ";
            sql.append(joinSet);
            countSql.append(joinSet);
        }
    }

    private StringBuilder buildWhereClause(TeamCompFilter filter, MapSqlParameterSource params) {
        List<String> conditions = new ArrayList<>();
        conditions.add("1=1");

        if (filter.getDeleted() != null) {
            conditions.add("tc.deleted = :deleted");
            params.addValue("deleted", filter.getDeleted());
        }

        if (filter.getSetDeleted() != null) {
            conditions.add("s.deleted = :setDeleted");
            params.addValue("setDeleted", filter.getSetDeleted());
        }

        if (filter.getSetId() != null) {
            conditions.add("tc.set_id = :setId");
            params.addValue("setId", filter.getSetId());
        }

        if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
            conditions.add("LOWER(tc.name) LIKE :keyword");
            params.addValue("keyword", "%" + filter.getKeyword().trim().toLowerCase() + "%");
        }

        if (filter.getStyles() != null && !filter.getStyles().isEmpty()) {
            conditions.add("UPPER(tc.style) IN (:styles)");
            params.addValue("styles", filter.getStyles().stream().map(String::toUpperCase).toList());
        }

        if (filter.getTiers() != null && !filter.getTiers().isEmpty()) {
            conditions.add("UPPER(tc.tier) IN (:tiers)");
            params.addValue("tiers", filter.getTiers().stream().map(String::toUpperCase).toList());
        }

        if (filter.getChampionId() != null) {
            conditions.add("tcc.champion_id = :championId");
            params.addValue("championId", filter.getChampionId());
        }

        return new StringBuilder(" WHERE ").append(String.join(" AND ", conditions));
    }

    private void buildOrderByV2(StringBuilder sql, Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            sql.append(" ORDER BY tc.id DESC ");
            return;
        }

        List<String> orders = new ArrayList<>();

        for (Sort.Order order : sort) {
            String property = order.getProperty();
            String direction = order.getDirection().name();

            if ("tier".equalsIgnoreCase(property)) {
                orders.add(String.format("""
                    CASE UPPER(TRIM(tc.tier))
                        WHEN 'S' THEN 1
                        WHEN 'A' THEN 2
                        WHEN 'B' THEN 3
                        WHEN 'C' THEN 4
                        WHEN 'D' THEN 5
                        ELSE 99
                    END %s""", direction));
            } else {
                String safe = property.replaceAll("\\W", "");
                orders.add("tc." + safe + " " + direction);
            }
        }

        sql.append(" ORDER BY ").append(String.join(", ", orders));
    }
}


