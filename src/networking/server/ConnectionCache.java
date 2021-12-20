package networking.server;

import networking.connection.Connection;
import java.util.HashMap;
import java.util.Set;

/**
 * @author AdamOron
 *
 * Represents the cache of connections stored by a server.
 */
public class ConnectionCache
{
	/**
	 * Is responsible for assigning a unique key for each connection.
	 * This interface will allow other classes that use the ConnectionCache to implement a different key generation method.
	 */
	public interface UniqueKeyGenerator
	{
		/**
		 * @param conn to generate a key for.
		 * @return uniquely generated key for the given connection.
		 */
		int generate(Connection conn);
	}

	/**
	 * A simple exception for whenever there's a duplicate key generated.
	 */
	private static class DuplicateKeyException extends RuntimeException
	{
		public DuplicateKeyException(String message)
		{
			super(message);
		}
	}

	/* Mapping of all connections by their unique key */
	private HashMap<Integer, Connection> conns;
	/* The unique key generator for this ConnectionCache */
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
