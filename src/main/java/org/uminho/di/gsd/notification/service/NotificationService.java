package org.uminho.di.gsd.notification.service;

import org.uminho.di.gsd.common.ApplicationServiceConstants;
import org.uminho.di.gsd.notification.service.operations.InfoTempNotification;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.schema.ComplexType;
import org.ws4d.java.schema.Element;
import org.ws4d.java.schema.SchemaUtil;
import org.ws4d.java.service.DefaultService;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.types.URI;

public class NotificationService extends DefaultService {

	private String identifier;
	private InfoTempNotification infoTempNotification;
	private double lastValue = -1.0;

	private int counter = 0;
	private ArrayList values;

	public NotificationService() {
		super();

		values = new ArrayList();

		//the optional-to-set ServiceId
		this.setServiceId(new URI(ApplicationServiceConstants.notificationServiceName));

		infoTempNotification = new InfoTempNotification(this);
		this.addEventSource(infoTempNotification);
	}

	public NotificationService(String id) {
		this();

		identifier = id;
	}

	public double getLastValue() {
		return lastValue;
	}

	public void setLastValue(double lastValue) {
		this.lastValue = lastValue;
	}

	public String getIdentifier() {
		return identifier;
	}

	public EventSource getInfoTempNotification() {
		return infoTempNotification;
	}

	public int fireNotification(double newValue) {
		ParameterValue pv = infoTempNotification.createOutputValue();

		pv.setValue(ApplicationServiceConstants.infoTempValueElementName, String.valueOf(newValue));

		pv.setValue(ApplicationServiceConstants.msgIdValueElementName, String.valueOf(counter));


		infoTempNotification.fire(pv, counter);

		values.add(newValue);

		return counter++;
	}

	public static Element buildInfoTempElement() {
		Element infoTemp = new Element(ApplicationServiceConstants.infoTempElementQName);

		ComplexType complexType = new ComplexType(ApplicationServiceConstants.infoTempComplexTypeElementQName, ComplexType.CONTAINER_SEQUENCE);

		complexType.addElement(new Element(ApplicationServiceConstants.infoTempValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_DOUBLE)));
		complexType.addElement(new Element(ApplicationServiceConstants.msgIdValueElementQName, SchemaUtil.getSchemaType(SchemaUtil.TYPE_ANYURI)));

		infoTemp.setType(complexType);

		return infoTemp;
	}

	public long fireNotification(double value, int port, int counter) {
		ParameterValue pv = infoTempNotification.createOutputValue();
		pv.setValue(ApplicationServiceConstants.infoTempValueElementName, String.valueOf(value));
		pv.setValue(ApplicationServiceConstants.msgIdValueElementName, port + "-" + counter);

		long time = System.nanoTime();
		infoTempNotification.fire(pv, counter);

		return time;
	}
}
