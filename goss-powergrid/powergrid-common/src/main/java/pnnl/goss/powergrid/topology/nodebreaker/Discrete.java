package pnnl.goss.powergrid.topology.nodebreaker;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;

import pnnl.goss.powergrid.topology.IdentifiedObject;
import pnnl.goss.powergrid.topology.NodeBreakerDataType;

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;

@Entity
public class Discrete extends IdentifiedObject implements NodeBreakerDataType  {
	
}
