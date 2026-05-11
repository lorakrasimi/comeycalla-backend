package com.loraadova.comeycalla.shoppinglist.controller;

import com.loraadova.comeycalla.shoppinglist.dto.ShoppingListSectionResponse;
import com.loraadova.comeycalla.shoppinglist.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-plans/{id}/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {
    @Autowired
    private ShoppingListService shoppingListService;

    @GetMapping
    public List<ShoppingListSectionResponse> createShoppingList(@PathVariable Long id) {
        return this.shoppingListService.createShoppingList(id);
    }
}
