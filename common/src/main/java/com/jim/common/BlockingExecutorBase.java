package com.jim.common;


import com.jim.common.exception.ExecuteException;
import com.jim.common.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockingExecutorBase<KEY, PARAM, RESULT, RETURN>
		implements BlockingExecutor<KEY, PARAM, RESULT, RETURN> {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.BlockingExecutorBase.class);

	private final SyncMessageList syncMessageList;

	public BlockingExecutorBase() {
		syncMessageList = new SyncMessageList();
	}

	public RETURN blockingExecute(KEY key, PARAM param, int timeout)
			throws InterruptedException, TimeoutException, ExecuteException {

		logger.debug("阻塞执行：开始，同步键：{}，参数：{}，超时时间：{}", key, param, timeout);

		SyncMessageResult<RESULT, RETURN> messageResult = syncMessageList.suspend(key);
		synchronized (messageResult) {
			execute(param);

			if (timeout > 0) {
				messageResult.wait(timeout);
			} else {
				messageResult.wait();
			}

			syncMessageList.remove(key);

			RETURN returnValue = messageResult.getReturn();

			if (returnValue == null) {
				logger.debug("阻塞执行：超时，同步键：{}，参数：{}", key, param);

				throw new TimeoutException("阻塞执行超时。");
			}

			logger.debug("阻塞执行：完成，同步键：{}，参数：{}，返回值：{}", key, param, returnValue);

			return returnValue;
		}

	}

	protected abstract void execute(PARAM param) throws ExecuteException;

	public void resume(KEY key, RESULT result) {
		syncMessageList.resume(key, result);
	}

	protected abstract SyncMessageResult<RESULT, RETURN> createSyncMessageResult();

	protected abstract boolean needNotify(RESULT result);

	protected interface SyncMessageResult<A, B> {

		public B getReturn();

		public void setResult(A result);

	}

	private class SyncMessageList {
		private Map<KEY, SyncMessageResult<RESULT, RETURN>> map = new HashMap<KEY, SyncMessageResult<RESULT, RETURN>>();

		public SyncMessageResult<RESULT, RETURN> suspend(KEY key) {
			SyncMessageResult<RESULT, RETURN> result = createSyncMessageResult();
			map.put(key, result);
			return result;
		}

		public void resume(KEY key, RESULT object) {
			SyncMessageResult<RESULT, RETURN> result = map.get(key);
			if (result == null) {
				logger.warn("阻塞执行：找不到同步键，同步键：{}，结果：{}", key, object);
				return;
			}
			synchronized (result) {
				result.setResult(object);
				if (needNotify(object)) {
					result.notify();
				}
			}
		}

		public void remove(KEY key) {
			map.remove(key);
		}

	}

}
