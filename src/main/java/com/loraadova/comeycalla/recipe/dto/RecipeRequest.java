package com.loraadova.comeycalla.recipe.dto;

import com.loraadova.comeycalla.recipe.entity.Difficulty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeRequest {
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 200, message = "Máximo 200 caracteres")
    private String title;

    @Size(max = 2000, message = "Descripción demasiado larga")
    private String description;

    private String category;

    private Integer cookingTime;

    private Integer servings;

    private Difficulty difficulty;

    private String img;

    private List<RecipeIngredientRequest> ingredients;

    private List<RecipeStepRequest> steps;

    private List<String> tags;
}
