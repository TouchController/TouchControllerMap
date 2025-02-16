import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class Cutter {
    private static boolean readPixel(BufferedImage image, int x, int y) {
        var argb = image.getRGB(x, y);
        var alpha = argb >> 24;
        var color = argb & 0xFFFFFF;
        if (alpha == 0) {
            return false;
        }
        if (color == 0) {
            return true;
        } else {
            throw new IllegalStateException("Bad color in non-transparent pixel [" + x + ", " + y + "]: " + color);
        }
    }

    private static void writeFile(BufferedImage image, File outputDir, String name, int x, int y, int w, int h) throws IOException {
        if (w <= 0 || h <= 0) {
            return;
        }
        var leftTop = image.getSubimage(x, y, w, h);
        ImageIO.write(leftTop, "png", new File(outputDir, name));
    }

    public static void cut(File file, Logger logger) throws IOException {
        var image = ImageIO.read(file);
        if (image == null) {
            logger.severe("Bad file: " + file);
            return;
        }
        logger.info("Process image: " + file);

        var leftStart = 0;
        while (!readPixel(image, 0, leftStart)) {
            leftStart++;
        }

        var leftLength = 0;
        while (readPixel(image, 0, leftStart + leftLength)) {
            leftLength++;
        }

        var topStart = 0;
        while (!readPixel(image, topStart, 0)) {
            topStart++;
        }

        var topLength = 0;
        while (readPixel(image, topStart + topLength, 0)) {
            topLength++;
        }

        var outputDir = new File(file + "_cutted");
        outputDir.mkdir();

        writeFile(image, outputDir, "left_top.png", 1, 1, topStart - 1, leftStart - 1);

        writeFile(image, outputDir, "center_top.png", topStart, 1, topLength, leftStart - 1);
        writeFile(image, outputDir, "right_top.png", topStart + topLength, 1, image.getWidth() - topStart - topLength - 1, leftStart - 1);
        writeFile(image, outputDir, "left_center.png", 1, leftStart, topStart - 1, leftLength);
        writeFile(image, outputDir, "center_center.png", topStart, leftStart, topLength, leftLength);
        writeFile(image, outputDir, "right_center.png", topStart + topLength, leftStart, image.getWidth() - topStart - topLength - 1, leftLength);
        writeFile(image, outputDir, "left_bottom.png", 1, leftStart + leftLength, topStart - 1, image.getHeight() - leftStart - leftLength - 1);
        writeFile(image, outputDir, "center_bottom.png", topStart, leftStart + leftLength, topLength, image.getHeight() - leftStart - leftLength - 1);
        writeFile(image, outputDir, "right_bottom.png", topStart + topLength, leftStart + leftLength, image.getWidth() - topStart - topLength - 1, image.getHeight() - leftStart - leftLength - 1);
    }

    public static void main(String[] args) throws IOException {
        for (var fileName : args) {
            cut(new File(fileName), Logger.getGlobal());
        }
    }
}
