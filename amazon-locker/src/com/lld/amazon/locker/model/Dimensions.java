package com.lld.amazon.locker.model;

public record Dimensions(int width, int height, int depth) {
    public boolean fitsInside(Dimensions container) {
        return width <= container.width() && height <= container.height() && depth <= container.depth();
    }
}
