package eventsystem.dispatcher;

import eventsystem.Event;
import eventsystem.EventHandler;

/**
 * @author AdamOron
 * EventDisptacher class that is responsible for dispatching events of any type.
 * Each EventDispatcher contains its own EventHandlers.
 */
public
interface EventDispatcher
{
	/**
	 * Register an EventHandler to handle all events of given type that are dispatched by this dispatcher.
	 *
	 * @param eventType the class of the Event type we want to handle.
	 * @param handler the handler that's supposed to handle the event type.
	 * @param <T> the event type, inherits from Event.
	 */
	<T extends Event> void register(Class<T> eventType, EventHandler<T> handler);

	/**
	 * Unregisters an EventHandler from handling events of given type dispatched by this dispatcher.
	 *
	 * @param eventType the class of the Event type we want to stop handling.
	 * @param handler the handler that we want to unregister. Should be already registered to this dispatcher.
	 * @param <T> the event type, inherits from Event.
	 */
	<T extends Event> void unregister(Class<T> eventType, EventHandler<T> handler);

	/**
	 * Dispatch given Event to all registered EventHandlers.
	 *
	 * @param event the event to dispatch.
	 * @param <T> the event type, inherits from Event.
	 */
	<T extends Event> void dispatch(T event);
}
