package com.loraadova.comeycalla.shoppinglist.service;
import com.loraadova.comeycalla.shoppinglist.FoodCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OpenFoodFactsClassifierService {

    private final Map<FoodCategory, List<String>> keywordsByCategory = Map.ofEntries(
            Map.entry(FoodCategory.FRUTA, List.of(
                    "manzana", "platano", "pera", "naranja", "limon", "fresa", "kiwi",
                    "melon", "sandia", "uva", "mango", "aguacate"
            )),
            Map.entry(FoodCategory.VERDURA, List.of(
                    "tomate", "lechuga", "cebolla", "zanahoria", "pimiento", "calabacin",
                    "berenjena", "brocoli", "coliflor", "espinaca", "patata", "ajo"
            )),
            Map.entry(FoodCategory.CARNE, List.of(
                    "pollo", "ternera", "cerdo", "pavo", "jamon", "bacon", "chorizo",
                    "salchicha", "carne picada"
            )),
            Map.entry(FoodCategory.PESCADO, List.of(
                    "atun", "salmon", "merluza", "bacalao", "sardina", "gamba",
                    "langostino", "calamar", "mejillon"
            )),
            Map.entry(FoodCategory.LACTEO, List.of(
                    "leche", "queso", "mozzarella", "yogur", "mantequilla", "nata",
                    "parmesano", "cheddar"
            )),
            Map.entry(FoodCategory.CEREAL, List.of(
                    "arroz", "pasta", "macarron", "espagueti", "pan", "harina",
                    "avena", "cuscus", "quinoa", "tortilla"
            )),
            Map.entry(FoodCategory.LEGUMBRE, List.of(
                    "lenteja", "garbanzo", "alubia", "judia", "soja", "guisante"
            )),
            Map.entry(FoodCategory.ESPECIA, List.of(
                    "sal", "pimienta", "oregano", "canela", "comino", "curry",
                    "pimenton", "perejil", "albahaca"
            )),
            Map.entry(FoodCategory.BEBIDA, List.of(
                    "agua", "zumo", "refresco", "vino", "cerveza", "leche de avena"
            )),
            Map.entry(FoodCategory.DULCE, List.of(
                    "azucar", "chocolate", "miel", "galleta", "bizcocho", "helado"
            )),
            Map.entry(FoodCategory.ACEITE_SALSA, List.of(
                    "aceite", "vinagre", "mayonesa", "ketchup", "mostaza", "salsa",
                    "soja"
            )),
            Map.entry(FoodCategory.CONSERVA, List.of(
                    "conserva", "lata", "en lata", "tomate frito"
            )),
            Map.entry(FoodCategory.CONGELADO, List.of(
                    "congelado", "congelada", "congelados"
            ))
    );

    public FoodCategory classify(String ingredient) {
        String normalizedIngredient = normalize(ingredient);

        if (normalizedIngredient.isBlank()) {
            return FoodCategory.OTRO;
        }

        for (Map.Entry<FoodCategory, List<String>> entry : keywordsByCategory.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalizedIngredient.contains(normalize(keyword))) {
                    return entry.getKey();
                }
            }
        }

        return FoodCategory.OTRO;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
    }
}