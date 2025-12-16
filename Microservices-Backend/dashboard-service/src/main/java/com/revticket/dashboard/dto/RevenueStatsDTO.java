package com.revticket.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatsDTO {
    private Double totalRevenue;
    private Double convenienceFees;
    private Double gstAmount;
    private Double netRevenue;
    private Long totalTransactions;
}
