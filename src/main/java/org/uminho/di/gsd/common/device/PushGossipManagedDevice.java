package org.uminho.di.gsd.common.device;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.gossip.client.ManagedGossipClient;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.uminho.di.gsd.membership.info.MembershipRepository;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

public class PushGossipManagedDevice extends PushGossipDevice implements Runnable, ManagedDevice {

	final static Logger logger = Logger.getLogger(PushGossipManagedDevice.class);

	protected ManagementService managementService;

	/* Initializers */

	public void initializeGossipClient() {
		gossipClient = new ManagedGossipClient(this);
		gossipService.setClient(gossipClient);
		gossipClient.readConfiguration();
	}

	public void initializeManagementService() {
		managementService = new ManagementService();
		managementService.setServiceId(Constants.ManagementServiceId);
		managementService.setDevice(this);
		managementService.setGossipClient((ManagedGossipClient) getGossipClient());
		managementService.setMembershipClient(getClient());

		String svcEPR = "http://" + IP + ":" + PORT + "/management";
		managementService.addBinding(new HTTPBinding(new URI(svcEPR)));

		this.addService(managementService);
	}

	@Override
	public void initializeWorkers() {

		// Membership
		// service search
		//        initializeSearchTask();

		// membership update
		initializeUpdateTask();

		// No Gossip Task as pure push is used
	}

	@Override
	public void startServices() {
		try {
			managementService.start();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}

		super.startServices();
	}


	@Override
	public void stopServices() {

		try {
			managementService.stop();
		} catch (IOException ex) {
			logger.error(idStr + ex.getMessage(), ex);
		}

		super.stopServices();
	}

	@Override
	public void run() {
		this.getGossipClient().startPushGossipDissemination();
	}

	public static void main(String[] args) throws Exception {
		if (args.length >= 2) {
			RunConstants constants = new RunConstants(args);

			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			// always start the framework first
			DPWSFramework.start(args);

			// Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
			Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
			// Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);

			// create the device ...
			PushGossipManagedDevice device = new PushGossipManagedDevice();
			device.setConstants(constants);

			try {
				device.initializeConfiguration();

				device.initializeBinding();

				// ... and the services
				device.initializeMembershipService();
				device.initializeGossipService();
				device.initializeApplicationService();


				// initialize repository
				MembershipRepository repository = new MembershipRepository();
				repository.initializeWithDevice(device);

				device.getMembershipService().setRepository(repository);

				// start services and device


				device.initializeClient(repository);
				device.initializeGossipClient();

				device.initializeManagementService();

				device.getGossipClient().setFanout(constants.getFanout());
				device.getGossipClient().setIters(constants.getMessages());
				device.getGossipClient().setTimeInterval(constants.getTimeInterval());

				device.startDevice();

				device.initializeShadowServices();
			}
			catch(Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public String getEndpoint() {
		return idStr;
	}

	@Override
	public void setMembership(String[] targets) {

		if(gossipClient != null)
		{
			int count = targets.length;
			logger.debug("Got " + count + " new targets");

			if(count > 0)
			{
				gossipClient.setTargets(targets);
			}
		}
	}
}
