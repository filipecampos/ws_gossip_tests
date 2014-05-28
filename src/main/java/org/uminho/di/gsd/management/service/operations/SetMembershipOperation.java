package org.uminho.di.gsd.management.service.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class SetMembershipOperation extends ManagementOperation {

	static Logger logger = Logger.getLogger(SetMembershipOperation.class);

	public SetMembershipOperation(ManagementService svc)
	{
		super(Constants.SetMembershipOperationName, svc);

		initInput();
		initOutput();
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {
		ManagedDevice device = getService().getDevice();

		if(device != null)
		{
			// get list of target endpoints
			String prefix = Constants.EndpointElementName;
			int count = parameterValue.getChildrenCount(prefix);
			logger.debug("Got " + count + " new targets");

			if(count > 0)
			{
				String[] targets = new String[count];

				for(int i=0; i < count; i++)
				{
					String indexed_prefix = prefix + "[" + i + "]";
					targets[i] = parameterValue.getValue(indexed_prefix);
				}

				device.setMembership(targets);
			}
		}

		return createOutputValue();
	}

	@Override
	protected void initInput() {
		Element endpoint = new Element(Constants.EndpointElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI));
		// set unlimited number of endpoint elements
		endpoint.setMaxOccurs(-1);
		ComplexType targetsListType = new ComplexType(Constants.TargetsListTypeQName, ComplexType.CONTAINER_SEQUENCE);
		targetsListType.addElement(endpoint);

		ComplexType setMembershipType = new ComplexType(Constants.SetMembershipRequestTypeQName, ComplexType.CONTAINER_SEQUENCE);
		setMembershipType.addElement(new Element(Constants.TargetsListElementQName, targetsListType));

		Element in = new Element(Constants.SetMembershipRequestMessageQName, targetsListType);
		setInput(in);
	}

	@Override
	protected void initOutput() {
		Element out = new Element(Constants.SetMembershipResponseMessageQName);
		setOutput(out);
	}

}
