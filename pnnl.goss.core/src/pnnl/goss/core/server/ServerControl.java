package pnnl.goss.core.server;

import com.northconcepts.exception.SystemException;

public interface ServerControl {
	
	/**
	 * Start the server.  During the execution of this method the
	 * implementor should initialize all properties such that the
	 * server can receive Request objects and route them to their
	 * appropriate handlers.
	 * 	
	 * @throws SystemException
	 */
	void start() throws SystemException;
	
	/**
	 * Stop the server.  During the execution of this method the
	 * system should shutdown its method of transport, stop all
	 * routing, release any tcp resources that it has available
	 * and change the status of the server to not running.
	 * 
	 * @throws SystemException
	 */
	void stop() throws SystemException;
	
	/**
	 * A plain status of whether the server is able to route Request
	 * objects currently.
	 * 
	 * @return
	 */
	boolean isRunning();
	
}
