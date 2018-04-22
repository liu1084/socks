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

package com.jim.common.socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class SocketConnection<R, W> extends com.jim.common.io.MessageDriverBase<R, W> {

	private static final Logger logger = LoggerFactory.getLogger(SocketConnection.class);
	private final SocketChannel socketChannel;
	private final Queue<W> queue;
	private final CountDownLatch registerLatch;
	private com.jim.common.socket.SocketProcessor socketProcessor;
	private SelectionKey key;
	private Boolean blockingReadFlag;

	private Boolean blockingWriteFlag;

	private boolean nonBlockingMode;

	public SocketConnection(String name, com.jim.common.io.MessageReader<R> reader, com.jim.common.io.MessageWriter<W> writer,
                            SocketChannel socketChannel) {
		super(name, reader, writer);
		this.socketChannel = socketChannel;
		queue = new ConcurrentLinkedQueue<W>();
		registerLatch = new CountDownLatch(1);
		blockingReadFlag = true;
		blockingWriteFlag = true;
		nonBlockingMode = false;
	}

	public void register(com.jim.common.socket.SocketProcessor socketProcessor) {
		this.socketProcessor = socketProcessor;
		try {
			key = socketChannel.register(socketProcessor.getSelector(), 0, this);
		} catch (Exception e) {
			close();
		}
		registerLatch.countDown();
	}

	@Override
	public void close() {
		logger.info("{}关闭连接。", getName());
		queue.clear();
		try {
			socketChannel.close();
		} catch (IOException e) {
			logger.warn(getName() + "关闭时发生错误。", e);
		}
		if (key != null) {
			key.cancel();
			key.attach(null);
		}
	}

	public void readyForRead() {
		key.interestOps(key.interestOps() | SelectionKey.OP_READ);
		socketProcessor.wakeup();
	}

	public void unreadyForRead() {
		key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
	}

	@Override
	public void writeMessage(W message) {
		synchronized (queue) {
			if (queue.isEmpty()) {
				queue.offer(message);
				if (isReady()) {
					readyForWrite();
				}
			} else {
				queue.offer(message);
			}
		}
	}

	protected boolean isReady() {
		return nonBlockingMode;
	}

	public void readyForWrite() {
		key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
		socketProcessor.wakeup();
	}

	@Override
	protected W getNextMessage() {
		return queue.poll();
	}

	@Override
	protected void writeComplete() {
		synchronized (queue) {
			if (queue.isEmpty()) {
				key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
			}
		}
	}

	public void awaitRegister() {
		while (true) {
			try {
				registerLatch.await();
				break;
			} catch (InterruptedException e) {
				logger.warn(getName() + "等待注册过程中线程被中断，忽略中断继续等待。", e);
			}
		}
		if (key == null) {
			throw new RuntimeException(getName() + "发生异常，不能正常使用。");
		}
	}

	@Override
	public R blockingRead(int timeout) throws com.jim.common.exception.TimeoutException {
		synchronized (blockingReadFlag) {
			if (!blockingReadFlag) {
				throw new RuntimeException(getName() + "已转入非阻塞模式。");
			}
			return super.blockingRead(timeout);
		}
	}

	@Override
	public void blockingWrite(W message, int timeout) throws com.jim.common.io.exception.BlockingWriteTimeoutException {
		synchronized (blockingWriteFlag) {
			if (!blockingWriteFlag) {
				throw new RuntimeException(getName() + "已转入非阻塞模式。");
			}
			super.blockingWrite(message, timeout);
		}
	}

	public void startNonBlockingMode() {
		// if (handler == null) {
		// throw new RuntimeException(getName() + "消息处理器没有正常初始化。");
		// }

		synchronized (blockingReadFlag) {
			blockingReadFlag = false;
		}

		synchronized (blockingWriteFlag) {
			blockingWriteFlag = false;
		}

		nonBlockingMode = true;
		if (!queue.isEmpty()) {
			readyForWrite();
		}
		readyForRead();

		logger.debug("{}转成非阻塞模式.", getName());
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

}
