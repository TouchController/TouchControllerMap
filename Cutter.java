import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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

    public static void main(String[] args) throws IOException {
        for (var fileName : args) {
            var file = new File(fileName);
            var image = ImageIO.read(file);
            if (image == null) {
                System.out.println("Bad file: " + fileName);
                continue;
            }
            System.out.println("Process image: " + fileName);

            var leftStart = 0;
            while (true) {
                var color = image.getRGB(0, leftStart) & 0xFFFFFF;
                if (readPixel(image, 0, leftStart)) {
                    break;
                }
                leftStart++;
            }

            var leftLength = 0;
            while (true) {
                if (!readPixel(image, 0, leftStart + leftLength)) {
                    break;
                }
                leftLength++;
            }

            var topStart = 0;
            while (true) {
                if (readPixel(image, topStart, 0)) {
                    break;
                }
                topStart++;
            }

            var topLength = 0;
            while (true) {
                if (!readPixel(image, topStart + topLength, 0)) {
                    break;
                }
                topLength++;
            }

            var outputDir = new File(fileName + "_cutted");
            outputDir.mkdir();

            var leftTop = image.getSubimage(1, 1, topStart - 1, leftStart - 1);
            ImageIO.write(leftTop, "png", new File(outputDir, "left_top.png"));
            var centerTop = image.getSubimage(topStart, 1, topLength, leftStart - 1);
            ImageIO.write(centerTop, "png", new File(outputDir, "center_top.png"));
            var rightTop = image.getSubimage(topStart + topLength, 1, image.getWidth() - topStart - topLength - 1, leftStart - 1);
            ImageIO.write(rightTop, "png", new File(outputDir, "right_top.png"));
            var leftCenter = image.getSubimage(1, leftStart, topStart - 1, leftLength);
            ImageIO.write(leftCenter, "png", new File(outputDir, "left_center.png"));
            var centerCenter = image.getSubimage(topStart, leftStart, topLength, leftLength);
            ImageIO.write(centerCenter, "png", new File(outputDir, "center_center.png"));
            var rightCenter = image.getSubimage(topStart + topLength, leftStart, image.getWidth() - topStart - topLength - 1, leftLength);
            ImageIO.write(rightCenter, "png", new File(outputDir, "right_center.png"));
            var leftBottom = image.getSubimage(1, leftStart + leftLength, topStart - 1, image.getHeight() - leftStart - leftLength - 1);
            ImageIO.write(leftBottom, "png", new File(outputDir, "left_bottom.png"));
            var centerBottom = image.getSubimage(topStart, leftStart + leftLength, topLength, image.getHeight() - leftStart - leftLength - 1);
            ImageIO.write(centerBottom, "png", new File(outputDir, "center_bottom.png"));
            var rightBottom = image.getSubimage(topStart + topLength, leftStart + leftLength, image.getWidth() - topStart - topLength - 1, image.getHeight() - leftStart - leftLength - 1);
            ImageIO.write(rightBottom, "png", new File(outputDir, "right_bottom.png"));
        }
    }
}
