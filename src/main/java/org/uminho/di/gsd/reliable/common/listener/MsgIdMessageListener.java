package org.uminho.di.gsd.reliable.common.listener;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.dispatch.MessageListener;
import org.ws4d.java.message.Message;

public class MsgIdMessageListener implements MessageListener
{

	static Logger logger = Logger.getLogger(MsgIdMessageListener.class);

	String myName = "[MsgIdMessageListener]";
	StringBuilder sb;
	int i = 0;

	public MsgIdMessageListener()
	{
		sb = new StringBuilder();

	}

	public void receivedInboundMessage(Message msg, ProtocolData pd) {
		sb.append(myName);
		sb.append(" Receiving...\n");

		sb.append(WSRM_Constants.messageToString(msg, Message.INVOKE_MESSAGE));

		logger.debug(sb.toString());

		sb.delete(0, sb.length());
	}

	public void receivedOutboundMessage(Message msg, ProtocolData pd) {
		sb.append(myName);
		sb.append(" Sending...\n");

		sb.append(WSRM_Constants.messageToString(msg, Message.INVOKE_MESSAGE));

		logger.debug(sb.toString());

		sb.delete(0, sb.length());
	}

}
