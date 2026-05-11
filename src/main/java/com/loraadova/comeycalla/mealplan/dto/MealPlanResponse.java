package com.loraadova.comeycalla.mealplan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MealPlanResponse {
    private Long id;
    private MealPlanConfigResponse config;
    private List<MealPlanDayResponse> days;

}