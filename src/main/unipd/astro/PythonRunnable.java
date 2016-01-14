/*
 * Copyright (C) 2015 Enzo
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
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enzo
 */
public class PythonRunnable implements Runnable {
    private Process python;
    private BufferedReader getResponse;
    private BufferedWriter passCommand;
    @Override
    public void run() {
        try {
            python = Runtime.getRuntime().exec("C:\\python27\\python.exe");// + this.basePath + File.separator + "execPython.py");
            passCommand = new BufferedWriter(new OutputStreamWriter(python.getOutputStream()));
            getResponse = new BufferedReader(new InputStreamReader(python.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Process getProcess() {
        return python;
    }
    
    public String fromPython() {
        if(getResponse != null) {
            try {
                return getResponse.readLine();
            } catch (IOException ex) {
                Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return "";
    }

    public void toPython(String message) {
        if(passCommand != null) {
            try {
                passCommand.write(message);
                passCommand.flush();
            } catch (IOException ex) {
                Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void dispose() {
        try {
            this.passCommand.close();
            this.getResponse.close();
            this.python.destroy();
        } catch (IOException ex) {
            Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
