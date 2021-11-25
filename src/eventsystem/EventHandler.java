package eventsystem;

/**
 * @author AdamOron
 * EventHandler class that handles a certain event type.
 *
 * @param <T> the type of events to be handled.
 */
public interface EventHandler<T extends Event>
{
	void handle(T event);
}
