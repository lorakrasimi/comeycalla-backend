package com.loraadova.comeycalla.mealplan.dto;

import com.loraadova.comeycalla.mealplan.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MealPlanConfigResponse {
    private Integer days;
    private List<MealType> meals;
    private Boolean excludeRepeatedRecipes;
}