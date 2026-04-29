package com.loraadova.comeycalla.recipe.repository;

import com.loraadova.comeycalla.recipe.entity.Recipe;
import com.loraadova.comeycalla.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findRecipeByIdAndUser_Id(Long id, Long user_id);

    void deleteRecipeByIdAndUser_Id(Long id, Long user_id);
}
