package networking.connection;

import eventsystem.Event;
import networking.data.Data;

/**
 * @author AdamOron
 *
 * Represents an event for the receival of Data from a connection.
 */
public class DataReceiveEvent extends Event
{
	/* The connection that sent the data */
	public final Connection sender;
	/* The data that was received */
	public final Data data;

	public DataReceiveEvent(Connection sender, Data data)
	{
		this.sender = sender;
		this.data = data;
	}

	@Override
	public String toString()
	{
		return "<" + sender.toString() + " sent " + data.toString() + ">";
	}
}
