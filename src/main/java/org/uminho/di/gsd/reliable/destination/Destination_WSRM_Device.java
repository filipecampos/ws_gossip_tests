package org.uminho.di.gsd.reliable.destination;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.device.CommonDevice;
import org.uminho.di.gsd.reliable.service.destination.DestinationService;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.util.Log;

public class Destination_WSRM_Device extends CommonDevice {
	static Logger logger = Logger.getLogger(Destination_WSRM_Device.class);

	DestinationService destService;

	Destination_WSRM_Device(RunConstants constants) {
		super(constants);
		name = "WSRM_Destination_Device";
	}

	@Override
	public void initializeServices() {
		super.initializeServices();

		// WSRM
		destService = new DestinationService(getConstants().getMessages(), this);
		destService.initializeOperations();
		destService.addBinding(new HTTPBinding(IP, port, WSRM_Constants.wsrmDestinationServiceName));
		addService(destService);
	}

	@Override
	public void writeStats() {
		destService.writeStats(fileWriter);
	}

	@Override
	public void startServices()
	{
		super.startServices();
		try {
			logger.info("Trying to start WSRM Destination Service: " + IP + ":" + PORT);
			destService.start();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void stopServices()
	{
		super.stopServices();
		try {
			destService.stop();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	protected void initializeUDP() {
		super.initializeUDP();

		handler.setInfoTempOp(destService.getInfoTempOp());

		destService.getAckClient().setSender(udpServer);
	}

	public static void main(String[] args) {

		if(args.length > 6)
		{
			RunConstants constants = new RunConstants(args);

			// configure loggers
			PropertyConfigurator.configure("log4j.properties");

			//The first action we have to invoke is the start() method of the DPWSFramework.
			DPWSFramework.start(new String[]{"50"});
			//            Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
			Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
			//            Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);

			Destination_WSRM_Device device;

			try
			{
				device = new Destination_WSRM_Device(constants);
				device.initializeDevice();
				device.initializeServices();
				device.initializeUDP();
				device.startDevice();
			}
			catch(Exception e)
			{
				logger.error(e.getMessage(), e);
				DPWSFramework.stop();
			}
		}
	}

	public String getStats() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}

