package org.uminho.di.gsd.reliable.service.source;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLongArray;
import org.apache.log4j.Logger;

import org.uminho.di.gsd.reliable.common.WSRM_Constants;
import org.uminho.di.gsd.reliable.device.CommonDevice;
import org.uminho.di.gsd.reliable.service.ManagedService;
import org.uminho.di.gsd.reliable.service.source.operations.SequenceAcknowledgementOperation;
import org.uminho.di.gsd.reliable.source.Source_WSRM_Client;
import org.ws4d.java.types.URI;

public class SourceService extends ManagedService {

	static Logger logger = Logger.getLogger(SourceService.class);
	private SequenceAcknowledgementOperation sequenceAcknowledgementOperation;
	Source_WSRM_Client client;
	AtomicLongArray sent;

	public SourceService(int num, CommonDevice dev) {
		super(dev);

		serviceName = "Source_" + WSRM_Constants.wsrmServiceName;
		this.setServiceId(new URI(serviceName));
		sent = new AtomicLongArray(num);
	}

	public void setClient(Source_WSRM_Client client) {
		this.client = client;
	}

	public Source_WSRM_Client getClient() {
		return client;
	}

	@Override
	public void initializeOperations() {
		sequenceAcknowledgementOperation = new SequenceAcknowledgementOperation(this);
		this.addOperation(sequenceAcknowledgementOperation);
	}

	public void setSent(long ind, long time) {
		int i = (int) ind;
		boolean set = sent.compareAndSet(i, 0, time);

		if ((!set) && (time < sent.get(i))) {
			sent.set(i, time);
		}
	}

	public void writeStats(FileWriter fw) {
		int iters = sent.length();
		StringBuilder sb = new StringBuilder();

		// write receiving times
		sb.append("Sending;");
		sb.append(device.getPort());
		sb.append(';');
		for (int i = 0; i < iters; i++) {
			logger.debug("Sent[" + i + "]=" + sent.get(i));
			sb.append(sent.get(i));
			sb.append(';');
		}
		sb.append("\n");

		try {
			fw.append(sb.toString());
			fw.flush();
		} catch (IOException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	public SequenceAcknowledgementOperation getSeqAckOp() {
		return sequenceAcknowledgementOperation;
	}
}
