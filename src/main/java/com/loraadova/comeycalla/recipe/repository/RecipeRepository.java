package com.loraadova.comeycalla.recipe.repository;

import com.loraadova.comeycalla.recipe.entity.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findRecipeByIdAndUser_Id(Long id, Long user_id);

    void deleteRecipeByIdAndUser_Id(Long id, Long user_id);

    List<Recipe> findTop10ByUserId(Long userId);

    List<Recipe> findByUserId(Long userId);

    @Query("""
    SELECT DISTINCT r
    FROM Recipe r
    LEFT JOIN r.ingredients i
    WHERE r.user.id = :userId
    AND (:search IS NULL OR :search = ''
        OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(r.category) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    AND (:maxTime IS NULL OR r.cookingTime <= :maxTime)
    AND (:servings IS NULL OR r.servings = :servings)
""")
    Page<Recipe> findRecipesFiltered(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("maxTime") Integer maxTime,
            @Param("servings") Integer servings,
            Pageable pageable
    );
}
