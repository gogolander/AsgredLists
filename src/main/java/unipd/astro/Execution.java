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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import unipd.astro.service.DataService;

/**
 * Execution is the interface between AsgredLists and any external process
 * needed. The main duty of this class is of course to run PyRAF and let
 * AsgredLists to control it. Every call to every process is ansynchronus.
 * 
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
public class Execution {
	private static int TIMEOUT = 10;
	private static Logger log = Logger.getLogger(Execution.class.getName());
	private ArrayList<InterruptableThread> openThread;

	public Execution() {
		openThread = new ArrayList<>();
	}

	/**
	 * This method mimes the respond from wlcal. Used to test the interaction
	 * between PyRAF and AsgredLists.
	 * 
	 * @param scriptPath
	 * @param callback
	 */
	public int mimeWlcal(final String scriptPath, final String[] commandsToClose, final AsyncCallback callback) {
		InterruptableThread thread = new InterruptableThread() {
			@Override
			public void run() {
				synchronized (this) {
					try {
						int nBytes = 0;
						log.info("Starting wlcal simulation...");
						log.trace("Creating the process...");
						System.setProperty("user.dir", DataService.getInstance().getProperty("iraf.home"));// perché
																											// non
																											// fa
																											// niente?
						log.trace("Done.");
						log.trace("Starting it...");
						process = Runtime.getRuntime().exec(new String[] { "python3", scriptPath });
						log.trace("Done.");
						while (process.isAlive()) {
							log.info("Waiting for activity...");
							while (commandsToRun.size() == 0 && process.getInputStream().available() == 0
									&& process.getInputStream().available() == 0 && process.isAlive() && !stop)
								wait(TIMEOUT);

							if (stop) {
								onClosing();
								return;
							}
							/**
							 * Pass the commands one at a time
							 */
							if (commandsToRun.size() > 0 && process.isAlive()) {
								log.info("Input received: " + commandsToRun.get(0));
								process.getOutputStream().write(commandsToRun.get(0).getBytes());
								process.getOutputStream().flush();
								callback.OnMessageSent(commandsToRun.get(0).trim());
								wait(TIMEOUT);
								commandsToRun.remove(0);
							}
							while ((nBytes = process.getInputStream().available()) != 0) {
								log.trace("Output received.");
								byte[] buffer = new byte[nBytes];
								process.getInputStream().read(buffer);
								for (String response : new String(buffer, 0, nBytes).split("\n"))
									callback.OnResponseReceived(response.replaceAll("\\e\\[[\\d;]*[^\\d;]", "").trim());
							}
							while ((nBytes = process.getErrorStream().available()) != 0) {
								log.trace("Error received.");
								byte[] buffer = new byte[nBytes];
								process.getErrorStream().read(buffer);
								for (String error : new String(buffer, 0, nBytes).split("\n"))
									callback.OnErrorReceived(error.replaceAll("\\e\\[[\\d;]*[^\\d;]", "").trim());
							}
						}
					} catch (Exception ex) {
						log.fatal(ex);
						callback.OnErrorReceived(ex.getMessage());
					} finally {
						log.info("Done");
						dispose();
						callback.OnScriptTerminated();
					}
				}
			}

			@Override
			public void onClosing() throws Exception {
				process.getInputStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		};
		thread.setCommandsToClose(commandsToClose);
		openThread.add(thread);
		thread.start();
		return openThread.size() - 1;
	}

	/**
	 * Launch the given python script.
	 * 
	 * @param path
	 * @param callback
	 */
	public int startScript(final String path, final String[] commandsToClose, final AsyncCallback callback) {
		if (!path.endsWith(".py")) {
			callback.OnErrorReceived("Wrong path to PyRAF script. Must be: /path/to/script/scriptName.py");
			return -1;
		}
		InterruptableThread thread = new InterruptableThread() {
			@Override
			public void run() {
				synchronized (this) {
					try {
						checkDs9IsRunning();
						int nBytes = 0;
						log.info("Begin to exec the PyRAF script...");
						log.trace("Starting process...");
						process = Runtime.getRuntime().exec(new String[] { "python3", path });
						while (process.isAlive()) {
							log.trace("Waiting for activity...");
							while (commandsToRun.size() == 0 && process.getInputStream().available() == 0
									&& process.getErrorStream().available() == 0 && process.isAlive() && !stop)
								wait(TIMEOUT);

							if (stop) {
								onClosing();
								return;
							}
							/**
							 * Pass the commands one at a time
							 */
							if (commandsToRun.size() > 0) {
								log.info("Input received: " + commandsToRun.get(0));
								process.getOutputStream().write(commandsToRun.get(0).getBytes());
								process.getOutputStream().flush();
								callback.OnMessageSent(commandsToRun.get(0).trim());
								wait(TIMEOUT);
								commandsToRun.remove(0);
							}
							while ((nBytes = process.getInputStream().available()) != 0) {
								log.trace("Output received.");
								byte[] buffer = new byte[nBytes];
								nBytes = process.getInputStream().read(buffer);
								for (String response : new String(buffer, 0, nBytes).split("\n"))
									callback.OnResponseReceived(response.replaceAll("\\e\\[[\\d;]*[^\\d;]", "").trim());
							}
							while ((nBytes = process.getErrorStream().available()) != 0) {
								log.trace("Error received.");
								byte[] buffer = new byte[nBytes];
								nBytes = process.getErrorStream().read(buffer);
								for (String error : new String(buffer, 0, nBytes).split("\n"))
									callback.OnErrorReceived(error.replaceAll("\\e\\[[\\d;]*[^\\d;]", "").trim());
							}

						}
					} catch (Exception ex) {
						log.fatal(ex);
						callback.OnErrorReceived(ex.getMessage());
					} finally {
						log.info("Done");
						dispose();
						callback.OnScriptTerminated();
					}
				}
			}

			@Override
			public void onClosing() throws Exception {
				process.getInputStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		};
		thread.setCommandsToClose(commandsToClose);
		openThread.add(thread);
		thread.start();
		return openThread.size() - 1;
	}

	/**
	 * Start a single command.
	 * 
	 * @param command
	 * @param callback
	 */
	public int startCommand(final String command, final String[] commandsToClose, final AsyncCallback callback) {
		InterruptableThread thread = new InterruptableThread() {
			@Override
			public void run() {
				synchronized (this) {
					try {
						this.commandsToRun = new ArrayList<String>();
						checkDs9IsRunning();
						int nBytes = 0;
						log.info("Begin to exec the command...");
						process = Runtime.getRuntime().exec(command.split(" "));
						while (process.isAlive()) {
							log.info("Waiting for activity...");
							nBytes = 0;
							while (this.commandsToRun.size() == 0 && this.process.getInputStream().available() == 0
									&& this.process.getErrorStream().available() == 0 && this.process.isAlive()
									&& !stop)
								wait(TIMEOUT);

							if (stop) {
								onClosing();
								return;
							}
							/**
							 * Pass the commands one at a time
							 */
							if (this.commandsToRun.size() > 0) {
								log.info("Input received: " + commandsToRun.get(0));
								process.getOutputStream().write(commandsToRun.get(0).getBytes());
								process.getOutputStream().flush();
								callback.OnMessageSent(commandsToRun.get(0).trim());
								wait(TIMEOUT);
								commandsToRun.remove(0);
							}
							while ((nBytes = process.getInputStream().available()) != 0) {
								log.trace("Output received.");
								byte[] buffer = new byte[nBytes];
								nBytes = process.getInputStream().read(buffer);
								for (String response : new String(buffer, 0, nBytes).split("\n"))
									callback.OnResponseReceived(response.replaceAll("\\e\\[[\\d;]*[^\\d;]", "").trim());
							}
							while ((nBytes = process.getErrorStream().available()) != 0) {
								log.trace("Error received.");
								byte[] buffer = new byte[nBytes];
								nBytes = process.getErrorStream().read(buffer);
								for (String error : new String(buffer, 0, nBytes).split("\n"))
									callback.OnErrorReceived(error.replaceAll("\\e\\[[\\d;]*[^\\d;]", "").trim());
							}
						}
					} catch (Exception ex) {
						log.fatal(ex);
						callback.OnErrorReceived(ex.getMessage());
					} finally {
						log.info("Done");
						dispose();
						callback.OnScriptTerminated();
					}
				}
			}

			@Override
			public void onClosing() throws Exception {
				if (getCommandsToClose() != null)
					for (String command : getCommandsToClose())
						process.getOutputStream().write(command.getBytes());
				process.getInputStream().close();
				process.getInputStream().close();
				process.getOutputStream().close();
				process.destroy();
			}
		};
		thread.setCommandsToClose(commandsToClose);
		openThread.add(thread);
		thread.start();
		return openThread.size() - 1;
	}

	/**
	 * Verify if the process controlled by the given thread is alive and running.
	 * 
	 * @return
	 */
	public boolean isRunning(final int id) {
		return openThread.get(id).isRunning();
	}

	/**
	 * Get the process controlled by the given thread.
	 * 
	 * @return
	 */
	public Process getProcess(final int id) {
		return openThread.get(id).getProcess();
	}

	/**
	 * Get the current instance of the thread.
	 * 
	 * @return
	 */
	public InterruptableThread getThread(final int id) {
		return openThread.get(id);
	}

	/**
	 * Send a command to the given thread.
	 * 
	 * @param command
	 */
	public void sendCommand(final int id, final String command) {
		openThread.get(id).appendCommandToRun(command + "\n");
	}

	/**
	 * Send a command to run and a command executed to close properly the
	 * given thread.
	 * 
	 * @param command
	 * @param closingCommand
	 */
	public void sendCommand(final int id, final String command, final String closingCommand) {
		openThread.get(id).appendCommandToRun(command + "\n");
		openThread.get(id).appendClosingCommand(closingCommand);
	}

	/**
	 * Stop all running threads and destroy the process.
	 */
	public void dispose() {
		log.info("Disposing the process...");
		if (openThread != null && openThread.size() > 0)
			for (InterruptableThread thread : openThread)
				if (thread.isAlive())
					thread.stopThread();
		openThread.clear();
		log.info("Disposed.");
	}

	/**
	 * Check if DS9 is running: if it isn't, launch it.
	 */
	private void checkDs9IsRunning() {
		try {
			log.info("Checking if DS9 is running...");
			String line;
			Process p = Runtime.getRuntime().exec("ps -e");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				if (line.contains(" ds9")) {
					log.info("It is running.");
					log.info("Done.");
					p.destroy();
					return;
				}
			}
			p.destroy();
			log.info("Lauching DS9...");
			new Thread() {
				@Override
				public void run() {
					try {
						Runtime.getRuntime().exec("ds9");
					} catch (IOException e) {
						log.fatal(e.getMessage());
					}
				}
			}.start();
			log.info("Done.");
		} catch (Exception ex) {
			log.fatal(ex.getMessage());
		}
	}

	/**
	 * Send the exit flag to the selected thread.
	 * @param id
	 */
	public void stop(final int id) {
		this.openThread.get(id).stopThread();
	}
}
