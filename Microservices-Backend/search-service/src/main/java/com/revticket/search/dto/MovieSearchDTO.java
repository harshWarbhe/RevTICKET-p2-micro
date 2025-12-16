package com.revticket.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSearchDTO {
    private String id;
    private String title;
    private String genre;
    private String language;
    private Integer duration;
    private String posterUrl;
}
