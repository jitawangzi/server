package cn.game.util;

public final class NioNetStackList<E>
{
	private final NioNetStackNode		start	= new NioNetStackNode();
	private final NioNetStackNodeBuf	buf		= new NioNetStackNodeBuf();
	private NioNetStackNode				end		= new NioNetStackNode();

	public NioNetStackList()
	{
		clear();
	}

	public final void addLast(final E elem)
	{
		final NioNetStackNode newEndNode = buf.removeFirst();
		end.value = elem;
		end.next = newEndNode;
		end = newEndNode;
	}

	public final E peek()
	{
		final NioNetStackNode old = start.next;
		final E value = old.value;
		return value;
	}

	public final E removeFirst()
	{
		final NioNetStackNode old = start.next;
		final E value = old.value;
		start.next = old.next;
		buf.addLast(old);
		return value;
	}

	public final boolean isEmpty()
	{
		return start.next == end;
	}

	public final void clear()
	{
		start.next = end;
	}

	private final class NioNetStackNode
	{
		private NioNetStackNode	next;
		private E				value;
	}

	private final class NioNetStackNodeBuf
	{
		private final NioNetStackNode	start	= new NioNetStackNode();
		private NioNetStackNode			end		= new NioNetStackNode();

		NioNetStackNodeBuf()
		{
			start.next = end;
		}

		final void addLast(final NioNetStackNode node)
		{
			node.next = null;
			node.value = null;
			end.next = node;
			end = node;
		}

		final NioNetStackNode removeFirst()
		{
			if (start.next == end) { return new NioNetStackNode(); }

			final NioNetStackNode old = start.next;
			start.next = old.next;
			return old;
		}
	}
}
