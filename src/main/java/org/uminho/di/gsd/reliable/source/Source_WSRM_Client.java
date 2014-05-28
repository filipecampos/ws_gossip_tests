package org.uminho.di.gsd.reliable.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.common.message.MessageIdGenerator;
import org.uminho.di.gsd.reliable.common.message.MessageRepository;
import org.uminho.di.gsd.reliable.service.destination.DestinationService;
import org.uminho.di.gsd.reliable.service.source.SourceService;
import org.uminho.di.gsd.reliable.service.source.operations.workers.MessageSendingThread;
import org.ws4d.java.DPWSFramework;

import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.communication.protocol.soap.generator.Message2SOAPGenerator;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;
import org.ws4d.java.message.InvokeMessage;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.Operation;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.ByteArrayBuffer;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

public class Source_WSRM_Client extends DefaultClient {

	static Logger logger = Logger.getLogger(Source_WSRM_Client.class);
	RunConstants constants;
	String myName = "[WSRM_Source_Client]";
	String serviceName = WSRM_Constants.wsrmDestinationServiceName;
	String wsrmSourceServiceUrl;
	SourceService svc;
	double last_value = 0.0d;
	////// multiple
	// key -> seqId, value -> service
	HashMap<String, Service> destinationServices;
	// key -> seqId, value -> service ip
	HashMap<String, String> destinationServicesIps;
	// key -> seqId, value -> service port
	HashMap<String, Integer> destinationServicesPorts;
	// key -> seqId, value -> MessageRepository
	TreeMap<String, MessageRepository> seqsRepo;
	////// single
	AtomicLong sequenceCounter = new AtomicLong();
	MessageIdGenerator idGenerator;
	DestinationService destinationService;
	Operation infoTempOperation;
	ParameterValue infoTempPV;
	ParameterValue[] messages;
	Operation createSequenceOperation;
	Operation terminateSequenceOperation;
	private SOAPoverUDPServer sender;

	Source_WSRM_Client() {
		seqsRepo = new TreeMap<String, MessageRepository>();
		destinationServices = new HashMap<String, Service>();
		destinationServicesIps = new HashMap<String, String>();
		destinationServicesPorts = new HashMap<String, Integer>();

		idGenerator = new MessageIdGenerator("WSRM_Source_Client");

		registerServiceListening();
	}

	public String getWsrmSourceServiceUrl() {
		return wsrmSourceServiceUrl;
	}

	public void setWsrmSourceServiceUrl(String wsrmSourceServiceUrl) {
		this.wsrmSourceServiceUrl = wsrmSourceServiceUrl;
	}

	public RunConstants getConstants() {
		return constants;
	}

	public void setConstants(RunConstants constants) {
		this.constants = constants;

	}

	public void setSender(SOAPoverUDPServer sender) {
		this.sender = sender;
	}

	// inits
	private void initializeOperationStubs(Service svc) {
		createSequenceOperation = svc.getAnyOperation(WSRM_Constants.wsrmDestinationServiceQName, WSRM_Constants.createSequenceOpName);
		terminateSequenceOperation = svc.getAnyOperation(WSRM_Constants.wsrmDestinationServiceQName, WSRM_Constants.terminateSequenceOpName);
		infoTempOperation = svc.getAnyOperation(WSRM_Constants.wsrmDestinationServiceQName, WSRM_Constants.infoTempOpName);
	}

