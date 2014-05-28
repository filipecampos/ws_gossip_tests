package org.uminho.di.gsd.reliable.destination;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.device.CommonDevice;
import org.uminho.di.gsd.reliable.service.source.operations.workers.MessageSendingThread;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.protocol.soap.generator.Message2SOAPGenerator;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.service.Operation;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

public class Destination_WSRM_Client extends DefaultClient {

	static Logger logger = Logger.getLogger(Destination_WSRM_Client.class);
	HashMap<String,Operation> seqAckOps;
	private SOAPoverUDPServer sender;
	HashMap<String,String> ackIps;
	HashMap<String,Integer> ackPorts;
	boolean set = false;

	CommonDevice device;

	public Destination_WSRM_Client() {
		super();

		seqAckOps = new HashMap<String, Operation>();
		ackIps = new HashMap<String, String>();
		ackPorts = new HashMap<String, Integer>();
	}

	public CommonDevice getDevice() {
		return device;
	}

	public void setDevice(CommonDevice device) {
		this.device = device;
	}

	public void setSender(SOAPoverUDPServer sender) {
		this.sender = sender;
	}

	public void initializeSequence(String seqId, String acksTo)
	{
		// initializeService - needs number of producers, base ip and port
		Service svc = initializeService(seqId, acksTo);

		// initializeSeqAckOp
		if(svc != null)
		{
			initializeSequenceAckOperation(seqId, svc);
		}
	}

	protected Service initializeService(String seqId, String epr) {
		Service service = null;

		try {
			URI uri = new URI(epr);
			ackIps.put(seqId, uri.getHost());
			ackPorts.put(seqId, uri.getPort());
			EndpointReference endpoint = new EndpointReference(uri);
			service = getServiceReference(endpoint).getService();
			logger.info("Service " + endpoint + " is set!");
		} catch (TimeoutException ex) {
			logger.error(ex.getMessage(), ex);
		}

		return service;
	}

	protected void initializeSequenceAckOperation(String seqId, Service service) {
		Operation sequenceAckOperation = service.getAnyOperation(WSRM_Constants.wsrmSourceServiceQName, WSRM_Constants.sequenceAcknowledgmentOpName);
		logger.info("SequenceAckOperation is set!");

		if(sequenceAckOperation != null)
		{
			seqAckOps.put(seqId, sequenceAckOperation);
		}
	}

	public boolean ackMessages(String seqId, ArrayList<Integer> msgs) {
		boolean ret = false;

		Operation sequenceAckOperation = seqAckOps.get(seqId);

		if (sequenceAckOperation != null) {
			ParameterValue pv = sequenceAckOperation.createInputValue();

			pv.setValue(WSRM_Constants.sequenceIdValueElementName, seqId);

			int counter = 0;
			for (Integer i : msgs) {
				pv.setValue(WSRM_Constants.acknowledgmentRangeElementName + "[" + counter + "]", Integer.toString(i));
				counter++;
			}

			try {
				InvokeMessage msg = new InvokeMessage(WSRM_Constants.MY_NAMESPACE + "/" + WSRM_Constants.sequenceAcknowledgmentOpName);

				msg.setContent(pv);
				msg.setInbound(false);

				ByteArrayBuffer b = null;
				try {
					b = Message2SOAPGenerator.generateSOAPMessage(msg);
				} catch (IOException ex) {
					logger.error("Source: " + ex.getMessage(), ex);
				}

				if (b != null) {
					byte[] data = b.getBuffer();
					int len = b.getContentLength();
					logger.debug("Source: Gonna unicast " + pv);

					MessageSendingThread t = new MessageSendingThread(sender, ackIps.get(seqId), ackPorts.get(seqId), data, len);
					DPWSFramework.getThreadPool().execute(t);
				}

				ret = true;
				logger.debug("SequenceAckOperation was invoked with pv: " + pv);
			} catch (Exception ex) {
				ret = false;
				logger.error(ex.getMessage(), ex);
			}
		} else {
			logger.error("SequenceAcknowledgment operation is still null!");
		}

		return ret;
	}

}
