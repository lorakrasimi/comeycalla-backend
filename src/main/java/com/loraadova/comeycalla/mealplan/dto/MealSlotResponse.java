package com.loraadova.comeycalla.mealplan.dto;

import com.loraadova.comeycalla.mealplan.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MealSlotResponse {

    private MealType type;
    private MealPlanRecipeResponse recipe;
}