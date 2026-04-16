package org.example.repositories.teamcomp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.example.repositories.teamcomp.custom.TeamCompRepositoryCustom;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TeamCompRepositoryImpl implements TeamCompRepositoryCustom {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Page<Long> filterTeamCompIds(Long setId, String keyword, List<String> styles, Long championId, Pageable pageable) {
        // 1. Khởi tạo câu SQL gốc
        StringBuilder sql = new StringBuilder("SELECT DISTINCT tc.id FROM team_comp tc ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(DISTINCT tc.id) FROM team_comp tc ");

        // Nếu có lọc theo tướng, bắt buộc phải JOIN bảng trung gian
        if (championId != null) {
            String joinSql = " INNER JOIN team_comp_champ tcc ON tc.id = tcc.team_comp_id ";
            sql.append(joinSql);
            countSql.append(joinSql);
        }

        // 2. Xử lý điều kiện động (WHERE)
        StringBuilder whereClause = new StringBuilder(" WHERE tc.deleted = false ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (setId != null) {
            whereClause.append(" AND tc.set_id = :setId ");
            params.addValue("setId", setId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            whereClause.append(" AND LOWER(tc.name) LIKE :keyword ");
            params.addValue("keyword", "%" + keyword.trim().toLowerCase() + "%");
        }

        if (styles != null && !styles.isEmpty()) {
            whereClause.append(" AND UPPER(tc.style) IN (:styles) ");
            params.addValue("styles",
                    styles.stream()
                            .map(String::toUpperCase)
                            .toList()
            );
        }

        if (championId != null) {
            whereClause.append(" AND tcc.champion_id = :championId ");
            params.addValue("championId", championId);
        }

        // Ráp WHERE vào SQL
        sql.append(whereClause);
        countSql.append(whereClause);

        // 3. Đếm tổng số lượng bản ghi (Phục vụ phân trang)
        Long totalElements = jdbcTemplate.queryForObject(countSql.toString(), params, Long.class);
        if (totalElements == null || totalElements == 0) {
            return Page.empty(pageable);
        }

        // 4. Bổ sung Phân trang (LIMIT & OFFSET) và Sắp xếp
        sql.append(" ORDER BY tc.id DESC ");
        sql.append(" LIMIT :limit OFFSET :offset ");
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        // 5. Thực thi lấy danh sách ID
        List<Long> ids = jdbcTemplate.queryForList(sql.toString(), params, Long.class);

        return new PageImpl<>(ids, pageable, totalElements);
    }
}