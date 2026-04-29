package com.loraadova.comeycalla.recipe.dto;

import com.loraadova.comeycalla.recipe.entity.Difficulty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RecipeResponse {
    private Long id;
    private String title;
    private String description;
    private String img;
    private Integer cookingTime;
    private Integer servings;
    private Difficulty difficulty;
    private String category;
    private List<RecipeIngredientResponse> ingredients;
    private List<RecipeStepResponse> steps;
    private List<String> tags;
    private LocalDateTime createdAt;
}
