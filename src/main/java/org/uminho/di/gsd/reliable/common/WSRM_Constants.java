package org.uminho.di.gsd.reliable.common;

import org.uminho.di.gsd.common.CommunicationProtocol;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.Message;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;

public abstract class WSRM_Constants
{
	public static CommunicationProtocol protocol = CommunicationProtocol.UDP;

	public static int predictableSequenceSize = 50;
	public static int ackEveryXMsgs = 5;

	public static String MY_NAMESPACE = "http://gsd.di.uminho.pt/example/";
	public final static String wsrmServiceName = "WSRM_Service";
	public final static QName wsrmServiceQName = new QName(wsrmServiceName, MY_NAMESPACE);
	public final static String wsrmSourceServiceName = "Source_WSRM_Service";
	public final static QName wsrmSourceServiceQName = new QName(wsrmSourceServiceName, MY_NAMESPACE);
	public final static String wsrmDestinationServiceName = "Destination_WSRM_Service";
	public final static QName wsrmDestinationServiceQName = new QName(wsrmDestinationServiceName, MY_NAMESPACE);
	public final static String temperatureServiceName = "TemperatureService";
	public static String infoTempOpName = "InfoTemp";
	public static URI infoTempOpActionURI = new URI(MY_NAMESPACE, wsrmServiceName + "/" + infoTempOpName);
	public static String infoTempElementName = "NewTemp";
	public static QName infoTempElementQName = new QName(infoTempElementName, MY_NAMESPACE);
	public static String infoTempComplexTypeElementName = "NewTempCType";
	public static QName infoTempComplexTypeElementQName = new QName(infoTempComplexTypeElementName, MY_NAMESPACE);
	public static String infoTempValueElementName = "TempValue";
	public static QName infoTempValueElementQName = new QName(infoTempValueElementName, MY_NAMESPACE);
	public static String msgIdValueElementName = "MsgId";
	public static QName msgIdValueElementQName = new QName(msgIdValueElementName, MY_NAMESPACE);
	public static String sequenceIdValueElementName = "SequenceId";
	public static QName sequenceIdValueElementQName = new QName(sequenceIdValueElementName, MY_NAMESPACE);
	public static String sequenceMsgNumberValueElementName = "MessageNumber";
	public static final QName sequenceMsgNumberValueElementQName = new QName(sequenceMsgNumberValueElementName, MY_NAMESPACE);
	public static String sequenceAckRequestedValueElementName = "AckRequested";
	public static final QName sequenceAckRequestedValueElementQName = new QName(sequenceAckRequestedValueElementName, MY_NAMESPACE);
	public static String createSequenceOpName = "CreateSequence";
	public static String createSequenceElementName = createSequenceOpName;
	public static QName createSequenceElementQName = new QName(createSequenceElementName, MY_NAMESPACE);
	public static String createSequenceComplexTypeName = createSequenceOpName + "Type";
	public static QName createSequenceComplexTypeQName = new QName(createSequenceComplexTypeName, MY_NAMESPACE);
	public static String acksToElementName = "AcksTo";
	public static QName acksToElementQName = new QName(acksToElementName, MY_NAMESPACE);
	public static String createSequenceResponseElementName = "CreateSequenceResponse";
	public static QName createSequenceResponseElementQName = new QName(createSequenceResponseElementName, MY_NAMESPACE);
	public static String createSequenceResponseComplexTypeName = createSequenceResponseElementName + "Type";
	public static QName createSequenceResponseComplexTypeQName = new QName(createSequenceResponseComplexTypeName, MY_NAMESPACE);
	public static String closeSequenceOpName = "CloseSequence";
	public static String closeSequenceElementName = closeSequenceOpName;
	public static QName closeSequenceElementQName = new QName(closeSequenceElementName, MY_NAMESPACE);
	public static String closeSequenceComplexTypeName = closeSequenceOpName + "Type";
	public static QName closeSequenceComplexTypeQName = new QName(closeSequenceComplexTypeName, MY_NAMESPACE);
	public static String closeSequenceResponseElementName = "CloseSequenceResponse";
	public static QName closeSequenceResponseElementQName = new QName(closeSequenceResponseElementName, MY_NAMESPACE);
	public static String closeSequenceResponseComplexTypeName = closeSequenceResponseElementName + "Type";
	public static QName closeSequenceResponseComplexTypeQName = new QName(closeSequenceResponseComplexTypeName, MY_NAMESPACE);
	public static String terminateSequenceOpName = "TerminateSequence";
	public static String terminateSequenceElementName = terminateSequenceOpName;
	public static QName terminateSequenceElementQName = new QName(terminateSequenceElementName, MY_NAMESPACE);
	public static String terminateSequenceComplexTypeName = terminateSequenceOpName + "Type";
	public static QName terminateSequenceComplexTypeQName = new QName(terminateSequenceComplexTypeName, MY_NAMESPACE);
	public static String terminateSequenceResponseElementName = "TerminateSequenceResponse";
	public static QName terminateSequenceResponseElementQName = new QName(terminateSequenceResponseElementName, MY_NAMESPACE);
	public static String terminateSequenceResponseComplexTypeName = terminateSequenceResponseElementName + "Type";
	public static QName terminateSequenceResponseComplexTypeQName = new QName(terminateSequenceResponseComplexTypeName, MY_NAMESPACE);
	public static String sequenceAcknowledgmentOpName = "SequenceAcknowledgment";
	public static String sequenceAcknowledgmentElementName = sequenceAcknowledgmentOpName;
	public static QName sequenceAcknowledgmentElementQName = new QName(sequenceAcknowledgmentElementName, MY_NAMESPACE);
	public static String sequenceAcknowledgmentComplexTypeName = sequenceAcknowledgmentOpName + "Type";
	public static QName sequenceAcknowledgmentComplexTypeQName = new QName(sequenceAcknowledgmentComplexTypeName, MY_NAMESPACE);
	public static String acknowledgmentRangeElementName = "AcknowledgmentRange";
	public static QName acknowledgmentRangeElementQName = new QName(acknowledgmentRangeElementName, MY_NAMESPACE);
	public static String noneElementName = "None";
	public static QName noneElementQName = new QName(noneElementName, MY_NAMESPACE);
	public static String nackElementName = "Nack";
	public static QName nackElementQName = new QName(nackElementName, MY_NAMESPACE);
	public static String subscriberName = "Subscriber";
	public static String temperatureDeviceName = "TemperatureDevice";
	public static String temperatureDeviceClientName = "TemperatureDeviceClient";
	public static int maxNumberOfMessagesToPull = 10;
	// time in milliseconds
	public static long devicePollingPeriod = 5000;
	public static long searchPollingPeriod = 1000;
	public static long serviceSearchPollingPeriod = 2000;
	public static long subscriptionDuration = 500000;
	public static long subscriptionRenovation = 495000;
	public static int numThreadsRenewal = 2;
	// gossip parameters
	public static int rounds = 3;
	public static int fanout = 3;
	public static int buffer = 50;

