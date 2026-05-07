package com.loraadova.comeycalla.shoppinglist.service;

import com.loraadova.comeycalla.mealplan.entity.MealPlanDayEntity;
import com.loraadova.comeycalla.mealplan.entity.MealPlanEntity;
import com.loraadova.comeycalla.mealplan.entity.MealSlotEntity;
import com.loraadova.comeycalla.mealplan.service.MealPlanService;
import com.loraadova.comeycalla.recipe.entity.RecipeEntity;
import com.loraadova.comeycalla.recipe.entity.RecipeIngredient;
import com.loraadova.comeycalla.shoppinglist.FoodCategory;
import com.loraadova.comeycalla.shoppinglist.dto.ShoppingListSectionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class ShoppingListService {
    @Autowired
    private OpenFoodFactsClassifierService classifierService;

    @Autowired
    private MealPlanService mealPlanService;

    public List<ShoppingListSectionResponse> createShoppingList(Long mealPlanId) {
        Map<FoodCategory, Set<String>> groupedItems = new LinkedHashMap<>();

        MealPlanEntity mealPlan = this.mealPlanService.getMealPlan(mealPlanId);

        for (MealPlanDayEntity day : mealPlan.getDays()) {
            for (MealSlotEntity slot : day.getSlots()) {
                RecipeEntity recipe = slot.getRecipe();

                if (recipe == null) continue;

                for (RecipeIngredient ingredient : recipe.getIngredients()) {
                    FoodCategory category = this.classifierService.classify(ingredient.getName());

                    groupedItems
                            .computeIfAbsent(category, key -> new LinkedHashSet<>())
                            .add(ingredient.getName());

                }
            }
        }

        return groupedItems.entrySet()
                .stream()
                .map(entry -> new ShoppingListSectionResponse(
                        entry.getKey(),
                        new ArrayList<>(entry.getValue())
                ))
                .toList();
    }
}