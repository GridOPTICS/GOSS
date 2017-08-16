package gov.pnnl.goss.client.api;

import java.io.IOException;
import java.io.Serializable;

public interface Response extends Serializable {
	String getId();
	void setId(String id);
	int sizeof() throws IOException;
}
