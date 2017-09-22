package com.jim.common.io;

import com.jim.common.Processor;
import com.jim.common.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChannelHeartbeatMonitor extends Processor implements ChannelMonitor {

	private static final Logger logger = LoggerFactory.getLogger(com.jim.common.io.ChannelHeartbeatMonitor.class);

	private final int timePeriod;

	private final List<HeartbeatMessageChannel<?, ?>> list;

	private final List<HeartbeatMessageChannel<?, ?>> tempAddList;

	private final List<HeartbeatMessageChannel<?, ?>> tempRemoveList;

	public ChannelHeartbeatMonitor(String name, int timePeriod) {
		super(name);
		list = new ArrayList<HeartbeatMessageChannel<?, ?>>();
		tempAddList = new ArrayList<HeartbeatMessageChannel<?, ?>>();
		tempRemoveList = new ArrayList<HeartbeatMessageChannel<?, ?>>();
		this.timePeriod = timePeriod;
	}

	public ChannelHeartbeatMonitor(int timePeriod) {
		this("心跳监控", timePeriod);
	}

	public void addChannel(MonitorableChannel<?, ?> channel) {
		synchronized (tempAddList) {
			tempAddList.add((HeartbeatMessageChannel<?, ?>) channel);
		}
	}

	public void removeChannel(MonitorableChannel<?, ?> channel) {
		synchronized (tempRemoveList) {
			tempRemoveList.add((HeartbeatMessageChannel<?, ?>) channel);
		}
	}

	protected boolean process() throws Exception {
		synchronized (tempAddList) {
			list.addAll(tempAddList);
			tempAddList.clear();
		}
		for (HeartbeatMessageChannel<?, ?> channel : list) {
			ChannelStatus channelStatus = channel.getChannelStatus();
			// logger.debug("检查通道状态，{}：{}", channel.getName(), channelStatus);

			if (channelStatus.isClosed()) {
				tempRemoveList.add(channel);
				continue;
			}

			long currentTime = System.currentTimeMillis();
			// logger.debug("当前系统时间：{}", currentTime);

			boolean readHeartbeatTimeout = false;
			boolean writeHeartbeatTimeout = false;

			if (channel.needCheckRead()
					&& currentTime - channelStatus.getLastReadTime() >= channel.getHeartbeatTimePeriod()) {
				logger.debug("通道{}超过{}毫秒没有读取到数据。", channel.getName(), channel.getHeartbeatTimePeriod());
				readHeartbeatTimeout = true;
				channel.onReadHeartbeatTimeout();
			}

			if (channel.needCheckWrite()
					&& currentTime - channelStatus.getLastWriteTime() >= channel.getHeartbeatTimePeriod()) {
				logger.debug("通道{}超过{}毫秒没有发送过数据。", channel.getName(), channel.getHeartbeatTimePeriod());
				writeHeartbeatTimeout = true;
				channel.onWriteHeartbeatTimeout();
			}

			if ((readHeartbeatTimeout | writeHeartbeatTimeout) && channel.isChannelDown()) {
				logger.error("通道{}心跳异常，关闭连接。", channel.getName());
				tempRemoveList.add(channel);
				channel.handleError(new TimeoutException("心跳超时。"), null);
				channel.close();
				continue;
			}

		}
		synchronized (tempRemoveList) {
			list.removeAll(tempRemoveList);
			tempRemoveList.clear();
		}

		if (timePeriod > 0) {
			Thread.sleep(timePeriod);
		}

		return true;
	}

}
