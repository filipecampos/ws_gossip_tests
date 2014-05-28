package org.uminho.di.gsd.reliable.source;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.device.CommonDevice;
import org.uminho.di.gsd.reliable.service.source.SourceService;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.util.Log;

public class Source_WSRM_Device extends CommonDevice implements Runnable {

	static Logger logger = Logger.getLogger(Source_WSRM_Device.class);
	SourceService srcService;
	Source_WSRM_Client client;

	Source_WSRM_Device(RunConstants constants) {
		super(constants);

		name = "WSRM_Source_Device";
	}

	@Override
	public void initializeServices() {
		super.initializeServices();

		//Preparations of our own service
		srcService = new SourceService(getConstants().getMessages(), this);
		srcService.initializeOperations();
		srcService.addBinding(new HTTPBinding(IP, port, WSRM_Constants.wsrmSourceServiceName));
		addService(srcService);
	}

	@Override
	public void startServices() {
		super.startServices();

		try {
			srcService.start();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void stopServices() {
		super.stopServices();

		try {
			srcService.stop();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initializeClient() {
		client = new Source_WSRM_Client();
		client.setConstants(getConstants());
		client.setService(srcService);
		srcService.setClient(client);
		client.setSender(udpServer);
	}

	@Override
	public void writeStats() {
		srcService.writeStats(fileWriter);
	}

	public void run() {
		client.setWsrmSourceServiceUrl("http://" + IP + ":" + port + "/" + WSRM_Constants.wsrmSourceServiceName);

		// initialize operations
		logger.debug("Initializing Destination Services, Operations and Sequences...");
		// get destination services, infoTempOps and create sequences
		client.initialize();

		// send all messages to all services
		long period = getConstants().getTimeInterval();
		int iters = getConstants().getMessages();
		logger.debug("Firing " + iters + "messages...");
		try {
			for (int i = 0; i < iters; i++) {
				client.fireMessage(i + 1);
				Thread.sleep(period);
			}
			logger.debug("Finished firing messages!");
			client.clearBuffers();
			logger.debug("Cleared buffers!");
			client.terminateSequences();
			logger.debug("Terminated sequences!");


			Thread.sleep(period);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}

		managementService.fireEndDisseminationNotification();
	}

	@Override
	protected void initializeUDP() {
		super.initializeUDP();

		handler.setSeqAckOp(srcService.getSeqAckOp());
	}

	public static void main(String[] args) {
		if (args.length > 6) {
			RunConstants constants = new RunConstants(args);

			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			//The first action we have to invoke is the start() method of the DPWSFramework.
			// always start the framework first
			int devices = Integer.parseInt(args[5]);
			DPWSFramework.start(new String[] {"" + (3 * devices)});

//            Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
			Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
//            Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);

			Source_WSRM_Device device;

			try {
				device = new Source_WSRM_Device(constants);
				device.initializeDevice();
				device.initializeServices();
				device.initializeUDP();
				device.initializeClient();



				device.startDevice();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				DPWSFramework.stop();
			}
		}
	}

	public String getStats() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
