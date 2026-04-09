package org.example.dto.champs;

import lombok.Data;

@Data
public class ChampFilterRequest {

    private String keyword;        // tìm theo name
    private Long setId;            // filter theo set
    private Integer cost;          // filter theo cost (gold)
    private String trait;          // filter theo trait/origin
    private String tier;           // filter theo tier (S, A, B...)
    private Boolean deleted;
}
