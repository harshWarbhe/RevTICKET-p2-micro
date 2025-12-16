package com.revticket.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    private Long totalUsers;
    private Long usersLast7Days;
    private Long usersLast30Days;
}
