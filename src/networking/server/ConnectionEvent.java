package networking.server;

import eventsystem.Event;
import networking.connection.Connection;

public abstract class ConnectionEvent extends Event
{
	public final Connection connection;
	public final int connectionKey;

	public ConnectionEvent(Connection connection, int connectionKey)
	{
		this.connection = connection;
		this.connectionKey = connectionKey;
	}

	public static class Accepted extends ConnectionEvent
	{
		public Accepted(Connection connection, int connectionKey)
		{
			super(connection, connectionKey);
		}
	}

	public static class Ended extends ConnectionEvent
	{
		public Ended(Connection connection, int connectionKey)
		{
			super(connection, connectionKey);
		}
	}
}
