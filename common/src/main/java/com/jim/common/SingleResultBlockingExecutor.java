package com.jim.common;


public abstract class SingleResultBlockingExecutor<KEY, PARAM, RESULT, RETURN>
		extends BlockingExecutorBase<KEY, PARAM, RESULT, RETURN> {

	@Override
	protected SyncMessageResult<RESULT, RETURN> createSyncMessageResult() {
		return new SyncMessageResult<RESULT, RETURN>() {

			private RESULT result;

			@Override
			public RETURN getReturn() {
				return convert(result);
			}

			@Override
			public void setResult(RESULT result) {
				this.result = result;
			}

		};
	}

	@Override
	protected boolean needNotify(RESULT result) {
		return true;
	}

	protected abstract RETURN convert(RESULT result);

}
