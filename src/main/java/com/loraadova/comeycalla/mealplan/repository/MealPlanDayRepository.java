package com.loraadova.comeycalla.mealplan.repository;

import com.loraadova.comeycalla.mealplan.entity.MealPlanDayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanDayRepository extends JpaRepository<MealPlanDayEntity, Long> {
}