package org.nikkii.eventhub.event;

public interface EventExecutor {

	public void execute(Event event) throws EventException;
	
}
