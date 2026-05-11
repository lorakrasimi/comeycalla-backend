package com.loraadova.comeycalla.imports.dto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RecipeScanResponseDto {

    private String title;
    private String description;
    private String img;
    private Integer cookingTime;
    private Integer servings;
    private String difficulty;
    private String category;
    private List<String> ingredients;
    private List<String> steps;
    private List<String> tags;
    private String rawText;

}