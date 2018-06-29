package utils;

import java.io.ByteArrayInputStream;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class UiUtils {

	public static void pleaseWait(Canvas canvas, Paint color, Paint fontColor, Font font) {
		final GraphicsContext context = canvas.getGraphicsContext2D();
		context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		context.setLineWidth(3.0);
		context.setStroke(color);
		context.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
		context.setFill(fontColor);
		context.setTextAlign(TextAlignment.CENTER);
		context.setTextBaseline(VPos.CENTER);
		context.setFont(font);
		context.fillText("Please, wait...", canvas.getWidth()/2, canvas.getHeight()/2);
	}

	public static void drawBinaryImage(Canvas canvas, byte[] data, int w, int h, Paint color, boolean selected) {
		final Image resultImage = new Image(new ByteArrayInputStream(data));
		final GraphicsContext context = canvas.getGraphicsContext2D();
		context.clearRect(0, 0, w, h);
		context.drawImage(resultImage, 0, 0);
		context.setLineWidth(selected ? 9.0 : 1.5);
		context.setStroke(color);
		context.strokeRect(0, 0, w, h);
	}

	public static void clearCanvas(Canvas canvas) {
		final GraphicsContext context = canvas.getGraphicsContext2D();
		context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

}
