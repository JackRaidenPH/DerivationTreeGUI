package dev.jackraidenph.dertreegui;

import javafx.scene.canvas.Canvas;

public class ResizableCanvas extends Canvas {
    @Override
    public double minHeight(double width) {
        return 0;
    }

    @Override
    public double minWidth(double height) {
        return 0;
    }

    @Override
    public double maxHeight(double width) {
        return 99999;
    }

    @Override
    public double maxWidth(double height) {
        return 99999;
    }

    @Override
    public double prefHeight(double width) {
        return 540;
    }

    @Override
    public double prefWidth(double height) {
        return 960;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double width, double height) {
        super.setWidth(width);
        super.setHeight(height);
    }
}
