package com.jim.common.io;

public interface MessageChannel<R, W> {

	public String getName();

	public void close();

}
