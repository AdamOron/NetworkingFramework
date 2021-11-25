package networking.data;

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
