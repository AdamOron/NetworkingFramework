import test.torrent.Leecher;

import java.io.IOException;

public class LeechTest
{
	public static void main(String[] args) throws IOException
	{
		Leecher leech = new Leecher(4999);
		leech.start();
	}
}
