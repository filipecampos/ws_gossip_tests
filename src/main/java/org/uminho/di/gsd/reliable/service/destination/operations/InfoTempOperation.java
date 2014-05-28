package org.uminho.di.gsd.reliable.service.destination.operations;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.CommunicationProtocol;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.common.message.MessageSequenceDestination;
import org.uminho.di.gsd.reliable.service.destination.DestinationService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class InfoTempOperation extends DestinationOperation
{
	static Logger logger = Logger.getLogger(InfoTempOperation.class);

	public InfoTempOperation(DestinationService svc)
	{
		super(WSRM_Constants.infoTempOpName, WSRM_Constants.wsrmDestinationServiceQName, svc);

		//We define the input for this method.
		Element infoTemp = new Element(WSRM_Constants.infoTempElementQName);

		ComplexType complexType = new ComplexType(WSRM_Constants.infoTempComplexTypeElementQName, ComplexType.CONTAINER_SEQUENCE);

		complexType.addElement(new Element(WSRM_Constants.infoTempValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_DOUBLE)));
		// Our message Id
		complexType.addElement(new Element(WSRM_Constants.msgIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		// WSRM specific
		complexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		complexType.addElement(new Element(WSRM_Constants.sequenceMsgNumberValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_INTEGER)));
		complexType.addElement(new Element(WSRM_Constants.sequenceAckRequestedValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_BOOLEAN)));

		infoTemp.setType(complexType);

		setInput(infoTemp);
	}

	/**
	 * We don't want to answer - therefore null is returned.
	 */
	public ParameterValue invoke(ParameterValue pv) throws InvocationException, TimeoutException
	{
		long nanoTime = System.nanoTime();
		long millisTime = System.currentTimeMillis();

		common_invoke(CommunicationProtocol.TCP, nanoTime, millisTime, pv);

		return null;
	}

	public void common_invoke(CommunicationProtocol communicationProtocol, long nanoTime, long millisTime, ParameterValue pv) {
		String tempValue = pv.getValue(WSRM_Constants.infoTempValueElementName);
		String msgId = pv.getValue(WSRM_Constants.msgIdValueElementName);
		String sequenceId = pv.getValue(WSRM_Constants.sequenceIdValueElementName);
		String sequenceMsgNumber = pv.getValue(WSRM_Constants.sequenceMsgNumberValueElementName);
		String sequenceAckRequested = pv.getValue(WSRM_Constants.sequenceAckRequestedValueElementName);

		logger.debug("[" + WSRM_Constants.wsrmServiceName
				+ "] New Temperature Information: " + tempValue
				+ ";MsgId: " + msgId
				+ ";SequenceId: " + sequenceId
				+ ";SequenceMessageNumber: " + sequenceMsgNumber
				+ ";SequenceAckRequested: " + sequenceAckRequested);

		// store sequence msg number
		int seqMsgNum = Integer.parseInt(sequenceMsgNumber);
		int msgIdInt = Integer.parseInt(msgId);

		service.receivedMessage(sequenceId, seqMsgNum, msgIdInt-1, nanoTime);

		boolean ackRequested = Boolean.parseBoolean(sequenceAckRequested);

		if(ackRequested)
		{
			MessageSequenceDestination msgSeqDest = service.getRecMessages(sequenceId);

			synchronized(msgSeqDest)
			{
				ArrayList<Integer> receivedMessages = msgSeqDest.getReceivedMessages();

				if(!receivedMessages.isEmpty())
				{
					// Invoke sequenceAcknowledgment
					boolean ok = service.getAckClient().ackMessages(sequenceId, receivedMessages);

					if (ok) {
						msgSeqDest.acknowledgedMessages(receivedMessages);
					}
					else
						logger.error("There was some problem invoking ackMessages!");
				}
			}

		}
	}
}
