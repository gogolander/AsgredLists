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

import java.util.ArrayList;
import org.apache.log4j.Logger;
import unipd.astro.service.DataService;

/**
 * PythonRunnable is the interface between AsgredLists and any external process needed.
 * The main duty of this class is of course to run PyRAF and let AsgredLists to control it.
 * Every call to every process is ansynchronus.
 * 
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class PythonRunnable {
	private static Logger log = Logger.getLogger(Main.class.getName());
	private Process python;
	private ArrayList<Thread> openThread;

	public PythonRunnable() {
		openThread = new ArrayList<>();
	}

	/**
	 * This method mimes the respond from wlcal. Used to test the interaction between
	 * PyRAF and AsgredLists.
	 * 
	 * @param scriptPath
	 * @param callback
	 */
	public void mimeWlcal(final String scriptPath, final AsyncCallback callback) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int nBytes = 0;
					log.info("Starting wlcal simulation...");
					log.trace("Creating the process...");
					System.setProperty("user.dir", DataService.getInstance().getProperty("iraf.home"));
					ProcessBuilder pr = new ProcessBuilder("python", scriptPath);
					log.trace("Done.");
					log.trace("Starting it...");
					python = pr.start();
					log.trace("Done.");
					while (python.isAlive()) {
						log.trace("Waiting for activity...");
						while (python.getInputStream().available() == 0 && python.getErrorStream().available() == 0
								&& python.isAlive())
							Thread.sleep(100);
						while ((nBytes = python.getInputStream().available()) != 0) {
							log.trace("Output received.");
							byte[] buffer = new byte[nBytes];
							python.getInputStream().read(buffer);
							for (String response : new String(buffer, 0, nBytes).split("\n"))
								callback.OnResponseReceived(response);
						}
						while ((nBytes = python.getErrorStream().available()) != 0) {
							log.trace("Error received.");
							byte[] buffer = new byte[nBytes];
							python.getInputStream().read(buffer);
							for (String response : new String(buffer, 0, nBytes).split("\n"))
								callback.OnErrorReceived(response);
						}
					}
					log.info("Done");
					callback.OnResponseReceived("terminated");
					dispose();
				} catch (Exception ex) {
					log.fatal(ex);
				}
			}
		});
		openThread.add(thread);
		thread.start();
	}

	/**
	 * Launch the given python script.
	 * 
	 * @param path
	 * @param callback
	 */
	public void startScript(final String path, final AsyncCallback callback) {
		if (!path.endsWith(".py")) {
			callback.OnErrorReceived("Wrong path to PyRAF script. Must be: /path/to/script/scriptName.py");
			return;
		}
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int nBytes = 0;
					log.info("Begin to exec the PyRAF script...");
					log.trace("Creating the process...");
					ProcessBuilder pr = new ProcessBuilder("python", path);
					log.trace("Done.");
					log.trace("Starting it...");
					python = pr.start();
					log.trace("Launching IRAF...");
					while (python.isAlive()) {
						log.trace("Waiting for activity...");
						while (python.getInputStream().available() == 0 && python.getErrorStream().available() == 0
								&& python.isAlive())
							Thread.sleep(100);
						while ((nBytes = python.getInputStream().available()) != 0) {
							log.trace("Output received.");
							byte[] buffer = new byte[nBytes];
							nBytes = python.getInputStream().read(buffer);
							for (String response : new String(buffer, 0, nBytes).split("\n"))
								callback.OnResponseReceived(response);
						}
						while ((nBytes = python.getErrorStream().available()) != 0) {
							log.trace("Error received.");
							byte[] buffer = new byte[nBytes];
							nBytes = python.getErrorStream().read(buffer);
							for (String response : new String(buffer, 0, nBytes).split("\n"))
								callback.OnErrorReceived(response);
						}
					}
					log.info("Done");
					callback.OnResponseReceived("terminated");
					callback.onScriptTerminated();
					dispose();
				} catch (Exception ex) {
					log.fatal(ex);
				}
			}
		});
		openThread.add(thread);
		thread.start();
	}
	
	/**
	 * Start a single command.
	 * 
	 * @param command
	 * @param callback
	 */
	public void startCommand(final String command, final AsyncCallback callback) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int nBytes = 0;
					log.info("Begin to exec the command...");
					log.trace("Creating the process...");
					ProcessBuilder pr = new ProcessBuilder(command.split(" "));
					log.trace("Done.");
					log.trace("Starting it...");
					python = pr.start();
					log.trace("Launching IRAF...");
					while (python.isAlive()) {
						log.trace("Waiting for activity...");
						while (python.getInputStream().available() == 0 && python.getErrorStream().available() == 0
								&& python.isAlive())
							Thread.sleep(100);
						while ((nBytes = python.getInputStream().available()) != 0) {
							log.trace("Output received.");
							byte[] buffer = new byte[nBytes];
							nBytes = python.getInputStream().read(buffer);
							for (String response : new String(buffer, 0, nBytes).split("\n"))
								callback.OnResponseReceived(response);
						}
						while ((nBytes = python.getErrorStream().available()) != 0) {
							log.trace("Error received.");
							byte[] buffer = new byte[nBytes];
							nBytes = python.getErrorStream().read(buffer);
							for (String response : new String(buffer, 0, nBytes).split("\n"))
								callback.OnErrorReceived(response);
						}
					}
					log.info("Done");
					callback.OnResponseReceived("terminated");
					callback.onScriptTerminated();
					dispose();
				} catch (Exception ex) {
					log.fatal(ex);
				}
			}
		});
		openThread.add(thread);
		thread.start();
	}

	/**
	 * Verify if the process is running and alive.
	 * 
	 * @return
	 */
	public boolean isAlive() {
		return (python != null && python.isAlive());
	}
	
	/**
	 * Get the current instance of the process.
	 * @return
	 */
	public Process getProcess() {
		return python;
	}

	/**
	 * Pass a request or a response to the running process. 
	 * @param message
	 */
	public void toPython(final String message) {
		try {
			log.info("Passing the command to the console...");
			log.trace("Is it alive?");
			if (python.isAlive()) {
				log.trace("Yes.");
				log.trace("Passing the command");
				python.getOutputStream().write((message + "\r\n").getBytes());
				python.getOutputStream().flush();
				log.info("Done.");
			} else
				log.fatal("Process is not running.");
		} catch (Exception ex) {
			log.fatal(ex);
		}
	}

	/**
	 * Stop all running threads and destroy the process.
	 */
	public void dispose() {
		log.info("Disposing the process...");
		for(Thread thread : openThread)
			thread.interrupt();
		this.python.destroy();
		this.python = null;
		log.info("Disposed.");
	}
}
