package com.jim.common.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageDriverBase<R, W> extends BlockingMessageChannelBase<R, W> implements MessageDriver<R, W> {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.io.MessageDriverBase.class);

	private W currentMessage;

	private NonBlockingMessageChannel<R, W> messageChannel;

	public MessageDriverBase(String name, MessageReader<R> reader, MessageWriter<W> writer) {
		super(name, reader, writer);
	}

	@Override
	public void read() {
		logger.debug("{}读取数据开始。", getName());
		try {
			while (true) {
				getReader().read();
				boolean completed = getReader().isMessageCompleted();
				if (completed) {
					logger.debug("{}读取到一条消息。", getName());
					messageChannel.onMessage(getReader().getMessage());
				}
				if (getReader().isReadFinished()) {

					if (logger.isDebugEnabled()) {
						if (completed) {
							logger.debug("{}读取数据结束。", getName());
						} else {
							logger.debug("{}读取数据结束，当前消息未读完。", getName());
						}
					}

					return;
				}
			}
		} catch (Exception e) {
			logger.debug("{}读取时发生异常，将进行异常处理并关闭消息通道。", getName());
			messageChannel.handleError(e, null);
			messageChannel.close();
		}

	}

	public void write() {
		logger.debug("{}写入数据开始。", getName());
		try {
			if (!getWriter().isMessageCompleted()) {
				logger.debug("{}前一消息未写完，继续写入。", getName());
				getWriter().write();
			}

			while (getWriter().isMessageCompleted()) {
				currentMessage = getNextMessage();
				if (currentMessage == null) {
					writeComplete();
					logger.debug("{}写入数据结束。", getName());
					return;
				}
				logger.debug("{}取得下一条消息，准备写入。", getName());
				getWriter().setMessage(currentMessage);
				getWriter().write();
			}
			logger.debug("{}写入数据结束，当前消息未写完。", getName());
		} catch (Exception e) {
			logger.debug("{}写入时发生异常，将进行异常处理并关闭消息通道。", getName());
			messageChannel.handleError(e, currentMessage);
			messageChannel.close();
		}
	}

	protected abstract void writeComplete();

	protected abstract W getNextMessage();

	@Override
	public void setMessageChannel(NonBlockingMessageChannel<R, W> messageChannel) {
		this.messageChannel = messageChannel;
	}

}
