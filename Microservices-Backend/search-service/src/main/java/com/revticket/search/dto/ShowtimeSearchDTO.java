package com.revticket.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSearchDTO {
    private String id;
    private String movieTitle;
    private String theaterName;
    private LocalDateTime showDateTime;
    private String screenName;
}
