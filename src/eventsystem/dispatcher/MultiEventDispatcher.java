package eventsystem.dispatcher;

import eventsystem.Event;
import eventsystem.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author AdamOron
 * MultiEventDispatcher class that is responsible for dispatching events to multiple handlers.
 * Implements EventDispatcher interface.
 */
public class MultiEventDispatcher implements EventDispatcher
{
	/**
	 * HashMap used to store all EventHandlers that are registered to a specific event type.
	 * Each entry has a key which is an event class, and a value which is a list of handlers.
	 */
	private HashMap<Class<? extends Event>, ArrayList<EventHandler>> handlers;

	public MultiEventDispatcher()
	{
		this.handlers = new HashMap<>();
	}

	public <T extends Event> void register(Class<T> eventType, EventHandler<T> handler)
	{
		ArrayList<EventHandler> registered = getRegisteredEnsured(eventType);
		registered.add(handler);
	}

	public <T extends Event> void unregister(Class<T> eventType, EventHandler<T> handler)
	{
		ArrayList<EventHandler> registered = getRegisteredEnsured(eventType);
		registered.remove(handler);
	}

	public void dispatch(Event event)
	{
		ArrayList<EventHandler> registered = handlers.get(event.getClass());

		if(registered == null)
		{
			return;
		}

		for(EventHandler handler : registered)
		{
			handler.handle(event);
		}
	}

	/**
	 * If the given event class has registered handlers in this dispatcher, the ArrayList of handlers will be returned.
	 * If it is not, an empty ArrayList of handlers will be added to the handlers HashMap, and then returned.
	 *
	 * @param eventType the class of the event type whose handlers we want to retrieve.
	 * @param <T> the event type, inherits from Event.
	 * @return ArrayList of all handlers registered to given event class.
	 */
	private <T extends Event> ArrayList<EventHandler> getRegisteredEnsured(Class<T> eventType)
	{
		ArrayList<EventHandler> registered = handlers.get(eventType);

		if(registered == null)
		{
			registered = new ArrayList<>();

			handlers.put(eventType, registered);
		}

		return registered;
	}
}
