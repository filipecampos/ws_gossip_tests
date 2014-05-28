package org.uminho.di.gsd.gossip.device.aggregation;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.common.device.PushGossipManagedDevice;
import org.uminho.di.gsd.gossip.client.aggregation.ManagedAggGossipClient;
import org.uminho.di.gsd.membership.info.MembershipRepository;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.util.Log;

public class UdpAggPushManagedDevice extends PushGossipManagedDevice {

	static final Logger logger = Logger.getLogger(UdpAggPushManagedDevice.class);

	public UdpAggPushManagedDevice()
	{
		super();
	}

	public static void main(String[] args)
	{
		// configure loggers
		PropertyConfigurator.configure("log4j.properties");

		if (args.length >= 2) {
			RunConstants consts = new RunConstants(args);

			UdpAggPushManagedDevice device = null;

			try {
				// always start the framework first
				DPWSFramework.start(args);

//                Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
				Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
//                Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);

				// create the device ...
				device = new UdpAggPushManagedDevice();
				device.setConstants(consts);

				device.initializeConfiguration();

				device.initializeBinding();

				// ... and the services
				device.initializeMembershipService();
				device.initializeGossipService();
				device.initializeApplicationService();


				logger.debug("Device has initialized services.");

				// initialize repository
				MembershipRepository repository = new MembershipRepository();
				repository.initializeWithDevice(device);

				device.getMembershipService().setRepository(repository);

				// start services and device

				device.initializeClient(repository);
				device.initializeGossipClient();

				device.initializeUDP();

				device.getApplicationService().setValue(consts.getPort());
				device.getGossipService().setAppService(device.getApplicationService());

				device.initializeManagementService();
				logger.debug("Device has initialized the management service.");

				device.getGossipClient().setFanout(consts.getFanout());
				device.getGossipClient().setIters(consts.getMessages());
				device.getGossipClient().setTimeInterval(consts.getTimeInterval());


				// start device
				device.startDevice();

				logger.debug("Device has started correctly!");
			}
			catch(Exception e)
			{
				logger.error(e.getMessage(), e);
				device.stopDevice();
				DPWSFramework.stop();

				System.exit(0);
			}
		}
	}

	@Override
	public void initializeGossipClient() {
		gossipClient = new ManagedAggGossipClient(this);
		gossipService.setClient(gossipClient);
		gossipClient.readConfiguration();
	}

	@Override
	public void run() {
		((ManagedAggGossipClient) getGossipClient()).startUdpAggPushGossipDissemination();
	}

}
