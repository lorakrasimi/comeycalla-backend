package com.loraadova.comeycalla.recipe.controller;

import com.loraadova.comeycalla.recipe.dto.RecipeRequest;
import com.loraadova.comeycalla.recipe.dto.RecipeResponse;
import com.loraadova.comeycalla.recipe.service.RecipeService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // // GET /api/recipes?page=&size=&search=
    // ResponseEntity<Page<RecipeResponse>> findAll(
    //         @RequestParam int page,
    //         @RequestParam int size,
    //         @RequestParam(required = false) String search
    // ) {
    // }
//
    // ;

    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(this.recipeService.findById(id));
    }


    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        this.recipeService.delete(id);
        return ResponseEntity.ok().build();
    }


    // (Opcional) GET /api/recipes/me
    @GetMapping("/last-recipes")
    ResponseEntity<List<RecipeResponse>> lastRecipes() {
        return ResponseEntity.ok(this.recipeService.getLastRecipes());
    }

}

