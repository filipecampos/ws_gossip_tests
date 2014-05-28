package org.uminho.di.gsd.notification.client;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.ApplicationServiceConstants;
import org.uminho.di.gsd.notification.device.NotificationConsumerDevice;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

public class NotificationConsumerClient extends DefaultClient {

	static Logger logger = Logger.getLogger(NotificationConsumerClient.class);
	private NotificationConsumerDevice device;
	private String idStr = "";
	private int producers;
	private int base_port;
	private long[][] stats;
	private int iters;
	private AtomicBoolean written = null;

	public NotificationConsumerClient() {
		super();

		written = new AtomicBoolean(false);
	}

	public void setDevice(NotificationConsumerDevice dvc) {
		device = dvc;

		idStr = "Device" + device.getPort();
		iters = device.getConstants().getMessages();
		producers = device.getConstants().getProducers();
		base_port = device.getConstants().getBasePort();
		stats = new long[producers][iters];
	}

	public void initNotificationService() {
		String firstProducerIp = device.getProducerIp();

		// manager ip
		int last_point = firstProducerIp.lastIndexOf(".");
		String base = firstProducerIp.substring(0, last_point + 1);
		int ip_index = Integer.parseInt(firstProducerIp.substring(last_point + 1));

		String producer_ip = firstProducerIp;


		for (int i = 0; i < producers; i++) {
			int producer_port = base_port + i;

			if (device.isSimulated()) {
				producer_ip = base + (ip_index + i);
			}

			Service service = null;
			EndpointReference svcEPR = new EndpointReference(new URI("http://" + producer_ip + ":" + producer_port + "/notification/service"));
			ServiceReference svcRef = getServiceReference(svcEPR);
			try {
				service = svcRef.getService();
			} catch (TimeoutException ex) {
				logger.error(idStr + ex.getMessage(), ex);
			} catch (Exception ex) {
				logger.error(idStr + ex.getMessage(), ex);
			}

			if (service != null) {
				subscribe(service, i);
				logger.debug(idStr + " Subscribed producer!");
			}
		}
	}

	public void subscribePublishers(String[] eprs) {
		int num = eprs.length;

		Service service = null;
		try {
			for (int i = 0; i < num; i++) {

				EndpointReference svcEPR = new EndpointReference(new URI(eprs[i]));
				ServiceReference svcRef = getServiceReference(svcEPR);
				service = svcRef.getService();
				if (service != null) {
					subscribe(service, i);
					logger.debug(idStr + " Subscribed producer!");
				}
			}
		} catch (TimeoutException ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	private void subscribe(Service service, int index) {
		EventSource event = service.getAnyEventSource(ApplicationServiceConstants.notificationServiceQName, ApplicationServiceConstants.infoTempEventName);

		if (event != null) {
			long duration = 0; // 0 - infinite

			DataStructure bindings = new ArrayList(1);
			bindings.add(new HTTPBinding(device.getIp(), Integer.parseInt(device.getPort()), "/EventingClientEventSink_" + index));
			try {
				ClientSubscription subscription = event.subscribe(this, duration, bindings);

				if (subscription != null) {
					logger.debug("\n" + idStr + " Has subscribed! SubscriptionId:" + subscription.getServiceSubscriptionId());
					FileWriter fw = device.getFileWriter();
					if (fw != null) {
						synchronized (fw) {
							fw.write("\n" + idStr + " Has subscribed! SubscriptionId:" + subscription.getServiceSubscriptionId());
							fw.flush();
						}
					}
				}
			} catch (EventingException ex) {
				logger.error(idStr + ex.getMessage(), ex);
			} catch (TimeoutException ex) {
				logger.error(idStr + ex.getMessage(), ex);
			} catch (IOException ex) {
				logger.error(idStr + ex.getMessage(), ex);
			}
		}
	}

	@Override
	public ParameterValue eventReceived(ClientSubscription subscription, URI actionURI, ParameterValue parameterValue) {
		long currentTime = System.nanoTime();
		logger.debug(idStr + "Received notification! Action:" + actionURI + "; PV:" + parameterValue);
		String msgId = parameterValue.getValue(ApplicationServiceConstants.msgIdValueElementName);
		String[] parts = msgId.split("-");
		if (parts.length > 1) {
			int producer_port = Integer.parseInt(parts[0]);
			int eventNumber = Integer.parseInt(parts[1]);
			stats[producer_port - base_port][eventNumber] = currentTime;
			logger.debug(idStr + " Setting event " + eventNumber + " time for producer " + producer_port);
		}

		return null;
	}

	@Override
	public void subscriptionEndReceived(ClientSubscription subscription, URI reason) {
		logger.debug(idStr + "Received Subscription End message. Writing stats...");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		}

		device.stopDevice();
		DPWSFramework.stop();
	}

	public void writeStats() {
		boolean goingToWrite = written.compareAndSet(false, true);
		logger.debug(idStr + " Going to write stats? " + goingToWrite);
		if (goingToWrite) {
			FileWriter fileWriter = device.getFileWriter();
			//            synchronized (fileWriter) {
			StringBuffer sb = new StringBuffer("\nConsumer;");
			sb.append(device.getPort());
			sb.append(';');
			for (int j = 0; j < producers; j++) {
				for (int i = 0; i < iters; i++) {
					sb.append(stats[j][i]);
					sb.append(';');
				}
			}

			try {
				fileWriter.write(sb.toString());
				logger.debug("Got the stats: " + sb.toString());
				fileWriter.flush();
			} catch (IOException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		logger.debug("Ending writeStats method.");
		//        }
	}

	public String getStats() {
		StringBuilder sb = new StringBuilder("\nConsumer;");
		sb.append(device.getPort());
		sb.append(';');
		for (int j = 0; j < producers; j++) {
			for (int i = 0; i < iters; i++) {
				sb.append(stats[j][i]);
				sb.append(';');
			}
		}

		return sb.toString();

	}
}
