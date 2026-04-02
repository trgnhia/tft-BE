package org.example.dto.teamcomp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamCompResponse {

    private Long id;
    private String tier;
    private String name;
    private String style;


    private List<ChampionSimpleDto> champions;

    // Object chứa thông tin rút gọn của Tướng
    @Getter
    @Setter
    public static class ChampionSimpleDto {
        private Long id;
        private String name;        // Để hiển thị chữ "Urgot", "Karma" dưới ảnh
        private String avatarUrl;   // Link ảnh đại diện của tướng
        private Integer cost;       // Dùng để Frontend css màu viền (1=Xám, 2=Xanh lá, 3=Xanh dương, 4=Tím, 5=Vàng)
    }
}