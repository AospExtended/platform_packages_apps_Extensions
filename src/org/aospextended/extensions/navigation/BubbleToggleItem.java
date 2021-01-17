package org.aospextended.extensions.navigation;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

class BubbleToggleItem {

    private Drawable icon;
    private Drawable shape;
    private String title = "";

    private int colorActive = Color.BLUE;
    private int titleColorActive = colorActive;
    private int colorInactive = Color.BLACK;
    private int shapeColor = Integer.MIN_VALUE;

    private float titleSize;
    private float iconWidth, iconHeight;

    private int titlePadding;
    private int internalPadding;

    BubbleToggleItem() {
    }

    Drawable getIcon() {
        return icon;
    }

    void setIcon(Drawable icon) {
        this.icon = icon;
    }

    Drawable getShape() {
        return shape;
    }

    void setShape(Drawable shape) {
        this.shape = shape;
    }

    int getShapeColor() {
        return shapeColor;
    }

    void setShapeColor(int shapeColor) {
        this.shapeColor = shapeColor;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    int getTitleColorActive() {
        return titleColorActive;
    }

    void setTitleColorActive(int titleColorActive) {
        this.titleColorActive = titleColorActive;
    }

    int getColorActive() {
        return colorActive;
    }

    void setColorActive(int colorActive) {
        this.colorActive = colorActive;
    }

    int getColorInactive() {
        return colorInactive;
    }

    void setColorInactive(int colorInactive) {
        this.colorInactive = colorInactive;
    }

    float getTitleSize() {
        return titleSize;
    }

    void setTitleSize(float titleSize) {
        this.titleSize = titleSize;
    }

    float getIconWidth() {
        return iconWidth;
    }

    void setIconWidth(float iconWidth) {
        this.iconWidth = iconWidth;
    }

    float getIconHeight() {
        return iconHeight;
    }

    void setIconHeight(float iconHeight) {
        this.iconHeight = iconHeight;
    }

    int getTitlePadding() {
        return titlePadding;
    }

    void setTitlePadding(int titlePadding) {
        this.titlePadding = titlePadding;
    }

    int getInternalPadding() {
        return internalPadding;
    }

    void setInternalPadding(int internalPadding) {
        this.internalPadding = internalPadding;
    }
}
