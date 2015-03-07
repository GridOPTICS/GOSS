package pnnl.goss.core.server;

public abstract class AbstractDataSourceObject implements DataSourceObject {
	
	@Override
	public void onGet() {
		// Performing a noop		
	}
	
	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
