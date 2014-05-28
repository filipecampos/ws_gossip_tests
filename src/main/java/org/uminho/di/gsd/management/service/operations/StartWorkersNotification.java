package org.uminho.di.gsd.management.service.operations;

import org.apache.log4j.Logger;
import org.uminho.di.gsd.common.Constants;
import org.ws4d.java.schema.Element;
import org.ws4d.java.service.DefaultEventSource;

public class StartWorkersNotification extends DefaultEventSource {

	static Logger logger = Logger.getLogger(StartWorkersNotification.class);

	public StartWorkersNotification()
	{
		super(Constants.StartWorkersNotificationName, Constants.ManagementPortQName);

		initOutput();
	}

	protected void initOutput() {
		Element out = new Element(Constants.StartWorkersElementQName);

		setOutput(out);
	}

}
