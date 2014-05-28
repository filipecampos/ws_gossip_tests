package org.uminho.di.gsd.management;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.RunConstants;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.util.Log;

public class SimNotificationManager extends SimManager implements Runnable {
	static Logger logger = Logger.getLogger(SimNotificationManager.class);

	public SimNotificationManager() {
		logger.info("Initialized SimNotificationManager!");
	}

	@Override
	protected void setTotalDevices()
	{
		total_devices = num_devices + num_producers;
		runConstants.setTotalDevices(total_devices);
		services = new Service[total_devices];
	}

	@Override
	public void execute() {
		initializeManagementServices();

		ParameterValue lastPV = null;

		lastPV = startSubscription();

		logger.info("Signaled " + num_devices + " subscribers!");

		if(lastPV != null)
		{
			java.util.ArrayList<Service> producers = new java.util.ArrayList<Service>(num_producers);

			for(int i = 0; i < num_producers; i++)
			{
				producers.add(services[i]);
			}

			startDissemination(producers);
		}
		else
			logger.error("Couldn't start dissemination!");
	}

	@Override
	public String[] getProducersUrls()
	{
		String[] prodURLs = new String[num_producers];

		for (int i = 0; i < num_producers; i++) {
			int temp_port = base_port + i;
			prodURLs[i] = "http://" + ips.get(temp_port) + ":" + temp_port + "/notification/service";
		}

		return prodURLs;
	}

	public static void main(String[] args) {
		if (args.length >= 7) {
			RunConstants constants = new RunConstants(args);

			int devices = Integer.parseInt(args[5]);
			DPWSFramework.start(new String[] {"" + (3 * devices)});

			Log.setLogLevel(Log.DEBUG_LEVEL_INFO);
//            Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);
//            Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);

			SimNotificationManager manager = new SimNotificationManager();

			try {
				manager.initConstants(constants);

				manager.execute();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

			long startRun = System.nanoTime();
			logger.info("Started run at " + startRun);
			manager.run();
			logger.info("Main is ending!");
			long endRun = System.nanoTime();
			logger.info("Ended run at " + endRun);

			DPWSFramework.stop();
		}

	}
}
