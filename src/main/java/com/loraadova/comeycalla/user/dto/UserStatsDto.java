package com.loraadova.comeycalla.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserStatsDto {
    private int savedRecipes;
    private int createdMenus;
    private int cookedRecipes;
}
