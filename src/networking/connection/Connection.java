package networking.connection;

import networking.data.Data;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Connection class representing a connection to a Server or a Client.
 * Basically a wrapper for a Socket with I/O capabilities.
 */
public class Connection implements Closeable
{
	/* The Socket of the Connection */
	private Socket conn;
	/* The OutputStream used to send data through the Connection */
	private ObjectOutputStream output;
	/* The InputStream used to read data from the Connection */
	private ObjectInputStream input;
	/* Boolean signifying whether the Connection is closed or not */
	private boolean isClosed;

	/**
	 * @param conn the Socket to be wrapped by this Connection.
	 * @throws IOException if failed to initialize I/O streams.
	 *
	 * Constructs new Connection for the given Socket, initializes input & output streams.
	 */
	public Connection(Socket conn) throws IOException
	{
		this.conn = conn;
		this.initStreams();
		this.isClosed = false;
	}

	/**
	 * @param data the data to be sent.
	 * @throws IOException if failed to send data over Connection.
	 */
	public void send(Data data) throws IOException
	{
		this.output.writeObject(data);
	}

	/**
	 * @return data sent from the Connection.
	 * @throws IOException if failed to read data from the Connection.
	 *
	 * Waits until data is received from Connection & returns it.
	 */
	public Data read() throws IOException
	{
		try
		{
			return (Data) this.input.readObject();
		}
		catch(ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public boolean isAlive()
	{
		return !isClosed;
	}

	@Override
	public void close() throws IOException
	{
		this.isClosed = true;
		this.conn.close();
	}

	@Override
	public String toString()
	{
		return "<" + this.conn.getInetAddress() + ":" + this.conn.getPort() + ">";
	}

	private void initStreams() throws IOException
	{
		this.output = new ObjectOutputStream(this.conn.getOutputStream());
		this.input = new ObjectInputStream(this.conn.getInputStream());
	}
}
