package com.lld.texteditor.manager;

import com.lld.texteditor.domain.value.Style;

public interface StyleManager {
    Style get(String font, int size, boolean bold, boolean italic);
}
