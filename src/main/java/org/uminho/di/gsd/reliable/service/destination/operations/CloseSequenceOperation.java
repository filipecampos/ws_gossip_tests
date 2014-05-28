package org.uminho.di.gsd.reliable.service.destination.operations;

import java.util.ArrayList;

import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.service.destination.DestinationService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class CloseSequenceOperation extends DestinationOperation
{

	public CloseSequenceOperation(DestinationService svc)
	{
		super(WSRM_Constants.closeSequenceOpName, WSRM_Constants.wsrmDestinationServiceQName, svc);

		Element closeSequenceIn = new Element(WSRM_Constants.closeSequenceElementQName);
		ComplexType complexType = new ComplexType(WSRM_Constants.closeSequenceComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);
		complexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		closeSequenceIn.setType(complexType);
		this.setInput(closeSequenceIn);

		Element closeSequenceResponseOut = new Element(WSRM_Constants.createSequenceResponseElementQName);
		ComplexType responseComplexType = new ComplexType(WSRM_Constants.createSequenceResponseComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);
		responseComplexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		closeSequenceResponseOut.setType(responseComplexType);
		this.setOutput(closeSequenceResponseOut);
	}

	@Override
	public ParameterValue invoke(ParameterValue pv) throws InvocationException, TimeoutException
	{
		String sequenceId = pv.getValue(WSRM_Constants.sequenceIdValueElementName);

		ArrayList<Integer> messages = service.getRecMessages(sequenceId).getReceivedMessages();
		System.out.println("Received the following messages: ");
		for(Integer i : messages)
			System.out.println(i);
		service.resetRecMessages();

		ParameterValue response = createOutputValue();
		response.setValue(WSRM_Constants.sequenceIdValueElementName, sequenceId);

		return response;
	}
}
