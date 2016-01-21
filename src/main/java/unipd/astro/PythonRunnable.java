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
import java.io.PipedOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import unipd.astro.service.DataService;

/**
 *
 * @author Enzo
 */
public class PythonRunnable {
	private Process python;
	private BufferedReader getResponse;
	private BufferedReader getError;
	private BufferedWriter passCommand;

	public void mimeWlcal(final String scriptPath, final AsyncCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.setProperty("user.dir", DataService.getInstance().getProperty("iraf.home"));
					ProcessBuilder pr = new ProcessBuilder("python", scriptPath);
					python = pr.start();
					passCommand = new BufferedWriter(new OutputStreamWriter(python.getOutputStream()));
					getResponse = new BufferedReader(new InputStreamReader(python.getInputStream()));
					getError = new BufferedReader(new InputStreamReader(python.getErrorStream()));
					while (python.isAlive()) {
						while (!getResponse.ready() && !getError.ready() && python.isAlive())
							Thread.sleep(50);
						while (getResponse.ready())
							callback.OnResponseReceived(getResponse.readLine().trim());
						while (getError.ready())
							callback.OnErrorReceived(getError.readLine().trim());
					}
					callback.OnResponseReceived("terminated");
					dispose();
				} catch (Exception ex) {
					Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}).start();
	}

	public void execCommand(final String command, final AsyncCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					System.setProperty("user.dir", DataService.getInstance().getProperty("iraf.home"));
					ProcessBuilder pr = new ProcessBuilder(command.trim().split(" "));
					python = pr.start();
					passCommand = new BufferedWriter(new OutputStreamWriter(python.getOutputStream()));
					getResponse = new BufferedReader(new InputStreamReader(python.getInputStream()));
					getError = new BufferedReader(new InputStreamReader(python.getErrorStream()));
					while (python.isAlive()) {
						while (!(getResponse.ready() || getError.ready()) && python.isAlive())
							Thread.sleep(50);
						while (getResponse.ready()) {
							String response = getResponse.readLine();
							callback.OnResponseReceived(response);
						}
						while (getError.ready())
							callback.OnErrorReceived(getError.readLine().trim());
					}
					callback.OnResponseReceived("terminated");
				} catch (Exception ex) {
					Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}).start();
	}

	public boolean isAlive() {
		return (python != null && python.isAlive());
	}

	public Process getProcess() {
		return python;
	}

	public void toPython(final String message) {
		if (passCommand != null) {
			try {
				if (python.isAlive()) {
					passCommand.write(message + "\n");
					passCommand.flush();
				} else
					throw new Exception("process not running.");
			} catch (Exception ex) {
				Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void dispose() {
		try {
			this.passCommand.close();
			this.passCommand = null;
			this.getResponse.close();
			this.getResponse = null;
			this.python.destroy();
			this.python = null;
		} catch (IOException ex) {
			Logger.getLogger(PythonRunnable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
