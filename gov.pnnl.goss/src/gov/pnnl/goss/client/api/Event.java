package gov.pnnl.goss.client.api;

import java.io.Serializable;

public interface Event extends Serializable {

	SeverityType[] getSeverityTypes();

	int getId();

	void setId(int id);

	String getStatus();

	void setStatus(String status);

	SeverityType getSeverity();

	void setSeverity(SeverityType severity);

	String getEventType();

	void setEventType(String eventType);

	String getDescription();

	void setDescription(String description);

	int getRelatedEventId();

	void setRelatedEventId(int relatedEventId);

}