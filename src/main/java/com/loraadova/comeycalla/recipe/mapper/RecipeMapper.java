package com.loraadova.comeycalla.recipe.mapper;

import com.loraadova.comeycalla.recipe.dto.*;
import com.loraadova.comeycalla.recipe.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "steps", ignore = true)
    @Mapping(target = "tags", ignore = true)
    RecipeEntity toEntity(RecipeRequest request);

    @Mapping(target = "tags", expression = "java(mapTags(recipeEntity.getTags()))")
    @Mapping(target = "ingredients", source = "ingredients")
    @Mapping(target = "steps", source = "steps")
    RecipeResponse toResponse(RecipeEntity recipeEntity);

    RecipeIngredient toEntity(RecipeIngredientRequest request);

    RecipeIngredientResponse toResponse(RecipeIngredient ingredient);

    @Mapping(target = "stepOrder", ignore = true)
    RecipeStep toEntity(RecipeStepRequest request);

    RecipeStepResponse toResponse(RecipeStep step);

    default List<String> mapTags(List<Tag> tags) {
        if (tags == null) {
            return List.of();
        }

        return tags.stream()
                .map(Tag::getName)
                .toList();
    }
}