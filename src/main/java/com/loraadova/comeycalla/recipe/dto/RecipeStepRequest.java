package com.loraadova.comeycalla.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeStepRequest {

    @NotBlank(message = "El paso no puede estar vacío")
    private String description;
}