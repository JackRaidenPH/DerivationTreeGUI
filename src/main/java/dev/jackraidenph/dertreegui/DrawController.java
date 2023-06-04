package dev.jackraidenph.dertreegui;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcType;
import javafx.scene.text.TextAlignment;

public class DrawController {

    private Controller parent;
    private Node root;
    @FXML
    private ResizableCanvas canvas;
    private double
            dragX = 0,
            dragY = 0,
            deltaX = 0,
            deltaY = 0,
            mouseX = 0,
            mouseY = 0,
            scaleX = 1,
            scaleY = 1,
            translateX = 0,
            translateY = 0,
            prevDragX = 0,
            prevDragY = 0;

    public void setParent(Controller parent) {
        this.parent = parent;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    private static final int NODE_WIDTH = 150;
    private static final int NODE_HEIGHT = 75;

    private static void drawNode(GraphicsContext gc, Node node, double x, double y) {
        gc.setStroke(Color.BLACK);
        gc.strokeRect(x, y, NODE_WIDTH, NODE_HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(node.contents, x + NODE_WIDTH / 2., y + NODE_HEIGHT / 2.);
    }

    private static void cleanCanvas(GraphicsContext gc) {
        gc.save();
        gc.setTransform(1, 0, 0, 1, 0, 0);
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.restore();
    }

    private static final int NODE_VERTICAL_SPACING = 75;
    private static final int NODE_HORIZONTAL_SPACING = 75;

    private static void drawTree(GraphicsContext gc, Node node, double x, double y) {
        drawNode(gc, node, x, y);
        int children = node.children.size();
        for (int i = 0; i < children; i++) {
            Node child = node.children.get(i);
            double x1 = x + (i) * (NODE_HORIZONTAL_SPACING + NODE_WIDTH);
            double y1 = y + NODE_VERTICAL_SPACING + NODE_HEIGHT;
            drawArrow(gc, x + NODE_WIDTH / 2., y + NODE_HEIGHT, x1 + NODE_WIDTH / 2., y1, Color.BLACK);
            drawTree(gc, child, x1, y1);
        }
    }

    private static void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, Paint color) {
        gc.save();

        gc.setStroke(color);

        double slope = (startY - endY) / (startX - endX);
        double lineAngle = Math.atan(slope);

        double arrowAngle = 0;
        if (startX > endX)
            arrowAngle = Math.toRadians(45);
        else if (startX < endX)
            arrowAngle = Math.toRadians(135);
        else if (startY > endY)
            arrowAngle = Math.toRadians(45);
        else if (startY < endY)
            arrowAngle = -Math.toRadians(45);

        gc.strokeLine(startX, startY, endX, endY);

        double lineLength = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
        double arrowLength = 9;

        gc.strokeLine(
                endX,
                endY,
                endX + arrowLength * Math.cos(lineAngle - arrowAngle),
                endY + arrowLength * Math.sin(lineAngle - arrowAngle));
        gc.strokeLine(
                endX,
                endY,
                endX + arrowLength * Math.cos(lineAngle + arrowAngle),
                endY + arrowLength * Math.sin(lineAngle + arrowAngle));

        gc.restore();
    }

    private static void drawArrow(GraphicsContext gc, double startX, double startY, double endX, double endY, String text, Paint color) {
        gc.save();

        gc.save();
        gc.setTextBaseline(VPos.BOTTOM);
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setFill(Color.BLUE);
        gc.fillText(text, (startX + endX) / 2., (startY + endY) / 2.);
        gc.restore();

        drawArrow(gc,
                startX,
                startY,
                endX,
                endY,
                color);
        gc.restore();
    }

    @FXML
    public void onDrag(MouseEvent event) {
        dragX = event.getX();
        dragY = event.getY();
    }

    @FXML
    public void onDragStop(MouseEvent event) {
        prevDragX = translateX;
        prevDragY = translateY;
    }

    @FXML
    public void onMouseDrag(MouseEvent event) {
        deltaX = (event.getX() - dragX + prevDragX);
        deltaY = (event.getY() - dragY + prevDragY);

        canvas.getGraphicsContext2D().translate(
                deltaX - translateX,
                deltaY - translateY);

        translateX = deltaX;
        translateY = deltaY;

        updateCanvas(canvas.getGraphicsContext2D());
    }

    @FXML
    public void onMouseMove(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
    }

    @FXML
    public void onScroll(ScrollEvent event) {
        double scale = event.getDeltaY() / 3000;

        scaleX += scale;
        scaleY += scale;

        canvas.getGraphicsContext2D().scale(scaleX, scaleY);
        canvas.getGraphicsContext2D().translate(-(mouseX - canvas.getWidth() / 2.) / 100., -(mouseY - canvas.getHeight() / 2.) / 100.);

        scaleX = 1;
        scaleY = 1;

        updateCanvas(canvas.getGraphicsContext2D());
    }

    private void updateCanvas(GraphicsContext gc) {
        cleanCanvas(gc);
        double center = (canvas.getWidth() - NODE_WIDTH) / 2.;
        double yOffset = 50;
        drawTree(gc, this.root, center, yOffset);
    }

    public void draw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        updateCanvas(gc);

        ChangeListener<Number> cleanAndDraw = (observable, oldValue, newValue) -> {
            updateCanvas(gc);
        };

        canvas.widthProperty().addListener(cleanAndDraw);
        canvas.heightProperty().addListener(cleanAndDraw);
    }
}
