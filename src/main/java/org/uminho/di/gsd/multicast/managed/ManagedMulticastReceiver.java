package org.uminho.di.gsd.multicast.managed;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.uminho.di.gsd.application.service.ApplicationService;
import org.uminho.di.gsd.common.ApplicationServiceConstants;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.common.device.BasicDevice;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.types.QNameSet;
import org.ws4d.java.util.Log;

public class ManagedMulticastReceiver extends BasicDevice implements ManagedDevice {

	public static final Logger logger = Logger.getLogger(ManagedMulticastReceiver.class);

	int myPort;
	String myName;
	int numberOfIters;
	ApplicationService myService;
	private ManagementService managementService;

	SOAPoverUDPServer udpServer;
	MulticastSOAPoverUDPDatagramHandler handler;

	public ManagedMulticastReceiver() {
		super();
	}

	private void initialize() {
		myName = "[Receiver-" + PORT + "]";
		numberOfIters = getConstants().getMessages();

		setManufacturerUrl("http://gsd.di.uminho.pt");
		setSerialNumber("1234567891" + PORT);
		addFriendlyName("en-US", "Multicast Receiver Device");
		addManufacturer("en-US", "DI-UM");
		addModelName("en-US", "Managed Model");
		setPortTypes(new QNameSet(ApplicationServiceConstants.receiverType));

		myPort = Integer.parseInt(PORT);
		//This will be the ip and port our device will be reachable at.
		//e.g. hello and get messages will be sent to this address.
		addBinding(new HTTPBinding(IP, myPort, ApplicationServiceConstants.receiverDeviceName + PORT));
	}

	public void initializeManagementService()
	{
		managementService = new ManagementService();
		managementService.setDevice(this);
		managementService.addBinding(new HTTPBinding(IP, myPort, "management"));
		addService(managementService);
	}

	public void initializeApplicationService() {
		myService = new ApplicationService("Receiver" + myPort);
		myService.addBinding(new HTTPBinding(IP, myPort, ApplicationServiceConstants.applicationServiceName));
		addService(myService);
	}

	@Override
	public void startServices() {
		try {
			initializeUdpMulticastServer(ManagedMulticastSender.destAddress, ManagedMulticastSender.destPort);
			//we have to start the services and the device separately.
			myService.start();
			managementService.start();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void initializeUdpMulticastServer(String address, int port) {
		MessageIdBuffer messageIdBuffer = new MessageIdBuffer();
		handler = new MulticastSOAPoverUDPDatagramHandler(messageIdBuffer, myService, myPort, numberOfIters, getConstants());

		logger.debug("Creating UDP Multicast Server for " + address + ":" + port);
		try {
			udpServer = SOAPoverUDPServer.get(address, port, handler);
			if (udpServer != null) {
				logger.debug("UDP Multicast Server created! Receiver" + myPort);
			} else {
				logger.error("ERROR! UDP Multicast Server not created! Receiver" + myPort);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("Could not create SOAP-over-UDP server.");
		}
	}

	public ApplicationService getApplicationService() {
		return myService;
	}

	@Override
	public void writeStats() {
		handler.writeStats(fileWriter);
	}

	public static void main(String[] args) {
		// configure loggers
		PropertyConfigurator.configure("log4j.properties");

		if (args.length >= 6) {
			RunConstants constants = new RunConstants(args);

			ManagedMulticastReceiver receiver = null;
			try {
				DPWSFramework.start(new String[] {"50"});

				// Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
				Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
				// Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);

				receiver = new ManagedMulticastReceiver();
				receiver.setConstants(constants);
				receiver.initialize();
				receiver.initializeApplicationService();
				receiver.initializeManagementService();

				receiver.startDevice();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);

				if(receiver != null)
					receiver.shutdown();
			}
		}
	}

	public String getEndpoint() {
		return this.idStr;
	}

	public void setMembership(String[] targets) {
		// do nothing
	}

	public String getStats() {
		// do nothing
		return "";
	}
}
