package org.uminho.di.gsd.management.service.operations;

import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.service.Operation;

public abstract class ManagementOperation extends Operation {

	private ManagementService service;

	public ManagementOperation(String operationName, ManagementService svc) {
		super(operationName, Constants.ManagementPortQName);
		service = svc;
	}

	@Override
	public ManagementService getService() {
		return service;
	}

	protected abstract void initInput();
	protected abstract void initOutput();
}
