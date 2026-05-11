package com.loraadova.comeycalla.recipe.controller;

import com.loraadova.comeycalla.recipe.dto.RecipeRequest;
import com.loraadova.comeycalla.recipe.dto.RecipeResponse;
import com.loraadova.comeycalla.recipe.service.RecipeService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecipeResponse> create(
            @RequestPart RecipeRequest recipe,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        RecipeResponse savedRecipe = this.recipeService.createRecipe(recipe, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<RecipeResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart RecipeRequest recipe,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        RecipeResponse updatedRecipe = this.recipeService.updateRecipe(id, recipe, image);
        return ResponseEntity.ok(updatedRecipe);
    }

    @GetMapping
    public ResponseEntity<Page<RecipeResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer maxTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                recipeService.getAllRecipes(search, category, maxTime, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.recipeService.findRecipeById(id));
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        this.recipeService.deleteRecipe(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/last-recipes")
    ResponseEntity<List<RecipeResponse>> lastRecipes() {
        return ResponseEntity.ok(this.recipeService.getLastRecipes());
    }

    @GetMapping("/random-recipe")
    ResponseEntity<RecipeResponse> randomRecipe() {
        return ResponseEntity.ok(this.recipeService.getRandomRecipe());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> findCategories() {
        return ResponseEntity.ok(this.recipeService.findCategories());
    }

}

