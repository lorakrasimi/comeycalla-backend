package com.loraadova.comeycalla.mealplan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plan_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanDayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @ManyToOne
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlanEntity mealPlan;

    @OneToMany(mappedBy = "mealPlanDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealSlotEntity> slots = new ArrayList<>();
}