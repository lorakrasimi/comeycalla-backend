package com.loraadova.comeycalla.mealplan.dto;

import com.loraadova.comeycalla.mealplan.entity.MealType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MealPlanRecipeResponse {
    private Long id;
    private String title;
    private String img;
    private MealType category;
    private Integer cookingTime;
    private Integer servings;

}
