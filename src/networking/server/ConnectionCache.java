package networking.server;

import networking.connection.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ConnectionCache
{
	public interface UniqueKeyGenerator
	{
		int generate(Connection conn);
	}

	private static class DuplicateKeyException extends RuntimeException
	{
		public DuplicateKeyException(String message)
		{
			super(message);
		}
	}

	private HashMap<Integer, Connection> conns;
	private UniqueKeyGenerator idGenerator;

	public ConnectionCache(UniqueKeyGenerator idGenerator)
	{
		this.conns = new HashMap<>();
		this.idGenerator = idGenerator;
	}

	public int size()
	{
		return conns.size();
	}

	public int cache(Connection conn)
	{
		int key = idGenerator.generate(conn);

		if(conns.putIfAbsent(key, conn) != null)
		{
			throw new DuplicateKeyException("Key " + key + " already exists in ConnectionCache.");
		}

		return key;
	}

	public void remove(int key)
	{
		conns.remove(key);
	}

	public Connection get(int key)
	{
		return conns.get(key);
	}

	public Set<Integer> getConnectionSet()
	{
		return conns.keySet();
	}
}
