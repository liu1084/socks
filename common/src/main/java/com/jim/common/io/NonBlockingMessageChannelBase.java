/*
 * Copyright 2018 Jim Liu.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * By the way, this Soft can only by education, can not be used in commercial products. All right be reserved.
 */

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
