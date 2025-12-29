package com.lld.cricbuzz.decorator;

import java.util.HashMap;
import java.util.Map;

/**
 * Decorator that adds translations to commentary
 * Enhances commentary with multilingual support
 */
public class TranslationDecorator extends CommentaryDecorator {
    private final String targetLanguage;
    private final Map<String, String> translations;

    public TranslationDecorator(CommentaryComponent wrapped, String targetLanguage) {
        super(wrapped);
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            throw new IllegalArgumentException("Target language cannot be null or empty");
        }
        this.targetLanguage = targetLanguage;
        this.translations = new HashMap<>();
        translate();
    }

    @Override
    public String getEnhancedText() {
        // Return translated text if available, otherwise return original
        String translated = translations.getOrDefault("text", wrapped.getEnhancedText());
        return translated + " [" + targetLanguage + "]";
    }

    @Override
    public Map<String, Object> getMetadata() {
        Map<String, Object> metadata = new HashMap<>(wrapped.getMetadata());
        metadata.put("translation", Map.of(
            "language", targetLanguage,
            "translatedText", translations.getOrDefault("text", wrapped.getEnhancedText())
        ));
        return metadata;
    }

    /**
     * Translate the commentary text
     * In a real system, this would call a translation service
     */
    private void translate() {
        String originalText = wrapped.getEnhancedText();
        // Simulated translation - in production, use a translation service
        String translated = simulateTranslation(originalText, targetLanguage);
        translations.put("text", translated);
    }

    /**
     * Simulated translation logic
     * In production, replace with actual translation service (Google Translate, etc.)
     */
    private String simulateTranslation(String text, String language) {
        // Simple simulation - in production, use actual translation API
        switch (language.toLowerCase()) {
            case "hindi":
                return "[Hindi] " + text;
            case "spanish":
                return "[Spanish] " + text;
            case "french":
                return "[French] " + text;
            default:
                return text; // Return original if language not supported
        }
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }
}

