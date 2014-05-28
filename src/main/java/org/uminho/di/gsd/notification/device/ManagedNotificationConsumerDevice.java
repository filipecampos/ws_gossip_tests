package org.uminho.di.gsd.notification.device;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

public class ManagedNotificationConsumerDevice extends NotificationConsumerDevice implements ManagedDevice {

	static Logger logger = Logger.getLogger(ManagedNotificationConsumerDevice.class);

	private ManagementService managementService;

	public ManagedNotificationConsumerDevice() {
		super();
	}

	public ManagementService getManagementService() {
		return managementService;
	}

	public void initializeManagementService() {
		managementService = new ManagementService();
		managementService.addBinding(new HTTPBinding(new URI("http://" + IP + ":" + PORT + "/management")));

		managementService.setDevice(this);

		this.addService(managementService);
	}

	@Override
	public void startServices()
	{
		try {
			managementService.start();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void writeStats() {
		getClient().writeStats();
	}

	public static void main(String[] args) {
		if (args.length >= 5) {
			RunConstants constants = new RunConstants(args);

			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			// always start the framework first
			DPWSFramework.start(new String[] {"50"});

			Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
//            Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);

			// create a simple device ...
			ManagedNotificationConsumerDevice device = new ManagedNotificationConsumerDevice();
			device.setConstants(constants);
			device.initializeBinding();


			// management service
			device.initializeManagementService();

			device.startDevice();


			device.initializeClient();
		}
	}

	@Override
	public String getEndpoint() {
		return "Endpoint";
	}

	@Override
	public void setMembership(String[] targets) {
		client.subscribePublishers(targets);
	}

	@Override
	public void stopDevice()
	{

	}

	@Override
	public String getStats() {
		return getClient().getStats();
	}
}
