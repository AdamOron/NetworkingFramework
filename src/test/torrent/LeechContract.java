package test.torrent;

import networking.data.Data;

public class LeechContract extends Data
{
	public final int startIndex;
	public final int charAmount;

	public LeechContract(int startIndex, int charAmount)
	{
		this.startIndex = startIndex;
		this.charAmount = charAmount;
	}
}
