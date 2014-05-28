package org.uminho.di.gsd.notification.device;

import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.common.device.BasicDevice;
import org.uminho.di.gsd.notification.service.NotificationService;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

public class NotificationProducerDevice extends BasicDevice {

	static Logger logger = Logger.getLogger(NotificationProducerDevice.class);
	protected NotificationService notificationService;
	int counter = 0;
	int port;
	protected long[] stats;
	public static final long MIN_TIME_TO_WAIT = 2000;

	public NotificationProducerDevice() {
		super();
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}

	public void initializeNotificationService() {
		notificationService = new NotificationService();
		notificationService.addBinding(new HTTPBinding(new URI("http://" + IP + ":" + PORT + "/notification/service")));

		port = Integer.parseInt(PORT);
		this.addService(notificationService);
	}

	@Override
	public void startDevice() {
		stats = new long[getConstants().getMessages()];
		this.startNotificationService();

		super.startDevice();
	}

	protected void startNotificationService() {
		try {
			notificationService.start();
		} catch (IOException ex) {
			logger.error(ex);
		}
	}

	public void fireNotification(double value) {
		long currentTime = notificationService.fireNotification(value, port, counter);
		stats[counter++] = currentTime;
	}

	@Override
	public void writeStats() {
		if (fileWriter != null) {
			StringBuffer sb = new StringBuffer("\nProducer;");
			sb.append(PORT);
			sb.append(';');
			for (int i = 0; i < getConstants().getMessages(); i++) {
				sb.append(stats[i]);
				sb.append(';');
			}
			try {
				fileWriter.write(sb.toString());
				fileWriter.flush();
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length >= 5) {
			RunConstants constants = new RunConstants(args);

			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			FileWriter fw = null;

			try {
				File file = new File("notification" + constants.getFileName() + ".csv");
				fw = new FileWriter(file, true);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}

			// always start the framework first
			DPWSFramework.start(args);

//            Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
			Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);

			// create a simple device ...
			NotificationProducerDevice device = null;
			try {
				device = new NotificationProducerDevice();

				EndpointReference epr = new EndpointReference(new URI("urn:uuid:f1e19260-39b9-11e0-bfb0-0d31660cf9e2"));
				device.setEndpointReference(epr);

				device.setConstants(constants);

				device.setFileWriter(fw);

				device.initializeBinding();

				// ... and a service
				device.initializeNotificationService();


				device.startDevice();

				Console cons = null;
				if ((cons = System.console()) != null) {
					cons.readLine();
				} else {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException ex) {
						logger.error(ex.getMessage(), ex);
					}
				}

				for (int i = 0; i < constants.getMessages(); i++) {
					try {
						device.fireNotification(5.5d);
						Thread.sleep(constants.getTimeInterval());

					} catch (InterruptedException e) {
						logger.error(e.getMessage(), e);
					}
				}

				device.writeStats();
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

			DPWSFramework.stop();
		}
	}
}
