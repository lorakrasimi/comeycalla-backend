package com.loraadova.comeycalla.mealplan.mapper;

import com.loraadova.comeycalla.mealplan.dto.MealPlanRecipeResponse;
import com.loraadova.comeycalla.mealplan.entity.MealType;
import com.loraadova.comeycalla.recipe.entity.RecipeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MealPlanMapper {

    @Mapping(target = "title", source = "recipe.title")
    @Mapping(target = "category", source = "mealType")
    MealPlanRecipeResponse toResponse(RecipeEntity recipe, MealType mealType);
}
