import networking.client.Client;
import networking.connection.Connection;
import networking.data.Data;
import networking.data.StringData;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

public class ClientTest
{
	private static void test() throws IOException
	{
		Client client = new Client();
		client.connect("localhost", 4999);

		client.registerDataReceiveHandler(event ->
		{
			System.out.println("Client Received: " + event.toString());
			System.out.println("Client will respond in 5s");

			try
			{
				Thread.sleep(5000);

				client.send(new StringData("Response"));
			}
			catch(IOException | InterruptedException e)
			{
				e.printStackTrace();
			}
		});

		client.send(new StringData("Ice Breaker"));
	}

	public static void main(String[] args) throws IOException
	{
		//test();

		HashSet<Integer> set = new HashSet<>();
		set.add(0);
		set.add(1);
		set.add(2);
		set.add(3);
		set.add(4);

		for(Integer i : set)
		{
			System.out.println(i);
		}
	}
}
