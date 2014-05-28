package org.uminho.di.gsd.reliable.service.destination.operations;

import org.uminho.di.gsd.reliable.service.destination.DestinationService;
import org.ws4d.java.service.Operation;
import org.ws4d.java.types.QName;

public abstract class DestinationOperation extends Operation {

	public DestinationService service;

	public DestinationOperation(String opName, QName qname, DestinationService svc)
	{
		super(opName, qname);

		service = svc;
	}
}
