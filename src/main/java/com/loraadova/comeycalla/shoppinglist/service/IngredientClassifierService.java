package com.loraadova.comeycalla.shoppinglist.service;
import com.loraadova.comeycalla.shoppinglist.FoodCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IngredientClassifierService {

    private final Map<FoodCategory, List<String>> keywordsByCategory = Map.ofEntries(
            Map.entry(FoodCategory.FRUTA, List.of(
                    "manzana", "platano", "banana", "pera", "naranja", "mandarina", "limon", "lima",
                    "fresa", "frambuesa", "arandano", "mora", "kiwi", "melon", "sandia", "uva",
                    "mango", "pina", "coco", "melocoton", "nectarina", "ciruela", "cereza",
                    "granada", "higo", "datil", "papaya", "aguacate"
            )),
            Map.entry(FoodCategory.VERDURA, List.of(
                    "tomate", "lechuga", "cebolla", "cebolleta", "ajo", "zanahoria", "pimiento",
                    "calabacin", "berenjena", "pepino", "brocoli", "coliflor", "espinaca",
                    "acelga", "repollo", "col", "lombarda", "puerro", "apio", "esparrago",
                    "alcachofa", "champinon", "seta", "patata", "boniato", "batata", "remolacha",
                    "rabano", "maiz", "calabaza", "rúcula", "rucula"
            )),
            Map.entry(FoodCategory.CARNE, List.of(
                    "pollo", "pechuga", "muslo", "ternera", "vaca", "buey", "cerdo", "lomo",
                    "costilla", "pavo", "cordero", "conejo", "jamon", "bacon", "panceta",
                    "chorizo", "salchicha", "morcilla", "sobrasada", "fuet", "carne picada",
                    "hamburguesa"
            )),
            Map.entry(FoodCategory.PESCADO, List.of(
                    "atun", "bonito", "salmon", "merluza", "bacalao", "sardina", "anchoa",
                    "boqueron", "trucha", "lubina", "dorada", "lenguado", "rape", "caballa",
                    "sepia", "calamar", "pulpo", "gamba", "langostino", "mejillon", "almeja",
                    "berberecho", "navaja", "vieira", "marisco"
            )),
            Map.entry(FoodCategory.LACTEO, List.of(
                    "leche", "queso", "mozzarella", "parmesano", "cheddar", "gouda", "emmental",
                    "brie", "camembert", "ricotta", "requeson", "yogur", "yogurt", "mantequilla",
                    "nata", "crema de leche", "cuajada", "kefir", "mascarpone"
            )),
            Map.entry(FoodCategory.CEREAL, List.of(
                    "arroz", "pasta", "macarron", "macarrones", "espagueti", "spaghetti",
                    "tallarines", "fideo", "noodle", "cuscus", "quinoa", "bulgur", "avena",
                    "cereal", "pan", "pan rallado", "harina", "trigo", "maizena", "semola",
                    "tortilla de trigo", "tortilla de maiz", "masa", "pizza", "hojaldre"
            )),
            Map.entry(FoodCategory.LEGUMBRE, List.of(
                    "lenteja", "lentejas", "garbanzo", "garbanzos", "alubia", "alubias",
                    "judia", "judias", "frijol", "frijoles", "habas", "guisante", "guisantes",
                    "soja", "edamame"
            )),
            Map.entry(FoodCategory.ESPECIA, List.of(
                    "sal", "pimienta", "oregano", "albahaca", "perejil", "cilantro", "romero",
                    "tomillo", "laurel", "comino", "curry", "pimenton", "azafran", "canela",
                    "nuez moscada", "clavo", "jengibre", "curcuma", "vainilla", "guindilla",
                    "chile", "cayena", "hierbas provenzales"
            )),
            Map.entry(FoodCategory.BEBIDA, List.of(
                    "agua", "zumo", "jugo", "refresco", "cola", "limonada", "batido",
                    "cafe", "te", "infusion", "vino", "cerveza", "sidra", "caldo"
            )),
            Map.entry(FoodCategory.DULCE, List.of(
                    "azucar", "miel", "chocolate", "cacao", "galleta", "bizcocho", "magdalena",
                    "croissant", "helado", "mermelada", "caramelo", "sirope", "natillas",
                    "flan", "tarta", "pastel", "dulce de leche"
            )),
            Map.entry(FoodCategory.ACEITE_SALSA, List.of(
                    "aceite", "aceite de oliva", "aceite de girasol", "vinagre", "mayonesa",
                    "ketchup", "mostaza", "salsa", "salsa de soja", "soja", "barbacoa",
                    "pesto", "alioli", "tabasco", "tomate frito"
            )),
            Map.entry(FoodCategory.CONSERVA, List.of(
                    "lata", "en lata", "conserva", "conservas", "atun en lata", "maiz en lata",
                    "tomate en conserva", "anchoas en conserva", "aceituna", "aceitunas",
                    "pepinillo", "pepinillos"
            )),
            Map.entry(FoodCategory.CONGELADO, List.of(
                    "congelado", "congelada", "congelados", "congeladas", "helado",
                    "verdura congelada", "pescado congelado"
            ))
    );

    public FoodCategory classify(String ingredient) {
        String normalizedIngredient = normalize(ingredient);

        if (normalizedIngredient.isBlank()) {
            return FoodCategory.OTRO;
        }

        for (Map.Entry<FoodCategory, List<String>> entry : keywordsByCategory.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (matchesKeyword(normalizedIngredient, normalize(keyword))) {
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
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean matchesKeyword(String ingredient, String keyword) {
        if (ingredient.equals(keyword)) {
            return true;
        }

        return ingredient.contains(keyword + " ")
                || ingredient.contains(" " + keyword)
                || ingredient.contains(" " + keyword + " ");
    }
}