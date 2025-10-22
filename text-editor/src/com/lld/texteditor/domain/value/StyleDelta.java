package com.lld.texteditor.domain.value;

import com.lld.texteditor.manager.StyleManager;

import java.util.Optional;

public record StyleDelta(Optional<String> fontName,
                         Optional<Integer> fontSize,
                         Optional<Boolean> bold,
                         Optional<Boolean> italic) {
    public Style applyTo(Style base, StyleManager styleManager) {
        String fn = fontName.orElse(base.fontName());
        int fs = fontSize.orElse(base.fontSize());
        boolean b = bold.orElse(base.bold());
        boolean i = italic.orElse(base.italic());
        return styleManager.get(fn, fs, b, i);
    }

    public static StyleDelta font(String name) {
        return new StyleDelta(Optional.of(name), Optional.empty(), Optional.empty(), Optional.empty());
    }
    public static StyleDelta bold(boolean v) {
        return new StyleDelta(Optional.empty(), Optional.empty(), Optional.of(v), Optional.empty());
    }
}

