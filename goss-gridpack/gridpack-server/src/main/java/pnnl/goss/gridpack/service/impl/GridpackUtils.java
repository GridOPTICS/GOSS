package pnnl.goss.gridpack.service.impl;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.powergrid.server.WebDataException;

public class GridpackUtils {

	public static void throwInputError(String errorMessageForClient){
		throw new WebDataException(errorMessageForClient);
	}
	
	public static void throwDataError(Response response) {
		DataResponse dataRes = null;
		if (response instanceof DataResponse) {
			dataRes = (DataResponse) response;
			if (dataRes.getData() instanceof DataError) {
				DataError de = (DataError) dataRes.getData();
				throw new WebDataException(de.getMessage());
			}
		}
	}
}
