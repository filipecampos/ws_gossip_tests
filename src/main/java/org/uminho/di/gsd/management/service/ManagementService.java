package org.uminho.di.gsd.management.service;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.uminho.di.gsd.management.ManagedClient;
import org.uminho.di.gsd.management.ManagedDevice;
import org.uminho.di.gsd.management.service.operations.EndDisseminationNotification;
import org.uminho.di.gsd.management.service.operations.GetEndpointOperation;
import org.uminho.di.gsd.management.service.operations.GetStatsOperation;
import org.uminho.di.gsd.management.service.operations.SetMembershipOperation;
import org.uminho.di.gsd.management.service.operations.StartDisseminationOperation;
import org.uminho.di.gsd.management.service.operations.StartWorkersNotification;
import org.uminho.di.gsd.management.service.operations.StopOperation;
import org.uminho.di.gsd.management.service.operations.WriteStatsOperation;
import org.uminho.di.gsd.membership.client.MembershipRepositoryClient;
import org.ws4d.java.service.DefaultService;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.types.URI;

public class ManagementService extends DefaultService {

	static Logger logger = Logger.getLogger(ManagementService.class);

	private ManagedDevice device;
	private ManagedClient gossipClient;
	private MembershipRepositoryClient membershipClient;

	protected GetEndpointOperation getEndpointOp;
	protected SetMembershipOperation setMembershipOp;
	protected StartDisseminationOperation startDisseminationOp;
	protected StopOperation stopOp;
	protected WriteStatsOperation writeStatsOp;
	protected GetStatsOperation getStatsOp;

	protected EndDisseminationNotification endDisseminationNot;

	protected StartWorkersNotification startWorkersNot;

	public ManagementService()
	{
		super();

		this.setServiceId(new URI(Constants.ManagementServiceName));

		initializeOperations();
	}

	protected void initializeOperations()
	{
		getEndpointOp = new GetEndpointOperation(this);
		addOperation(getEndpointOp);
		setMembershipOp = new SetMembershipOperation(this);
		addOperation(setMembershipOp);
		startDisseminationOp = new StartDisseminationOperation(this);
		addOperation(startDisseminationOp);
		stopOp = new StopOperation(this);
		addOperation(stopOp);
		writeStatsOp = new WriteStatsOperation(this);
		addOperation(writeStatsOp);
		getStatsOp = new GetStatsOperation(this);
		addOperation(getStatsOp);

		endDisseminationNot = new EndDisseminationNotification();
		addEventSource(endDisseminationNot);
	}

	public ManagedDevice getDevice() {
		return device;
	}

	public void setDevice(ManagedDevice device) {
		this.device = device;
	}

	public ManagedClient getGossipClient() {
		return gossipClient;
	}

	public void setGossipClient(ManagedClient gossipClient) {
		this.gossipClient = gossipClient;

		this.gossipClient.setManagementService(this);
	}

	public MembershipRepositoryClient getMembershipClient() {
		return membershipClient;
	}

	public void setMembershipClient(MembershipRepositoryClient membershipClient) {
		this.membershipClient = membershipClient;
	}

	public void initializeStartWorkersNotification()
	{
		startWorkersNot = new StartWorkersNotification();
		addEventSource(startWorkersNot);
	}

	public void fireEndDisseminationNotification()
	{

		try
		{
			ParameterValue pv = endDisseminationNot.createOutputValue();
			endDisseminationNot.fire(pv, 0);
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public void fireStartWorkersNotification()
	{
		if(startWorkersNot != null)
		{
			try
			{
				ParameterValue pv = startWorkersNot.createOutputValue();
				startWorkersNot.fire(pv, 0);
			}
			catch(Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
}
