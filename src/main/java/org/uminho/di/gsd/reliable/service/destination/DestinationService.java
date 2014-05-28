package org.uminho.di.gsd.reliable.service.destination;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLongArray;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.common.message.MessageSequenceDestination;
import org.uminho.di.gsd.reliable.destination.Destination_WSRM_Client;
import org.uminho.di.gsd.reliable.device.CommonDevice;
import org.uminho.di.gsd.reliable.service.ManagedService;
import org.uminho.di.gsd.reliable.service.destination.operations.CloseSequenceOperation;
import org.uminho.di.gsd.reliable.service.destination.operations.CreateSequenceOperation;
import org.uminho.di.gsd.reliable.service.destination.operations.InfoTempOperation;
import org.uminho.di.gsd.reliable.service.destination.operations.TerminateSequenceOperation;
import org.ws4d.java.types.URI;

public class DestinationService extends ManagedService {

	static Logger logger = Logger.getLogger(DestinationService.class);
	Destination_WSRM_Client ackClient;
	InfoTempOperation infoTempOp;
	CreateSequenceOperation createSequenceOp;
	CloseSequenceOperation closeSequenceOp;
	TerminateSequenceOperation terminateSequenceOp;

	//    received messages not yet acknowledged
	HashMap<String, MessageSequenceDestination> recMessages;
	TreeMap<String, AtomicLongArray> received;
	int iters;

	public DestinationService(int iters, CommonDevice dev) {
		super(dev);

		this.iters = iters;
		serviceName = "Destination_" + WSRM_Constants.wsrmServiceName;
		this.setServiceId(new URI(serviceName));

		recMessages = new HashMap<String, MessageSequenceDestination>();
		received = new TreeMap<String, AtomicLongArray>();

		ackClient = new Destination_WSRM_Client();
		ackClient.setDevice(device);
	}

	@Override
	public void initializeOperations() {
		// operations declaration
		infoTempOp = new InfoTempOperation(this);
		this.addOperation(infoTempOp);

		createSequenceOp = new CreateSequenceOperation(this);
		this.addOperation(createSequenceOp);

		closeSequenceOp = new CloseSequenceOperation(this);
		this.addOperation(closeSequenceOp);

		terminateSequenceOp = new TerminateSequenceOperation(this);
		this.addOperation(terminateSequenceOp);
	}

	public Destination_WSRM_Client getAckClient() {
		return ackClient;
	}

	public void initializeSequence(String seqId, String acksTo) {
		createRecMessages(seqId);

		createReceiving(seqId);

		ackClient.initializeSequence(seqId, acksTo);
	}

	public void receivedMessage(String seqId, int seqNum, int msgId, long time) {
		if(recMessages != null)
		{
			// set time
			setReceivingTime(seqId, msgId, time);

			// set message received
			MessageSequenceDestination rec = recMessages.get(seqId);
			if (rec == null) {
				logger.error("Trying to store sequence " + seqId + "! Already got " + recMessages.keySet());
			} else {
				rec.receivedMessage(seqNum);
			}
		}
		else
			logger.error("Trying to store message " + msgId + " for sequence " + seqId + " but it doesn't exist!");
	}

	public void createRecMessages(String seqId) {
		if (!recMessages.containsKey(seqId)) {

			recMessages.put(seqId, new MessageSequenceDestination());
		}
	}

	public MessageSequenceDestination getRecMessages(String seqId) {
		MessageSequenceDestination ret = null;
		if(recMessages != null)
		{
			ret = recMessages.get(seqId);
		}

		return ret;
	}

	public void createReceiving(String seqId) {
		if (!received.containsKey(seqId)) {
			received.put(seqId, new AtomicLongArray(iters));
		}
	}

	public void resetRecMessages() {
		recMessages = null;
	}

	public void writeStats(FileWriter fw) {
		Collection<AtomicLongArray> values = received.values();

		StringBuilder sb = new StringBuilder();

		logger.debug("Going to write times for seqs: " + received.keySet());

		// write receiving times
		sb.append("Receiving;");
		sb.append(device.getPort());
		sb.append(';');
		for (AtomicLongArray arr : values) {
			for (int i = 0; i < iters; i++) {
				logger.debug("Rec[" + i + "]=" + arr.get(i));
				sb.append(arr.get(i));
				sb.append(';');
			}
		}
		sb.append("\n");

		try {
			fw.append(sb.toString());
			fw.flush();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public void setReceivingTime(String seqId, int id, long time) {
		AtomicLongArray arr = received.get(seqId);

		if (arr == null) {
			logger.error("Receiving message " + id + " for unknown seq " + seqId);
		} else {
			boolean newMessage = arr.compareAndSet(id, 0, time);

			if (newMessage) {
				logger.debug("Received new message " + id + " at " + time);
			} else {
				if (arr.get(id) > time) {
					arr.set(id, time);
					logger.debug("Received message " + id + " at " + time);
				} else {
					logger.debug("Received duplicate message " + id + " at " + time);
				}
			}
		}
	}

	public InfoTempOperation getInfoTempOp() {
		return infoTempOp;
	}
}
