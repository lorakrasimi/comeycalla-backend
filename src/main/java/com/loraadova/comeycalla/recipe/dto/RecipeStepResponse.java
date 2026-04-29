package com.loraadova.comeycalla.recipe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeStepResponse {

    private Long id;
    private Integer stepOrder;
    private String description;
}