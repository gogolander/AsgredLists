/**
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unipd.astro;

import org.apache.log4j.Logger;

import unipd.astro.service.DataService;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class AsgredLists {
	private static Logger log = Logger.getLogger(AsgredLists.class.getName());
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
        	SplashScreen.getInstance().setProgress("Creating standard.list...", 0);
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            log.info("Does ${user.home}/standard.list exist?");
    		File atlas = Paths.get(System.getProperty("user.home"), "standard.list").toFile();
    		if(!atlas.exists()) {
    			log.info("No. Copying the default one to the home...");
    			Files.copy(Main.class.getClassLoader().getResourceAsStream("standard.list"), Paths.get(System.getProperty("user.home"), "standard.list"));
    			log.info("Copied.");
    		}
    		else log.info("Yes. Nothing to do.");
    		log.info("Done.");
    		log.info("Does \"loadTasks.py\" exist?");
    		if(!Paths.get(DataService.getInstance().getProperty("iraf.home"), "loadTasks.py").toFile().exists()) {
    			log.info("No. Copying the default one to the home...");
    			Files.copy(Main.class.getClassLoader().getResourceAsStream("loadTasks.py"), Paths.get(DataService.getInstance().getProperty("iraf.home"), "loadTasks.py"));
    			log.info("Copied.");
    		}
    		else log.info("Yes. Nothing to do.");
    		Main mainFrame = new Main();
            mainFrame.initDatabase();
            JFrame frame = new JFrame();
            frame.addWindowListener(new WindowListener() {

				@Override
				public void windowOpened(WindowEvent e) {
					
				}

				@Override
				public void windowClosing(WindowEvent e) {
					mainFrame.dispose();					
				}

				@Override
				public void windowClosed(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowIconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowDeiconified(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowActivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void windowDeactivated(WindowEvent e) {
					// TODO Auto-generated method stub
					
				}
            	
            });
            frame.setTitle("AsgredLists");
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(mainFrame);
            frame.pack();
            frame.setVisible(true);
            SplashScreen.getInstance().close();
        } catch (Exception ex) {
        	System.out.println(ex.getMessage());
        	log.error(ex.getMessage());
        }
    }
}
