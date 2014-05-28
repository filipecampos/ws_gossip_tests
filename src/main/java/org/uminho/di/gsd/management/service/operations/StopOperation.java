package org.uminho.di.gsd.management.service.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.schema.Element;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class StopOperation extends ManagementOperation {

	static Logger logger = Logger.getLogger(StopOperation.class);

	public StopOperation(ManagementService svc)
	{
		super(Constants.StopOperationName, svc);

		initInput();
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {

		logger.debug("Going to stop device...");
		// invoke stopDevice on device
		ManagedDevice device = getService().getDevice();

		if(device != null)
			device.stopDevice();

		return null;
	}

	@Override
	protected void initInput() {
		Element in = new Element(Constants.StopRequestQName);
		setInput(in);
	}

	@Override
	protected void initOutput() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
