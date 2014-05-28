package org.uminho.di.gsd.multicast.managed;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLongArray;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.application.service.ApplicationService;
import org.uminho.di.gsd.common.ApplicationServiceConstants;
import org.uminho.di.gsd.common.RunConstants;
import org.ws4d.java.communication.DPWSProtocolData;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer.SOAPoverUDPDatagramHandler;
import org.ws4d.java.message.FaultMessage;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.message.discovery.ByeMessage;
import org.ws4d.java.message.discovery.HelloMessage;
import org.ws4d.java.message.discovery.ProbeMatchesMessage;
import org.ws4d.java.message.discovery.ProbeMessage;
import org.ws4d.java.message.discovery.ResolveMatchesMessage;
import org.ws4d.java.message.discovery.ResolveMessage;
import org.ws4d.java.message.eventing.GetStatusMessage;
import org.ws4d.java.message.eventing.GetStatusResponseMessage;
import org.ws4d.java.message.eventing.RenewMessage;
import org.ws4d.java.message.eventing.RenewResponseMessage;
import org.ws4d.java.message.eventing.SubscribeMessage;
import org.ws4d.java.message.eventing.SubscribeResponseMessage;
import org.ws4d.java.message.eventing.SubscriptionEndMessage;
import org.ws4d.java.message.eventing.UnsubscribeMessage;
import org.ws4d.java.message.eventing.UnsubscribeResponseMessage;
import org.ws4d.java.message.metadata.GetMessage;
import org.ws4d.java.message.metadata.GetMetadataMessage;
import org.ws4d.java.message.metadata.GetMetadataResponseMessage;
import org.ws4d.java.message.metadata.GetResponseMessage;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.MessageIdBuffer;

public class MulticastSOAPoverUDPDatagramHandler extends SOAPoverUDPDatagramHandler {

	static final Logger logger = Logger.getLogger(MulticastSOAPoverUDPDatagramHandler.class);
	private ApplicationService service;
	private int port;
	private int iters;
	private HashMap<Integer, AtomicLongArray> receiving = new HashMap<Integer, AtomicLongArray>();
	ArrayList<Long> times = new ArrayList<Long>();

	public MulticastSOAPoverUDPDatagramHandler(MessageIdBuffer messageIdBuffer) {
		super(messageIdBuffer);
	}

	public MulticastSOAPoverUDPDatagramHandler(MessageIdBuffer messageIdBuffer, ApplicationService svc, int index, int its) {
		super(messageIdBuffer);
		service = svc;
		port = index;
		iters = its;
	}

	public MulticastSOAPoverUDPDatagramHandler(MessageIdBuffer messageIdBuffer, ApplicationService svc, int index, int its, RunConstants consts) {
		this(messageIdBuffer, svc, index, its);
	}

	public void receive(HelloMessage hm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(ByeMessage bm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(ProbeMessage pm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(ProbeMatchesMessage pmm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(ResolveMessage rm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(ResolveMatchesMessage rmm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(GetMessage gm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(GetResponseMessage grm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(GetMetadataMessage gmm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(GetMetadataResponseMessage gmrm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(SubscribeMessage sm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(SubscribeResponseMessage srm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(GetStatusMessage gsm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(GetStatusResponseMessage gsrm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(RenewMessage rm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(RenewResponseMessage rrm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(UnsubscribeMessage um, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(UnsubscribeResponseMessage urm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(SubscriptionEndMessage sem, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receive(InvokeMessage im, DPWSProtocolData dpwspd) {
		long currentTime = System.nanoTime();

		int srcPort = dpwspd.getSourcePort();
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder(port + ":Received InvokeMessage at ").append(currentTime);
			sb.append(" Source host:").append(dpwspd.getSourceHost());
			sb.append(" Source port:").append(srcPort);
			sb.append(" Destination host: ").append(dpwspd.getDestinationHost());
			sb.append(" Destination port: ").append(dpwspd.getDestinationPort());
			sb.append(ApplicationServiceConstants.messageToString(im));
			logger.debug(sb.toString());
		}
		ParameterValue pv = im.getContent();
		String msgId = pv.getValue(ApplicationServiceConstants.msgIdValueElementName);
		int index = Integer.parseInt(msgId);
		logger.debug(port + ":Received msgId: " + msgId + " (parsed " + index + ") from producer " + srcPort);

		AtomicLongArray list = receiving.get(srcPort);

		if (list == null) {
			logger.debug(port + ":Creating array for receiving times from producer " + srcPort);
			list = new AtomicLongArray(iters);
			receiving.put(srcPort, list);
			logger.debug(port + ":Array put in hash!");
		}

		boolean newMessage = list.compareAndSet(index, 0, currentTime);
		logger.debug(port + ":Setting reception time for message " + index + " is " + newMessage);

		if (newMessage) {
			logger.debug(port + ":Received new message " + index + " at " + currentTime);

			try {
				service.getInfoTempOperation().invoke(pv);
			} catch (InvocationException ex) {
				logger.error(ex.getMessage(), ex);
			} catch (TimeoutException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		else if (list.get(index) > currentTime) {
			list.set(index, currentTime);
			logger.debug(port + ":Received duplicate message " + index + " at " + currentTime);
		}
		else
		{
			logger.debug(port + ":Did not set time for received message " + index + " at " + currentTime);
		}

		logger.debug(port + ": Rec[" + srcPort + "][" + index + "]=" + receiving.get(srcPort).get(index));
	}

	public void receive(FaultMessage fm, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receiveFailed(Exception excptn, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void sendFailed(Exception excptn, DPWSProtocolData dpwspd) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void receiveFailed(Exception excptn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void sendFailed(Exception excptn) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void writeStats(FileWriter fileWriter) {
		try {
			// write receiving times
			StringBuilder sb = new StringBuilder("Consumer;");
			sb.append(port);
			sb.append(';');

			logger.debug(port + ":Has received from " + receiving.values().size() + " producers.");

			TreeSet<Integer> keys = new TreeSet<Integer>(receiving.keySet());
			logger.debug(port + ":Receiving keys: " + keys.size());

			if (keys.isEmpty()) {
				logger.error(port + ":Receiving is empty!");
			} else {
				logger.debug(port + ":Receiving keys: " + keys);
				for (Integer p : keys) {
					AtomicLongArray array = receiving.get(p);
					logger.debug(port + ":Rec[" + p + "] has " + array.length() + " elements.");
					for (int i = 0; i < iters; i++) {
						logger.debug(port + ":Rec[" + p + "][" + i + "]=" + array.get(i));
						sb.append(array.get(i));
						sb.append(';');
					}
				}
			}
			sb.append("\n");
			fileWriter.write(sb.toString());

			fileWriter.flush();
			fileWriter.close();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}
}
