package com.jim.common.io;


import com.jim.common.exception.TimeoutException;
import com.jim.common.io.exception.BlockingWriteTimeoutException;

public interface BlockingMessageChannel<R, W> extends MessageChannel<R, W> {

	public R blockingRead(int timeout) throws TimeoutException;

	public void blockingWrite(W message, int timeout) throws BlockingWriteTimeoutException;

}
