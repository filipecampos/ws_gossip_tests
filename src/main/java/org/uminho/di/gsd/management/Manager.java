package org.uminho.di.gsd.management;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.common.RunConstants;
import org.ws4d.java.DPWSFramework;
import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.HTTPBinding;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.dispatch.HelloData;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.service.InvocationException;
import org.ws4d.java.service.Operation;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.reference.ServiceReference;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.URI;

public class Manager extends DefaultClient implements Runnable {

	static Logger logger = Logger.getLogger(Manager.class);

	protected final Lock lock = new ReentrantLock();
	protected final Condition disseminated = lock.newCondition();
	protected boolean diss;

	Random random;

	AtomicLong disseminatedCounter;
	AtomicLong counter;
	RunConstants runConstants;
	EventSource endDisseminationNot;
	int num_producers;
	int num_devices;

	int fanout;
	int myport;

	protected Service[] services;
	java.util.ArrayList<Integer> producers_index;

	String ip;
	int base_port;

	int total_devices;

	public Manager() {
		counter = new AtomicLong();
		disseminatedCounter = new AtomicLong();

		registerHelloListening();

		registerServiceListening();

		random = new Random(System.nanoTime());
	}

	@Override
	public void helloReceived(HelloData helloData) {
		URI address = helloData.getEndpointReference().getAddress();

		long val = counter.incrementAndGet();
		logger.info("Received Hello from " + address + "! Received " + val + " hellos!");

		if (val == total_devices) {
			execute();
		}
	}

	public void initializeManagementServices()
	{
		services = new Service[total_devices];

		logger.debug("Initializing Devices' Management Services...");

		for (int i = 0; i < total_devices; i++) {
			int port = base_port + i;
			services[i] = initializeManagementService(ip, port);
		}

	}

