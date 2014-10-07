package pnnl.goss.gridpack.common.datamodel;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="TransmissionElements")
public class TransmissionElements {
	
	private List<TransmissionElementLine> lines;
	private List<TransmissionElementTransformer> transformers;
	
	private TransmissionElements(){
		
	}
	
	public TransmissionElements(List<TransmissionElementLine> lines, List<TransmissionElementTransformer> transformers){
		this.lines = lines;
		this.transformers = transformers;
	}

	@XmlElement(name="Line")
	public Collection<TransmissionElementLine> getLines(){
		return lines;
	}
	
	@XmlElement(name="Transformer")
	public Collection<TransmissionElementTransformer> getTransformers(){
		return transformers;
	}
}
