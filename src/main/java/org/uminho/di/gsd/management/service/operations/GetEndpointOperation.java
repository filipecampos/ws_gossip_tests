package org.uminho.di.gsd.management.service.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class GetEndpointOperation extends ManagementOperation {

	static Logger logger = Logger.getLogger(GetEndpointOperation.class);

	public GetEndpointOperation(ManagementService svc)
	{
		super(Constants.GetEndpointOperationName, svc);

		initOutput();
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {
		String endpoint = "";
		ManagedDevice device = getService().getDevice();

		if(device != null)
			endpoint = device.getEndpoint();

		ParameterValue response = createOutputValue();
		response.setValue(endpoint);

		return response;
	}

	@Override
	protected void initInput() {
	}

	@Override
	protected void initOutput() {
		Element element = new Element(Constants.EndpointElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI));

		setOutput(element);
	}

}
