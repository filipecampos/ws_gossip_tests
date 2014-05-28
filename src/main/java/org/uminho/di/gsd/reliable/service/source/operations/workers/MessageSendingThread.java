package org.uminho.di.gsd.reliable.service.source.operations.workers;

import java.util.Random;
import org.apache.log4j.Logger;
import org.ws4d.java.communication.protocol.soap.server.SOAPoverUDPServer;

public class MessageSendingThread implements Runnable {
	static Logger logger = Logger.getLogger(MessageSendingThread.class);

	static Random random = new Random();

	final SOAPoverUDPServer sender;
	final String targetIp;
	final int targetPort;
	final byte[] msg;
	final int length;
	long number;
	String seqId = null;


	public MessageSendingThread(SOAPoverUDPServer sender, String targetIp, int targetPort, byte[] msg, int length) {
		super();
		this.sender = sender;
		this.targetIp = targetIp;
		this.targetPort = targetPort;
		this.msg = msg;
		this.length = length;
	}

	public MessageSendingThread(SOAPoverUDPServer sender, String targetIp, int targetPort, byte[] msg, int length, long num, String seqId) {
		this(sender, targetIp, targetPort, msg, length);
		this.number = num;
		this.seqId = seqId;
		logger.debug("Creating MessageSendingThread: " + number + " " + targetIp + ":" + targetPort);
	}

	public long getNumber() {
		return number;
	}

	public void run() {
		try {
			if(seqId != null)
			{
				logger.debug("Sending msg " + number + " to " + targetIp + ":" + targetPort + " through seq " + seqId);
			}
			sender.send(targetIp, targetPort, msg, length);
		} catch (Exception ex) {
			logger.error("Could not send msg " + msg + " to " + targetIp + ":" + targetPort);
			logger.error(ex.getMessage(), ex);
		}   
	}

}
