package com.jim.common.io;

import java.io.IOException;

public interface MessageWriter<T> {

	public void setMessage(T message);

	public void write() throws IOException;

	public boolean isMessageCompleted();

	public IOWriter getIOWriter();

}
