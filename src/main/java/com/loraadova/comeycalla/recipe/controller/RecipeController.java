package com.loraadova.comeycalla.recipe.controller;

import com.loraadova.comeycalla.recipe.dto.RecipeRequest;
import com.loraadova.comeycalla.recipe.dto.RecipeResponse;
import com.loraadova.comeycalla.recipe.service.RecipeService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    @Autowired
    private RecipeService recipeService;

    @PostMapping("/new")
    ResponseEntity<RecipeResponse> create(@Valid @RequestBody RecipeRequest request) {
        RecipeResponse savedRecipe = this.recipeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRecipe);
    }

    @PutMapping("/{id}")
    ResponseEntity<RecipeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RecipeRequest request
    ) {
        RecipeResponse updatedRecipe = this.recipeService.update(id, request);
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
                recipeService.findAll(search, category, maxTime, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.recipeService.findById(id));
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        this.recipeService.delete(id);
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
        return ResponseEntity.ok(recipeService.findCategories());
    }

}

