package networking.data;

/**
 * @author AdamOron
 *
 * Represents String data transferred by connections.
 */
public class StringData extends Data
{
	public String content;

	public StringData(String content)
	{
		this.content = content;
	}

	@Override
	public String toString()
	{
		return "<" + content + ">";
	}
}
