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

public class TerminateSequenceOperation extends DestinationOperation
{
	public TerminateSequenceOperation(DestinationService svc)
	{
		super(WSRM_Constants.terminateSequenceOpName, WSRM_Constants.wsrmDestinationServiceQName, svc);

		Element terminateSequenceIn = new Element(WSRM_Constants.terminateSequenceElementQName);

		ComplexType complexType = new ComplexType(WSRM_Constants.terminateSequenceComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);
		complexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		terminateSequenceIn.setType(complexType);

		this.setInput(terminateSequenceIn);

		Element terminateSequenceResponseOut = new Element(WSRM_Constants.terminateSequenceResponseElementQName);

		ComplexType responseComplexType = new ComplexType(WSRM_Constants.terminateSequenceResponseComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);
		responseComplexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		terminateSequenceResponseOut.setType(responseComplexType);

		this.setOutput(terminateSequenceResponseOut);
	}

	@Override
	public ParameterValue invoke(ParameterValue pv) throws InvocationException, TimeoutException
	{
		String sequenceId = pv.getValue(WSRM_Constants.sequenceIdValueElementName);
		if(service != null)
		{
			if(service.getRecMessages(sequenceId) != null)
			{
				ArrayList<Integer> messages = service.getRecMessages(sequenceId).getReceivedMessages();
				System.out.println("Received the following messages: ");
				for(Integer i : messages)
					System.out.println(i);
				service.resetRecMessages();
			}
		}
		ParameterValue response = createOutputValue();
		response.setValue(WSRM_Constants.sequenceIdValueElementName, sequenceId);

		return response;
	}
}
