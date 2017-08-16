package gov.pnnl.goss.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.UUID;

import gov.pnnl.goss.client.api.Response;

public class GossResponse implements Response, Serializable {
	
	private static final long serialVersionUID = 8541810525300877513L;
	
	String id = UUID.randomUUID().toString();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public int sizeof() throws IOException {

	    ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	    ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream);

	    objectOutputStream.writeObject(this);
	    objectOutputStream.flush();
	    objectOutputStream.close();

	    return byteOutputStream.toByteArray().length;
	}
}

