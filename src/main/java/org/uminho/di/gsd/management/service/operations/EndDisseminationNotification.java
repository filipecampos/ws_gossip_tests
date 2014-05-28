package org.uminho.di.gsd.management.service.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.ws4d.java.schema.Element;
import org.ws4d.java.service.DefaultEventSource;

public class EndDisseminationNotification extends DefaultEventSource {

	static Logger logger = Logger.getLogger(EndDisseminationNotification.class);

	public EndDisseminationNotification()
	{
		super(Constants.EndDisseminationNotificationName, Constants.ManagementPortQName);

		initOutput();
	}

	protected void initOutput() {
		Element out = new Element(Constants.EndDisseminationElementQName);

		setOutput(out);
	}

}
