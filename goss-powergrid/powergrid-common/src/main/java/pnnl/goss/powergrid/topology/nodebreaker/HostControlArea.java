// THIS IS NOT USED THROUGHOUT THE HOST CONTROLLER.

//package pnnl.goss.powergrid.topology.nodebreaker;
//import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
//
//import javax.persistence.AttributeOverride;
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Table;
//
//import com.impetus.kundera.index.Index;
//import com.impetus.kundera.index.IndexCollection;
//
//import pnnl.goss.powergrid.topology.IdentifiedObject;
//import pnnl.goss.powergrid.topology.NodeBreakerDataType;
//import static pnnl.goss.powergrid.topology.NodeBreakerDataType.*;
//@Entity
//@Table(name=BASE_VOLTAGE)
//@AttributeOverride(name="mrid", column=@Column(name=BASE_VOLTAGE_MRID))
//@IndexCollection(columns={@Index(name=DATA_TYPE)})
//public class HostControlArea extends IdentifiedObject implements NodeBreakerDataType  {
//
//	private static final long serialVersionUID = -6300701230534749L;
//
//	@Column(name=DATA_TYPE)
//	protected String dataType;
//
//	@Override
//	public String getDataType() {
//		return dataType;
//	}
//
//	@Override
//	public void setDataType(String dataType) {
//		this.dataType = dataType;
//	}
//	
//}
