package com.spring.schoolmate.service;

import com.google.api.client.util.Value;
import com.google.cloud.translate.v3.TranslationServiceClient;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.LocationName;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

  @Value("${gcp.project-id}")
  private String projectId;

  public String translate(String text, String targetLanguage) {
    if (text == null || text.isBlank()) {
      return "";
    }

    try (TranslationServiceClient client = TranslationServiceClient.create()) {
      LocationName parent = LocationName.of(projectId, "global");

      TranslateTextRequest request = TranslateTextRequest.newBuilder()
        .setParent(parent.toString())
        .setMimeType("text/plain") // text/html도 가능
        .setTargetLanguageCode(targetLanguage)
        .addContents(text)
        .build();

      TranslateTextResponse response = client.translateText(request);

      if (!response.getTranslationsList().isEmpty()) {
        return response.getTranslationsList().get(0).getTranslatedText();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return text;
  }
}
