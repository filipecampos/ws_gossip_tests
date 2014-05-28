package org.uminho.di.gsd.reliable.service.destination.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.service.destination.DestinationService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class CreateSequenceOperation extends DestinationOperation
{
	static Logger logger = Logger.getLogger(CreateSequenceOperation.class);

	public CreateSequenceOperation(DestinationService svc)
	{
		super(WSRM_Constants.createSequenceOpName, WSRM_Constants.wsrmDestinationServiceQName, svc);

		Element createSequenceIn = new Element(WSRM_Constants.createSequenceElementQName);

		ComplexType complexType = new ComplexType(WSRM_Constants.createSequenceComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);

		complexType.addElement(new Element(WSRM_Constants.acksToElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));
		complexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));

		createSequenceIn.setType(complexType);

		this.setInput(createSequenceIn);

		Element createSequenceResponseOut = new Element(WSRM_Constants.createSequenceResponseElementQName);

		ComplexType responseComplexType = new ComplexType(WSRM_Constants.createSequenceResponseComplexTypeQName, ComplexType.CONTAINER_SEQUENCE);

		responseComplexType.addElement(new Element(WSRM_Constants.sequenceIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));

		createSequenceResponseOut.setType(responseComplexType);

		this.setOutput(createSequenceResponseOut);
	}

	@Override
	public ParameterValue invoke(ParameterValue pv) throws InvocationException, TimeoutException
	{
		String acksTo = pv.getValue(WSRM_Constants.acksToElementName);
		String sequenceId = pv.getValue(WSRM_Constants.sequenceIdValueElementName);

		service.initializeSequence(sequenceId, acksTo);
		logger.info("CreateSequenceOperation - AckClient Created!");


		service.createRecMessages(sequenceId);
		logger.info("CreateSequenceOperation - RecMessages Created!");

		// everything ok, so return sequenceId
		ParameterValue response = createOutputValue();
		response.setValue(WSRM_Constants.sequenceIdValueElementName, sequenceId);

		return response;
	}
}
