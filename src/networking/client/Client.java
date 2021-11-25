package networking.client;

import eventsystem.EventHandler;
import eventsystem.dispatcher.MultiEventDispatcher;
import networking.connection.Connection;
import networking.connection.DataReceiveEvent;
import networking.data.Data;
import networking.server.Server;

import java.io.IOException;
import java.net.Socket;

public class Client
{
	private class ThreadedConnection extends Thread
	{
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
	}

	private MultiEventDispatcher eventDispatcher;
	private Connection conn;

	public Client()
	{
		this.eventDispatcher = new MultiEventDispatcher();
	}

	public void connect(String host, int port) throws IOException
	{
		this.conn = new Connection(new Socket(host, port));
		new ThreadedConnection().start();
	}

	public void send(Data data) throws IOException
	{
		this.conn.send(data);
	}

	public void registerDataReceiveHandler(EventHandler<DataReceiveEvent> handler)
	{
		this.eventDispatcher.register(DataReceiveEvent.class, handler);
	}
}
