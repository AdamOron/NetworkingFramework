package test.torrent;

import networking.data.Data;
import networking.data.StringData;

public class TorrentChunk extends Data
{
	public final StringData data;
	public final int index;

	public TorrentChunk(StringData data, int index)
	{
		this.data = data;
		this.index = index;
	}

	@Override
	public String toString()
	{
		return data.toString();
	}
}
