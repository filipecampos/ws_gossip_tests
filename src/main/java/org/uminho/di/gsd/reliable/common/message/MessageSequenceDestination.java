package org.uminho.di.gsd.reliable.common.message;

import java.util.ArrayList;

public class MessageSequenceDestination {

	private ArrayList<Integer> receivedMessages;


	public MessageSequenceDestination()
	{
		receivedMessages = new ArrayList<Integer>();
	}

	public synchronized void receivedMessage(Integer num)
	{
		if(!receivedMessages.contains(num))
			receivedMessages.add(num);
	}

	public synchronized ArrayList<Integer> getReceivedMessages()
	{
		return (ArrayList<Integer>) receivedMessages.clone();
	}

	public synchronized void acknowledgedMessages(ArrayList<Integer> nums)
	{
		receivedMessages.removeAll(nums);
	}
}
