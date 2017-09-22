package com.jim.common;

import java.util.List;

public abstract class MultiResultBlockingExecutor<KEY, PARAM, RESULT, RETURN>
		extends BlockingExecutorBase<KEY, PARAM, RESULT, RETURN> {

	@Override
	protected SyncMessageResult<RESULT, RETURN> createSyncMessageResult() {
		return new SyncMessageResult<RESULT, RETURN>() {

			protected List<RESULT> result;

			@Override
			public RETURN getReturn() {
				return convert(result);
			}

			@Override
			public void setResult(RESULT result) {
				this.result.add(result);
			}

		};
	}

	protected abstract RETURN convert(List<RESULT> result);

}
