package pnnl.goss.core.server.runner.datasource;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.dm.annotation.api.Component;

import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourceType;

@Component
public class CommandLogDataSource implements DataSourceObject  {
	
	private final List<String> log = new ArrayList<>();
		
	public List<String> getList(){
		return log;
	}
	
	@Override
	public DataSourceType getDataSourceType() {
		return DataSourceType.DS_TYPE_OTHER;
	}
	public void log(String cmdText){
		log.add(cmdText);
	}
	
	public void clear(){
		log.clear();
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
