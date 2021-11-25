package test.torrent;

import eventsystem.EventHandler;
import networking.client.Client;
import networking.connection.DataReceiveEvent;
import networking.data.StringData;
import networking.data.Data;
import java.io.IOException;

public class Seeder
{
	class LeechContractHandler implements EventHandler<DataReceiveEvent>
	{
		@Override
		public void handle(DataReceiveEvent event)
		{
			if(!(event.data instanceof LeechContract))
			{
				throw new IllegalArgumentException("Unknown Data type");
			}

			handleContract((LeechContract) event.data);
		}

		private void handleContract(LeechContract contract)
		{
			if(threadedSeed != null)
			{
				threadedSeed.stopSeeding();
			}

			Seeder.this.contract = contract;
			Seeder.this.nextIndex = contract.startIndex;

			System.out.println("Seeding " + contract.startIndex + "-" + (contract.startIndex + contract.charAmount) + ", " + contract.charAmount);

			threadedSeed = new ThreadedSeed();
			threadedSeed.start();
		}
	}

	class ThreadedSeed extends Thread
	{
		private boolean isSeeding;

		public ThreadedSeed()
		{
			this.isSeeding = true;
		}

		public void stopSeeding()
		{
			this.isSeeding = false;
		}

		@Override
		public void run()
		{
			while(isSeeding)
			{
				sendChunk();

				cooldown();
			}
		}

		private void sendChunk()
		{
			try
			{
				Data data = new TorrentChunk(new StringData(Leecher.TORRENT.getSegment(nextIndex++, nextIndex)), nextIndex - 1);
				System.out.println("Seeding " + data);
				client.send(data);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			if(nextIndex >= contract.startIndex + contract.charAmount)
			{
				stopSeeding();
			}
		}
	}
	
	private Client client;
	private ThreadedSeed threadedSeed;
	private LeechContract contract;
	private long seedInterval;
	private int nextIndex;

	public Seeder()
	{
		this.client = new Client();
		this.client.registerDataReceiveHandler(new LeechContractHandler());
		this.threadedSeed = null;
		this.contract = null;
		this.seedInterval = 0;
		this.nextIndex = 0;
	}

	public void start(String host, int port) throws IOException
	{
		client.connect(host, port);
	}

	public void setSeedInterval(long seedInterval)
	{
		this.seedInterval = seedInterval;
	}

	private void waitForContract()
	{
		while(contract == null)
		{
			System.out.println("Waiting for LeechContract");

			sleep(500);
		}
	}

	private void cooldown()
	{
		if(seedInterval == 0) return;

		System.out.println("Seeding Cooldown: " + seedInterval);

		sleep(seedInterval);
	}

	private static void sleep(long millis)
	{
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() - start < millis)
		{
		}
	}
}
