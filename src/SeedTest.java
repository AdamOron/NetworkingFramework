import test.torrent.Seeder;

import java.io.IOException;

public class SeedTest
{
	public static void main(String[] args) throws IOException
	{
		Seeder seeder = new Seeder();
		seeder.setSeedInterval(200);
		seeder.start("localhost", 4999);
	}
}
