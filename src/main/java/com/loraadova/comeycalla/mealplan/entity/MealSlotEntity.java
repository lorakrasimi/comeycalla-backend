package com.loraadova.comeycalla.mealplan.entity;

import com.loraadova.comeycalla.recipe.entity.RecipeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MealType type;

    @ManyToOne
    @JoinColumn(name = "meal_plan_day_id", nullable = false)
    private MealPlanDayEntity mealPlanDay;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private RecipeEntity recipe;
}