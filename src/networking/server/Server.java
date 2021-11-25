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
	/**
	 * ThreadedConnection allows simultaneous handling of all connections to this server.
	 * Responsible for dispatching DataReceiveEvent for when a Client sends data.
	 */
	private class ThreadedConnection extends Thread
	{
		/* The connection to the Client */
		private Connection conn;
		/* The unique ID of the connection */
		private int connKey;

		/**
		 * @param conn the connection to be handled.
		 * @param connKey the ID of the connection.
		 */
		public ThreadedConnection(Connection conn, int connKey)
		{
			this.conn = conn;
			this.connKey = connKey;
		}

		@Override
		public void run()
		{
			/* As long as handled connection to Client is alive, continue handling the connection and receiving data */
			while(conn.isAlive())
			{
				try
				{
					receiveData();
				}
				catch(IOException readException)
				{
					try
					{
						/* If reading data caused exception, we terminate the connection & exit the method */
						terminate();
						return;
					}
					catch(IOException closeException)
					{
						/* If we fail to terminate the connection, print the exception */
						closeException.printStackTrace();
					}
				}
			}
		}

		/**
		 * @throws IOException if failed to receive data from Client.
		 *
		 * Receives data from Client.
		 */
		private void receiveData() throws IOException
		{
			/* Wait until data is received from Client */
			Data received = conn.read();
			/* Once data is received, dispatch DataReceiveEvent with the received data */
			eventDispatcher.dispatch(new DataReceiveEvent(conn, received));
		}

		/**
		 * @throws IOException if failed to terminate the connection.
		 *
		 * Terminate the connection & remove it from the cache.
		 */
		private void terminate() throws IOException
		{
			close(connKey);
			connectionCache.remove(connKey);
		}
	}

	/**
	 * Interface used to filtering each received connection.
	 * Using such a filter allows implementing the filtering logic from outside the Server class, meaning that any
	 * class that uses a Server can use its own filtering logic through implementing a ConnectionFilter and passing
	 * it as a parameter.
	 */
	public interface ConnectionFilter
	{
		/**
		 * @param request the connection request to be filtered.
		 * @return whether the connection request should be accepted or not.
		 */
		boolean filter(Connection request);
	}

	/* The ServerSocket of this Server */
	private ServerSocket serverSocket;
	/* The ConnectionCache for all of this Server's connection */
	private ConnectionCache connectionCache;
	/* The ConnectionFilter used to filter each connection request */
	private ConnectionFilter filter;
	/* The EventDispatcher for all DataReceiveEvents/ConnectionEvents */
	private MultiEventDispatcher eventDispatcher;
	/* Saves whether the connection has been closed or not. TODO: find a better way. */
	private boolean isClosed;

	/**
	 * @param port the desired local port of this Server.
	 * @param filter the filter for any incoming connection requests.
	 * @throws IOException if failed to host local Server.
	 *
	 * Constructs new Server and hosts local Server on given port.
	 */
	public Server(int port, ConnectionFilter filter) throws IOException
	{
		/* Host local Server */
		initServerSocket(port);
		/* Initialize ConnectionCache where each Connection's ID is its 'index' in the cache */
		this.connectionCache = new ConnectionCache(conn -> this.connectionCache.size());
		this.eventDispatcher = new MultiEventDispatcher();
		this.filter = filter;
	}

	/**
	 * @param port the desired local port of this Server.
	 * @throws IOException if failed to host local Server.
	 *
	 * Constructs new Server and hosts local Server on given port.
	 * Accepts any incoming connection request.
	 */
	public Server(int port) throws IOException
	{
		/* Construct Server with given port & always true ConnectionFilter (accepts any incoming connection request) */
		this(port, request -> true);
	}

	/**
	 * @throws IOException if failed to host local Server.
	 *
	 * Start the Server, handle incoming connection requests.
	 */
	public void start() throws IOException
	{
		/* As long as the Server's Socket isn't closed */
		while(!serverSocket.isClosed())
		{
			/* Save incoming connection request */
			Connection conn = acceptConnection();

			/* Pass connection request to filter */
			if(filter.filter(conn))
			{
				/* If filter accepted the request, add the connection to the cache & start handling it */
				add(conn);
			}
			else
			{
				/* If connection request isn't accepted, close it */
				conn.close();
			}
		}
	}

	/**
	 * @param id the id of the target connection.
	 * @param data the data to be sent.
	 * @throws IOException if failed to send data.
	 *
	 * Sends given data to connection whose id matches given id.
	 */
	public void send(int id, Data data) throws IOException
	{
		/* Get matching connection */
		Connection conn = connectionCache.get(id);

		/* If there is no matching connection, throw an exception */
		if(conn == null)
		{
			throw new NullPointerException("Connection ID does not exist in the Server's ConnectionCache.");
		}

		/* Send given data to matching connection if it exists */
		conn.send(data);
	}

	/**
	 * @param data the data to be sent.
	 * @throws IOException if failed to send data.
	 *
	 * Send given data to all Clients connected to this Server.
	 */
	public void broadcast(Data data) throws IOException
	{
		/* For every connection's ID */
		for(int connKey : connectionCache.getConnectionSet())
		{
			/* Send given data to connection ID */
			send(connKey, data);
		}
	}

	@Override
	public void close() throws IOException
	{
		isClosed = true;

		Iterator<Integer> iterator = getConnectionSet().iterator();

		while(iterator.hasNext())
		{
			close(iterator.next());
			iterator.remove();
		}

		serverSocket.close();
		serverSocket = null;
	}

	public boolean isClosed()
	{
		return isClosed;
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
