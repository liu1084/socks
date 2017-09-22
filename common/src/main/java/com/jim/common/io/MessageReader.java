package com.jim.common.io;

import java.io.IOException;

public interface MessageReader<T> {

	public void read() throws IOException;

	public boolean isMessageCompleted();

	public boolean isReadFinished();

	public T getMessage();

	public IOReader getIOReader();

}
