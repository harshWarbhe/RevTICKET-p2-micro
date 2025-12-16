package com.revticket.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TheaterSearchDTO {
    private String id;
    private String name;
    private String location;
    private String city;
}
