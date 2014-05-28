package org.uminho.di.gsd.management;

public interface ManagedDevice {

	public String getStats();

	public void writeStats(String filename);

	public String getEndpoint();

	public void stopDevice();

	public void setMembership(String[] targets);

}
