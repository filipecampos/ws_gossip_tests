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

public class GetStatsOperation extends ManagementOperation {

	static Logger logger = Logger.getLogger(GetStatsOperation.class);

	public GetStatsOperation(ManagementService svc) {
		super(Constants.GetStatsOperationName, svc);

		initInput();
		initOutput();
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {
		// get stats to return to manager
		ManagedDevice device = getService().getDevice();
		String response = "";

		if (device != null) {
			logger.debug(device.getEndpoint() + " is going to get stats...");
			response = device.getStats();
		} else {
			logger.error("Couldn't write stats!");
		}

		logger.debug("Creating return message...");
		ParameterValue ret = createOutputValue();
		ret.setValue(response);
		logger.debug("Created return message.");

		return ret;
	}

	@Override
	protected void initInput() {
	}

	@Override
	protected void initOutput() {
		Element out = new Element(Constants.GetStatsResponseQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_STRING));
		setOutput(out);
	}
}
