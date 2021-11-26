import networking.connection.Connection;
import networking.data.Data;
import networking.data.StringData;
import networking.server.Server;

import java.io.IOException;

public class ServerTest
{
	private static void test() throws Exception
	{
		Server server = new Server(4999);

		server.registerConnectionAcceptedHandler(connEvent -> System.out.println("Conn " + connEvent.connectionKey));

		server.registerDataReceiveHandler(dataEvent ->
		{
			System.out.println("Server Received: " + dataEvent.toString());

			try
			{
				dataEvent.sender.send(new StringData("received"));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		});

		server.start();
	}

	public static void main(String[] args) throws Exception
	{
		test();
	}
}
