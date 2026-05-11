package com.loraadova.comeycalla.mealplan.repository;

import com.loraadova.comeycalla.mealplan.entity.MealPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository  extends JpaRepository<MealPlanEntity, Long> {

    int countByUserId(Long userId);

}
