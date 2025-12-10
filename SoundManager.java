package com.utils;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the loading and playback of various sound effects for the game.
 * Includes volume control for individual effects using Decibels (dB).
 */

public class SoundManager {

    public enum SoundEffect {
        CLICK, 
        ERROR, 
        HOVER, 
        TYPING,
        MAIN    
    }

    private final Map<SoundEffect, Clip> soundClips = new HashMap<>();

    // *** MODIFICATION 1: Map for individual sound volumes (in dB) ***
    // 0.0f is original volume. Positive is louder, negative is quieter.
    // Example: CLICK and TYPING are +5.0dB louder, MAIN is -3.0dB quieter.
    private static final Map<SoundEffect, Float> VOLUME_ADJUSTMENTS = Map.of(
        SoundEffect.CLICK, 10.0f,    // Louder
        SoundEffect.ERROR, 10.0f,    // Default volume
        SoundEffect.HOVER, 5.0f,   // Quieter
        SoundEffect.TYPING, 10.0f,   // Louder
        SoundEffect.MAIN, 3.0f     // Background music often needs to be quieter
    );
    // ***************************************************************

    public SoundManager() {
        System.out.println("Initializing SoundManager...");
        
       //Sound file path
        loadSound(SoundEffect.CLICK, "/com/utils/assets/sound/click.wav");			//Hashmap
        loadSound(SoundEffect.ERROR, "/com/utils/assets/sound/error.wav");
        loadSound(SoundEffect.HOVER, "/com/utils/assets/sound/hover.wav");
        loadSound(SoundEffect.TYPING, "/com/utils/assets/sound/typing.wav");
        loadSound(SoundEffect.MAIN, "/com/utils/assets/sound/main-theme.wav"); 
        
        System.out.println("Sound effects loaded.");
    }

    private void loadSound(SoundEffect effect, String filePath) {
        try (InputStream audioSrc = getClass().getResourceAsStream(filePath)) {
            if (audioSrc == null) {
                System.err.println("Sound file not found: " + filePath);
                return;
            }
            
            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(audioSrc))) {
                
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                
                // *** MODIFICATION 2: Use the specific volume from the map ***
                Float adjustment = VOLUME_ADJUSTMENTS.getOrDefault(effect, 0.0f);
                adjustGain(clip, adjustment); 
                // ***********************************************************
                
                soundClips.put(effect, clip);
            }	
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Adjusts the gain (volume) of a Clip by a specified dB amount.
     * @param clip The Clip to modify.
     * @param dB The adjustment in Decibels. Positive is louder, negative is quieter.
     */
    private void adjustGain(Clip clip, float dB) {
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                // Calculate new gain (dB) and clamp it within the control's limits
                float newGain = gainControl.getValue() + dB;
                
                // Ensure the new gain doesn't exceed the sound card's min/max limits
                newGain = Math.min(newGain, gainControl.getMaximum());
                newGain = Math.max(newGain, gainControl.getMinimum());
                
                gainControl.setValue(newGain);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Gain control not supported or value out of bounds: " + e.getMessage());
        }
    }
    
    public void playSound(SoundEffect effect) {
        Clip clip = soundClips.get(effect);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0); 
            clip.start();
        } 
    }

 // Inside SoundManager.java -> startMainLoop()

    public void startMainLoop() {
        Clip clip = soundClips.get(SoundEffect.MAIN);
        if (clip != null) {
            if (!clip.isRunning()) { // CRITICAL CHECK
                clip.setFramePosition(0); 
                clip.loop(Clip.LOOP_CONTINUOUSLY); 
                clip.start();
            }
        }
    }

    public void stopMainLoop() {
        Clip clip = soundClips.get(SoundEffect.MAIN);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }
    
    public void cleanup() {
        System.out.println("Cleaning up SoundManager...");
        for (Clip clip : soundClips.values()) {
            if (clip != null) {
                clip.close();
            }
        }
        soundClips.clear();
    }
}