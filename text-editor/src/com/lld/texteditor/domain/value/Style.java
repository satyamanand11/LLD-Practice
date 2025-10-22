package com.lld.texteditor.domain.value;

public record Style(String fontName, int fontSize, boolean bold, boolean italic) {
    public Style {
        if (fontName == null || fontName.isBlank()) throw new IllegalArgumentException("fontName");
        if (fontSize <= 0) throw new IllegalArgumentException("fontSize");
    }
}
