package test.torrent;

public class Torrent
{
	public String data;

	public Torrent(String data)
	{
		this.data = data;
	}

	public String getSegment(int start)
	{
		return data.substring(start);
	}

	public String getSegment(int start, int end)
	{
		return data.substring(start, end);
	}

	public int size()
	{
		return data.length();
	}
}
