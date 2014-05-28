package org.uminho.di.gsd.reliable.service.source.operations;

import org.uminho.di.gsd.reliable.service.source.SourceService;
import org.ws4d.java.service.Operation;
import org.ws4d.java.types.QName;

public abstract class SourceOperation extends Operation {

	public SourceService service;

	public SourceOperation(String opName, QName qname, SourceService svc)
	{
		super(opName, qname);

		service = svc;
	}
}
