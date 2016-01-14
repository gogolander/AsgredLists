package unipd.astro;
/**
* Code found at http://alvinalexander.com/java/java-splash-screen-with-progress-bar-1
* @author Alvin Alexander
*/

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

	private static SplashScreen instance = null;
	
	BorderLayout borderLayout1 = new BorderLayout();
	JLabel imageLabel = new JLabel();
	JPanel southPanel = new JPanel();
	FlowLayout southPanelFlowLayout = new FlowLayout();
	JProgressBar progressBar = new JProgressBar();
	ImageIcon imageIcon;

	public static SplashScreen getInstance() {
		if(instance == null) {
			instance = new SplashScreen(new ImageIcon(Main.class.getClassLoader().getResource("ngc1097.jpg")));
			instance.setLocationRelativeTo(null);
			instance.setProgressMax(100);
			instance.setScreenVisible(true);
		}
		return instance;
	}
	
	private SplashScreen(ImageIcon imageIcon) {
		this.imageIcon = imageIcon;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// note - this class created with JBuilder
	private void jbInit() throws Exception {
		imageLabel.setIcon(imageIcon);
		this.getContentPane().setLayout(borderLayout1);
		southPanel.setLayout(southPanelFlowLayout);
		southPanel.setBackground(Color.BLACK);
		this.getContentPane().add(imageLabel, BorderLayout.CENTER);
		this.getContentPane().add(southPanel, BorderLayout.SOUTH);
		southPanel.add(progressBar, null);
		this.pack();
	}

	private void setProgressMax(int maxProgress) {
		progressBar.setMaximum(maxProgress);
	}

	public void setProgress(int progress) {
		final int theProgress = progress;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(theProgress);
			}
		});
	}

	public void setProgress(String message, int progress) {
		final int theProgress = progress;
		final String theMessage = message;
		setProgress(progress);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(theProgress);
				setMessage(theMessage);
			}
		});
	}

	public void setScreenVisible(boolean b) {
		final boolean boo = b;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setVisible(boo);
			}
		});
	}

	private void setMessage(String message) {
		if (message == null) {
			message = "";
			progressBar.setStringPainted(false);
		} else {
			progressBar.setStringPainted(true);
		}
		progressBar.setString(message);
	}
	
	public void close() {
		this.setScreenVisible(false);
		this.dispose();
		instance = null;
	}
}