package networking.client;

import eventsystem.EventHandler;
import eventsystem.dispatcher.MultiEventDispatcher;
import networking.connection.Connection;
import networking.connection.DataReceiveEvent;
import networking.data.Data;
import java.io.IOException;
import java.net.Socket;

/**
 * @author AdamOron
 *
 * Class representing a Client connecting to a Server.
 * Data received from the Server is handled using EventHandlers, which are registered outside the Client class.
 */
public final class Client
{
	/**
	 * ThreadedConnection runs data receival on a separate thread.
	 * Waiting for data to be received from a Socket freezes the thread, so we take advantage of multi-threading,
	 * allowing simultaneous input & output.
	 */
	private class ThreadedConnection extends Thread
	{
		@Override
		public void run()
		{
			/* Receive data as long as there's a connection to the Server */
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
						System.out.println("Client: Terminating");

						conn.close();
					}
					catch(IOException closeException)
					{
						closeException.printStackTrace();
					}
				}
			}
		}

		/**
		 * @throws IOException if failed to receive data.
		 *
		 * Receive data from Server & dispatch matching Event.
		 */
		private void receiveData() throws IOException
		{
			/* Wait until data is received from Server */
			Data received = conn.read();
			/* Once data is received, dispatch DataReceiveEvent with the received data */
			eventDispatcher.dispatch(new DataReceiveEvent(conn, received));
		}
	}

	/* An EventDispatcher for multiple registered handlers */
	private MultiEventDispatcher eventDispatcher;
	/* The connection to the Server */
	private Connection conn;

	/**
	 * Construct a new Client without connecting it to a Server.
	 */
	public Client()
	{
		this.eventDispatcher = new MultiEventDispatcher();
	}

	/**
	 * @param host the target Server's host.
	 * @param port the target Server's port.
	 * @throws IOException if connection to Server failed.
	 *
	 * Connect this Client to Server.
	 */
	public void connect(String host, int port) throws IOException
	{
		/* Establish Connection with given host & port */
		this.conn = new Connection(new Socket(host, port));
		/* Begin simultaneous data receival */
		new ThreadedConnection().start();
	}

	/**
	 * @param data to be sent to the Server.
	 * @throws IOException if failed to send Data.
	 */
	public void send(Data data) throws IOException
	{
		/* Send data to Server using Connection's send method */
		this.conn.send(data);
	}

	/**
	 * @param handler to be registered.
	 *
	 * Register given handler to all DataReceivalEvents. This is what allows us to actually handle data receival.
	 */
	public void registerDataReceiveHandler(EventHandler<DataReceiveEvent> handler)
	{
		this.eventDispatcher.register(DataReceiveEvent.class, handler);
	}
}
