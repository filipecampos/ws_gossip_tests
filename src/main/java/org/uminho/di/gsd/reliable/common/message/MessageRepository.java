package org.uminho.di.gsd.reliable.common.message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.reliable.service.source.operations.workers.MessageSendingThread;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.service.parameter.ParameterValue;

public class MessageRepository extends LinkedHashMap<Long, ParameterValue> {
	static Logger logger = Logger.getLogger(MessageRepository.class);

	ArrayList<Long> nacked;
	ArrayList<Long> acked;
	int buffer_size;

	ArrayList<MessageSendingThread> buffer;

	public MessageRepository(int b)
	{
		super(b);
		buffer_size = b;
		nacked = new ArrayList<Long>(b);
		acked = new ArrayList<Long>(b);
		buffer = new ArrayList<MessageSendingThread>();
	}

	public ArrayList<ParameterValue> getAllMessages()
	{
		return new ArrayList<ParameterValue>(values());
	}

	public ArrayList<Long> getNackedMessages()
	{
		logger.debug("Returning nacked messages: " + nacked);
		ArrayList<Long> ret = (ArrayList<Long>) nacked.clone();
		return ret;
	}

	// should be invoked only the first time the message is sent
	public void storeMessage(Long msgNum, ParameterValue pv)
	{
		put(msgNum, pv);
		nacked.add(msgNum);
	}

	public ParameterValue getMessage(Long msgNum)
	{
		return this.get(msgNum);
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<Long,ParameterValue> eldest)
	{
		return size() > buffer_size;
	}

	public ArrayList<ParameterValue> getMessages(ArrayList<Long> list) {
		ArrayList<ParameterValue> messages = new ArrayList<ParameterValue>(list.size());

		for(Long index : list)
		{
			messages.add(get(index));
		}

		return messages;
	}

	public void ackMessages(ArrayList<Long> list) {
		logger.debug("Acked " + list + " messages!");
		for(Long num : list)
		{
			if(nacked.remove(num))
				logger.debug("Removed " + num + " from nackedMessages!");
			else
				logger.debug("Did not remove " + num + " from nackedMessages!");
		}
	}

	public void addToBuffer(SOAPoverUDPServer sender, String targetIp, int targetPort, byte[] msg, int length, int i, String seqId) {
		MessageSendingThread thread = new MessageSendingThread(sender, targetIp, targetPort, msg, length, i, seqId);

		buffer.add(thread);
	}
}
