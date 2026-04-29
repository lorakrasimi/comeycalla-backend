package com.loraadova.comeycalla.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeIngredientRequest {

    @NotBlank(message = "El nombre del ingrediente es obligatorio")
    private String name;
}