	public void initialize() {
		// basePort is source's port
		String ip = constants.getIp();
		int basePort = constants.getBasePort();
		int numDevices = constants.getDevices();
		int numProducers = constants.getProducers();
		boolean simulated = constants.isSimulated();

		messages = new ParameterValue[numDevices];

		// manager ip
		int last_point = ip.lastIndexOf(".");
		String base = ip.substring(0, last_point + 1);
		int ip_index = numProducers + 2;

		try {
			for (int i = 0; i < numDevices; i++) {
				String devIp = ip;

				if (simulated) {
					devIp = base + (ip_index + i);
				}

				int devPort = basePort + numProducers + i;
				String seqId = createSequenceId(devPort);

				logger.debug("Initializing service " + devPort + "; seqId=" + seqId);

				Service service = getServiceReference(new EndpointReference(new URI(
						"http://" + devIp + ":" + devPort + "/" + WSRM_Constants.wsrmDestinationServiceName))).getService();

				destinationServices.put(seqId, service);
				destinationServicesIps.put(seqId, devIp);
				destinationServicesPorts.put(seqId, devPort);
				logger.debug("Put destination service port " + devPort + " for seq " + seqId);

				if (i == 0) {
					initializeOperationStubs(service);
				}

				createSequence(seqId, service);

				Thread.sleep(500);
			}
		} catch (TimeoutException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	private String createSequenceId(int port) {
		return "Seq_" + constants.getPort() + "_" + port;
	}

	private void createSequence(String seqId, Service wsrmService) {
		//We need to get the operation from the service.
		//getAnyOperation returns the first Operation that fits the specification in the parameters.

		ParameterValue response = null;
		try {
			Operation createSequenceOp = wsrmService.getAnyOperation(WSRM_Constants.wsrmDestinationServiceQName, WSRM_Constants.createSequenceOpName);

			ParameterValue ourExampleValue = createSequenceOp.createInputValue();

			// set acks to
			ourExampleValue.setValue(WSRM_Constants.acksToElementName, wsrmSourceServiceUrl);

			// set sequence id
			ourExampleValue.setValue(WSRM_Constants.sequenceIdValueElementName, seqId);

			response = createSequenceOp.invoke(ourExampleValue);

			String recSequenceId = response.getValue(WSRM_Constants.sequenceIdValueElementName);

			// check if recSequenceId is equal to sent sequenceId and initialize messageRepository
			if (seqId.equalsIgnoreCase(recSequenceId)) {
				logger.debug(myName + " Sequence " + seqId + " created correctly!");

				MessageRepository repo = new MessageRepository(constants.getMessages());

				seqsRepo.put(seqId, repo);
			}

		} catch (InvocationException e) {
			logger.error(e.getMessage(), e);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void terminateSequences() {
		for (String seqId : seqsRepo.keySet()) {
			terminateSequence(seqId);
		}
	}

	private void terminateSequence(String sequenceId) {
		ParameterValue response = null;
		try {
			Service service = destinationServices.get(sequenceId);
			Operation terminateSequenceOp = service.getAnyOperation(WSRM_Constants.wsrmDestinationServiceQName, WSRM_Constants.terminateSequenceOpName);
			ParameterValue ourExampleValue = terminateSequenceOp.createInputValue();

			// set sequence id
			ourExampleValue.setValue(WSRM_Constants.sequenceIdValueElementName, sequenceId);

			response = terminateSequenceOperation.invoke(ourExampleValue);

			String recSequenceId = response.getValue(WSRM_Constants.sequenceIdValueElementName);

			// check if recSequenceId is equal to sent sequenceId
			if (sequenceId.equalsIgnoreCase(recSequenceId)) {
				logger.debug(myName + " Sequence " + sequenceId + " terminated correctly!");
				sequenceId = null;
			}

		} catch (InvocationException e) {
			logger.error(e.getMessage(), e);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void sendAllMessages(String seqId) {
		logger.info("sendAllMessages: " + seqId);

		MessageRepository repo = seqsRepo.get(seqId);
		ArrayList<ParameterValue> allMessages = repo.getAllMessages();

		for (ParameterValue pv : allMessages) {
			Integer index = Integer.parseInt(pv.getValue(WSRM_Constants.msgIdValueElementName));
			sendUDPMessage(seqId, pv, index, false);
		}
	}

	public void sendNackedMessages(String seqId) {
		logger.info("sendNackedMessages: " + seqId);

		MessageRepository repo = seqsRepo.get(seqId);
		ArrayList<ParameterValue> nackedMessages = repo.getMessages(repo.getNackedMessages());

		for (ParameterValue pv : nackedMessages) {
			Integer index = Integer.parseInt(pv.getValue(WSRM_Constants.msgIdValueElementName));
			sendUDPMessage(seqId, pv, index, false);
		}
	}

	public void sendMessages(String seqId, ArrayList<Long> list) {
		logger.info("sendMessages: " + seqId + ", " + list.toString());

		MessageRepository repo = seqsRepo.get(seqId);
		Collections.sort(list);
		ArrayList<ParameterValue> allMessages = repo.getMessages(list);

		int last = allMessages.size();
		int counter = 0;
		for (ParameterValue pv : allMessages) {
			counter++;
			String ackStr = Boolean.toString(counter == last);
			pv.setValue(WSRM_Constants.sequenceAckRequestedValueElementName, ackStr);
			Long index = Long.parseLong(pv.getValue(WSRM_Constants.msgIdValueElementName));
			sendUDPMessage(seqId, pv, index, false);
		}
	}

	public void ackMessages(String seqId, ArrayList<Long> list) {
		logger.info("ackMessages: " + seqId + ", " + list.toString());

		seqsRepo.get(seqId).ackMessages(list);
	}

	private void sendMessageToService(SOAPoverUDPServer udpServer, String ip, int port, byte[] data, int len, long index, String seqId) {
		logger.debug("Sending message " + index + " to " + ip + ":" + port);
		MessageSendingThread msgSend = new MessageSendingThread(udpServer, ip, port, data, len, index, seqId);

		long time = System.nanoTime();
		DPWSFramework.getThreadPool().execute(msgSend);
		svc.setSent(index - 1, time);
	}

	public void fireMessage(int i) {
		String msgId = Integer.toString(i);
		String seqMsgNumber = msgId;
		int offset = i % constants.getAckInterval();
		String ackRequested = Boolean.toString(offset == 0);


		logger.debug("Firing message " + i + " to seqs " + seqsRepo.keySet());

		int counter = 0;
		for (String seqId : seqsRepo.keySet()) {
			// Send new message
			// create message
			messages[counter] = infoTempOperation.createInputValue();

			// infoTemp
			messages[counter].setValue(WSRM_Constants.infoTempValueElementName, "346.7");
			// msgId
			messages[counter].setValue(WSRM_Constants.msgIdValueElementName, msgId);
			// seqId
			messages[counter].setValue(WSRM_Constants.sequenceIdValueElementName, seqId);
			// seqMsgNumber
			messages[counter].setValue(WSRM_Constants.sequenceMsgNumberValueElementName, seqMsgNumber);
			// seqAckRequested
			messages[counter].setValue(WSRM_Constants.sequenceAckRequestedValueElementName, ackRequested);

			counter++;
		}


		counter = 0;
		for (String seqId : seqsRepo.keySet()) {
			// Send the new message
			sendUDPMessage(seqId, messages[counter++], i, true);
		}

		clearMyBuffers();
	}

	public void clearMyBuffers() {
		Set<String> keys = seqsRepo.keySet();
		try {
			// timeout: 50ms
			// From the SOAP-over-UDP standard:
			// UNICAST_UDP_REPEAT - 1
			// UDP_MIN_DELAY - 50
			// UDP_MAX_DELAY - 250
			// UDP_UPPER_DELAY - 500
			Thread.sleep(50);

			logger.debug("clearMyBuffers started!");

			for (String seqId : keys) {
				ArrayList<Long> nackedMsgs = seqsRepo.get(seqId).getNackedMessages();
				boolean seq_all_sent = nackedMsgs.isEmpty();
				if (!seq_all_sent) {
					logger.debug("clearMyBuffers sending nacked messages for sequence " + seqId);
					sendMessages(seqId, nackedMsgs);
				} else {
					logger.debug("clearMyBuffers sequence " + seqId + " is clear!");
				}
			}
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}

		logger.debug("clearMyBuffers terminated!");
	}

	public void clearBuffers() {
		for (String key : seqsRepo.keySet()) {
			clearSequenceBuffer(key);
			terminateSequence(key);
		}
	}

	private void clearSequenceBuffer(String seqId) {
		MessageRepository repo = seqsRepo.get(seqId);
		ArrayList<Long> nackedMsgs = repo.getNackedMessages();

		if (!nackedMsgs.isEmpty()) {
			sendMessages(seqId, nackedMsgs);
		}
	}

	private void sendUDPMessage(String seqId, ParameterValue pv, long index, boolean store) {
		InvokeMessage msg = new InvokeMessage(WSRM_Constants.MY_NAMESPACE + "/" + WSRM_Constants.infoTempOpName);

		msg.setContent(pv);
		String readSeqId = pv.getValue(WSRM_Constants.sequenceIdValueElementName);
		msg.setInbound(false);

		ByteArrayBuffer b = null;
		try {
			b = Message2SOAPGenerator.generateSOAPMessage(msg);
		} catch (IOException ex) {
			logger.error("Source: " + ex.getMessage(), ex);
		}

		if (b != null) {
			byte[] data = b.getBuffer().clone();
			int len = b.getContentLength();
			logger.debug("Source: Gonna unicast " + pv);

			String destIp = destinationServicesIps.get(seqId);
			int destPort = destinationServicesPorts.get(seqId);
			logger.debug("Got destination service port " + destPort + " for seq " + seqId);

			MessageRepository repo = seqsRepo.get(seqId);

			logger.debug("Firing message " + index + " through " + seqId + " Port is " + destPort + " ReadSeqId:" + readSeqId);
			sendMessageToService(sender, destIp, destPort, data, len, index, seqId);

			if (store) {
				repo.storeMessage(index, pv);
			}
		}
	}

	public void setService(SourceService srcService) {
		svc = srcService;
	}
}
