package com.loraadova.comeycalla.imports.url.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loraadova.comeycalla.imports.dto.RecipeScanResponseDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

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
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                    .referrer("https://www.google.com/")
                    .followRedirects(true)
                    .timeout(15000)
                    .get();

            RecipeScanResponseDto recipeFromJsonLd = extractFromJsonLd(document);

            if (recipeFromJsonLd != null) {
                return recipeFromJsonLd;
            }

            RecipeScanResponseDto recipeFromMicrodata = extractFromMicrodata(document);

            if (recipeFromMicrodata != null) {
                return recipeFromMicrodata;
            }

            return extractFallback(document);

        } catch (Exception e) {
            throw new RuntimeException(
                    "No se ha podido importar la receta desde esta URL.", e
            );
        }
    }

    private void validateUrl(String url) {
        try {
            URI uri = URI.create(url);

            if (!"http".equalsIgnoreCase(uri.getScheme()) &&
                    !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("La URL no es válida.");
        }
    }

    private RecipeScanResponseDto extractFromJsonLd(Document document) {
        for (Element script : document.select("script[type=application/ld+json]")) {
            try {
                JsonNode root = this.objectMapper.readTree(script.html());
                JsonNode recipeNode = findRecipeNode(root);

                if (recipeNode == null) {
                    continue;
                }

                String title = this.getText(recipeNode, "name");
                String description = this.getText(recipeNode, "description");
                String image = this.extractImage(recipeNode.get("image"));
                Integer time = this.extractTime(recipeNode);

                List<String> ingredients = this.extractStringList(recipeNode.get("recipeIngredient"));
                List<String> instructions = this.extractInstructions(recipeNode.get("recipeInstructions"));

                return new RecipeScanResponseDto(
                        title,
                        description,
                        image,
                        time,
                        0,
                        "easy",
                        "",
                        ingredients,
                        instructions,
                        new ArrayList<>(),
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

        if (this.isRecipe(node)) {
            return node;
        }

        if (node.has("@graph")) {
            for (JsonNode child : node.get("@graph")) {
                if (this.isRecipe(child)) {
                    return child;
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode result = this.findRecipeNode(child);
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
            return firstImage.isTextual() ? firstImage.asText() : getText(firstImage, "url");
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
                result.add(item.asText());
            }
        }

        return result;
    }

    private List<String> extractInstructions(JsonNode node) {
        List<String> result = new ArrayList<>();

        if (node == null) {
            return result;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual()) {
                    result.add(item.asText());
                } else {
                    String text = getText(item, "text");
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            }
        }

        return result;
    }

    private RecipeScanResponseDto extractFallback(Document document) {
        Element titleElement = document.selectFirst("h1, h2.wp-block-heading, h2");
        String title = titleElement != null ? titleElement.text() : document.title();

        String description = "";
        Element firstParagraph = document.selectFirst("p");
        if (firstParagraph != null) {
            description = firstParagraph.text();
        }

        Integer servings = extractServingsFromText(document.text());

        Integer cookingTime = null;
        Matcher timeMatcher = Pattern
                .compile("(\\d+)\\s*(min|minutos|hora|horas)", Pattern.CASE_INSENSITIVE)
                .matcher(document.text());

        if (timeMatcher.find()) {
            cookingTime = extractMinutes(timeMatcher.group());
        }

        List<String> ingredients = document
                .select("h3:containsOwn(Ingredientes) + ul li, ul.wp-block-list li")
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .toList();

        List<String> steps = document
                .select("h3:containsOwn(Cómo hacer) + ol li, ol.wp-block-list li")
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .toList();

        return new RecipeScanResponseDto(
                cleanParsedItem(title),
                cleanParsedItem(description),
                document.select("meta[property=og:image]").attr("content"),
                cookingTime,
                servings,
                "",
                "",
                ingredients,
                steps,
                new ArrayList<>(),
                document.text()
        );
    }

    private RecipeScanResponseDto extractFromMicrodata(Document document) {
        Element recipeElement = document.selectFirst("[itemscope][itemtype*=Recipe]");

        if (recipeElement == null) {
            return null;
        }

        String title = textFromFirst(recipeElement);
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
                .toList();

        List<String> steps = recipeElement
                .select("[itemprop=recipeInstructions]")
                .eachText()
                .stream()
                .map(this::cleanParsedItem)
                .filter(item -> !item.isBlank())
                .toList();

        if (title.isBlank() && ingredients.isEmpty() && steps.isEmpty()) {
            return null;
        }

        return new RecipeScanResponseDto(
                cleanParsedItem(title),
                cleanParsedItem(description),
                image,
                cookingTime,
                servings,
                "",
                "",
                ingredients,
                steps,
                new ArrayList<>(),
                recipeElement.text()
        );
    }

    private String textFromFirst(Element parent) {
        Element element = parent.selectFirst("[itemprop=name]");

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

        Element image = recipeElement.selectFirst("[itemprop=image] img, img[itemprop=image], img.wp-post-image");

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
        Integer hours = null;
        Integer minutes = null;

        Matcher hourMatcher = Pattern
                .compile("(\\d+)\\s*(h|hora|horas)")
                .matcher(text);

        if (hourMatcher.find()) {
            hours = Integer.parseInt(hourMatcher.group(1));
        }

        Matcher minuteMatcher = Pattern
                .compile("(\\d+)\\s*(m|min|mins|minuto|minutos)")
                .matcher(text);

        if (minuteMatcher.find()) {
            minutes = Integer.parseInt(minuteMatcher.group(1));
        }

        if (hours != null || minutes != null) {
            return (hours == null ? 0 : hours * 60) + (minutes == null ? 0 : minutes);
        }

        Matcher simpleNumberMatcher = Pattern.compile("(\\d+)").matcher(text);

        if (simpleNumberMatcher.find()) {
            return Integer.parseInt(simpleNumberMatcher.group(1));
        }

        return null;
    }

    private Integer extractServingsFromText(String text) {
        Matcher matcher = Pattern
                .compile("(\\d+)\\s*(comensales|personas|raciones|porciones)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }
}