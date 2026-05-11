package com.loraadova.comeycalla.recipe.repository;

import com.loraadova.comeycalla.recipe.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, Long> {
    void deleteByRecipeEntityId(Long id);
}
