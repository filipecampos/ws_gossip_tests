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
import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.communication.protocol.soap.generator.Message2SOAPGenerator;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer.SOAPoverUDPDatagramHandler;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.SOAPHeader;
import org.ws4d.java.schema.Element;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.MessageIdBuffer;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;

public class ManagedMulticastSender extends BasicDevice implements Runnable, ManagedDevice {

	static final Logger logger = Logger.getLogger(ManagedMulticastSender.class);
	int myPort;
	private SOAPoverUDPServer udpServer;
	public final static String destAddress = "239.255.255.251";
	public final static int destPort = 7404; // 2 * DPWSConstants.DPWS_MCAST_PORT
	SOAPoverUDPDatagramHandler handler;
	ProtocolData pd;
	int counter = 0;
	private long[] stats;
	private int iters;
	private long period;

	ManagementService manService;

	public ManagedMulticastSender() {
		super();
	}

	public void preInitialize() {
		RunConstants constants = getConstants();
		if (constants != null) {
			myPort = constants.getPort();

			iters = constants.getMessages();
			period = constants.getTimeInterval();

			idStr = "Sender" + myPort;
			initializeUdpUnicastServer(IP, myPort);
			stats = new long[iters];
		}
	}

	protected void initializeUdpUnicastServer(String address, int port) {
		MessageIdBuffer msgIdBuffer = new MessageIdBuffer();
		SOAPoverUDPDatagramHandler handler = new MulticastSOAPoverUDPDatagramHandler(msgIdBuffer);

		try {
			udpServer = SOAPoverUDPServer.get(address, port, handler);
			if (udpServer != null) {
				logger.debug(idStr + " UDP Unicast Server created!");
			} else {
				logger.error(idStr + " UDP Unicast Server not created!");
			}
		} catch (IOException e) {
			logger.error(idStr + e.getMessage(), e);
		}
	}

	public void initializeManagementService()
	{
		manService = new ManagementService();
		manService.addBinding(new HTTPBinding(IP, myPort, "management"));
		manService.setDevice(this);
		addService(manService);
	}

	public void initialize() {
		pd = new DPWSProtocolData(IP, myPort, destAddress, destPort, false);

		addBinding(new HTTPBinding(IP, myPort, ApplicationServiceConstants.senderDeviceName));
	}

	public void multicastMsg(InvokeMessage msg, int index) {
		long now = -1;
		logger.info("Going to multicast msg with id " + msg.getMessageId() + "\n\n\n");

		ByteArrayBuffer b = null;
		try {
			b = Message2SOAPGenerator.generateSOAPMessage(msg);
		} catch (IOException ex) {
			logger.error(getIdStr() + ex.getMessage(), ex);
		}

		if (b != null) {
			byte[] data = b.getBuffer();
			int len = b.getContentLength();

			logger.debug("The message is " + len + " bytes long!");
			try {
				if (udpServer == null) {
					logger.error("UDPServer is null!");
				} else {
					now = System.nanoTime();
					udpServer.send(destAddress, destPort, data, len);
					stats[index] = now;
					logger.info("Multicast msg " + msg.getMessageId() + " to port " + destPort + " at " + now);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		else
			logger.error("It was impossible to generate the SOAP Message!");

	}

	private InvokeMessage buildMessage(int index) {
		InvokeMessage im = new InvokeMessage(ApplicationServiceConstants.MY_NAMESPACE + "/" + ApplicationServiceConstants.infoTempOpName);

		// build element
		Element infoTemp = ApplicationService.buildInfoTempElement();

		// set element in parameter value
		ParameterValue pv = ParameterValue.createElementValue(infoTemp);
		pv.setValue(ApplicationServiceConstants.infoTempValueElementName, String.valueOf(234.6f));
		pv.setValue(ApplicationServiceConstants.msgIdValueElementName, Integer.toString(index));

		im.setContent(pv);
		im.setInbound(false);
		im.setTargetAddress(new URI("udp://" + destAddress + ":" + destPort));

		return im;
	}

	@Override
	public void writeStats() {
		StringBuilder sb = new StringBuilder("Producer;");
		sb.append(myPort).append(';');

		for (int i = 0; i < iters; i++) {
			sb.append(stats[i]).append(';');
		}

		sb.append('\n');

		logger.info(sb.toString());
		try {
			fileWriter.write(sb.toString());
			fileWriter.flush();

			fileWriter.close();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public static StringBuffer messageToString(InvokeMessage msg) {
		StringBuffer lsb = new StringBuffer();

		lsb.append("\nMessage: ");

		SOAPHeader soapHeader = msg.getHeader();
		lsb.append("Header: ").append(soapHeader);

		AttributedURI msgId = msg.getMessageId();
		lsb.append("; MsgId: ").append(msgId);

		URI targetAddress = msg.getTargetAddress();
		lsb.append("; TargetAddress: ").append(targetAddress);

		AttributedURI to = msg.getTo();
		lsb.append("; To: ").append(to);

		int type = msg.getType();
		lsb.append("; Type: ").append(type);

		InvokeMessage iMsg = (InvokeMessage) msg;
		ParameterValue content = iMsg.getContent();
		lsb.append("; Content: ").append(content);
		lsb.append("\n");

		return lsb;
	}

	public void run() {
		logger.info("Gonna fire " + iters + " multicast notifications with a period of "
				+ period + " ms");
		try {
			for (int i = 0; i < iters; i++) {
				logger.debug("Firing notification number " + i + "...");
				multicastMsg(buildMessage(i), i);
				logger.debug("Fired notification number " + i + "!");

				Thread.sleep(period);
			}
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}

		manService.fireEndDisseminationNotification();
	}

	public static void main(String[] args) {
		// configure loggers
		PropertyConfigurator.configure("log4j.properties");

		if (args.length >= 6) {
			RunConstants constants = new RunConstants(args);

			ManagedMulticastSender sender = null;
			try {
				DPWSFramework.start(new String[] {"100"});

//                Log.setLogLevel(Log.DEBUG_LEVEL_NO_LOGGING);
				Log.setLogLevel(Log.DEBUG_LEVEL_ERROR);
//                Log.setLogLevel(Log.DEBUG_LEVEL_DEBUG);

				sender = new ManagedMulticastSender();

				sender.setConstants(constants);

				sender.preInitialize();

				sender.initialize();

				sender.initializeManagementService();

				sender.startDevice();

			} catch (Exception e) {
				logger.error(e.getMessage(), e);

				if(sender != null)
					sender.shutdown();
			}
		}
	}

	public String getEndpoint() {
		return this.idStr;
	}

	public void setMembership(String[] targets) {
	}

	public String getStats() {
		return "";
	}
}
