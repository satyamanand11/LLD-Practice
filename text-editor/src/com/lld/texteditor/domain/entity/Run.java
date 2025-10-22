package com.lld.texteditor.domain.entity;

import com.lld.texteditor.domain.value.Style;

import java.util.Objects;

public final class Run {

    public final StringBuilder text;
    private final Style style;

    public Run(String text, Style style) {
        this.text = new StringBuilder(Objects.requireNonNull(text, "text"));
        this.style = Objects.requireNonNull(style, "style");
    }

    public int length() {
        return text.length();
    }


    public boolean isEmpty() {
        return text.length() == 0;
    }


    public Style style() {
        return style;
    }


    public String text() {
        return text.toString();
    }


    public char charAt(int index) {
        return text.charAt(index);
    }


    public void append(String more) {
        if (more == null || more.isEmpty()) return;
        text.append(more);
    }

    public boolean coalesceWith(Run other) {
        if (other == null) return false;
        if (this.style != other.style) return false; // identity equality: flyweight instance
        if (!other.isEmpty()) this.text.append(other.text);
        return true;
    }

    public Split splitAt(int offset) {
        if (offset < 0 || offset > length()) {
            throw new IndexOutOfBoundsException("splitAt offset=" + offset + " length=" + length());
        }
        String left = text.substring(0, offset);
        String right = text.substring(offset);
        return new Split(new Run(left, style), new Run(right, style));
    }

    public Run snapshot() {
        return new Run(this.text.toString(), this.style);
    }

    @Override
    public String toString() {
        return "Run{" + '"' + text + '"' + ", style=" + style + '}';
    }

    public static final class Split {
        public final Run left;
        public final Run right;

        private Split(Run left, Run right) {
            this.left = left;
            this.right = right;
        }
    }
}