	public static StringBuilder messageToString(Message msg, int msgType)
	{
		StringBuilder lsb = new StringBuilder();

		if(msgType == msg.getType())
		{
			lsb.append(messageToString(msg));
		}

		return lsb;
	}

	public static StringBuilder messageToString(Message msg)
	{
		StringBuilder lsb = new StringBuilder();

		lsb.append("\nMessage: ");

		SOAPHeader soapHeader = msg.getHeader();
		lsb.append("Header: ").append(soapHeader);

		AttributedURI msgId = msg.getMessageId();
		lsb.append("; MsgId: ").append(msgId);

		URI targetAddress = msg.getTargetAddress();
		lsb.append("; TargetAddress: ").append(targetAddress);

		AttributedURI to = msg.getTo();
		lsb.append("; To: ").append(to);

		int type = msg.getType();
		lsb.append("; Type: ").append(type);

		InvokeMessage iMsg = (InvokeMessage) msg;
		ParameterValue content = iMsg.getContent();
		lsb.append("; Content: ").append(content);
		lsb.append("\n");

		return lsb;
	}

	public static StringBuffer getMetadataMessageToString(Message msg)
	{
		StringBuffer lsb = new StringBuffer();

		lsb.append("\nMessage: ");

		SOAPHeader soapHeader = msg.getHeader();
		lsb.append("Header: ").append(soapHeader);

		AttributedURI msgId = msg.getMessageId();
		lsb.append("; MsgId: ").append(msgId);

		AttributedURI to = msg.getTo();
		lsb.append("; To: ").append(to);

		lsb.append("\n");

		return lsb;
	}
}
