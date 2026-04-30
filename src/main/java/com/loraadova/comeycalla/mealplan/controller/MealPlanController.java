package com.loraadova.comeycalla.mealplan.controller;

import com.loraadova.comeycalla.mealplan.dto.CreateMealPlanRequest;
import com.loraadova.comeycalla.mealplan.dto.MealPlanRecipeResponse;
import com.loraadova.comeycalla.mealplan.dto.MealPlanResponse;
import com.loraadova.comeycalla.mealplan.entity.MealType;
import com.loraadova.comeycalla.mealplan.service.MealPlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping
    public MealPlanResponse createMealPlan(@RequestBody CreateMealPlanRequest request) {
        return mealPlanService.createMealPlan(request);
    }

    @PutMapping("/{mealPlanId}/days/{dayNumber}/slots/{mealType}/replace")
    public MealPlanRecipeResponse replaceSlot(
            @PathVariable Long mealPlanId,
            @PathVariable Integer dayNumber,
            @PathVariable MealType mealType
    ) {
        return mealPlanService.replaceSlot(mealPlanId, dayNumber, mealType);
    }
}