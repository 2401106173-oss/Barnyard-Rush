package com.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class GamePanel extends JPanel implements ActionListener, MouseListener, KeyListener {	//Inheritance / Polymorphism
    private Timer timer = new Timer(5, this);
    
    private GameState state = GameState.MENU;
    
    
    // --- Font ---
    private Font customFont;
    
    // --- Player Name System ---
    private String playerName = "";
    private BufferedImage enterNameImage;
    private Rectangle confirmButton, backButton;
    private boolean hoverConfirm = false, hoverBack = false;
    
    // --- Timer ---
    private int timeLeft = 60; // seconds		//Encapsulation, security and control
    private int mapStartTime = 60;				
    private Timer gameTimer;

    // --- Scoring ---
    private int score = 0;

    private Menu menu;	//Menu
    private MapMenu mapMenu; // Map Menu
 
    private BufferedImage background;	//Background Sprite

    // --- Typing Gameplay ---
    private float cowX = 50f;	
    private int cowY = 330;
    private int step = 50; 		// Cow movement per correct word
    private int progress = 0;

    private String currentWord = "";
    private String typedWord = "";
    private String[] currentWordList;
    
    // --- Pause Menu ---
    private boolean isPaused = false;
    private BufferedImage pauseImage; 
    private Rectangle resumeButton, restartButton, exitButton;
    private boolean hoverResume = false, hoverRestart = false, hoverExit = false;
    
    // --- Pause Button UI ---
    private Rectangle pauseButton;
    private BufferedImage pauseButtonImage;
    
    // --- You Win UI
    private BufferedImage winImage; 
    private Rectangle continueButton;
    private boolean hoverContinue = false;
    private boolean isWinScreen = false;
    
    // --- You Win Continue Button Images ---
    private BufferedImage continueButtonImage;      
    private BufferedImage continueButtonHoverImage; 

    
    // --- Game Over UI
    private BufferedImage gameOverImage;
    private Rectangle gameOverContinueButton;
    private Rectangle exitButtonRect;   
    private boolean isGameOverScreen = false;
    private boolean hoverGameOverContinue = false;
    
    
    // --- Game Over Buttons Images ---
    private BufferedImage tryAgainButtonImage;
    private BufferedImage tryAgainButtonHoverImage;
    private BufferedImage exitButtonImage;
    private BufferedImage exitButtonHoverImage;

    
    // --- Sound Manager Initializer
    private SoundManager soundManager;
    
    // --- Border
    private Rectangle PauseButton;
    private BufferedImage PauseButtonImage;
    private BufferedImage borderImage;
    
    // --- Map animation ---
    private BufferedImage[] currentMapAnimation; // frames of the current map
    private int mapAnimFrameIndex = 0;           // current frame index
    private long lastMapAnimTime = 0;            // last update time
    private int mapAnimDelay = 50;               
    private boolean mapAnimForward = true;  	//pingpong loop

    // --- Cow Animation ---
    private BufferedImage[] cowFrames = new BufferedImage[25];
    private int cowFrameIndex = 0;
    private boolean isCowAnimating = false;
    private long lastCowAnimTime = 0;
    private int cowAnimDelay = 25; // speed (ms per frame)
    
    private float cowTargetX = 50f;   // 
    private float cowStepPerFrame = 10; // smooth pixels per frame


    // --- Transition Effect ---
    private float fadeAlpha = 0f;
    private boolean isTransitioning = false;
    
    // --- Finish Line
    private int finishLineX = 0; 
    private boolean levelFinished; // flag to freeze game
    
    // Overall score for all maps
    private int totalScore = 0;   
    
    //Auto main menu in 5 sec
    private boolean waitingToReturnToMenu = false;
    private long winTimeStamp = 0;
    private final int WIN_DELAY = 5000; // 5 seconds
    
    private int returnCountdown = 0; // counts seconds left before auto-return
    private Timer returnTimer;        // Swing timer for countdown


    
    // --- Word Lists ---
    private final String[] barnWords = {				//Array	fixed string
        "barn", "grass", "cow", "milk", "tractor", "muddy", "farmhouse", "chicken", 
        "harvest", "haystack", "ranch", "farmer", "plow", "hooves", "pasture"
    };

    private final String[] seasideWords = {
        "beach", "waves", "sand", "seagull", "shell", "boat", "pier", "tide",
        "sunset", "surf", "lifeguard", "umbrella", "bucket", "shore", "fishing"
    };

    private final String[] forestWords = {
        "tree", "mushroom", "river", "leaf", "wild", "bear", "owl", "trail",
        "pine", "fox", "cabin", "campfire", "stream", "bush", "twig"
    };

    private final String[] mountainWords = {
        "peak", "cliff", "rock", "snow", "trail", "summit", "hiker", "eagle",
        "ridge", "cave", "boulder", "valley", "forest", "altitude", "crag"
    };


    private final Random random = new Random();
    
    public GamePanel() {
    	  	
    	setCustomCursor();
    		
        timer.start();
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

                int mx = e.getX();
                int my = e.getY();

                // ---- GAME OVER HOVER ----
                if (state == GameState.PLAYING && isGameOverScreen) {
                   
                    hoverGameOverContinue = gameOverContinueButton.contains(mx, my);
                    hoverExit = exitButtonRect.contains(mx, my);  // new hover for Exit
                }


                // ---- WIN SCREEN HOVER ----
                else if (state == GameState.PLAYING && isWinScreen) {
                    hoverContinue =
                        continueButton != null &&
                        continueButton.contains(mx, my);
                }

                // ---- ENTER NAME HOVER ----
                else if (state == GameState.ENTER_NAME) {
                    hoverConfirm =
                        confirmButton != null &&
                        confirmButton.contains(mx, my);

                    hoverBack =
                        backButton != null &&
                        backButton.contains(mx, my);
                }

                // ---- PAUSE MENU HOVER ----
                else if (state == GameState.PLAYING && isPaused) {
                    hoverResume  = resumeButton  != null && resumeButton.contains(mx, my);
                    hoverRestart = restartButton != null && restartButton.contains(mx, my);
                    hoverExit    = exitButton    != null && exitButton.contains(mx, my);
                }

                // ---- MAIN MENU HOVER ----
                else if (state == GameState.MENU) {
                    menu.mouseMoved(e);
                }

                // ---- MAP SELECT HOVER ----
                else if (state == GameState.SELECT_MAP) {
                    mapMenu.mouseMoved(e);
                }

                repaint();
            }
        });


        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(this);


        menu = new Menu(this::repaint);
        mapMenu = new MapMenu();
        
        loadCowFrames();


        
        //Sound Manager
        soundManager = new SoundManager(); // Initialize SoundManager
        soundManager.startMainLoop();      // Start main background music loop
        

        try {
            
            // Load Custom Font
            customFont = Font.createFont(Font.TRUETYPE_FONT,getClass().getResourceAsStream("/com/utils/assets/pressstart2p.ttf")).deriveFont(36f);	//font
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
            
            //Enter name Image
            enterNameImage = ImageIO.read(
            	    getClass().getResourceAsStream("/com/utils/assets/entername.png"));
            
            //Pause Image
            pauseImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/pause.png"));
            
            //Pause UI Button Image
            pauseButtonImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/pausebutton.png"));
            
            //Win UI Image
            winImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/youwinui.png"));
            
            //Win UI Continue Buttons
            continueButtonImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/continuenormal.png"));
            continueButtonHoverImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/continuehover.png"));
            
            //Game Over UI Image
            gameOverImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/gameoverui.png"));
            
            //Game Over UI Buttons
            tryAgainButtonImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/tryagainnormal.png"));
            tryAgainButtonHoverImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/tryagainhover.png"));

            exitButtonImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/exitnormal.png"));
            exitButtonHoverImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/exithover.png"));
            
            // Border UI Image
            borderImage = ImageIO.read(getClass().getResourceAsStream("/com/utils/assets/border.png"));
            
      
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            customFont = new Font("Monospaced", Font.BOLD, 36);
        }
    }

    // --- GAME LOGIC ---
    private void startGame() {
        if (isTransitioning) return;
        isTransitioning = true;
        fadeAlpha = 1f;
        
        finishLineX = getWidth() - 150; 
        levelFinished = false;

        Timer fadeTimer = new Timer(30, e -> {
            fadeAlpha -= 0.05f;
            if (fadeAlpha <= 0f) {
                fadeAlpha = 0f;
                ((Timer)e.getSource()).stop();
                isTransitioning = false;

                cowX = 50;
                progress = 0;
                typedWord = "";
                nextWord();
                state = GameState.PLAYING;
                requestFocusInWindow();
                startCountdown();
            }
            repaint();
        });
        fadeTimer.start();
    }
    
    
    private void startCountdown() {
        if (gameTimer != null) gameTimer.stop();

        gameTimer = new Timer(1000, e -> {
            timeLeft--;
            if (timeLeft <= 0) {
                ((Timer)e.getSource()).stop();
                gameOver();
            }
            repaint();
        });
        gameTimer.start();
    }


    private void gameOver() {
    	 if (gameTimer != null) gameTimer.stop();

    	    isGameOverScreen = true;
    	    repaint();
    }


    private void nextWord() {
        if (currentWordList != null && currentWordList.length > 0) {
            currentWord = currentWordList[random.nextInt(currentWordList.length)];
        } else {
            currentWord = "error"; // fallback if word list is empty
        }
        typedWord = "";
        repaint();
    }


    private void moveCowForward() {
        // If the level is finished, do nothing
        if (levelFinished) return;

        // Calculate the target X position after moving one step
        cowTargetX = cowX + step;

        // Make sure the cow does not go past the finish line
        if (cowTargetX > finishLineX) {
            cowTargetX = finishLineX;
        }

        // Calculate smooth movement per frame
        cowStepPerFrame = (cowTargetX - cowX) / (float)cowFrames.length;

        // Start the cow animation
        isCowAnimating = true;

        // Increment progress
        progress++;
    }

    private void gameWin() {
        isWinScreen = true;

        if (currentWordList == mountainWords) { // final map
            returnCountdown = 5; // start from 5 seconds

            returnTimer = new Timer(1000, e -> { // fires every 1 second
                returnCountdown--;  // decrease countdown
                repaint();          // update display

                if (returnCountdown <= 0) {
                    ((Timer)e.getSource()).stop();
                    isWinScreen = false;
                    resetToMenu();
                }
            });
            returnTimer.start();
        }
        if (gameTimer != null) gameTimer.stop();

        totalScore += score;
        mapMenu.unlockNextMap();
        repaint();
    }




    private void resetGame() {
        cowX = 50;
        progress = 0;
        score = 0;  // reset score
        totalScore = 0;
        state = GameState.MENU;
        repaint();
        menu = new Menu(this::repaint); 
    }
    
    //Restart current map
    private void restartCurrentMap() {
        // reset gameplay state
        score = 0;
        cowX = 50;
        progress = 0;
        typedWord = "";

        // reset time to map's starting time
        timeLeft = mapStartTime;

        // pick a new word for the player
        nextWord();
        // ensure paused flag off
        isPaused = false;
        // restart the Swing countdown timer
        if (gameTimer != null) {
            gameTimer.stop();
        }
        startCountdown();
        repaint();
    }
    
    //Set cursor
    private void setCustomCursor() {
        try {
            BufferedImage cursorImg = ImageIO.read(
                getClass().getResource("/com/utils/assets/gamecursor.png")
            );

            int newSize = 48; // desired cursor size
            Image scaled = cursorImg.getScaledInstance(newSize, newSize, Image.SCALE_SMOOTH);
            
            // Hotspot (click point)
            Point hotspot = new Point(0, 0); 
            // Try (8,8) or (16,16) if misaligned

            Cursor customCursor = Toolkit.getDefaultToolkit()
                    .createCustomCursor(cursorImg, hotspot, "GameCursor");

            this.setCursor(customCursor);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //Clean up to avoid leaks
    public void cleanup() {
        if (timer != null) timer.stop();
        if (gameTimer != null) gameTimer.stop();
    }


    // --- DRAWING ---
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {
        case MENU -> menu.draw(g, getWidth(), getHeight());
        
        case ENTER_NAME -> {
            menu.draw(g, getWidth(), getHeight());  // draw main menu first
            //dim background
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRect(0, 0, getWidth(), getHeight());
            drawEnterName(g);                      // draw name popup
        }
        case SELECT_MAP -> {
        	mapMenu.updateButtonPositions();

            mapMenu.draw(g, getWidth(), getHeight());
        }
        case PLAYING -> drawGame(g);
    }

        if (isTransitioning) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0f, 0f, 0f, fadeAlpha));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    
    private void drawGame(Graphics g) {
    	
    	if (currentMapAnimation != null && currentMapAnimation.length > 0) {
    	    g.drawImage(currentMapAnimation[mapAnimFrameIndex], 0, 0, getWidth(), getHeight(), null);
    	} else if (background != null) {
    	    g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
    	} else {
    	    g.setColor(new Color(135, 206, 235));
    	    g.fillRect(0, 0, getWidth(), getHeight());
    	}


    	// --- Cow drawing ---
    	if (cowFrames != null && cowFrames.length > 0) {
    	    BufferedImage frame = isCowAnimating ? cowFrames[cowFrameIndex] : cowFrames[0];

    	    int cowWidth = 140;  // adjust size here
    	    int cowHeight = 100;

    	    g.drawImage(frame, (int)cowX, cowY, cowWidth, cowHeight, null);

    	}
       
  // --- TOP UI: Time and Score - With Boarder Image ---
        if (borderImage != null) {
            int topBoarderWidth = 250; // Adjust size
            int topBoarderHeight = 100;
            int topY = 20;

            // Draw Boarder for Time Left
            g.drawImage(borderImage, 20, topY, topBoarderWidth, topBoarderHeight, null);
            
            // Draw Time Left text
            g.setFont(customFont.deriveFont(13f)); // Use a smaller custom font
            g.setColor(Color.WHITE);
            // Center text inside the boarder
            g.drawString("Time Left: " + timeLeft + "s", -10 + 65, topY + 55); 

            // Draw Boarder for Score
            int scoreX = getWidth() - topBoarderWidth - 10;
            g.drawImage(borderImage, scoreX, topY, topBoarderWidth, topBoarderHeight, null);
            
            // Draw Score text
            g.drawString("Score: " + score, scoreX + 80, topY + 55);

        } else {
            // Fallback for Time and Score
            g.setFont(new Font("Monospaced", Font.BOLD, 28));
            g.setColor(Color.WHITE);
            g.drawString("Time Left: " + timeLeft + "s", 30, 50);
            g.drawString("Score: " + score, getWidth() - 200, 50);
        }


     // --- BOTTOM UI: Typing Interface ---
     int screenH = getHeight();

     if (borderImage != null) {

    	 //Adjust size
         int bottomBoarderWidth = 650;
         int bottomBoarderHeight = 200; 

         //Center the border
         int bottomX = (getWidth() - bottomBoarderWidth) / 2;
         // Position it slightly above the very bottom edge of the screen
         int bottomY = screenH - bottomBoarderHeight - 5; 
         
         g.drawImage(borderImage, bottomX, bottomY, bottomBoarderWidth, bottomBoarderHeight, null);
               
         // --- LABEL: "Type this word:" ---
         g.setFont(customFont.deriveFont(15f)); 
         g.setColor(Color.WHITE);
         g.drawString("Type this word:", bottomX + 220, bottomY + 70); 

         // --- CURRENT WORD (Dynamically Centered) ---
         g.setFont(customFont.deriveFont(20f)); 
         g.setColor(Color.YELLOW);
         
        //Get the current Font Metrics
         FontMetrics fmCurrent = g.getFontMetrics();
        
         int currentWordWidth = fmCurrent.stringWidth(currentWord);
         int currentWordX = bottomX + (bottomBoarderWidth / 2) - (currentWordWidth / 2);
         
         // Draw the centered word
         g.drawString(currentWord, currentWordX, bottomY + 110); 

         // --- TYPED WORD
         String typedTextWithCursor = typedWord + "_";
         
         g.setFont(customFont.deriveFont(20f)); 
         g.setColor(Color.GREEN);
         
         int typedWordWidth = fmCurrent.stringWidth(typedTextWithCursor); 
         int typedWordX = bottomX + (bottomBoarderWidth / 2) - (typedWordWidth / 2);
         g.drawString(typedTextWithCursor, typedWordX, bottomY + 140); 

     } else {
         // Fallback UI
         int textCenterX = getWidth() / 2 - 150;
         g.setFont(new Font("Monospaced", Font.BOLD, 28));
         g.setColor(Color.WHITE);
         g.drawString("Type this word:", textCenterX, screenH - 150); 
         g.setFont(customFont.deriveFont(36f));
         g.setColor(Color.YELLOW);
         g.drawString(currentWord, textCenterX + 50, screenH - 100); 
         g.setColor(Color.GREEN);
         g.drawString(typedWord, textCenterX + 50, screenH - 50); 
     }
        
     // --- Pause Menu Overlay ---
        if (isPaused) {
            Graphics2D g2 = (Graphics2D) g;

            // Dim background
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (pauseImage != null) {
                int imgW = pauseImage.getWidth();
                int imgH = pauseImage.getHeight();
                int x = (getWidth() - imgW) / 2;
                int y = (getHeight() - imgH) / 2;

                g2.drawImage(pauseImage, x, y, null);

                // --- Pause Button Layout ---
                int btnX = x + 364;         // shift right/left
                int btnWidth = 300;         // button width
                int btnHeight = 70;         // height
                int spacing = 15;           // space between buttons

                resumeButton  = new Rectangle(btnX, y + 190, btnWidth, btnHeight);
                restartButton = new Rectangle(btnX, y + 190 + btnHeight + spacing, btnWidth, btnHeight);
                exitButton    = new Rectangle(btnX, y + 190 + (btnHeight + spacing) * 2, btnWidth, btnHeight);

                // Hover highlight
                g2.setColor(new Color(255, 255, 255, 80));
                if (hoverResume)  g2.fill(resumeButton);
                if (hoverRestart) g2.fill(restartButton);
                if (hoverExit)    g2.fill(exitButton);
                
            }
        }
        
     //Pause Button
        if (!isPaused) {

            int rawW = pauseButtonImage.getWidth();
            int rawH = pauseButtonImage.getHeight();
            double scale = 0.10;

            int btnWidth = (int)(rawW * scale);
            int btnHeight = (int)(rawH * scale);

            int pauseX = (getWidth() - btnWidth) / 2; // top-center
            int pauseY = 15;

            pauseButton = new Rectangle(pauseX, pauseY, btnWidth, btnHeight);

            if (pauseButtonImage != null) {
                g.drawImage(
                    pauseButtonImage,
                    pauseButton.x,
                    pauseButton.y,
                    pauseButton.width,
                    pauseButton.height,
                    null
                );
            } else {
                g.setColor(Color.RED);
                g.fillRect(
                    pauseButton.x,
                    pauseButton.y,
                    pauseButton.width,
                    pauseButton.height
                );
            }
        }
        
     // Win Screen UI Overlay
        if (isWinScreen) {
            Graphics2D g2 = (Graphics2D) g;

            // Dim background
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (winImage != null) {
                
                double maxWidth = getWidth() * 1.1;
                double maxHeight = getHeight() * 1.1;

                double imgW = winImage.getWidth();
                double imgH = winImage.getHeight();

                // Preserve aspect ratio of image
                double scale = Math.min(maxWidth / imgW, maxHeight / imgH);

                int drawW = (int)(imgW * scale);
                int drawH = (int)(imgH * scale);

                int x = (getWidth() - drawW) / 2;
                int y = (getHeight() - drawH) / 2;

                g2.drawImage(winImage, x, y, drawW, drawH, null);
                
                // --- Draw Score on the Win Screen ---
                String scoreText;

                if (currentWordList == mountainWords) {
                    scoreText = playerName + "'s Overall Score: " + totalScore;
                    g2.setFont(customFont.deriveFont(24f));
                } else {
                    scoreText = "Map Score: " + score;
                    g2.setFont(customFont.deriveFont(24f));
                }

                g2.setColor(Color.WHITE); 


                FontMetrics fmScore = g2.getFontMetrics();
                int scoreWidth = fmScore.stringWidth(scoreText);
                int scoreX = x + (drawW / 2) - (scoreWidth / 2);
                

                int scoreY = y + 320; 
   
                g2.drawString(scoreText, scoreX, scoreY);
                // ------------------------------------------

                // Continue button
                int btnW = 300, btnH = 70;
                continueButton = new Rectangle(
                    x + (drawW - btnW) / 2, 
                    y + drawH - btnH - 110, 
                    btnW,
                    btnH
                );       
                     // Continue button for first 3 maps
                    if (currentWordList != mountainWords) {
                        if (hoverContinue && continueButtonHoverImage != null) {
                            g2.drawImage(continueButtonHoverImage, continueButton.x, continueButton.y,
                                         continueButton.width, continueButton.height, null);
                        } else if (continueButtonImage != null) {
                            g2.drawImage(continueButtonImage, continueButton.x, continueButton.y,
                                         continueButton.width, continueButton.height, null);
                    }
                }
            }
            
            if (currentWordList == mountainWords && returnCountdown > 0) {

                g2.setColor(Color.YELLOW);

                String countdownText = "Returning to menu in " + returnCountdown + "...";
                g2.setFont(customFont.deriveFont(20f));

                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(countdownText);
                int x = (getWidth() - textWidth) / 2;
                int y = getHeight() / 2 + 100;
                g2.drawString(countdownText, x, y);
            }
            
            return;
        }
        	

        // Game Over UI Overlay
        if (isGameOverScreen) { 
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            if (gameOverImage != null) {
                double maxWidth = getWidth() * 1.1;
                double maxHeight = getHeight() * 1.1;

                double imgW = gameOverImage.getWidth();
                double imgH = gameOverImage.getHeight();
                
                // Preserve aspect ratio of image
                double scale = Math.min(maxWidth / imgW, maxHeight / imgH);
                
                int drawW = (int)(imgW * scale);
                int drawH = (int)(imgH * scale);
                
                int x = (getWidth() - drawW) / 2;
                int y = (getHeight() - drawH) / 2;
                g2.drawImage(gameOverImage, x, y, drawW, drawH, null);
                
                // Draw Score on the Game Over Screen ---
                String scoreText = "Score: " + score;
                
                g2.setFont(customFont.deriveFont(32f)); 
                g2.setColor(Color.WHITE); 

                FontMetrics fmScore = g2.getFontMetrics();
                int scoreWidth = fmScore.stringWidth(scoreText);
               
                int scoreX = x + (drawW / 2) - (scoreWidth / 2);
              
                int scoreY = y + 320; 
                
                g2.drawString(scoreText, scoreX, scoreY);
                // ------------------------------------------


                int btnW = 220;
                int btnH = 70;
                int spacing = 40;


                int centerX = x + drawW / 2;
                int btnY = y + drawH - btnH - 90;

                // LEFT button (Try Again)
                gameOverContinueButton = new Rectangle(
                    centerX - btnW - spacing / 2,
                    btnY,
                    btnW,
                    btnH
                );

                // RIGHT button (Exit)
                exitButtonRect = new Rectangle(
                    centerX + spacing / 2,
                    btnY,
                    btnW,
                    btnH
                );




                // Draw Try Again button
                if (hoverGameOverContinue && tryAgainButtonHoverImage != null) {
                    g2.drawImage(tryAgainButtonHoverImage, gameOverContinueButton.x, gameOverContinueButton.y, gameOverContinueButton.width, gameOverContinueButton.height, null);
                } else if (tryAgainButtonImage != null) {
                    g2.drawImage(tryAgainButtonImage, gameOverContinueButton.x, gameOverContinueButton.y, gameOverContinueButton.width, gameOverContinueButton.height, null);
                }

                // Draw Exit button
                if (hoverExit && exitButtonHoverImage != null) {
                    g2.drawImage(exitButtonHoverImage, exitButtonRect.x, exitButtonRect.y, exitButtonRect.width, exitButtonRect.height, null);
                } else if (exitButtonImage != null) {
                    g2.drawImage(exitButtonImage, exitButtonRect.x, exitButtonRect.y, exitButtonRect.width, exitButtonRect.height, null);
                }
                
            }
            return;
            
        }
        
        // --- AUTO RETURN TO MENU AFTER WIN ---
        if (waitingToReturnToMenu) {
            long now = System.currentTimeMillis();
            if (now - winTimeStamp >= WIN_DELAY) {
                waitingToReturnToMenu = false;
                isWinScreen = false;

                resetToMenu();
                return;
            }
        }
    }

    
    
    private void drawEnterName(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // --- Draw Enter Name background UI ---
        int imgW = enterNameImage.getWidth();
        int imgH = enterNameImage.getHeight();
        int x = (getWidth() - imgW) / 2;
        int y = (getHeight() - imgH) / 2;
        g2.drawImage(enterNameImage, x, y, null);

        // --- Draw typed player name (Dynamically Centered) ---
        g2.setFont(customFont.deriveFont(28f));
        g2.setColor(Color.WHITE);
        
        // 1. Define the string including the cursor
        String nameWithCursor = playerName + "_";
        
        FontMetrics fm = g2.getFontMetrics();
        int nameWidth = fm.stringWidth(nameWithCursor);
       
        int textX = (getWidth() / 2) - (nameWidth / 2);

        int textY = getHeight() / 2 + 20; 
        
        g2.drawString(nameWithCursor, textX, textY);

        // --- Button positions
        int btnWidth = 200;
        int btnHeight = 60;
        
        // Vertical adjustment
        int verticalOffset = 195;
        int btnY = y + imgH - verticalOffset;

        // Horizontal adjustment
        int horizontalSpacing = 18;
        confirmButton = new Rectangle(getWidth() / 2 - horizontalSpacing - btnWidth, btnY, btnWidth, btnHeight);
        backButton    = new Rectangle(getWidth() / 2 + horizontalSpacing, btnY, btnWidth, btnHeight);

        // --- Hover effect
        g2.setColor(new Color(255, 255, 255, 80));
        if (hoverConfirm) g2.fill(confirmButton);
        if (hoverBack)    g2.fill(backButton);
    }
    
    // --- INPUT HANDLING ---
    @Override
    public void keyTyped(KeyEvent e) {
    	//Enter name
    	if (state == GameState.ENTER_NAME) {
    	    char c = e.getKeyChar();

    	    if (Character.isLetterOrDigit(c) && playerName.length() < 12) {
    	        playerName += c;
    	    } 
    	    else if (c == KeyEvent.VK_BACK_SPACE && playerName.length() > 0) {
    	        playerName = playerName.substring(0, playerName.length() - 1);
    	    }

    	    repaint();
    	    return;
    	}

    	//Playing
    	if (state != GameState.PLAYING || levelFinished) return;


        char c = e.getKeyChar();
        if (!Character.isLetter(c)) return;
        typedWord += c;

        if (!currentWord.startsWith(typedWord)) {
            typedWord = "";
            flashWrong();
            
         // --- ADD ERROR SOUND ---
            soundManager.playSound(SoundManager.SoundEffect.ERROR); 
            
        } else if (typedWord.equals(currentWord)) {
            score += 10;

            // Start cow animation
            isCowAnimating = true;
            cowFrameIndex = 0;
            cowTargetX = cowX + step;            // Move cow forward by step
            cowStepPerFrame = step / cowFrames.length * 1.5f;  // Smooth movement per frame

            nextWord();  // pick next word
            soundManager.playSound(SoundManager.SoundEffect.TYPING);
        }

        repaint();
    }

    private void flashWrong() {
        Color old = getBackground();
        setBackground(new Color(255, 150, 150));
        new javax.swing.Timer(150, e -> setBackground(old)).start();
    }
    
    private void resetToMenu() {
        state = GameState.MENU;  
        score = 0;
        cowX = 50;
        progress = 0;
        repaint();
    }


    @Override
    public void mousePressed(MouseEvent e) {
    	
    	if (waitingToReturnToMenu) return;
    	
    	// --- ADD GENERAL CLICK SOUND ---
        soundManager.playSound(SoundManager.SoundEffect.CLICK);
    	
    	//Game over screen click handling
        if (state == GameState.PLAYING && isGameOverScreen) {
            int mx = e.getX(), my = e.getY();

            if (gameOverContinueButton.contains(mx, my)) {
                // Try again logic
                restartCurrentMap();
                isGameOverScreen = false;
            }

            if (exitButtonRect.contains(mx, my)) {
                // Exit logic
                state = GameState.MENU;
                isGameOverScreen = false;
                score = 0;
                cowX = 50;
                repaint();
            }

            return;
        }


    	
    	// Win screen click handling
        if (state == GameState.PLAYING && isWinScreen) {
            int mx = e.getX(), my = e.getY();

            // Only allow Continue click for non-final maps
            if (currentWordList != mountainWords && continueButton != null && continueButton.contains(mx, my)) {
                isWinScreen = false;
                state = GameState.SELECT_MAP;
                repaint();
            }
        }

    	//Enter name click handling
    	if (state == GameState.ENTER_NAME) {
    	    int mx = e.getX();
    	    int my = e.getY();

    	    if (confirmButton.contains(mx, my) && !playerName.isEmpty()) {
    	        state = GameState.SELECT_MAP;
    	    }

    	    if (backButton.contains(mx, my)) {
    	        state = GameState.MENU;
    	    }

    	    repaint();
    	    return;
    	}

    	
    	//Pause click handling
    	if (state == GameState.PLAYING && isPaused) {
    	    int mx = e.getX(), my = e.getY();

    	    if (resumeButton.contains(mx, my)) {
    	        // Resume game
    	        isPaused = false;
    	        if (gameTimer != null) gameTimer.start();
    	    }
    	    
    	    else if (restartButton.contains(mx, my)) {
    	        // Restart current map
    	    	restartCurrentMap();

    	    }
    	    else if (exitButton.contains(mx, my)) {
    	        // Exit to map selection
    	        isPaused = false;
    	        state = GameState.MENU;
    	        score = 0;
    	        cowX = 50;
    	        repaint();
    	    }
    	    return;
    	}
    	
    	// Pause button click handling
    	if (state == GameState.PLAYING && pauseButton.contains(e.getX(), e.getY())) {
    	    isPaused = !isPaused;
    	    if (isPaused) {
    	        if (gameTimer != null) gameTimer.stop();
    	        // Stop Music
    	        soundManager.stopMainLoop(); 
    	    } else {
    	        if (gameTimer != null) gameTimer.start();
    	    }
    	    repaint();
    	    return;
    	}
  
    	
        if (state == GameState.MENU) {
            String action = menu.mousePressed(e);
            if ("ENTER".equals(action)) {
                state = GameState.ENTER_NAME;
            } else if ("EXIT".equals(action)) {
            	// Add cleanup and exit (music)
                soundManager.cleanup();
                System.exit(0);
            }
        } else if (state == GameState.SELECT_MAP) {
            String map = mapMenu.mousePressed(e);
            if ("BACK".equals(map)) {
                state = GameState.MENU;
                repaint();
                return;
            }

            if (!map.isEmpty()) {
                setMap(map);
            }
        }
        
        
    }
    	
    @Override
    public void actionPerformed(ActionEvent e) {
    	 if (levelFinished) return;
    	 
        long currentTime = System.currentTimeMillis();
        
        // --- Cow animation ---
        if (isCowAnimating) {
            if (currentTime - lastCowAnimTime >= cowAnimDelay) {
                // Animate cow frames
                cowFrameIndex++;
                if (cowFrameIndex >= cowFrames.length) {
                    cowFrameIndex = 0;
                }

                if (cowX < cowTargetX) {
                    cowX += cowStepPerFrame;
                    if (cowX > cowTargetX) cowX = cowTargetX;
                }

                // Stop animation immediately when cow reaches target
                if (cowX >= cowTargetX) {
                    cowX = cowTargetX;       
                    isCowAnimating = false;  
                    cowFrameIndex = 0; 

                    // Check if reached finish line
                    if (cowX >= finishLineX) {
                        levelFinished = true;
                        gameWin();
                    }
                }

                lastCowAnimTime = currentTime;
                repaint();
            }
        }

        
        // --- Map animation ---
        if (currentMapAnimation != null && currentMapAnimation.length > 0) {
            if (currentTime - lastMapAnimTime >= mapAnimDelay) {
                if (mapAnimForward) {
                    mapAnimFrameIndex++;
                    if (mapAnimFrameIndex >= currentMapAnimation.length - 1) {
                        mapAnimFrameIndex = currentMapAnimation.length - 1;
                        mapAnimForward = false;
                    }
                } else {
                    mapAnimFrameIndex--;
                    if (mapAnimFrameIndex <= 0) {
                        mapAnimFrameIndex = 0;
                        mapAnimForward = true;
                    }
                }
                lastMapAnimTime = currentTime;
                repaint();
            }
        }
    }


    
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    
    @Override public void keyPressed(KeyEvent e) {
    	 if (state == GameState.PLAYING && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
    	        isPaused = !isPaused;

    	        if (isPaused) {
    	            if (gameTimer != null) gameTimer.stop();
    	            soundManager.stopMainLoop();
    	        } else {
    	            if (gameTimer != null) gameTimer.start();
    	        }

    	        repaint();
    	    }
    }
    @Override public void keyReleased(KeyEvent e) {}

    //Set map selection (animated)
    private void setMap(String map) {
    	
        switch (map) {
        
            case "MAP1" -> { 
                background = loadImage("/com/utils/assets/Barn Map.png"); 
                currentWordList = barnWords; 
                step = 70; 
                mapStartTime = 60; 
                timeLeft = mapStartTime;
                currentMapAnimation = loadMapFrames("Barn");
                cowY = 380; // Default Y position for Barn
            }
            case "MAP2" -> { 
                background = loadImage("/com/utils/assets/Seaside Map.png"); 
                currentWordList = seasideWords; 
                step = 70; 
                mapStartTime = 50;
                timeLeft = mapStartTime;
                currentMapAnimation = loadMapFrames("Seaside");
                cowY = 315; // Default Y position for Barn
            }
            case "MAP3" -> { 
                background = loadImage("/com/utils/assets/Forest Map.png"); 
                currentWordList = forestWords; 
                step = 70; 
                mapStartTime = 45;
                timeLeft = mapStartTime;
                currentMapAnimation = loadMapFrames("Forest");
                cowY = 355; // Default Y position for Barn
            }
            case "MAP4" -> { 
                background = loadImage("/com/utils/assets/Mountain Map.png"); 
                currentWordList = mountainWords; 
                step = 70; 
                mapStartTime = 40;
                timeLeft = mapStartTime;
                currentMapAnimation = loadMapFrames("Mountain");
                cowY = 350; // Default Y position for Barn
            }     
        }

        // Reset gameplay values
        score = 0;
        cowX = 50;
        progress = 0;
        typedWord = "";

        startGame();
        
        cowFrameIndex = 0;

        finishLineX = getWidth() - 150;
        levelFinished = false; // reset at start
    }

    private void loadCowFrames() {
        try {
            for (int i = 0; i < 25; i++) {
                String index = String.format("%03d", i);
                String path = "/com/utils/assets/Cow Animation/frame_" + index + ".png";

                var stream = getClass().getResourceAsStream(path);
                if (stream == null) {
                    System.err.println("Cow frame missing: " + path);
                    continue;
                }

                cowFrames[i] = ImageIO.read(stream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private BufferedImage[] loadMapFrames(String mapName) {
        BufferedImage[] frames = new BufferedImage[79];
        try {
            for (int i = 0; i < 79; i++) {
                String index = String.format("%03d", i + 1);
 
                String path = "/com/utils/assets/" + mapName + " Map/ezgif-frame-" + index + ".png";

                // Debug print
                System.out.println("Loading frame: " + path);

                var stream = getClass().getResourceAsStream(path);
                if (stream == null) {
                    System.err.println("Frame not found: " + path);
                    continue; 
                }

                frames[i] = ImageIO.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frames;
    }

    private BufferedImage loadImage(String path) {
        try { return ImageIO.read(getClass().getResourceAsStream(path)); }
        catch (IOException e) { e.printStackTrace(); return null; }
    }

}

