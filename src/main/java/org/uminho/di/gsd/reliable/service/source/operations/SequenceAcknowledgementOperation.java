package org.uminho.di.gsd.reliable.service.source.operations;

import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.CommunicationProtocol;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.service.source.SourceService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.Iterator;

public class SequenceAcknowledgementOperation extends SourceOperation {

	static Logger logger = Logger.getLogger(SequenceAcknowledgementOperation.class);

	public SequenceAcknowledgementOperation(SourceService svc) {
		super(WSRM_Constants.sequenceAcknowledgmentOpName, WSRM_Constants.wsrmSourceServiceQName, svc);

		Element seqAckIn = new Element(WSRM_Constants.sequenceAcknowledgmentElementQName);

		ComplexType complexType = new ComplexType(WSRM_Constants.sequenceAcknowledgmentComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);

		complexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));

		Element ackRange = new Element(WSRM_Constants.acknowledgmentRangeElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_INTEGER));
		ackRange.setMaxOccurs(999);
		ackRange.setMinOccurs(0);
		complexType.addElement(ackRange);

		Element none = new Element(WSRM_Constants.noneElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYTYPE));
		none.setMinOccurs(0);
		complexType.addElement(none);

		Element nack = new Element(WSRM_Constants.nackElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_INTEGER));
		nack.setMinOccurs(0);
		complexType.addElement(nack);


		seqAckIn.setType(complexType);

		this.setInput(seqAckIn);
	}

	public ParameterValue invoke(ParameterValue parameterValues) throws InvocationException, TimeoutException {
		common_invoke(CommunicationProtocol.TCP, parameterValues);

		return null;
	}

	public void common_invoke(CommunicationProtocol communicationProtocol, ParameterValue pv) {
		logger.debug("Received PV: " + pv);

		// mandatory
		String seqId = pv.getValue(WSRM_Constants.sequenceIdValueElementName);

		// alternative elements
		String nack = pv.getValue(WSRM_Constants.nackElementName);

		if (nack != null) {
			logger.debug("Received the following nack " + nack);
			// resend specified messages
			ArrayList<Long> list = new ArrayList<Long>();
			list.add(Long.parseLong(nack));
			service.getClient().sendMessages(seqId, list);
		} else {

			int acksNum = pv.getChildrenCount(WSRM_Constants.acknowledgmentRangeElementName);

			if(acksNum > 0)
			{
				Iterator children = pv.getChildren(WSRM_Constants.acknowledgmentRangeElementName);

				ArrayList<Long> nums = new ArrayList<Long>();
				while (children.hasNext()) {
					ParameterValue val = (ParameterValue) children.next();
					if (val != null) {
						Long num = Long.parseLong(val.getValue());
						nums.add(num);
						logger.debug("ACK: " + val + " Num: " + num);
					}
				}

				// resend missing messages
				service.getClient().ackMessages(seqId, nums);
			}
			else
			{
				// received None which is discarded as it has no value
				service.getClient().sendAllMessages(seqId);
			}
		}
	}
}
