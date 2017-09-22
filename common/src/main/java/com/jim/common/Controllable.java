package com.jim.common;

public interface Controllable {

	public void startup();

	public void shutdown();

	public Object execute(String executeName, String[] args);

}
