package org.uminho.di.gsd.management.service.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;

public class StartDisseminationOperation extends ManagementOperation {

	static Logger logger = Logger.getLogger(StartDisseminationOperation.class);

	public StartDisseminationOperation(ManagementService svc) {
		super(Constants.StartDisseminationOperationName, svc);

		initInput();
	}

	@Override
	public ParameterValue invoke(ParameterValue parameterValue) throws InvocationException, TimeoutException {
		long start = System.currentTimeMillis();
		logger.debug("Going to start dissemination...");

		logger.debug("But first going to notify consumers to start their workers...");
		this.getService().fireStartWorkersNotification();
		logger.debug("Consumers notified.");

		try {
			Runnable producer = (Runnable) getService().getDevice();

			if (producer != null) {
				DPWSFramework.getThreadPool().execute(producer);
			} else {
				logger.error("No client to start dissemination...");
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		long time = System.currentTimeMillis() - start;

		logger.info("Returning from StartDissemination Op. Took " + time + "ms.");

		return null;
	}

	@Override
	protected void initInput() {
	}

	@Override
	protected void initOutput() {
	}
}
