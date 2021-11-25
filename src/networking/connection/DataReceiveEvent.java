package networking.connection;

import eventsystem.Event;
import networking.data.Data;

public class DataReceiveEvent extends Event
{
	public final Connection sender;
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
