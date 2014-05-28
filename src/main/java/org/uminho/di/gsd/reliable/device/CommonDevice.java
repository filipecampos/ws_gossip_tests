package org.uminho.di.gsd.reliable.device;

import java.io.IOException;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.common.device.BasicDevice;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.uminho.di.gsd.reliable.common.InSOAPoverUDPDatagramHandlerImpl;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.structures.MessageIdBuffer;

public abstract class CommonDevice extends BasicDevice implements ManagedDevice {

	static Logger logger = Logger.getLogger(CommonDevice.class);

	protected int port;
	protected int base_port;
	protected String name;

	protected SOAPoverUDPServer udpServer;
	protected InSOAPoverUDPDatagramHandlerImpl handler;
	MessageIdBuffer msgIdBuffer;

	// management service
	protected ManagementService managementService;

	public CommonDevice()
	{
		super();
	}

	public CommonDevice(RunConstants consts)
	{
		super();
		setConstants(consts);

		IP = consts.getIp();
		port = consts.getPort();
		base_port = consts.getBasePort();
	}

	public void initializeDevice()
	{
		setManufacturerUrl(WSRM_Constants.MY_NAMESPACE);
		setSerialNumber("1234567890" + port);
		addFriendlyName("en-US", name);
		addManufacturer("en-US", "DI-UM");
		addModelName("en-US", "Basic Model");

		//This will be the ip and port our device will be reachable at.
		//e.g. hello and get messages will be sent to this address.
		addBinding(new HTTPBinding(IP, port, name));

		logger.info("Device binding added - " + IP + ":" + port);
	}

	public void initializeServices()
	{
		//Management
		managementService = new ManagementService();
		managementService.setServiceId(org.uminho.di.gsd.common.Constants.ManagementServiceId);
		managementService.setDevice(this);
		managementService.addBinding(new HTTPBinding(IP, port, "management"));
		addService(managementService);
	}

	public int getIntPort() {
		return port;
	}

	public SOAPoverUDPServer getUdpServer() {
		return udpServer;
	}

	@Override
	public void startServices()
	{
		super.startServices();
		try {
			logger.info("Trying to start managementService: " + IP + ":" + PORT);
			managementService.start();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	@Override
	public void stopServices()
	{
		super.stopServices();
		try {
			managementService.stop();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	protected void initializeUdpUnicastServer(String address, int port) {
		msgIdBuffer = new MessageIdBuffer();
		handler = new InSOAPoverUDPDatagramHandlerImpl(msgIdBuffer);

		int tries = 3;
		while((udpServer == null) && (tries > 0))
		{
			tries--;
			try {
				udpServer = SOAPoverUDPServer.get(address, port, handler);
				if (udpServer != null) {
					logger.debug(idStr + " UDP Unicast Server created!");
				} else {
					logger.error(idStr + " UDP Unicast Server not created! Tries: " + tries);
				}
			} catch (IOException e) {
				logger.error(idStr + e.getMessage(), e);
				logger.error(idStr + " UDP Unicast Server not created!");
				try {
					Thread.sleep(300);
				} catch (InterruptedException ex) {
					logger.error(idStr + ex.getMessage(), ex);
				}
			}
		}

		if(udpServer == null)
		{
			logger.fatal(idStr + " UDP Unicast Server not created! Shutting down...");
			shutdown();
		}
	}

	protected void initializeUDP() {
		initializeUdpUnicastServer(getConstants().getIp(), getConstants().getPort());
	}


	public String getEndpoint() {
		return idStr;
	}

	public void setMembership(String[] targets) {
	}
}
