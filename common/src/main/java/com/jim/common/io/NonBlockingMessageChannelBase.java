package com.jim.common.io;

public abstract class NonBlockingMessageChannelBase<R, W> extends MessageChannelBase<R, W>
		implements NonBlockingMessageChannel<R, W> {

	private MessageHandler<R, W> handler;

	private MessageDriver<R, W> driver;

	public NonBlockingMessageChannelBase(String name) {
		super(name);
	}

	public void setMessageHandler(MessageHandler<R, W> handler) {
		this.handler = handler;
	}

	public void setDriver(MessageDriver<R, W> driver) {
		this.driver = driver;
		driver.setMessageChannel(this);
	}

	@Override
	public final void onMessage(R message) {
		if (preOnMessage(message)) {
			handler.onMessage(message);
		}
	}

	protected boolean preOnMessage(R message) {
		return true;
	}

	@Override
	public final void handleError(Exception e, W message) {
		if (preHandleError(e, message)) {
			handler.handleError(e, message);
		}
	}

	protected boolean preHandleError(Exception e, W message) {
		return true;
	}

	@Override
	public final void writeMessage(W message) {
		if (preWriteMessage(message)) {
			driver.writeMessage(message);
		}
	}

	protected boolean preWriteMessage(W message) {
		return true;
	}

	@Override
	public void close() {
		driver.close();
		super.close();
	}

}
