package org.uminho.di.gsd.notification.service.operations;

import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.ApplicationServiceConstants;
import org.uminho.di.gsd.notification.service.NotificationService;
import org.ws4d.java.communication.TimeoutException;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventListener;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.service.DefaultEventSource;
import org.ws4d.java.structures.DataStructure;

public class InfoTempNotification extends DefaultEventSource {

    static Logger logger = Logger.getLogger(InfoTempNotification.class);

    private AtomicLong subscriptionCounter;

    public InfoTempNotification(NotificationService svc) {
        super(ApplicationServiceConstants.infoTempEventName, ApplicationServiceConstants.notificationServiceQName);
        subscriptionCounter = new AtomicLong();

        setOutput(NotificationService.buildInfoTempElement());
    }

    @Override
    public ClientSubscription subscribe(EventListener el, long l, DataStructure ds) throws EventingException, TimeoutException {
        long numberOfSubs = subscriptionCounter.incrementAndGet();
        logger.debug("Number of Subscriptions=" + numberOfSubs);
        return super.subscribe(el, l, ds);
    }

    

}
