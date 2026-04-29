package com.loraadova.comeycalla.recipe.repository;

import com.loraadova.comeycalla.recipe.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long>  {
    void deleteByRecipeId(Long id);
}
