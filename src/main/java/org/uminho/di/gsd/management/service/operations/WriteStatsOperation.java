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

public class WriteStatsOperation extends ManagementOperation {

	static Logger logger = Logger.getLogger(WriteStatsOperation.class);

	public WriteStatsOperation(ManagementService svc) {
		super(Constants.WriteStatsOperationName, svc);

		initInput();
		initOutput();
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {
		// get file name to write o
		String filename = parameterValue.getValue();

		ManagedDevice device = getService().getDevice();

		if (device != null) {
			logger.debug(device.getEndpoint() + " is going to write stats on " + filename);
			device.writeStats(filename);
		} else {
			logger.error("Couldn't write stats!");
		}

		logger.debug("Returning...");

		return null;
	}

	@Override
	protected void initInput() {
		Element writeStatsElement = new Element(Constants.FilenameElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI));

		setInput(writeStatsElement);
	}

	@Override
	protected void initOutput() {
	}
}
