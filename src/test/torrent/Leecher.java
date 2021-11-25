package test.torrent;

import eventsystem.EventHandler;
import networking.connection.DataReceiveEvent;
import networking.server.ConnectionEvent;
import networking.server.Server;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Leecher
{
	class SeederJoinHandler implements EventHandler<ConnectionEvent.Accepted>
	{
		@Override
		public void handle(ConnectionEvent.Accepted event)
		{
			try
			{
				updateContracts();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	class SeederLeaveHandler implements EventHandler<ConnectionEvent.Ended>
	{
		@Override
		public void handle(ConnectionEvent.Ended event)
		{
			try
			{
				updateContracts();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	class SeedDataReceived implements EventHandler<DataReceiveEvent>
	{
		private ConcurrentHashMap<Character, Integer> received;

		public SeedDataReceived()
		{
			this.received = new ConcurrentHashMap<>();
		}

		@Override
		public void handle(DataReceiveEvent event)
		{
			System.out.println(event.data.toString());
			char chKey = ((TorrentChunk)event.data).data.content.charAt(0);
			this.received.put(chKey, this.received.getOrDefault(chKey, 0) + 1);
			receivedChars++;

			if(receivedChars >= TORRENT.size())
			{
				System.out.println(received.equals(expected));
				System.out.println(expected);
				System.out.println(received);

				try
				{
					server.close();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static final Torrent TORRENT = new Torrent("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");

	private static final HashMap<Character, Integer> expected;
	static
	{
		expected = new HashMap<>();

		for(char ch : TORRENT.data.toCharArray())
		{
			expected.put(ch, expected.getOrDefault(ch, 0) + 1);
		}
	}

	private Server server;

	private int receivedChars;

	public Leecher(int port) throws IOException
	{
		this.server = new Server(port, request -> server.getConnectionAmount() < remainingSize());

		this.server.registerConnectionAcceptedHandler(new SeederJoinHandler());
		this.server.registerConnectionEndedHandler(new SeederLeaveHandler());
		this.server.registerDataReceiveHandler(new SeedDataReceived());

		this.receivedChars = 0;
	}

	public void start() throws IOException
	{
		this.server.start();
	}

	private int remainingSize()
	{
		return TORRENT.size() - receivedChars;
	}

	private void updateContracts() throws IOException
	{
		if(server.getConnectionAmount() == 0) return;

		int fixedSize = remainingSize() / server.getConnectionAmount();
		int remainder = remainingSize() % server.getConnectionAmount();

		int last = receivedChars;
		for(int key : server.getConnectionSet())
		{
			int correctedSize = fixedSize + remainder;

			server.send(key, new LeechContract(last, correctedSize));
			remainder = 0;

			last += correctedSize;
		}
	}
}
