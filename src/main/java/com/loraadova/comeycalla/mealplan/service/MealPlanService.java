package com.loraadova.comeycalla.mealplan.service;

import com.loraadova.comeycalla.auth.security.CurrenUserService;
import com.loraadova.comeycalla.mealplan.dto.*;
import com.loraadova.comeycalla.mealplan.entity.*;
import com.loraadova.comeycalla.mealplan.mapper.MealPlanMapper;
import com.loraadova.comeycalla.mealplan.repository.MealPlanRepository;
import com.loraadova.comeycalla.recipe.entity.RecipeEntity;
import com.loraadova.comeycalla.recipe.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class MealPlanService {

    @Autowired
    private  MealPlanRepository mealPlanRepository;

    @Autowired
    private  RecipeRepository recipeRepository;

    @Autowired
    private CurrenUserService currenUserService;

    @Autowired
    private MealPlanMapper mealPlanRecipeMapper;

    @Transactional
    public MealPlanResponse createMealPlan(CreateMealPlanRequest request) {
        validateRequest(request);

        List<RecipeEntity> recipes = this.recipeRepository.findByUserId(this.getCurrentUserId());

        int neededRecipes = request.getDays() * request.getMeals().size();

        if (request.getExcludeRepeatedRecipes() && recipes.size() < neededRecipes) {
            throw new RuntimeException("No hay suficientes recetas para generar un menú sin repetir.");
        }

        Collections.shuffle(recipes);

        MealPlanEntity mealPlan = new MealPlanEntity();
        mealPlan.setDaysCount(request.getDays());
        mealPlan.setExcludeRepeatedRecipes(request.getExcludeRepeatedRecipes());

        List<MealPlanDayEntity> days = new ArrayList<>();
        int recipeIndex = 0;

        for (int dayNumber = 1; dayNumber <= request.getDays(); dayNumber++) {
            MealPlanDayEntity day = new MealPlanDayEntity();
            day.setDayNumber(dayNumber);
            day.setMealPlan(mealPlan);

            List<MealSlotEntity> slots = new ArrayList<>();

            for (MealType mealType : request.getMeals()) {
                RecipeEntity recipe;

                if (request.getExcludeRepeatedRecipes()) {
                    // Move through the list and not repeat recipes
                    recipe = recipes.get(recipeIndex);
                    recipeIndex++;
                } else {
                    //Chose any recipe even if its repeated
                    recipe = recipes.get(new Random().nextInt(recipes.size()));
                }

                MealSlotEntity slot = new MealSlotEntity();
                slot.setType(mealType);
                slot.setRecipe(recipe);
                slot.setMealPlanDay(day);

                slots.add(slot);
            }

            day.setSlots(slots);
            days.add(day);
        }

        mealPlan.setDays(days);

        MealPlanEntity savedMealPlan = this.mealPlanRepository.save(mealPlan);

        return mapToResponse(savedMealPlan, request.getMeals());
    }

    private void validateRequest(CreateMealPlanRequest request) {
        if (request.getDays() == null || request.getDays() < 1 || request.getDays() > 7) {
            throw new RuntimeException("El número de días debe estar entre 1 y 7.");
        }

        if (request.getMeals() == null || request.getMeals().isEmpty()) {
            throw new RuntimeException("Debes seleccionar al menos una comida.");
        }
    }
    private MealPlanResponse mapToResponse(MealPlanEntity mealPlan, List<MealType> mealsConfig) {

        List<MealPlanDayResponse> dayResponses = mealPlan.getDays()
                .stream()
                .map(day -> new MealPlanDayResponse(
                        day.getDayNumber(),
                        day.getSlots()
                                .stream()
                                .map(slot -> new MealSlotResponse(
                                        slot.getType(),
                                        mealPlanRecipeMapper.toResponse(
                                                slot.getRecipe(),
                                                slot.getType()
                                        )
                                ))
                                .toList()
                ))
                .toList();

        MealPlanConfigResponse config = new MealPlanConfigResponse(
                mealPlan.getDaysCount(),
                mealsConfig,
                mealPlan.getExcludeRepeatedRecipes()
        );

        return new MealPlanResponse(
                mealPlan.getId(),
                config,
                dayResponses
        );
    }

    private Long getCurrentUserId() {
        return this.currenUserService.getCurrentUserId();
    }

    @Transactional
    public MealPlanRecipeResponse replaceSlot(Long mealPlanId, Integer dayNumber, MealType mealType) {

        MealPlanEntity mealPlan = this.mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new RuntimeException("Meal plan not found"));

        MealPlanDayEntity day = mealPlan.getDays()
                .stream()
                .filter(d -> d.getDayNumber().equals(dayNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Day not found"));

        MealSlotEntity slot = day.getSlots()
                .stream()
                .filter(s -> s.getType().equals(mealType))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        List<RecipeEntity> recipes = this.recipeRepository.findTop10ByUserId(this.getCurrentUserId());

        if (recipes.isEmpty()) {
            throw new RuntimeException("No recipes available");
        }

        RecipeEntity currentRecipe = slot.getRecipe();

        List<RecipeEntity> filtered = recipes.stream()
                .filter(r -> currentRecipe == null || !r.getId().equals(currentRecipe.getId()))
                .toList();

        if (filtered.isEmpty()) {
            throw new RuntimeException("No alternative recipes available");
        }

        RecipeEntity newRecipe = filtered.get(new Random().nextInt(filtered.size()));

        slot.setRecipe(newRecipe);

        this.mealPlanRepository.save(mealPlan);

        return this.mealPlanRecipeMapper.toResponse(newRecipe, mealType);
    }


    public MealPlanEntity getMealPlan(Long mealPlanId){
        return this.mealPlanRepository.findById(mealPlanId) .orElseThrow(() -> new RuntimeException("No se encontró menú plan con ese id"));
    }
}