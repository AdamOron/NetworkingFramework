package networking.connection;

import networking.data.Data;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable
{
	private Socket conn;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private boolean isClosed;

	public Connection(Socket conn) throws IOException
	{
		this.conn = conn;
		this.initStreams();
		this.isClosed = false;
	}

	public void send(Data data) throws IOException
	{
		this.output.writeObject(data);
	}

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
