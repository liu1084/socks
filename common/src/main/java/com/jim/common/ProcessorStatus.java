package com.jim.common;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

public class ProcessorStatus {

	private String name;

	private boolean running;

	private Date startupTime;

	private Date shutdownTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public Date getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(Date startupTime) {
		this.startupTime = startupTime;
	}

	public Date getShutdownTime() {
		return shutdownTime;
	}

	public void setShutdownTime(Date shutdownTime) {
		this.shutdownTime = shutdownTime;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", name).append("running", running)
				.append("startupTime", startupTime).append("shutdownTime", shutdownTime).toString();
	}

}
