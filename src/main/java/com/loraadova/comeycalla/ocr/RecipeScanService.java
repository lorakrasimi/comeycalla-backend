package com.loraadova.comeycalla.ocr;

import com.loraadova.comeycalla.ocr.dto.RecipeScanResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.rekognition.model.TextTypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecipeScanService {

    private final RekognitionClient rekognitionClient;

    public RecipeScanService(RekognitionClient rekognitionClient) {
        this.rekognitionClient = rekognitionClient;
    }

    public RecipeScanResponseDto scanImages(List<MultipartFile> images) {
        List<String> allLines = new ArrayList<>();

        for (MultipartFile image : images) {
            try {
                List<String> lines = extractLinesFromImage(image);
                allLines.addAll(lines);
            } catch (Exception e) {
                // ignore image if fails
            }
        }

        List<String> uniqueLines = removeDuplicateLines(allLines);
        String rawText = String.join("\n", uniqueLines);

        return parseRecipe(rawText);
    }

    private List<String> removeDuplicateLines(List<String> lines) {
        List<String> result = new ArrayList<>();

        for (String line : lines) {
            String clean = cleanLine(line);

            if (clean.isBlank()) {
                continue;
            }

            boolean exists = result.stream()
                    .anyMatch(existing -> normalize(existing).equals(normalize(clean)));

            if (!exists) {
                result.add(clean);
            }
        }

        return result;
    }

    private List<String> extractLinesFromImage(MultipartFile imageFile) throws IOException {
        SdkBytes imageBytes = SdkBytes.fromInputStream(imageFile.getInputStream());

        Image image = Image.builder()
                .bytes(imageBytes)
                .build();

        DetectTextRequest request = DetectTextRequest.builder()
                .image(image)
                .build();

        DetectTextResponse response = rekognitionClient.detectText(request);

        return response.textDetections()
                .stream()
                .filter(text -> text.type() == TextTypes.LINE)
                .map(TextDetection::detectedText)
                .toList();
    }

    private RecipeScanResponseDto parseRecipe(String rawText) {
        List<String> cleanLines = getCleanLines(rawText);

        String title = extractTitle(cleanLines);
        Integer cookingTime = extractCookingTime(cleanLines);
        Integer servings = extractServings(cleanLines);
        String difficulty = extractDifficulty(cleanLines);

        List<String> ingredients = new ArrayList<>();
        List<String> steps = new ArrayList<>();

        boolean readingIngredients = false;
        boolean readingSteps = false;

        for (String line : cleanLines) {
            String lower = normalize(line);

            if (line.equals(title)) {
                continue;
            }

            if (isMetadataLine(lower)) {
                continue;
            }

            if (isIngredientsTitle(lower)) {
                readingIngredients = true;
                readingSteps = false;
                continue;
            }

            if (isStepsTitle(lower)) {
                readingIngredients = false;
                readingSteps = true;
                continue;
            }

            if (isStopSection(lower)) {
                readingIngredients = false;
                readingSteps = false;
                continue;
            }

            if (readingIngredients) {
                ingredients.add(cleanIngredientLine(line));
                continue;
            }

            if (readingSteps) {
                steps.add(cleanStepLine(line));
            }
        }

        steps = mergeBrokenStepLines(steps);
        ingredients = processIngredients(ingredients);

        return new RecipeScanResponseDto(
                title,
                "",
                null,
                cookingTime,
                servings,
                difficulty,
                "",
                ingredients,
                steps,
                new ArrayList<>(),
                rawText
        );
    }

    private List<String> getCleanLines(String rawText) {
        String[] lines = rawText.split("\\n");
        List<String> cleanLines = new ArrayList<>();

        for (String line : lines) {
            String cleanLine = cleanLine(line);

            if (cleanLine.isBlank()) {
                continue;
            }

            if (isNoiseLine(normalize(cleanLine))) {
                continue;
            }

            cleanLines.add(cleanLine);
        }

        return cleanLines;
    }

    private String extractTitle(List<String> lines) {
        for (String line : lines) {
            String lower = normalize(line);

            if (
                    !isIngredientsTitle(lower)
                            && !isStepsTitle(lower)
                            && !isMetadataLine(lower)
                            && !isNoiseLine(lower)
            ) {
                return line;
            }
        }

        return "";
    }

    private Integer extractCookingTime(List<String> lines) {
        for (String line : lines) {
            String lower = normalize(line);

            if (
                    lower.contains("tiempo")
                            || lower.contains("duracion")
                            || lower.contains("preparacion")
                            || lower.contains("coccion")
                            || lower.contains("horneado")
                            || lower.contains("min")
                            || lower.contains("hora")
            ) {
                Integer minutes = extractMinutes(lower);

                if (minutes != null) {
                    return minutes;
                }
            }
        }

        return null;
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

    private Integer extractServings(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String lower = normalize(lines.get(i));

            if (
                    lower.contains("raciones")
                            || lower.contains("comensales")
                            || lower.contains("personas")
                            || lower.contains("porciones")
                            || lower.contains("servings")
                            || lower.contains("serves")
            ) {
                Matcher matcher = Pattern.compile("(\\d+)").matcher(lower);

                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            }

            if (lower.contains("cantidad de la racion")) {
                return null;
            }
        }

        return null;
    }

    private String extractDifficulty(List<String> lines) {
        for (String line : lines) {
            String lower = normalize(line);

            if (
                    lower.contains("dificultad")
                            || lower.contains("facil")
                            || lower.contains("media")
                            || lower.contains("medio")
                            || lower.contains("dificil")
                            || lower.contains("easy")
                            || lower.contains("medium")
                            || lower.contains("hard")
            ) {
                if (lower.contains("facil") || lower.contains("easy") || lower.contains("baja")) {
                    return "easy";
                }

                if (lower.contains("dificil") || lower.contains("hard") || lower.contains("alta")) {
                    return "hard";
                }

                if (lower.contains("media") || lower.contains("medio") || lower.contains("medium")) {
                    return "medium";
                }
            }
        }

        return "medium";
    }

    private List<String> mergeBrokenIngredientLines(List<String> ingredients) {
        List<String> result = new ArrayList<>();

        for (String ingredient : ingredients) {
            if (ingredient.isBlank()) {
                continue;
            }

            if (!result.isEmpty() && looksLikeContinuationOfIngredient(ingredient)) {
                int lastIndex = result.size() - 1;
                result.set(lastIndex, result.get(lastIndex) + " " + ingredient);
            } else {
                result.add(ingredient);
            }
        }

        return result;
    }

    private boolean looksLikeContinuationOfIngredient(String line) {
        String lower = normalize(line);

        return !startsWithQuantity(lower)
                && !lower.equals("sal")
                && !lower.contains("pimienta")
                && !lower.contains("azucar")
                && !lower.contains("aceite")
                && line.length() < 25;
    }

    private List<String> mergeBrokenStepLines(List<String> steps) {
        List<String> result = new ArrayList<>();

        for (String step : steps) {
            if (step.isBlank()) {
                continue;
            }

            if (result.isEmpty()) {
                result.add(step);
                continue;
            }

            String previous = result.get(result.size() - 1);

            if (shouldMergeWithPreviousStep(previous, step)) {
                result.set(result.size() - 1, previous + " " + step);
            } else {
                result.add(step);
            }
        }

        return result;
    }

    private boolean shouldMergeWithPreviousStep(String previous, String current) {
        String trimmedPrevious = previous.trim();
        String trimmedCurrent = current.trim();

        if (trimmedPrevious.endsWith(".") || trimmedPrevious.endsWith("!") || trimmedPrevious.endsWith("?")) {
            return false;
        }

        if (startsWithStepNumber(trimmedCurrent)) {
            return false;
        }

        return trimmedCurrent.length() < 90;
    }

    private boolean startsWithStepNumber(String line) {
        return line.matches("^\\d+[\\).\\-].*");
    }

    private boolean startsWithQuantity(String line) {
        return line.matches("^\\d+.*")
                || line.matches("^(medio|media|un|una|dos|tres|cuatro|cinco).*");
    }

    private String cleanIngredientLine(String line) {
        return line
                .replaceFirst("^[-•*]\\s*", "")
                .trim();
    }

    private String cleanStepLine(String line) {
        return line
                .replaceFirst("^\\d+[\\).\\-]\\s*", "")
                .replaceFirst("^[-•*]\\s*", "")
                .trim();
    }

    private String cleanLine(String line) {
        return line
                .replace(":", "")
                .replace("•", "")
                .replace("*", "")
                .trim();
    }

    private boolean isIngredientsTitle(String lowerLine) {
        return lowerLine.equals("ingredientes")
                || lowerLine.equals("ingredientes:")
                || lowerLine.contains("lista de ingredientes")
                || lowerLine.equals("ingredients")
                || lowerLine.equals("ingredients:");
    }

    private boolean isStepsTitle(String lowerLine) {
        return lowerLine.equals("instrucciones")
                || lowerLine.equals("instrucciones:")
                || lowerLine.equals("preparacion")
                || lowerLine.equals("preparación")
                || lowerLine.equals("preparación:")
                || lowerLine.equals("preparacion:")
                || lowerLine.equals("elaboracion")
                || lowerLine.equals("elaboracion:")
                || lowerLine.equals("pasos")
                || lowerLine.equals("pasos:")
                || lowerLine.equals("modo de preparacion")
                || lowerLine.equals("preparation")
                || lowerLine.equals("instructions")
                || lowerLine.equals("steps")
                || lowerLine.equals("método");
    }

    private boolean isMetadataLine(String lowerLine) {
        return lowerLine.contains("tiempo")
                || lowerLine.contains("duracion")
                || lowerLine.contains("dificultad")
                || lowerLine.contains("racion")
                || lowerLine.contains("raciones")
                || lowerLine.contains("comensal")
                || lowerLine.contains("comensales")
                || lowerLine.contains("persona")
                || lowerLine.contains("personas")
                || lowerLine.contains("porciones")
                || lowerLine.contains("servings")
                || lowerLine.contains("serves");
    }

    private boolean isStopSection(String lowerLine) {
        return lowerLine.contains("notas")
                || lowerLine.contains("nota")
                || lowerLine.contains("consejos")
                || lowerLine.contains("tips")
                || lowerLine.contains("informacion nutricional")
                || lowerLine.contains("nutricion")
                || lowerLine.contains("calorias");
    }

    private boolean isNoiseLine(String lowerLine) {
        return lowerLine.startsWith("#")
                || lowerLine.contains("www.")
                || lowerLine.contains("http")
                || lowerLine.contains("@")
                || lowerLine.contains("copyright");
    }

    private String normalize(String text) {
        return text
                .toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .trim();
    }

    private boolean looksLikeOnlyQuantity(String line) {
        String lower = normalize(line);

        return lower.matches("^\\d+[.,]?\\d*\\s*(g|gr|gramo|gramos|gramo\\(s\\)|kg|ml|mililitro|mililitros|mililitro\\(s\\)|l|litro|litros|sobre|sobres|sobre\\(s\\)|unidad|unidades|unidad\\(es\\)|pizca|pizcas|pizca\\(s\\)|cucharada|cucharadas|cucharada\\(s\\))\\.?$")
                || lower.matches("^\\d+/\\d+\\s*(g|gr|kg|ml|l)?\\.?$")
                || lower.matches("^(medio|media|un|una|dos|tres|cuatro|cinco|seis|siete|ocho|nueve|diez)\\s*(unidad|unidades)?$");
    }

    private List<String> processIngredients(List<String> rawIngredients) {
        List<String> cleaned = new ArrayList<>();

        for (String line : rawIngredients) {
            String clean = cleanIngredientLine(line);
            String lower = normalize(clean);

            if (clean.isBlank()) continue;

            // Remove serving selector and irrelevant ingredient-section labels
            if (lower.contains("cantidad de la racion")) continue;
            if (lower.equals("2") || lower.equals("4")) continue;
            if (lower.contains("no incluido")) continue;
            if (lower.contains("entrega")) continue;

            cleaned.add(clean);
        }

        List<String> result = new ArrayList<>();

        int i = 0;

        while (i < cleaned.size()) {
            String current = cleaned.get(i);

            if (looksLikeOnlyQuantity(current)) {
                List<String> quantities = new ArrayList<>();

                while (i < cleaned.size() && looksLikeOnlyQuantity(cleaned.get(i))) {
                    quantities.add(cleaned.get(i));
                    i++;
                }

                List<String> names = new ArrayList<>();

                while (
                        i < cleaned.size()
                                && !looksLikeOnlyQuantity(cleaned.get(i))
                                && names.size() < quantities.size()
                ) {
                    names.add(cleaned.get(i));
                    i++;
                }

                for (int j = 0; j < quantities.size(); j++) {
                    if (j < names.size()) {
                        result.add(quantities.get(j) + " " + names.get(j));
                    } else {
                        result.add(quantities.get(j));
                    }
                }

            } else {
                result.add(current);
                i++;
            }
        }

        return mergeBrokenIngredientLines(result);
    }
}