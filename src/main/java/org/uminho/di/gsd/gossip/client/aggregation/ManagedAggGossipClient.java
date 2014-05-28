package org.uminho.di.gsd.gossip.client.aggregation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.RunConstants;
import org.uminho.di.gsd.common.device.BasicDevice;
import org.uminho.di.gsd.gossip.device.GossipDevice;
import org.uminho.di.gsd.management.service.ManagementService;
import org.ws4d.java.DPWSFramework;

public class ManagedAggGossipClient extends AggGossipClient {

	static Logger logger = Logger.getLogger(ManagedAggGossipClient.class);

	protected ManagementService manService;

	public ManagedAggGossipClient(GossipDevice dvc) {
		super(dvc);
	}

	/* getters and setters */
	public void setManagementService(ManagementService manService) {
		this.manService = manService;
	}

	public static void main(String[] args) {
		// always start the framework first
		DPWSFramework.start(args);

		ManagedAggGossipClient client = new ManagedAggGossipClient(null);
		client.readConfiguration();
		client.initializeMonitoringService();
	}

	public void startTcpAggPushGossipDissemination() {
		super.startTcpAggPushGossipDissemination();
		manService.fireEndDisseminationNotification();
	}

	public void startUdpAggPushGossipDissemination() {
		super.startUdpAggPushGossipDissemination();
		manService.fireEndDisseminationNotification();
	}

	public void startTcpAggGossipDissemination() {
		super.startTcpAggGossipDissemination();
		manService.fireEndDisseminationNotification();
	}

	public void startUdpAggGossipDissemination() {
		super.startUdpAggGossipDissemination();
		manService.fireEndDisseminationNotification();
	}

	public void startTcpAggPullGossipDissemination() {
		super.startTcpAggPullGossipDissemination();
		manService.fireEndDisseminationNotification();
	}

	public void startUdpAggPullGossipDissemination() {
		super.startUdpAggPullGossipDissemination();
		manService.fireEndDisseminationNotification();
	}

	@Override
	protected void writeStatsToFile(FileWriter file, FileWriter hopsFile) {
		super.writeStatsToFile(file, hopsFile);

		FileWriter aggFileWriter = null;
		RunConstants runConstants = ((BasicDevice) device).getConstants();
		String filename = "values_" + runConstants.getDisseminationType() + runConstants.getFileName() + ".csv";

		try {
			File aggFile = new File(filename);
			aggFileWriter = new FileWriter(aggFile, true);

			if(aggFileWriter != null)
			{
				logger.debug(device.getIdStr() + ": Writing Agg Values to file " + filename);
				StringBuilder sb = new StringBuilder();
				// write sending times
				sb.append("Agg;");
				sb.append(device.getGossipService().getSvcEPR());
				sb.append(';');


				for(Integer key : aggregateValues.keySet())
				{
					String[] values = aggregateValues.get(key);
					if(values != null)
					{
						for (int i = 0; i < iters; i++) {
							sb.append(values[i]);
							sb.append(';');
						}
					}
				}
				sb.append("\n");

				aggFileWriter.append(sb.toString());
				aggFileWriter.flush();
			}

		} catch (IOException ex) {
			logger.error(device.getIdStr() + ":" + ex.getMessage(), ex);
		} finally {
			try {
				aggFileWriter.close();
			} catch (IOException ex) {
				logger.error(device.getIdStr() + ":" + ex.getMessage(), ex);
			}
		}
	}

	public void setAggregateValue(int port, int msgId, String value) {
		logger.debug(device.getIdStr() + " setting aggregate value " + value + " for port " + port + " and msgId " + msgId);

		String[] values = aggregateValues.get(port);

		if(values == null)
		{
			values = new String[iters];
			aggregateValues.put(port, values);
		}

		values[msgId] = value;

		// setting aggregate value on service
		if((value != null) && (!value.isEmpty()))
		{
			device.getGossipService().getAppService().setLastValue(Double.parseDouble(value));
		}
	}


}
