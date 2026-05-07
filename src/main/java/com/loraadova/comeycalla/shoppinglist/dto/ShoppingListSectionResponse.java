package com.loraadova.comeycalla.shoppinglist.dto;
import com.loraadova.comeycalla.shoppinglist.FoodCategory;

import java.util.List;

public record ShoppingListSectionResponse(
        FoodCategory category,
        List<String> ingredients
) {
}