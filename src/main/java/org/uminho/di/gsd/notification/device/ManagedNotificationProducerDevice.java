package org.uminho.di.gsd.notification.device;

import java.io.File;
import java.io.FileWriter;
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

public class ManagedNotificationProducerDevice extends NotificationProducerDevice implements Runnable, ManagedDevice {

	static Logger logger = Logger.getLogger(ManagedNotificationProducerDevice.class);
	protected ManagementService managementService;

	public ManagedNotificationProducerDevice() {
		super();
	}

	public void initializeManagementService() {
		managementService = new ManagementService();
		managementService.addBinding(new HTTPBinding(new URI("http://" + IP + ":" + PORT + "/management")));

		managementService.setDevice(this);

		this.addService(managementService);
	}

	@Override
	public void startDevice() {
		stats = new long[getConstants().getMessages()];

		super.startDevice();
	}

	@Override
	public void startServices() {
		super.startServices();

		startManagementService();
		startNotificationService();
	}

	private void startManagementService() {
		try {
			managementService.start();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void run() {

		int iters = getConstants().getMessages();
		long time = getConstants().getTimeInterval();

		logger.info("Started dissemination!");
		for (int i = 0; i < iters; i++) {
			try {
				fireNotification(5.5d);
				logger.debug("Fired " + i + " notification! Should fire " + iters);
				Thread.sleep(time);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}

		logger.info("Ended dissemination!");
		managementService.fireEndDisseminationNotification();
		logger.info("Fired EndDissemination!");
	}

	public static void main(String[] args) throws Exception {
		if (args.length >= 7) {
			RunConstants constants = new RunConstants(args);


			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			// always start the framework first
			int devices = Integer.parseInt(args[5]);
			DPWSFramework.start(new String[] {"" + (3 * devices)});

			//            Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
			Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
			//            Log.setLogLevel(Log.DEBUG_LEVEL_INFO);

			// create a simple device ...
			ManagedNotificationProducerDevice device = null;
			try {
				device = new ManagedNotificationProducerDevice();

				device.setConstants(constants);

				device.initializeBinding();

				// ... and a service
				device.initializeNotificationService();
				device.initializeManagementService();


				device.startDevice();
			} catch (Exception ex) {
				logger.debug(ex.getMessage(), ex);

				device.writeStats();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					logger.error(e.getMessage(), e);
				}
				device.stopDevice();
			}

			//            DPWSFramework.stop();
		}
	}

	@Override
	public void writeStats(String filename) {
		File file = new File(filename);
		try {
			fileWriter = new FileWriter(file, true);

			writeStats();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public String getEndpoint() {
		return "Endpoint";
	}

	@Override
	public void setMembership(String[] targets) {

	}

	@Override
	public void stopDevice()
	{

	}

	@Override
	public String getStats() {
		StringBuilder sb = new StringBuilder("\nProducer;");
		sb.append(PORT);
		sb.append(';');
		for (int i = 0; i < getConstants().getMessages(); i++) {
			sb.append(stats[i]);
			sb.append(';');
		}

		return sb.toString();
	}
}
