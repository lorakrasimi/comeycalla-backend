package com.loraadova.comeycalla.recipe.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecipeIngredientResponse {

    private Long id;
    private String name;
    private Integer position;
}