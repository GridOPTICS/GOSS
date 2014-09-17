package pnnl.goss.powergrid.nodebreaker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Result {
	private long start = 0;
	private long stop = 0;
	private boolean running = false;
	private List<UUID> items = new ArrayList<UUID>();
	
	public void addItem(UUID uuid){
		items.add(uuid);
	}
	
	public String getUuidString(int indx){
		return items.get(indx).toString();
	}

	public void start() {
		start(true);
	}

	public void start(boolean reset) {
		start = System.nanoTime();
		running = true;
	}

	public void stop() {
		stop = System.nanoTime();
		running = false;
	}

	public long getDiff() {
		if (running) {
			return System.nanoTime() - start;
		}
		return stop - start;
	}
}
