// THIS IS NOT USED IN THE ESCA60 EXAMPLE FILE


//package pnnl.goss.powergrid.topology.nodebreaker;
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
//public class DayType extends IdentifiedObject implements NodeBreakerDataType  {
//
//	private static final long serialVersionUID = -5952166562010225309L;
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
