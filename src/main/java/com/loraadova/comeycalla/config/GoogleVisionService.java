package com.loraadova.comeycalla.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class GoogleVisionService {

    public String extractText(MultipartFile file) {
        try {
            GoogleCredentials credentials = getCredentials();

            ImageAnnotatorSettings settings =
                    ImageAnnotatorSettings.newBuilder()
                            .setCredentialsProvider(() -> credentials)
                            .build();

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create(settings)) {
                ByteString imgBytes = ByteString.readFrom(file.getInputStream());

                Image img = Image.newBuilder()
                        .setContent(imgBytes)
                        .build();

                Feature feature = Feature.newBuilder()
                        .setType(Feature.Type.DOCUMENT_TEXT_DETECTION)
                        .build();

                AnnotateImageRequest request =
                        AnnotateImageRequest.newBuilder()
                                .addFeatures(feature)
                                .setImage(img)
                                .build();

                BatchAnnotateImagesResponse response =
                        client.batchAnnotateImages(List.of(request));

                AnnotateImageResponse res = response.getResponses(0);

                if (res.hasError()) {
                    throw new RuntimeException(res.getError().getMessage());
                }

                return res.getFullTextAnnotation().getText();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error processing image", e);
        }
    }

    private GoogleCredentials getCredentials() throws IOException {
        String credentialsJson = System.getenv("GOOGLE_CREDENTIALS_JSON");

        if (credentialsJson != null && !credentialsJson.isBlank()) {
            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(
                            credentialsJson.getBytes(StandardCharsets.UTF_8)
                    )
            );
        }

        return GoogleCredentials.fromStream(
                new ClassPathResource("google-credentials.json").getInputStream()
        );
    }
}