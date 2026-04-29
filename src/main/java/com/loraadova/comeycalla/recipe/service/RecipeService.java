package com.loraadova.comeycalla.recipe.service;

import com.loraadova.comeycalla.auth.security.CurrenUserService;
import com.loraadova.comeycalla.recipe.dto.RecipeRequest;
import com.loraadova.comeycalla.recipe.dto.RecipeResponse;
import com.loraadova.comeycalla.recipe.entity.Recipe;
import com.loraadova.comeycalla.recipe.entity.RecipeIngredient;
import com.loraadova.comeycalla.recipe.entity.RecipeStep;
import com.loraadova.comeycalla.recipe.entity.Tag;
import com.loraadova.comeycalla.recipe.mapper.RecipeMapper;
import com.loraadova.comeycalla.recipe.repository.RecipeIngredientRepository;
import com.loraadova.comeycalla.recipe.repository.RecipeRepository;
import com.loraadova.comeycalla.recipe.repository.RecipeStepRepository;
import com.loraadova.comeycalla.recipe.repository.TagRepository;
import com.loraadova.comeycalla.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;


@Service
public class RecipeService {
    @Autowired
    CurrenUserService currenUserService;

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    RecipeMapper recipeMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private RecipeStepRepository recipeStepRepository;

    @PersistenceContext
    private EntityManager entityManager;


    public RecipeResponse create(RecipeRequest request) {
        Recipe newRecipe = this.recipeMapper.toEntity(request);
        newRecipe.setUser(this.getCurrentUser());

        if (request.getIngredients() != null) setIngredients(request, newRecipe);
        if (request.getSteps() != null) setSteps(request, newRecipe);
        if (request.getTags() != null) setTags(request, newRecipe);

        Recipe saverdRecipe = this.recipeRepository.save(newRecipe);
        return this.recipeMapper.toResponse(saverdRecipe);
    }

    public RecipeResponse findById(Long id) {
        Recipe recipe = recipeRepository
                .findRecipeByIdAndUser_Id(id, this.getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        return this.recipeMapper.toResponse(recipe);
    }

    @Transactional
    public RecipeResponse update(Long id, RecipeRequest request) {
        Recipe recipe = this.recipeRepository
                .findRecipeByIdAndUser_Id(id, this.getCurrentUser().getId())
                .orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setImg(request.getImg());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setServings(request.getServings());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setCategory(request.getCategory());

        recipe.getIngredients().clear();
        recipe.getSteps().clear();
        recipe.getTags().clear();

        this.recipeIngredientRepository.deleteByRecipeId(recipe.getId());
        this.recipeStepRepository.deleteByRecipeId(recipe.getId());
        this.entityManager.flush();

        setIngredients(request, recipe);
        setSteps(request, recipe);
        setTags(request, recipe);

        Recipe savedRecipe = this.recipeRepository.save(recipe);
        return this.recipeMapper.toResponse(savedRecipe);
    }

    private void setTags(RecipeRequest request, Recipe recipe) {
        for (String tagName : request.getTags()) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });

            recipe.getTags().add(tag);
        }

    }

    private void setSteps(RecipeRequest request, Recipe recipe) {
        for (int i = 0; i < request.getSteps().size(); i++) {
            RecipeStep step = recipeMapper.toEntity(request.getSteps().get(i));
            step.setRecipe(recipe);
            step.setStepOrder(i + 1);
            recipe.getSteps().add(step);
        }
    }

    private void setIngredients(RecipeRequest request, Recipe recipe) {
        for (int i = 0; i < request.getIngredients().size(); i++) {
            RecipeIngredient ingredient = recipeMapper.toEntity(request.getIngredients().get(i));
            ingredient.setRecipe(recipe);
            ingredient.setPosition(i + 1);
            recipe.getIngredients().add(ingredient);
        }
    }

    private User getCurrentUser() {
        return this.currenUserService.getCurrentUser();
    }

// // Obtener listado paginado (opcional filtro por texto)
// Page<RecipeResponse> findAll(Pageable pageable, String search){};


// // Eliminar receta
// void delete(Long id, Long userId){};

//// (Opcional) Obtener recetas del usuario
// Page<RecipeResponse> findByUser(Long userId, Pageable pageable){};
}
