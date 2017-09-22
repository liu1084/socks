package com.jim.common;

import com.jim.common.exception.ExecuteException;
import com.jim.common.exception.TimeoutException;

public interface BlockingExecutor<KEY, PARAM, RESULT, RETURN> {

	public RETURN blockingExecute(KEY key, PARAM param, int timeout)
			throws InterruptedException, TimeoutException, ExecuteException;

	public void resume(KEY key, RESULT result);

}
