package com.utils;

import javax.swing.JFrame;

public class GameWindow {
	
	private JFrame jframe;
	
	public GameWindow(GamePanel gamePanel) {
		
		jframe = new JFrame();
		
		jframe.setSize(1150, 700);	//Size
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setResizable(false);
		jframe.add(gamePanel);
		jframe.setVisible(true);
	    gamePanel.requestFocusInWindow();
	    jframe.setTitle("Barnyard Rush");	//Title
	    jframe.setLocationRelativeTo(null); //Center screen
	}
}
