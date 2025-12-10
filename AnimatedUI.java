package com.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class AnimatedUI {
    private List<BufferedImage> frames;
    private int currentFrame = 0;
    private Timer animationTimer;
    private int fps = 15;
    private boolean isPlaying = true;
    private Runnable repaintCallback; // Changed to callback pattern
    private boolean forwardDirection = true; //ping-pong

    // Updated constructor to accept callback
    public AnimatedUI(int frameCount, Runnable repaintCallback) {
        this.repaintCallback = repaintCallback;
        frames = new ArrayList<>();
        loadFrames(frameCount);
        startAnimation();
    }

    private void loadFrames(int frameCount) {
        String folderPath = "com/utils/assets/mainmenu anmation/";
        System.out.println("Loading frames from: " + folderPath);
        
        for (int i = 1; i <= frameCount; i++) {
            try {
                String frameNumber = String.format("%03d", i);
                String filename = "ezgif-frame-" + frameNumber + ".png";
                String path = folderPath + filename;
                
                System.out.println("Trying to load: " + path);
                
                BufferedImage frame = ImageIO.read(getClass().getClassLoader().getResourceAsStream(path));
                
                if (frame != null) {
                    frames.add(frame);
                    System.out.println("Successfully loaded " + filename);
                } else {
                    System.err.println(filename + " could not be loaded");
                }
            } catch (IOException e) {
                System.err.println("IO Exception loading frame: " + e.getMessage());
            }
        }
        
        System.out.println("Total frames loaded: " + frames.size());
    }

    private void startAnimation() {
        int delay = 1000 / fps;
        animationTimer = new Timer(delay, e -> {
            if (isPlaying && frames != null && !frames.isEmpty()) {
                // Ping-pong animation logic
                if (forwardDirection) {
                    currentFrame++;
                    if (currentFrame >= frames.size() - 1) {
                        forwardDirection = false; // Change direction at the end
                    }
                } else {
                    currentFrame--;
                    if (currentFrame <= 0) {
                        forwardDirection = true; // Change direction at the beginning
                    }
                }
                
                if (repaintCallback != null) {
                    repaintCallback.run();
                }
            }
        });
        animationTimer.start();
    }

    public void draw(Graphics g, int panelWidth, int panelHeight) {
        if (frames != null && !frames.isEmpty()) {
            BufferedImage currentFrameImage = frames.get(currentFrame);
            g.drawImage(currentFrameImage, 0, 0, panelWidth, panelHeight, null);
        }
    }
}