package networking.data;

import java.io.Serializable;

/**
 * @author AdamOron
 *
 * Represents any piece of data transferred by connections.
 * This is abstract to allow transferring of sophisticated data types.
 */
public abstract class Data implements Serializable
{
//	private String content;
//
//	public Data(String content)
//	{
//		this.content = content;
//	}
//
//	@Override
//	public String toString()
//	{
//		return "<" + content + ">";
//	}
}
