/*
    Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE

    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
 */

package pnnl.goss.core;

import java.io.Serializable;
import java.lang.reflect.Type;

import javax.jms.Destination;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import pnnl.goss.core.DataError;
import pnnl.goss.core.Response;

public class DataResponse extends Response implements Serializable {

	private static final long serialVersionUID = 3555288982317165831L;
	Serializable data;

	DataError error;

	boolean responseComplete;

	String destination;

	Destination replyDestination;

	public DataResponse() {

	}

	public DataResponse(Serializable data) {
		setData(data);
	}

	public boolean wasDataError() {
		return isError();
	}

	public boolean isError() {
		return data.getClass().equals(DataError.class);
	}

	public void setError(DataError error) {
		this.error = error;
	}

	public DataError getError() {
		return error;
	}

	public Serializable getData() {
		return data;
	}

	public void setData(Serializable data) {
		this.data = data;
	}

	/**
	 * To check if response is complete in case of request with recurring
	 * responses.
	 * 
	 * @return True if this is the last response for the query, false otherwise.
	 */
	public boolean isResponseComplete() {
		return responseComplete;
	}

	/**
	 * To set if response is complete in case of request with recurring
	 * responses.
	 * 
	 * @param responseComplete
	 *            : True if this is the last response for the query, false
	 *            otherwise.
	 */
	public void setResponseComplete(boolean responseComplete) {
		this.responseComplete = responseComplete;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}
	
	

	public Destination getReplyDestination() {
		return replyDestination;
	}

	public void setReplyDestination(Destination replyDestination) {
		this.replyDestination = replyDestination;
	}

	@Override
	public String toString() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Serializable.class, new InterfaceAdapter());
		Gson gson = builder.create();
		return gson.toJson(this);
	}

	public static DataResponse parse(String jsonString) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Serializable.class, new InterfaceAdapter());
		Gson gson = builder.create();
		DataResponse obj = gson.fromJson(jsonString, DataResponse.class);
		 if(obj.id==null || (obj.data==null && obj.error==null))
			 throw new JsonSyntaxException("Expected attribute id and data/error not found");
		return obj;

	}

	private static class InterfaceAdapter implements
			JsonSerializer<Serializable>, JsonDeserializer<Serializable> {

		private static final String CLASSNAME = "CLASSNAME";
		private static final String DATA = "DATA";

		public Serializable deserialize(JsonElement jsonElement, Type type,
				JsonDeserializationContext jsonDeserializationContext)
				throws JsonParseException {

			if (jsonElement instanceof JsonPrimitive) {
				return jsonElement.getAsString();
			} else {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
				String className = prim.getAsString();

				if ("java.lang.String".equals(className)) {
					return jsonObject.get(DATA).getAsString();
				} else {
					Class klass = getObjectClass(className);
					return jsonDeserializationContext.deserialize(
							jsonObject.get(DATA), klass);
				}
			}
		}

		/****** Helper method to get the className of the object to be deserialized *****/
		public Class getObjectClass(String className) {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				// e.printStackTrace();
				throw new JsonParseException(e.getMessage());
			}
		}

		@Override
		public JsonElement serialize(Serializable jsonElement, Type type,
				JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty(CLASSNAME, jsonElement.getClass().getName());
			jsonObject.add(DATA,
					jsonSerializationContext.serialize(jsonElement));
			return jsonObject;
		}
	}

}
