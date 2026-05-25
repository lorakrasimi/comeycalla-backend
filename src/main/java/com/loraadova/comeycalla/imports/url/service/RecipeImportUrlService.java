package com.loraadova.comeycalla.imports.url.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loraadova.comeycalla.imports.dto.RecipeScanResponseDto;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecipeImportUrlService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RecipeScanResponseDto importFromUrl(String url) {
        validateUrl(url);

        try {
            Document document = fetchDocument(url);
            validateReadablePage(document);

            RecipeScanResponseDto recipe;

            recipe = extractFromJsonLd(document);
            if (hasUsefulData(recipe)) {
                return recipe;
            }

            recipe = extractFromMicrodata(document);
            if (hasUsefulData(recipe)) {
                return recipe;
            }

            recipe = extractFromKnownRecipeCards(document);
            if (hasUsefulData(recipe)) {
                return recipe;
            }

            recipe = extractFromHeadings(document);
            if (hasUsefulData(recipe)) {
                return recipe;
            }

            recipe = extractFallback(document);
            if (hasUsefulData(recipe)) {
                return recipe;
            }

            throw new RuntimeException("No se ha encontrado una receta reconocible en esta página.");

        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 403) {
                throw new RuntimeException("Esta web no permite importar recetas automáticamente.");
            }

            throw new RuntimeException("Error al acceder a la URL. Código HTTP: " + e.getStatusCode());

        } catch (Exception e) {
            throw new RuntimeException("No se ha podido importar la receta desde esta URL.", e);
        }
    }

    private Document fetchDocument(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .header("Upgrade-Insecure-Requests", "1")
                .referrer("https://www.google.com/")
                .followRedirects(true)
                .timeout(15000)
                .get();
    }

    private void validateReadablePage(Document document) {
        String text = document.text().toLowerCase();

        if (
                text.contains("please enable js")
                        || text.contains("disable any ad blocker")
                        || text.contains("enable javascript")
                        || text.contains("access denied")
                        || text.contains("cloudflare")
        ) {
            throw new RuntimeException(
                    "Esta web bloquea la importación automática. Prueba con otra URL, sube una captura o introduce la receta manualmente."
            );
        }
    }

    private boolean hasUsefulData(RecipeScanResponseDto recipe) {
        if (recipe == null) {
            return false;
        }

        return recipe.getTitle() != null && !recipe.getTitle().isBlank()
                && (
                recipe.getIngredients() != null && !recipe.getIngredients().isEmpty()
                        || recipe.getSteps() != null && !recipe.getSteps().isEmpty()
        );
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);

            if (!"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException();
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("La URL no es válida.");
        }
    }

    private RecipeScanResponseDto extractFromJsonLd(Document document) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            try {
                JsonNode root = objectMapper.readTree(script.html());
                JsonNode recipeNode = findRecipeNode(root);

                if (recipeNode == null) {
                    continue;
                }

                String title = getText(recipeNode, "name");
                String description = getText(recipeNode, "description");
                String image = extractImage(recipeNode.get("image"));
                Integer time = extractTime(recipeNode);

                List<String> ingredients = extractStringList(recipeNode.get("recipeIngredient"));
                List<String> instructions = extractInstructions(recipeNode.get("recipeInstructions"));

                return buildResponse(
                        title,
                        description,
                        image,
                        time,
                        0,
                        ingredients,
                        instructions,
                        ""
                );

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private JsonNode findRecipeNode(JsonNode node) {
        if (node == null) {
            return null;
        }

        if (isRecipe(node)) {
            return node;
        }

        if (node.has("@graph")) {
            for (JsonNode child : node.get("@graph")) {
                JsonNode result = findRecipeNode(child);

                if (result != null) {
                    return result;
                }
            }
        }

        if (node.isArray() || node.isObject()) {
            for (JsonNode child : node) {
                JsonNode result = findRecipeNode(child);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private boolean isRecipe(JsonNode node) {
        JsonNode type = node.get("@type");

        if (type == null) {
            return false;
        }

        if (type.isTextual()) {
            return "Recipe".equalsIgnoreCase(type.asText());
        }

        if (type.isArray()) {
            for (JsonNode value : type) {
                if ("Recipe".equalsIgnoreCase(value.asText())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        return value != null && value.isTextual() ? value.asText() : "";
    }

    private String extractImage(JsonNode imageNode) {
        if (imageNode == null) {
            return "";
        }

        if (imageNode.isTextual()) {
            return imageNode.asText();
        }

        if (imageNode.isArray() && !imageNode.isEmpty()) {
            JsonNode firstImage = imageNode.get(0);

            if (firstImage.isTextual()) {
                return firstImage.asText();
            }

            return getText(firstImage, "url");
        }

        return getText(imageNode, "url");
    }

    private Integer extractTime(JsonNode recipeNode) {
        String totalTime = getText(recipeNode, "totalTime");

        if (totalTime == null || totalTime.isBlank()) {
            return null;
        }

        return parseIsoDurationToMinutes(totalTime);
    }

    private Integer parseIsoDurationToMinutes(String duration) {
        try {
            return (int) java.time.Duration.parse(duration).toMinutes();
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> extractStringList(JsonNode node) {
        List<String> result = new ArrayList<>();

        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                result.add(cleanParsedItem(item.asText()));
            }
        }

        return result.stream()
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private List<String> extractInstructions(JsonNode node) {
        List<String> result = new ArrayList<>();

        if (node == null) {
            return result;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    result.add(cleanParsedItem(item.asText()));
                } else {
                    String text = getText(item, "text");

                    if (!text.isBlank()) {
                        result.add(cleanParsedItem(text));
                    }
                }
            }
        }

        return result.stream()
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private RecipeScanResponseDto extractFromMicrodata(Document document) {
        Element recipeElement = document.selectFirst("[itemscope][itemtype*=Recipe]");

        if (recipeElement == null) {
            return null;
        }

        String title = textFromFirst(recipeElement, "[itemprop=name]");
        String description = contentOrTextFromFirst(recipeElement, "[itemprop=description]");
        String image = extractMicrodataImage(recipeElement);

        Integer cookingTime = extractMicrodataCookingTime(recipeElement);
        Integer servings = extractIntegerFromText(
                contentOrTextFromFirst(recipeElement, "[itemprop=recipeYield]")
        );

        List<String> ingredients = recipeElement
                .select("[itemprop=recipeIngredient]")
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();

        List<String> steps = recipeElement
                .select("[itemprop=recipeInstructions] li, [itemprop=recipeInstructions] p, [itemprop=recipeInstructions]")
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();

        return buildResponse(
                title,
                description,
                image,
                cookingTime,
                servings,
                ingredients,
                steps,
                recipeElement.text()
        );
    }

    private RecipeScanResponseDto extractFromKnownRecipeCards(Document document) {
        String title = textFirst(
                document,
                "h1",
                ".recipe-title",
                ".recipe-card-title",
                ".wprm-recipe-name",
                ".tasty-recipes-title",
                ".mv-create-title"
        );

        String description = textFirst(
                document,
                "meta[name=description]",
                ".recipe-summary",
                ".wprm-recipe-summary",
                ".tasty-recipes-description",
                ".mv-create-description"
        );

        List<String> ingredients = document
                .select(
                        ".wprm-recipe-ingredient, " +
                                ".tasty-recipes-ingredients li, " +
                                ".mv-create-ingredients li, " +
                                ".recipe-ingredients li, " +
                                ".recipe-card-ingredients li, " +
                                "[class*=ingredient] li"
                )
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();

        List<String> steps = document
                .select(
                        ".wprm-recipe-instruction, " +
                                ".tasty-recipes-instructions li, " +
                                ".mv-create-instructions li, " +
                                ".recipe-instructions li, " +
                                ".recipe-card-instructions li, " +
                                "[class*=instruction] li, " +
                                "[class*=preparation] li, " +
                                "[class*=method] li"
                )
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();

        return buildResponse(
                title,
                description,
                getOgImage(document),
                extractMinutes(document.text()),
                extractServingsFromText(document.text()),
                ingredients,
                steps,
                document.text()
        );
    }

    private RecipeScanResponseDto extractFromHeadings(Document document) {
        String title = textFirst(document, "h1", "h2", "title");
        String description = textFirst(document, "meta[name=description]", "p");

        List<String> ingredients = extractItemsAfterHeading(
                document,
                "(?i).*ingredientes.*",
                "(?i).*método.*|.*metodo.*|.*preparación.*|.*preparacion.*|.*elaboración.*|.*elaboracion.*|.*pasos.*|.*instrucciones.*"
        );

        List<String> steps = extractItemsAfterHeading(
                document,
                "(?i).*método.*|.*metodo.*|.*preparación.*|.*preparacion.*|.*elaboración.*|.*elaboracion.*|.*pasos.*|.*instrucciones.*",
                "(?i).*notas.*|.*consejos.*|.*comentarios.*|.*más recetas.*|.*mas recetas.*|.*síguenos.*"
        );

        return buildResponse(
                title,
                description,
                getOgImage(document),
                extractMinutes(document.text()),
                extractServingsFromText(document.text()),
                ingredients,
                steps,
                document.text()
        );
    }

    private List<String> extractItemsAfterHeading(
            Document document,
            String startRegex,
            String endRegex
    ) {
        Element start = findHeading(document, startRegex);

        if (start == null) {
            return new ArrayList<>();
        }

        Element section = getSectionElement(start);
        List<String> result = new ArrayList<>();

        for (
                Element element = section.nextElementSibling();
                element != null;
                element = element.nextElementSibling()
        ) {
            String text = element.text();

            if (isHeadingLike(element) && text.matches(endRegex)) {
                break;
            }

            if (text.matches(endRegex)) {
                break;
            }

            if (element.tagName().equals("ul") || element.tagName().equals("ol")) {
                result.addAll(element.select("li").eachText());
                continue;
            }

            if (!element.select("li").isEmpty()) {
                result.addAll(element.select("li").eachText());
                continue;
            }

            if (element.tagName().equals("p") && looksLikeIngredient(text)) {
                result.add(text);
            }
        }

        return result.stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();
    }

    private Element findHeading(Document document, String regex) {
        return document.selectFirst(
                "h1:matches(" + regex + "), " +
                        "h2:matches(" + regex + "), " +
                        "h3:matches(" + regex + "), " +
                        "h4:matches(" + regex + "), " +
                        "h5:matches(" + regex + "), " +
                        "strong:matches(" + regex + "), " +
                        "p:matches(" + regex + ")"
        );
    }

    private Element getSectionElement(Element element) {
        if (isHeadingLike(element)) {
            return element;
        }

        Element parent = element.parent();

        if (parent != null && isHeadingLike(parent)) {
            return parent;
        }

        return element;
    }

    private boolean isHeadingLike(Element element) {
        return element.tagName().matches("h1|h2|h3|h4|h5|h6");
    }

    private RecipeScanResponseDto extractFallback(Document document) {
        String title = textFirst(document, "h1", "h2", "title");
        String description = textFirst(document, "meta[name=description]", "p");

        List<String> ingredients = document
                .select(
                        "ul li, " +
                                "[class*=ingredient] li, " +
                                ".recipe-ingredients li"
                )
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(this::looksLikeIngredient)
                .distinct()
                .toList();

        List<String> steps = document
                .select(
                        "ol li, " +
                                "[class*=instruction] li, " +
                                "[class*=preparation] li, " +
                                "[class*=method] li"
                )
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .distinct()
                .toList();

        return buildResponse(
                title,
                description,
                getOgImage(document),
                extractMinutes(document.text()),
                extractServingsFromText(document.text()),
                ingredients,
                steps,
                document.text()
        );
    }

    private RecipeScanResponseDto buildResponse(
            String title,
            String description,
            String image,
            Integer cookingTime,
            Integer servings,
            List<String> ingredients,
            List<String> steps,
            String rawText
    ) {
        return new RecipeScanResponseDto(
                cleanParsedItem(title),
                cleanParsedItem(description),
                image == null ? "" : image,
                cookingTime,
                servings == null ? 0 : servings,
                "",
                "",
                ingredients == null ? new ArrayList<>() : ingredients,
                steps == null ? new ArrayList<>() : steps,
                new ArrayList<>(),
                rawText == null ? "" : rawText
        );
    }

    private String textFirst(Document document, String... selectors) {
        for (String selector : selectors) {
            Element element = document.selectFirst(selector);

            if (element == null) {
                continue;
            }

            String content = element.attr("content");

            if (!content.isBlank()) {
                return content;
            }

            String text = element.text();

            if (!text.isBlank()) {
                return text;
            }
        }

        return "";
    }

    private String textFromFirst(Element parent, String selector) {
        Element element = parent.selectFirst(selector);

        if (element == null) {
            return "";
        }

        return element.text();
    }

    private String contentOrTextFromFirst(Element parent, String selector) {
        Element element = parent.selectFirst(selector);

        if (element == null) {
            return "";
        }

        String content = element.attr("content");

        if (!content.isBlank()) {
            return content;
        }

        return element.text();
    }

    private String extractMicrodataImage(Element recipeElement) {
        Element imageMeta = recipeElement.selectFirst("[itemprop=image] meta[itemprop=url]");

        if (imageMeta != null && !imageMeta.attr("content").isBlank()) {
            return imageMeta.attr("content");
        }

        Element image = recipeElement.selectFirst(
                "[itemprop=image] img, img[itemprop=image], img.wp-post-image"
        );

        if (image == null) {
            return "";
        }

        String src = image.attr("src");

        if (!src.isBlank() && !src.startsWith("data:image")) {
            return src;
        }

        String dataSrc = image.attr("data-src");

        if (!dataSrc.isBlank()) {
            return dataSrc;
        }

        return "";
    }

    private Integer extractMicrodataCookingTime(Element recipeElement) {
        Element time = recipeElement.selectFirst("[itemprop=totalTime]");

        if (time == null) {
            return null;
        }

        String datetime = time.attr("datetime");

        if (!datetime.isBlank()) {
            Integer minutes = parseIsoDurationToMinutes(datetime);

            if (minutes != null) {
                return minutes;
            }
        }

        return extractMinutes(time.text());
    }

    private String getOgImage(Document document) {
        String image = document.select("meta[property=og:image]").attr("content");

        if (!image.isBlank()) {
            return image;
        }

        return document.select("meta[name=twitter:image]").attr("content");
    }

    private Integer extractIntegerFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern.compile("\\d+").matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        return null;
    }

    private boolean looksLikeIngredient(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        return text.matches(
                "(?i).*(\\d+|g|gr|kg|ml|l|litro|litros|cucharada|cucharadita|taza|unidad|unidades|diente|dientes|pizca|sal|aceite|harina|azúcar|azucar|mantequilla|huevo|huevos|queso|pollo|cerdo|ternera|pescado|tomate|cebolla|ajo).*"
        );
    }

    private String cleanParsedItem(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replaceAll("^[\\s\\p{Punct}·•●▪]+", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Integer extractMinutes(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher fullMatcher = Pattern
                .compile("(\\d+)\\s*(h|hora|horas).*?(\\d+)\\s*(m|min|mins|minuto|minutos)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (fullMatcher.find()) {
            return Integer.parseInt(fullMatcher.group(1)) * 60
                    + Integer.parseInt(fullMatcher.group(3));
        }

        Matcher hourMatcher = Pattern
                .compile("(\\d+)\\s*(h|hora|horas)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (hourMatcher.find()) {
            return Integer.parseInt(hourMatcher.group(1)) * 60;
        }

        Matcher minuteMatcher = Pattern
                .compile("(\\d+)\\s*(m|min|mins|minuto|minutos)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (minuteMatcher.find()) {
            return Integer.parseInt(minuteMatcher.group(1));
        }

        return null;
    }

    private Integer extractServingsFromText(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        Matcher matcher = Pattern
                .compile("(\\d+)\\s*(comensales|personas|raciones|porciones|bocadillos)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }
}
