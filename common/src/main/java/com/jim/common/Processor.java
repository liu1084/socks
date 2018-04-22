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

package com.jim.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Processor implements com.jim.common.Controllable {

	private static final Logger logger = LoggerFactory.getLogger(Processor.class);
	private final AtomicBoolean alive;
	private final Map<String, Object> context;
	private final String name;
	private CountDownLatch startupLatch;
	private CountDownLatch shutdownLatch;
	private Thread thread;
	private long startupTime;

	private long shutdownTime;

	public Processor(String name) {
		this.name = name;
		alive = new AtomicBoolean(false);
		context = new HashMap<String, Object>();
	}

	@Override
	public final synchronized void startup() {
		if (thread != null && thread.isAlive()) {
			throw new RuntimeException(name + "已经启动。");
		}

		logger.info("{}准备启动。", name);

		startupLatch = new CountDownLatch(1);
		shutdownLatch = new CountDownLatch(1);

		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				logger.debug("{}线程开始。", name);
				init();

				alive.set(true);
				startupLatch.countDown();

				while (isRunning()) {
					try {
						if (!process()) {
							alive.set(false);
							break;
						}
					} catch (Exception e) {
						if (!handleError(e)) {
							alive.set(false);
							break;
						}
					}
				}

				destory();

				shutdownLatch.countDown();

				thread = null;
				context.clear();
				shutdownTime = System.currentTimeMillis();

				logger.debug("{}线程结束。", name);
			}
		});
		thread.start();
		awaitStartup();
		startupTime = System.currentTimeMillis();

		logger.info("{}启动完毕。", name);
	}

	@Override
	public final synchronized void shutdown() {
		if (!isRunning()) {
			return;
		}

		logger.info("{}停止。", name);

		alive.set(false);
		wakeup();
		while (true) {
			try {
				shutdownLatch.await();
				break;
			} catch (InterruptedException e) {
				logger.warn("关闭过程中线程被中断，忽略中断继续关闭。");
			}
		}

		logger.info("{}停止完毕。", name);
	}

	@Override
	public synchronized Object execute(String executeName, String[] args) {
		logger.debug("{}执行命令，命令名：{}，参数：{}", name, executeName, args);
		if ("status".equals(executeName)) {
			return getStatus();
		} else {
			//FIXME!!
			return executeOthers(executeName, args);
		}
	}

	protected com.jim.common.ProcessorStatus getStatus() {
		com.jim.common.ProcessorStatus status = new com.jim.common.ProcessorStatus();
		status.setName(name);
		boolean running = isRunning();
		status.setRunning(running);
		status.setStartupTime(new Date(startupTime));
		if (!running && shutdownTime > startupTime) {
			status.setShutdownTime(new Date(shutdownTime));
		}
		return status;
	}

	protected Object executeOthers(String executeName, String[] args) {
		throw new RuntimeException(name + "不支持该执行命令，命令：" + executeName);
	}

	public final void awaitStartup() {
		while (true) {
			try {
				startupLatch.await();
				break;
			} catch (InterruptedException e) {
				logger.warn("等待过程中线程被中断，忽略中断继续等待。");
			}
		}
	}

	protected final boolean isRunning() {
		return alive.get();
	}

	protected boolean handleError(Exception e) {
		logger.error(name + "线程发生异常，忽略异常，继续执行。", e);
		return true;
	}

	protected void init() {
	}

	protected abstract boolean process() throws Exception;

	protected void wakeup() {
		logger.debug("使用中断唤醒{}。", name);
		thread.interrupt();
	}

	protected void destory() {
	}

	protected Map<String, Object> getContext() {
		return context;
	}

	public String getName() {
		return name;
	}

}