	protected Service initializeManagementService(String ip, int port) {
		Service svc = null;

		String deviceAddress = buildManagementAddress(ip, port);

		try {
			EndpointReference svcEPR = new EndpointReference(new URI(deviceAddress));
			ServiceReference svcRef = getServiceReference(svcEPR);

			svc = svcRef.getService();

			logger.debug("Got Management Service " + port);

			Thread.sleep(100);
		} catch (TimeoutException ex) {
			logger.error("Timeouted getting management service for " + ip + ":" + port);
			logger.error(ex.getMessage(), ex);
			shutdown();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return svc;
	}

	protected void setTotalDevices()
	{
		total_devices = num_devices;
		runConstants.setTotalDevices(total_devices);
	}

	public void initConstants(RunConstants constants) {
		runConstants = constants;

		num_devices = constants.getDevices();
		setTotalDevices();
		fanout = constants.getFanout();
		base_port = constants.getBasePort();
		num_producers = constants.getProducers();

		producers_index = new java.util.ArrayList<Integer>(num_producers);

		ip = constants.getIp();
		myport = constants.getPort();
	}

	public String[] getProducersUrls()
	{
		return new String[0];
	}

	public ParameterValue startSubscription() {
		ParameterValue pv = null;

		int incr_port = base_port + num_producers;

		// setup producers urls
		String[] prodURLs = getProducersUrls();

		String prefix = Constants.EndpointElementName;

		for (int i = 0; i < num_devices; i++) {
			// for each consumer device
			int port = incr_port + i;


			try {
				// get management service
				Service svc = services[i+num_producers];

				Operation setMembershipOp = svc.getAnyOperation(Constants.ManagementPortQName, Constants.SetMembershipOperationName);
				// create parameterValue
				ParameterValue membershipPV = setMembershipOp.createInputValue();

				for (int j = 0; j < num_producers; j++) {
					// add every producer's url
					String temp_prefix = prefix + "[" + j + "]";
					membershipPV.setValue(temp_prefix, prodURLs[j]);
				}

				logger.info("Going to send " + membershipPV + " to " + svc.getEndpointReferences().next() + ":" + port);
				try {
					// invoke set membership op
					pv = setMembershipOp.invoke(membershipPV);
				} catch (InvocationException ex) {
					logger.error(ex.getMessage(), ex);
				}
			} catch (TimeoutException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		logger.info("Ended execution.");

		return pv;
	}

	public void execute() {
		logger.info("Got all the " + total_devices + " hellos!");

		initializeManagementServices();

		// select targets randomly and set them in all devices
		ArrayList lists = generateAllTargets(base_port, total_devices, fanout);

		for (int i = 0; i < total_devices; i++) {
			int port = base_port + i;

			ArrayList targets = (ArrayList) lists.get(i);
			logger.debug("List " + i + " : " + targets.toString());

			try {

				Operation setMembershipOp = services[i].getAnyOperation(Constants.ManagementPortQName, Constants.SetMembershipOperationName);
				ParameterValue membershipPV = setMembershipOp.createInputValue();

				String prefix = Constants.EndpointElementName + "[";

				int size = targets.size();

				logger.debug("Going to set " + size + " targets for " + port);
				for (int j = 0; j < size; j++) {
					membershipPV.setValue(prefix + j + "]", (String) targets.get(j));
				}

				logger.info("Going to send " + membershipPV);
				try {
					setMembershipOp.invoke(membershipPV);
				} catch (InvocationException ex) {
					logger.error(ex.getMessage(), ex);
				}
			} catch (TimeoutException ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		selectProducers();

		java.util.ArrayList<Service> producers = new java.util.ArrayList<Service>(num_producers);

		for(Integer i : producers_index)
		{
			producers.add(services[i]);
		}

		startDissemination(producers);

		logger.info("Ended execute method...");
	}

	protected void selectProducers()
	{
		// select producers randomly and start dissemination
		while(producers_index.size() < num_producers)
		{
			int producer_index = random.nextInt(total_devices);

			if(!producers_index.contains(producer_index))
			{
				producers_index.add(producer_index);
				logger.debug("Adding as a producer service with index " + producer_index);
			}
		}
	}

	public void startDissemination(List<Service> producers) {
		logger.debug("Subscribing Producer's EndDissemination Op on " + producers.size() + " producers.");

		int j = 0;
		for (Service producer : producers)
		{
			try {
				endDisseminationNot = producer.getAnyEventSource(Constants.ManagementPortQName, Constants.EndDisseminationElementName);

				DataStructure bindings = new ArrayList(1);
				bindings.add(new HTTPBinding(ip, myport, "/EndDisseminationEventSink_" + j++));

				endDisseminationNot.subscribe(this, 0, bindings);
			} catch (EventingException ex) {
				logger.error(ex.getMessage(), ex);
			} catch (TimeoutException ex) {
				logger.error(ex.getMessage(), ex);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		logger.debug("Invoking start of dissemination on " + producers.size() + " producers...");
		// start messages dissemination
		for (Service producer : producers)
		{
			Operation startDisseminationOp = producer.getAnyOperation(Constants.ManagementPortQName, Constants.StartDisseminationOperationName);
			ParameterValue in = startDisseminationOp.createInputValue();
			try {
				startDisseminationOp.invoke(in);
			} catch (InvocationException ex) {
				logger.error(ex.getMessage(), ex);
			} catch (TimeoutException ex) {
				logger.error(ex.getMessage(), ex);
				logger.error("Got Timeout!");
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		logger.info("Ended execute method...");
	}

	@Override
	public ParameterValue eventReceived(ClientSubscription subscription, URI actionURI, ParameterValue parameterValue) {
		long now = System.nanoTime();

		logger.info("Received event " + parameterValue);

		long dissCounter = disseminatedCounter.incrementAndGet();

		if (dissCounter == num_producers) {
			lock.lock();
			try {
				diss = true;
				disseminated.signal();
			} finally {
				lock.unlock();
			}
		}

		long time = System.nanoTime() - now;
		logger.info("Returning took " + time + "ns... Disseminators terminated: " + dissCounter);

		return null;
	}

	@Override
	public void run() {
		logger.info("Waiting for dissemination end...");

		lock.lock();

		try {
			while (!diss) {
				disseminated.await();
			}

		} catch (InterruptedException ex) {
			logger.error(ex.getMessage(), ex);
		} finally {
			lock.unlock();
		}

		try {
			String filename = runConstants.getDisseminationType() + runConstants.getFileName() + ".csv";

			try {
				// after dissemination wait some time
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				logger.error(ex.getMessage(), ex);
			}

			logger.info("Invoking WriteStats on devices...");

			// signal all devices to write stats
			for (int i = 0; i < total_devices; i++) {
				Service svc = services[i];

				Object epr = svc.getEndpointReferences().next();
				logger.debug("Invoking WriteStats on " + epr);
				writeStats(svc, filename);
				logger.debug("Invoked WriteStats on " + epr);

				try {
					// after dissemination wait some time
					Thread.sleep(200);
				} catch (InterruptedException ex) {
					logger.error(ex.getMessage(), ex);
				}
			}

			logger.info("Ended execution.");

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			shutdown();
		}

		shutdown();
	}

	protected void stop(Service service) {
		Operation stopOp = service.getAnyOperation(Constants.ManagementPortQName, Constants.StopOperationName);

		ParameterValue pv = stopOp.createInputValue();
		try {
			stopOp.invoke(pv);
		} catch (InvocationException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (TimeoutException ex) {
			logger.error(ex.getMessage(), ex);
		}
	}

	protected void writeStats(Service service, String filename) {
		Operation writeStatsOp = service.getAnyOperation(Constants.ManagementPortQName, Constants.WriteStatsOperationName);

		ParameterValue pv = writeStatsOp.createInputValue();
		pv.setValue(filename);
		try {
			writeStatsOp.invoke(pv);
		} catch (InvocationException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (TimeoutException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

	}

	protected String getStats(Service service) {
		String ret = "";

		Operation getStatsOp = service.getAnyOperation(Constants.ManagementPortQName, Constants.GetStatsOperationName);

		ParameterValue pv = getStatsOp.createInputValue();
		try {
			ParameterValue retPV = getStatsOp.invoke(pv);

			if(retPV != null)
			{
				logger.debug("Received Stats: " + retPV);
				ret = retPV.getValue();
			}
		} catch (InvocationException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (TimeoutException ex) {
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return ret;
	}

	protected String buildManagementAddress(String ip, int port) {
		return "http://" + ip + ":" + port + "/management";

	}

	protected String buildGossipAddress(String ip, int port) {
		return "http://" + ip + ":" + port + "/device/gossip/service";

	}

	protected ArrayList generateAllTargets(int base_port, int num, int fanout) {
		ArrayList retList = new ArrayList();
		ArrayList list = new ArrayList();
		ArrayList selected = new ArrayList();

		ArrayList genList = new ArrayList();

		// in order to use random peer sampling prior to dissemination
		int listSize = fanout * 2;

		while (selected.size() < num) {
			logger.debug("Generating targets...");

			retList.clear();
			selected.clear();

			if (listSize < num) {
				for (int i = 0; i < num; i++) {
					list = new ArrayList(listSize);
					genList = new ArrayList(listSize);
					int port = base_port + i;

					while (genList.size() < listSize) {
						int randomPort = base_port + random.nextInt(num);

						if ((randomPort != port) && (!genList.contains(randomPort))) {
							logger.debug("For " + port + " generated " + randomPort);

							if (!selected.contains(randomPort)) {
								selected.add(randomPort);
							}

							genList.add(randomPort);
						}
					}

					Iterator iter = genList.iterator();

					while (iter.hasNext()) {
						int genPort = (Integer) iter.next();

						String gossipAddress = buildGossipAddress(ip, genPort);

						logger.debug("Got address: " + gossipAddress);
						list.add(gossipAddress);
					}

					retList.add(list);
				}

			}
			else
			{
				int newPort = -1;
				for(int i = 0; i < num; i++)
				{
					list = new ArrayList(listSize);
					genList = new ArrayList(listSize);

					for (int j = 0; j < num; j++) {
						if (j != i) {
							newPort = base_port + j;
							genList.add(newPort);
							if (!selected.contains(newPort)) {
								selected.add(newPort);
							}
						}
					}

					Iterator iter = genList.iterator();

					while (iter.hasNext()) {
						int genPort = (Integer) iter.next();

						String gossipAddress = buildGossipAddress(ip, genPort);

						logger.debug("Got address: " + gossipAddress);
						list.add(gossipAddress);
					}

					retList.add(list);
				}
			}

		}

		return retList;
	}

	protected void shutdown() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			logger.error(ex);
		}

		logger.info("Shutting down...");

		System.exit(0);
	}

	public boolean disseminated()
	{
		return (disseminatedCounter.get() == num_producers);
	}

	public static void main(String[] args) {
		if (args.length >= 7) {
			RunConstants constants = new RunConstants(args);

			int devices = Integer.parseInt(args[5]);
			DPWSFramework.start(new String[] {"" + (3 * devices)});

			Manager manager = new Manager();

			logger.info("Manager executing...");
			try {
				manager.initConstants(constants);

				manager.run();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}

			logger.info("Manager terminated executing.");
		}

	}
}
