package org.uminho.di.gsd.management;

import java.util.HashMap;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.service.Service;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.Log;

public class SimManager extends Manager implements Runnable {

	static Logger logger = Logger.getLogger(SimManager.class);
	protected HashMap<Integer, String> ips;

	public SimManager() {
		super();

		logger.info("Initialized SimManager!");
	}

	protected void initializeIps(int num_devices, int base_port) {

		// manager ip
		int last_point = ip.lastIndexOf(".");
		String base = ip.substring(0, last_point);
		int penultimate_point = base.lastIndexOf(".");
		String group = ip.substring(0, penultimate_point + 1);

		int ip_index = Integer.parseInt(ip.substring(last_point + 1));

		for (int i = 0; i < num_devices; i++) {
			int temp_ip = (ip_index + i + 1);
			int group_number = temp_ip / 255;
			int modulo = temp_ip % 254;
			if (modulo == 0) {
				modulo = 254;
			}
			ips.put(base_port + i, group + group_number + "." + modulo);
			logger.debug("Initialized ip: " + (group + group_number + "." + modulo) + ":" + (base_port + i));
		}

		logger.debug("Device IPs: " + ips.size());
	}

	@Override
	public void initializeManagementServices() {
		logger.debug("Initializing Devices' Management Services...");

		services = new Service[total_devices];

		// initialize all services
		for (int j = 0; j < total_devices; j++) {
			int port = base_port + j;
			services[j] = initializeManagementService(ips.get(port), port);
		}
	}

	@Override
	protected void setTotalDevices() {
		total_devices = num_devices;
		runConstants.setTotalDevices(total_devices);
		services = new Service[total_devices];
	}

	@Override
	public void initConstants(RunConstants constants) {
		super.initConstants(constants);

		setTotalDevices();
		ips = new HashMap<Integer, String>();

		initializeIps(total_devices, base_port);

		logger.info("Initialized IPs.");
	}

	@Override
	protected ArrayList generateAllTargets(int base_port, int num, int fanout) {
		// in order to use random peer sampling prior to dissemination
		int listSize = fanout * 2;

		ArrayList retList = new ArrayList(num);
		ArrayList list = null;
		ArrayList genList = null;


		logger.debug("Expected List Size: " + listSize + "; Num: " + num);

		if (listSize < num) {
			for (int i = 0; i < num; i++) {
				int port = base_port + i;

				list = new ArrayList(listSize);
				genList = new ArrayList(listSize);

				while (genList.size() < listSize) {
					int random_index = random.nextInt(num);
					int randomPort = base_port + random_index;

					if ((randomPort != port) && (!genList.contains(randomPort))) {
						logger.debug("For " + port + " generated " + randomPort);

						genList.add(randomPort);
					}
				}

				Iterator iter = genList.iterator();

				while (iter.hasNext()) {
					int genPort = (Integer) iter.next();

					String gossipAddress = buildGossipAddress(ips.get(genPort), genPort);

					logger.debug("Got address: " + gossipAddress);
					list.add(gossipAddress);
				}

				retList.add(list);
				logger.debug("Added " + list);
			}
		} else {
			for (int i = 0; i < num; i++) {
				list = new ArrayList(listSize);
				genList = new ArrayList(listSize);

				for (int j = 0; j < num; j++) {
					// avoids passing peer as its own target
					if (j != i) {
						logger.debug("For " + (base_port + i) + " generated " + (base_port + j));
						genList.add(base_port + j);
					}
				}

				Iterator iter = genList.iterator();

				while (iter.hasNext()) {
					int genPort = (Integer) iter.next();

					String gossipAddress = buildGossipAddress(ips.get(genPort), genPort);

					logger.debug("Got address: " + gossipAddress);
					list.add(gossipAddress);
				}

				retList.add(list);
				logger.debug("retList size: " + retList.size());
				logger.debug("Added " + list);
			}
		}

		logger.debug("Going to return list " + retList);

		return retList;
	}

	public static void main(String[] args) {
		// configure loggers
		PropertyConfigurator.configure("log4j.properties");

		if (args.length >= 7) {
			RunConstants constants = new RunConstants(args);

			int devices = Integer.parseInt(args[5]);
			DPWSFramework.start(new String[]{"" + (3 * devices)});

			SimManager manager = new SimManager();

			Log.setLogLevel(Log.DEBUG_LEVEL_INFO);
//            Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);
//            Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);

			logger.info("SimManager executing...");
			try {
				manager.initConstants(constants);

				manager.execute();

				manager.run();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

			logger.info("SimManager terminated executing.");
		}

	}
}
