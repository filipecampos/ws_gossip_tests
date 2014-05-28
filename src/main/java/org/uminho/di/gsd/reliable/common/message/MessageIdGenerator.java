package org.uminho.di.gsd.reliable.common.message;

import java.util.concurrent.atomic.AtomicLong;

public class MessageIdGenerator {

	private AtomicLong counter;
	private String deviceId;

	public MessageIdGenerator(String deviceId)
	{
		counter = new AtomicLong();
		this.deviceId = deviceId;
	}

	public synchronized String getNewMessageId()
	{
		return deviceId + ":" + counter.getAndIncrement();
	}

}
