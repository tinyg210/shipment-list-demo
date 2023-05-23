package dev.ancaghenade.shipmentpicturelambdavalidator;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Watermark {

  static byte[] watermarkImage(byte[] objectData, String extension)
      throws IOException {
    var originalImage = ImageIO.read(new ByteArrayInputStream(objectData));
    var watermarkedImage = new BufferedImage(originalImage.getWidth(),
        originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
    var g2d = (Graphics2D) watermarkedImage.getGraphics();
    g2d.drawImage(originalImage, 0, 0, null);
    var font = new Font("Arial", Font.BOLD, 40);
    var color = Color.WHITE;

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

    g2d.setFont(font);
    g2d.setColor(color);
    g2d.rotate(Math.toRadians(45), 50, 50);
    g2d.drawString("Banana for scale approved!", 50, 50);

    var baos = new ByteArrayOutputStream();
    ImageIO.write(watermarkedImage, extension, baos);
    baos.close();
    return baos.toByteArray();

  }
}
