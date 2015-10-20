/*
 * Copyright (C) 2015 Vincenzo Abate <gogolander@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
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
 * @author Vincenzo Abate <gogolander@gmail.com>
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
