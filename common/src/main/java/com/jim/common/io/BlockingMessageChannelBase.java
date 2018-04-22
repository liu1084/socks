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

import com.jim.common.exception.TimeoutException;
import com.jim.common.io.exception.BlockingWriteTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BlockingMessageChannelBase<R, W> implements BlockingMessageChannel<R, W> {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.io.BlockingMessageChannelBase.class);

	private final MessageReader<R> reader;

	private final String name;

	private final MessageWriter<W> writer;

	private Boolean readFlag;

	private Boolean writeFlag;

	public BlockingMessageChannelBase(String name, MessageReader<R> reader, MessageWriter<W> writer) {
		this.name = name;
		this.reader = reader;
		this.writer = writer;
		readFlag = true;
		writeFlag = true;
	}

	@Override
	public R blockingRead(int timeout) throws TimeoutException {
		long time = 0;
		if (timeout > 0) {
			time = System.currentTimeMillis();
		}
		synchronized (readFlag) {
			while (true) {
				if (timeout > 0 && System.currentTimeMillis() - time > timeout) {
					logger.debug(name + "读取消息超时。");
				}
				try {
					reader.read();
				} catch (Exception e) {
					logger.debug("{}读取时发生异常，将关闭消息通道。", name);
					close();
					throw new RuntimeException(e);
				}
				if (reader.isMessageCompleted()) {
					return reader.getMessage();
				}
			}
		}
	}

	@Override
	public void blockingWrite(W message, int timeout) throws BlockingWriteTimeoutException {
		long time = 0;
		if (timeout > 0) {
			time = System.currentTimeMillis();
		}
		synchronized (writeFlag) {
			if (!writer.isMessageCompleted()) {
				logger.debug("{}继续上次未写完的消息。", name);
				while (true) {
					if (timeout > 0 && System.currentTimeMillis() - time > timeout) {
						throw new BlockingWriteTimeoutException(name + "写入消息超时。", false);
					}
					try {
						writer.write();
					} catch (Exception e) {
						logger.debug("{}写入时发生异常，将关闭消息通道。", name);
						close();
						throw new RuntimeException(e);
					}
					if (writer.isMessageCompleted()) {
						break;
					}
				}
			}

			writer.setMessage(message);

			while (true) {
				if (timeout > 0 && System.currentTimeMillis() - time > timeout) {
					throw new BlockingWriteTimeoutException(name + "写入消息超时。", true);
				}
				try {
					writer.write();
				} catch (Exception e) {
					logger.debug("{}写入时发生异常，将关闭消息通道。", name);
					close();
					throw new RuntimeException(e);
				}
				if (writer.isMessageCompleted()) {
					return;
				}
			}
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public MessageReader<R> getReader() {
		return reader;
	}

	public MessageWriter<W> getWriter() {
		return writer;
	}

}
