package com.lax.sme_manager.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ChequeImageGenerator {

    public static void main(String[] args) {
        int width = 2020; // High res
        int height = 920;
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Background (Light Blue/Teal with wave pattern)
        g2d.setColor(new Color(240, 253, 250)); // Very light teal
        g2d.fillRect(0, 0, width, height);
        
        // 2. Wave Pattern (Simple sine waves)
        g2d.setColor(new Color(204, 251, 241)); // Slightly darker teal
        g2d.setStroke(new BasicStroke(2));
        for (int y = 0; y < height; y += 40) {
            drawWave(g2d, y, width);
        }

        // 3. Border
        g2d.setColor(new Color(15, 118, 110)); // Teal 700
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(20, 20, width - 40, height - 40);

        // 4. Bank Name Placeholder (Top Left)
        g2d.setFont(new Font("Serif", Font.BOLD, 48));
        g2d.setColor(new Color(15, 118, 110));
        g2d.drawString("Standard Bank of India", 60, 100);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g2d.drawString("Any Branch, Any City - 110001", 60, 140);
        g2d.drawString("IFSC: SBIN0001234", 60, 170);

        // 5. CTS-2010 Watermark
        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2d.setColor(new Color(200, 200, 200));
        g2d.rotate(Math.toRadians(-15), 300, 500);
        g2d.drawString("CTS-2010", 100, 600);
        g2d.rotate(Math.toRadians(15), 300, 500); // Reset rotation

        // 6. Date Box (Top Right)
        int dateX = 1580;
        int dateY = 50;
        g2d.setColor(new Color(15, 118, 110));
        g2d.setStroke(new BasicStroke(2));
        for (int i = 0; i < 8; i++) {
            g2d.drawRect(dateX + (i * 45), dateY, 40, 50);
        }
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g2d.drawString("D   D   M   M   Y   Y   Y   Y", dateX + 10, dateY - 10);

        // 7. Payee Line
        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
        g2d.drawString("Pay", 60, 300);
        g2d.drawLine(140, 300, 1900, 300);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g2d.drawString("Or Bearer", 1800, 260);

        // 8. Amount in Words Line
        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
        g2d.drawString("Rupees", 60, 400);
        g2d.drawLine(200, 400, 1300, 400); // Line 1
        g2d.drawLine(60, 500, 1300, 500);  // Line 2

        // 9. Amount Box (Right)
        g2d.setColor(new Color(13, 148, 136)); // Teal 600
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(1450, 420, 500, 100);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 48));
        g2d.setColor(new Color(15, 118, 110));
        g2d.drawString("₹", 1470, 490);

        // 10. Account Number Box
        g2d.setColor(new Color(15, 118, 110));
        g2d.drawRect(300, 550, 600, 70);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2d.drawString("A/c No.", 180, 595);

        // 11. Signature Area
        g2d.drawString("Please sign above", 1600, 750);

        g2d.dispose();

        try {
            File outputfile = new File("src/main/resources/images/standard_cheque.png");
            outputfile.getParentFile().mkdirs();
            ImageIO.write(image, "png", outputfile);
            System.out.println("Cheque image generated successfully: " + outputfile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void drawWave(Graphics2D g2d, int yOffset, int width) {
        Polygon p = new Polygon();
        p.addPoint(0, yOffset);
        for (int x = 0; x <= width; x += 50) {
            p.addPoint(x, yOffset + (x % 100 == 0 ? 10 : -10));
        }
        p.addPoint(width, yOffset);
        g2d.drawPolyline(p.xpoints, p.ypoints, p.npoints);
    }
}
