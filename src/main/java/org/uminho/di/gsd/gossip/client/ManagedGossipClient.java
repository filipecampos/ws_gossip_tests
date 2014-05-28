package org.uminho.di.gsd.gossip.client;

import org.uminho.di.gsd.gossip.device.GossipDevice;
import org.uminho.di.gsd.management.ManagedClient;
import org.uminho.di.gsd.management.service.ManagementService;

public class ManagedGossipClient extends GossipClient implements ManagedClient {

    protected ManagementService manService;
    
    public ManagedGossipClient(GossipDevice dvc) {
    	super(dvc);
    }

    @Override
	public void setManagementService(ManagementService manService) {
        this.manService = manService;
    }

    public void startPushGossipDissemination() {
    	super.startPushGossipDissemination();
    	manService.fireEndDisseminationNotification();
    }

}
