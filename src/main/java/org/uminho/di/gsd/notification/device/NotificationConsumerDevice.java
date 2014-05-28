package org.uminho.di.gsd.notification.device;

import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.common.device.BasicDevice;
import org.uminho.di.gsd.notification.client.NotificationConsumerClient;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.util.Log;

public class NotificationConsumerDevice extends BasicDevice {

	static Logger logger = Logger.getLogger(NotificationConsumerDevice.class);
	protected NotificationConsumerClient client;

	private String producer_ip;

	public NotificationConsumerDevice() {
		super();
	}

	public NotificationConsumerClient getClient() {
		return client;
	}

	public void initializeClient() {
		client = new NotificationConsumerClient();
		client.setDevice(this);
	}

	public static void main(String[] args) {
		if (args.length >= 5) {
			RunConstants constants = new RunConstants(args);

			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			// always start the framework first
			DPWSFramework.start(args);

//            Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
			Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);

			// create a simple device ...
			NotificationConsumerDevice device = new NotificationConsumerDevice();
			device.setConstants(constants);
			device.initializeBinding();

			FileWriter fw = null;

			try {
				File file = new File("notification" + constants.getFileName() + ".csv");
				fw = new FileWriter(file, true);
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}

			device.setFileWriter(fw);

			// no service

			device.startDevice();

			device.initializeClient();

			Console cons = null;
			if ((cons = System.console()) != null) {
				cons.readLine();
			}

			device.stopDevice();

			DPWSFramework.stop();
		}
	}

	public String getProducerIp() {
		if(simulated)
			producer_ip = "10.0.0.2";
		else
			producer_ip = IP;

		return producer_ip;
	}
}
