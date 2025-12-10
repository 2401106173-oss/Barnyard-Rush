package com.utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MapMenu {

    // --- MAP MENU ANIMATION ---
    private BufferedImage[] mapAnimFrames;
    private int mapAnimIndex = 0;
    private javax.swing.Timer mapAnimTimer;
    private int mapAnimDirection = 1; // 1 = forward, -1 = backward

    // --- Back button ---
    private Rectangle backButton;
    private BufferedImage backImage;
    private double backScale = 0.2;
    private int backX = 10, backY = 25;

    // --- Lock overlay ---
    private BufferedImage lockImage;

    // --- Map buttons ---
    private MapButton[] maps;

    public MapMenu() {
        try {
            // Back button
            backImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/back_button.png"));

            // Lock overlay
            lockImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/map_lock_ui.png"));

            // Map buttons (normal and hover images)
            maps = new MapButton[4];
            maps[0] = new MapButton("/com/utils/assets/barn_normal.png", "/com/utils/assets/barn_hover.png", true);
            maps[1] = new MapButton("/com/utils/assets/seaside_normal.png", "/com/utils/assets/seaside_hover.png", false);
            maps[2] = new MapButton("/com/utils/assets/forest_normal.png", "/com/utils/assets/forest_hover.png", false);
            maps[3] = new MapButton("/com/utils/assets/mountain_normal.png", "/com/utils/assets/mountain_hover.png", false);

            // --- Load animation frames ---
            mapAnimFrames = new BufferedImage[79];
            for (int i = 1; i <= 79; i++) {
                String num = String.format("%03d", i);
                mapAnimFrames[i - 1] = ImageIO.read(getClass().getResourceAsStream(
                        "/com/utils/assets/select map animation/ezgif-frame-" + num + ".png"));
            }

            // --- Start animation timer (ping-pong) ---
            mapAnimTimer = new javax.swing.Timer(70, e -> {
                mapAnimIndex += mapAnimDirection;
                if (mapAnimIndex >= mapAnimFrames.length - 1) {
                    mapAnimIndex = mapAnimFrames.length - 1;
                    mapAnimDirection = -1;
                } else if (mapAnimIndex <= 0) {
                    mapAnimIndex = 0;
                    mapAnimDirection = 1;
                }
            });
            mapAnimTimer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Back button rectangle
        backButton = new Rectangle(backX, backY, (int)(backImage.getWidth()*backScale), (int)(backImage.getHeight()*backScale));

        // --- Default positions and scale for maps ---
        updateButtonPositions();
    }

    public void unlockNextMap() {
        for (MapButton map : maps) {
            if (!map.isUnlocked()) {
                map.setUnlocked(true);
                break;
            }
        }
    }

    public void draw(Graphics g, int width, int height) {
        // Draw full-screen animation
        if (mapAnimFrames != null && mapAnimFrames[mapAnimIndex] != null) {
            g.drawImage(mapAnimFrames[mapAnimIndex], 0, 0, width, height, null);
        }

        // Draw maps
        for (MapButton map : maps) {
            map.draw(g, lockImage);
        }

        // Draw back button
        g.drawImage(backImage, backX, backY, (int)(backImage.getWidth()*backScale), (int)(backImage.getHeight()*backScale), null);
    }

    //Default positions and scale for maps
    public void updateButtonPositions() {
        // Layout array: {x, y, scale}
        double[][] layout = {
        	
            { 254, 120, 0.5 },	// Map 1 - top-left
            { 600, 120, 0.5 },   // Map 2 - top-right
            { 254, 360, 0.5 },  // Map 3 - bottom-left
            { 600, 362, 0.5 }   // Map 4 - bottom-right
        };

        for (int i = 0; i < maps.length; i++) {
            maps[i].setPosition((int) layout[i][0], (int) layout[i][1]);
            maps[i].setScale(layout[i][2]);
        }
    }

    public void mouseMoved(MouseEvent e) {
        int mx = e.getX(), my = e.getY();
        for (MapButton map : maps) {
            map.setHover(map.contains(mx, my));
        }
    }

    public String mousePressed(MouseEvent e) {
        int mx = e.getX(), my = e.getY();

        if (backButton.contains(mx, my)) return "BACK";

        for (int i = 0; i < maps.length; i++) {
            if (maps[i].contains(mx, my) && maps[i].isUnlocked()) return "MAP" + (i+1);
        }
        return "";
    }

    // Unlock maps externally
    public void unlockMap2() { maps[1].setUnlocked(true); }
    public void unlockMap3() { maps[2].setUnlocked(true); }
    public void unlockMap4() { maps[3].setUnlocked(true); }

    // --- Inner class for map button ---
    private static class MapButton {
        private BufferedImage image;
        private BufferedImage hoverImage;
        private int x, y;
        private double scale = 1.0;
        private boolean unlocked;
        private boolean hover = false;

        public MapButton(String imgPath, String hoverPath, boolean unlocked) throws IOException {
            this.image = ImageIO.read(getClass().getResourceAsStream(imgPath));
            this.hoverImage = ImageIO.read(getClass().getResourceAsStream(hoverPath));
            this.unlocked = unlocked;
        }

        public void setPosition(int x, int y) { this.x = x; this.y = y; }
        public void setScale(double scale) { this.scale = scale; }
        public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
        public boolean isUnlocked() { return unlocked; }
        public void setHover(boolean hover) { this.hover = hover; }

        public boolean contains(int mx, int my) {
            int w = (int)(image.getWidth() * scale);
            int h = (int)(image.getHeight() * scale);
            return mx >= x && mx <= x + w && my >= y && my <= y + h;
        }

        public void draw(Graphics g, BufferedImage lockImage) {
            int w = (int)(image.getWidth() * scale);
            int h = (int)(image.getHeight() * scale);

            if (unlocked) {

                g.drawImage(hover && hoverImage != null ? hoverImage : image, x, y, w, h, null);
            } else {
                g.drawImage(image, x, y, w, h, null);
                if (lockImage != null) g.drawImage(lockImage, x, y, w, h, null);
            }
        }
    }
}
