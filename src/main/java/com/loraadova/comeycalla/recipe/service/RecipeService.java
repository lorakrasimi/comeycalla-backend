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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        Recipe recipe = recipeRepository.findRecipeByIdAndUser_Id(id, this.getCurrentUser().getId()).orElseThrow(() -> new RuntimeException("Receta no encontrada"));

        return this.recipeMapper.toResponse(recipe);
    }

    @Transactional
    public RecipeResponse update(Long id, RecipeRequest request) {
        Recipe recipe = findCurrentUserRecipe(id);

        updateBasicFields(recipe, request);
        replaceRecipeChildren(recipe, request);

        Recipe savedRecipe = recipeRepository.save(recipe);

        return recipeMapper.toResponse(savedRecipe);
    }

    private void setTags(RecipeRequest request, Recipe recipe) {
        for (String tagName : request.getTags()) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElseGet(() -> {
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

    private Recipe findCurrentUserRecipe(Long id) {
        return recipeRepository.findRecipeByIdAndUser_Id(id, getCurrentUser().getId()).orElseThrow(() -> new RuntimeException("Receta no encontrada"));
    }

    private void updateBasicFields(Recipe recipe, RecipeRequest request) {
        recipe.setTitle(request.getTitle());
        recipe.setDescription(request.getDescription());
        recipe.setImg(request.getImg());
        recipe.setCookingTime(request.getCookingTime());
        recipe.setServings(request.getServings());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setCategory(request.getCategory());
    }

    private void replaceRecipeChildren(Recipe recipe, RecipeRequest request) {
        clearRecipeChildren(recipe);

        setIngredients(request, recipe);
        setSteps(request, recipe);
        setTags(request, recipe);
    }

    private void clearRecipeChildren(Recipe recipe) {
        recipe.getIngredients().clear();
        recipe.getSteps().clear();
        recipe.getTags().clear();

        recipeIngredientRepository.deleteByRecipeId(recipe.getId());
        recipeStepRepository.deleteByRecipeId(recipe.getId());

        entityManager.flush();
    }


    @Transactional
    public void delete(Long id) {
        this.recipeRepository.deleteRecipeByIdAndUser_Id(id, this.getCurrentUser().getId());
    }

    public List<RecipeResponse> getLastRecipes() {
        List<Recipe> recipeList = this.recipeRepository.findTop10ByUserId(this.getCurrentUser().getId());
        List<RecipeResponse> recipeResponseList = new ArrayList<>();
        recipeList.forEach(recipe -> recipeResponseList.add(this.recipeMapper.toResponse(recipe)));
        return recipeResponseList;
    }

    public RecipeResponse getRandomRecipe() {
        List<Recipe> result = recipeRepository.findByUserId(this.getCurrentUser().getId());

        if (result.isEmpty()) {
            return new RecipeResponse();
        }

        int index = new Random().nextInt(result.size());
        Recipe randomRecipe = result.get(index);

        return this.recipeMapper.toResponse(randomRecipe);
    }

    public Page<RecipeResponse> findAll(
            String search,
            Integer maxTime,
            Integer servings,
            Pageable pageable
    ) {
        Long userId = this.getCurrentUser().getId();

        return recipeRepository
                .findRecipesFiltered(userId, search, maxTime, servings, pageable)
                .map(recipeMapper::toResponse);
    }
}

