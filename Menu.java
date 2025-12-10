package com.utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Scanner;
import java.awt.AlphaComposite;

public class Menu {

    private BufferedImage background;
    private AnimatedUI animatedBackground;

    // Button images
    private BufferedImage enterNormal, enterHover;
    private BufferedImage exitNormal, exitHover;

    private Rectangle enterButton, exitButton;
    private boolean hoverEnter = false, hoverExit = false;

    
    private Runnable repaintCallback; // Callback for repaints


    public Menu(Runnable repaintCallback) {
        this.repaintCallback = repaintCallback;
        try {
        
        	enterNormal = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/enterbutton1.png"));
            enterHover  = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/enterbutton2.png"));

            exitNormal = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/exitbutton1.png"));
            exitHover  = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/exitbutton2.png"));
            

            animatedBackground = new AnimatedUI(79, this::requestRepaint);
            
        } catch (IOException e) {
            e.printStackTrace();
        }

      
    }


    public Menu() {
        this(null); 
    }

    // Helper method to request repaint through callback
    private void requestRepaint() {
        if (repaintCallback != null) {
            repaintCallback.run();
        }
    }

    public void draw(Graphics g, int panelWidth, int panelHeight) {
        updateButtonPositions(panelWidth, panelHeight);

        // Draw animated background first
        animatedBackground.draw(g, panelWidth, panelHeight);
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.drawImage(background, 0, 0, panelWidth, panelHeight, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Draw ENTER button
        BufferedImage enterImg = hoverEnter ? enterHover : enterNormal;
        g.drawImage(enterImg, enterButton.x, enterButton.y, enterButton.width, enterButton.height, null);

        // Draw EXIT button
        BufferedImage exitImg = hoverExit ? exitHover : exitNormal;
        g.drawImage(exitImg, exitButton.x, exitButton.y, exitButton.width, exitButton.height, null);
        

    }

    public void updateButtonPositions(int panelWidth, int panelHeight) {
        double baseW = 320.0;
        double baseH = 180.0;

        double scaleX = panelWidth / baseW;
        double scaleY = panelHeight / baseH;

        int enterX = 128, enterY = 108, enterW = 69, enterH = 26;
        int exitX  = 128, exitY  = 137, exitW  = 68, exitH  = 28;

        enterButton = new Rectangle(
            (int)(enterX * scaleX),
            (int)(enterY * scaleY),
            (int)(enterW * scaleX),
            (int)(enterH * scaleY)
        );

        exitButton = new Rectangle(
            (int)(exitX * scaleX),
            (int)(exitY * scaleY),
            (int)(exitW * scaleX),
            (int)(exitH * scaleY)
        );
    }

    public void mouseMoved(MouseEvent e) {
        int mx = e.getX(), my = e.getY();
        boolean oldHoverEnter = hoverEnter;
        boolean oldHoverExit = hoverExit;
        
        hoverEnter = enterButton.contains(mx, my);
        hoverExit  = exitButton.contains(mx, my);
        
        // Only repaint if hover state changed
        if (oldHoverEnter != hoverEnter || oldHoverExit != hoverExit) {
            requestRepaint(); // Use helper method
        }
    }

    public String mousePressed(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        if (enterButton.contains(mx, my)) return "ENTER";
        if (exitButton.contains(mx, my))  return "EXIT";
        return "";
    }
}