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
/**
 * @author Vincenzo Abate <gogolander@gmail.com>
 */
package unipd.astro;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class InterruptableThread extends Thread {
	private static Logger log = Logger.getLogger(Execution.class.getName());
	protected List<String> commandsToClose;
	protected List<String> commandsToRun;
	protected boolean stop;
	protected boolean isPyraf = false;
	protected Process process;
	
	public InterruptableThread() {
		this.commandsToRun = new ArrayList<>();
		this.commandsToClose = new ArrayList<>();
		this.stop = false;
	}

	public abstract void run();

	public abstract void onClosing() throws Exception;

	public synchronized void stopThread() {
		this.stop = true;
		notify();
	}
	
	public synchronized List<String> getCommandsToClose() {
		return this.commandsToClose;
	}

	public synchronized void setCommandsToClose(String[] commandsToClose) {
		for (String command : commandsToClose)
			this.commandsToClose.add(command);
	}

	public synchronized void appendClosingCommand(String command) {
		log.trace("Append new command for closing: " + command);
		this.commandsToClose.add(command);
		notify();
	}

	public synchronized void appendCommandToRun(String command) {
		log.trace("Append new command to run: " + command);
		this.commandsToRun.add(command);
		notify();
	}
	
	public boolean isRunning() {
		return (this.process != null && this.process.isAlive());
	}
	
	public Process getProcess() {
		return this.process;
	}

	public boolean isPyraf() {
		return isPyraf;
	}
}