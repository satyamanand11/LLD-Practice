package com.lld.texteditor.manager;

import com.lld.texteditor.domain.value.Style;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryStyleManager implements StyleManager {
    private final Map<Style, Style> pool = new ConcurrentHashMap<>();

    @Override
    public Style get(String font, int size, boolean bold, boolean italic) {
        var candidate = new Style(font, size, bold, italic);
        return pool.computeIfAbsent(candidate, _ -> candidate);
    }
}
