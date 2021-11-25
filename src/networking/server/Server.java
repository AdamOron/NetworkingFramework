package networking.server;

import eventsystem.EventHandler;
import eventsystem.dispatcher.MultiEventDispatcher;
import networking.connection.Connection;
import networking.connection.DataReceiveEvent;
import networking.data.Data;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Set;

public class Server implements Closeable
{
	private class ThreadedConnection extends Thread
	{
		private Connection conn;
		private int connKey;

		public ThreadedConnection(Connection conn, int connKey)
		{
			this.conn = conn;
			this.connKey = connKey;
		}

		@Override
		public void run()
		{
			while(conn.isAlive())
			{
				try
				{
					Data received = conn.read();
					eventDispatcher.dispatch(new DataReceiveEvent(conn, received));
				}
				catch(IOException readException)
				{
					try
					{
						close(connKey);
						connectionCache.remove(connKey);
						return;
					}
					catch(IOException closeException)
					{
						closeException.printStackTrace();
					}
				}
			}
		}
	}

	public interface ConnectionFilter
	{
		boolean filter(Connection request);
	}

	private ServerSocket serverSocket;
	private ConnectionCache connectionCache;
	private MultiEventDispatcher eventDispatcher;
	private ConnectionFilter filter;

	public Server(int port, ConnectionFilter filter) throws IOException
	{
		initServerSocket(port);
		this.connectionCache = new ConnectionCache(conn -> this.connectionCache.size());
		this.eventDispatcher = new MultiEventDispatcher();
		this.filter = filter;
	}

	public Server(int port) throws IOException
	{
		this(port, request -> true);
	}

	public void start() throws IOException
	{
		while(true)
		{
			Connection conn = acceptConnection();

			if(filter.filter(conn))
			{
				add(conn);
			}
			else
			{
				conn.close();
			}
		}
	}

	public void send(int id, Data data) throws IOException
	{
		Connection conn = connectionCache.get(id);
		conn.send(data);
	}

	public void broadcast(Data data) throws IOException
	{
		for(Integer connKey : connectionCache.getConnectionSet())
		{
			Connection conn = connectionCache.get(connKey);
			conn.send(data);
		}
	}

	@Override
	public void close() throws IOException
	{
		Iterator<Integer> iterator = getConnectionSet().iterator();

		while(iterator.hasNext())
		{
			close(iterator.next());
			iterator.remove();
		}

		serverSocket.close();
	}

	public Set<Integer> getConnectionSet()
	{
		return connectionCache.getConnectionSet();
	}

	public int getConnectionAmount()
	{
		return connectionCache.size();
	}

	public void registerDataReceiveHandler(EventHandler<DataReceiveEvent> handler)
	{
		this.eventDispatcher.register(DataReceiveEvent.class, handler);
	}

	public void registerConnectionAcceptedHandler(EventHandler<ConnectionEvent.Accepted> handler)
	{
		this.eventDispatcher.register(ConnectionEvent.Accepted.class, handler);
	}

	public void registerConnectionEndedHandler(EventHandler<ConnectionEvent.Ended> handler)
	{
		this.eventDispatcher.register(ConnectionEvent.Ended.class, handler);
	}

	private void add(Connection conn)
	{
		int key = connectionCache.cache(conn);

		System.out.println("Server: Accepting <" + key + ">");

		eventDispatcher.dispatch(new ConnectionEvent.Accepted(conn, key));

		new ThreadedConnection(conn, key).start();
	}

	private void close(int connKey) throws IOException
	{
		System.out.println("Server: Terminating <" + connKey + ">");

		Connection conn = connectionCache.get(connKey);
		conn.close();

		eventDispatcher.dispatch(new ConnectionEvent.Ended(conn, connKey));
	}

	private Connection acceptConnection() throws IOException
	{
		return new Connection(this.serverSocket.accept());
	}

	private void initServerSocket(int port) throws IOException
	{
		this.serverSocket = new ServerSocket(port);
	}
}
