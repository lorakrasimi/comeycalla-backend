package com.loraadova.comeycalla.recipe.repository;

import com.loraadova.comeycalla.recipe.entity.RecipeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

    Optional<RecipeEntity> findRecipeByIdAndUser_Id(Long id, Long user_id);

    void deleteRecipeByIdAndUser_Id(Long id, Long user_id);

    List<RecipeEntity> findTop10ByUserId(Long userId);

    List<RecipeEntity> findByUserId(Long userId);

    int countByUserId(Long userId);

    @Query("""
    SELECT DISTINCT r
    FROM RecipeEntity r
    LEFT JOIN r.ingredients i
    WHERE r.user.id = :userId
    AND (:search IS NULL OR :search = ''
        OR LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%'))
    )
    AND (:category IS NULL OR :category = '' OR LOWER(r.category) = LOWER(:category))
    AND (:maxTime IS NULL OR r.cookingTime <= :maxTime)
""")
    Page<RecipeEntity> findRecipesFiltered(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("category") String category,
            @Param("maxTime") Integer maxTime,
            Pageable pageable
    );

}
