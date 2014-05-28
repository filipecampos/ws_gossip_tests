package org.uminho.di.gsd.reliable.service;

import org.uminho.di.gsd.reliable.device.CommonDevice;
import org.ws4d.java.service.DefaultService;

public abstract class ManagedService extends DefaultService {

	protected String serviceName;
	protected CommonDevice device;

	public ManagedService(CommonDevice dvc)
	{
		super();
		device = dvc;
	}

	public abstract void initializeOperations();
}
