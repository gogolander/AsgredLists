/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unipd.astro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Vincenzo Abate
 */
public class AsgredLists {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Main mainFrame = new Main();
            JFrame frame = new JFrame();
            frame.setTitle("AsgredLists");
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(mainFrame);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception ex) {
            Logger.getLogger(AsgredLists.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
