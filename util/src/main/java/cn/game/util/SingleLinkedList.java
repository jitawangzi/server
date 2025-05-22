package cn.game.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 * 最简单的链表，满足迭代时修改，不用维持顺序
 * 
 * 也可以使用Comparator来维持顺序
 * 
 * 2021年6月16日 下午2:35:09
 * @author SYQ
 * @param <T>
 */
public class SingleLinkedList<T> implements Iterable<T> {

	/** 哨兵头节点，不保存数据 */
	public Node<T> sentry = new Node<>();
	private Comparator<? super T> comparator;

	public SingleLinkedList() {
		this(null);
	}

	public SingleLinkedList(Comparator<? super T> comparator) {
		this.comparator = comparator;
	}

	public void setComparator(Comparator<? super T> comparator) {
		this.comparator = comparator;
	}

	/**
	 * 链表头部增加数据，一般用这个，速度快，不用维持顺序,迭代时增加的数据不会被迭代到
	 * 
	 * @param data
	 */
	public void addFirst(T data) {
		Node<T> add = new Node<T>(data);
		add.next = sentry.next;
		sentry.next = add;
	}

	/**
	* 插入元素，保持较小的元素在前面
	* @param data 要插入的数据
	*/
	public void addSorted(T data) {
		if (comparator == null) {
			throw new IllegalStateException("Comparator is not set");
		}
		Node<T> newNode = new Node<>(data);
		Node<T> current = sentry;

		while (current.next != null && comparator.compare(current.next.data, data) < 0) {
			current = current.next;
		}
		newNode.next = current.next;
		current.next = newNode;
	}

	/**
	 * 链表尾部增加节点
	 * @param data
	 */
	private void addLast(Node<T> data) {
		
		Node<T> last = findLast();
		last.next = data;
	}

	/**
	 * 链表尾部增加数据
	 * @param data
	 */
	private void addLast(T data) {

		Node<T> add = new Node<T>(data);
		Node<T> last = findLast();
		last.next = add;
	}

	/**
	 *  在指定的节点后增加数据
	 * @param node
	 * @param data
	 */
	private void add(Node<T> node, T data) {

		Node<T> add = new Node<T>(data);
		add.next = node.next;
		node.next = add;
	}

	public Node<T> find(T data) {
		Node<T> cur = sentry.next;
		while (cur != null) {
			if (cur.data.equals(data)) {
				return cur;
			}
			cur = cur.next;
		}
		return null;
	}

	public boolean contains(T data) {
		return find(data) != null;
	}

	public Node<T> findLast() {
		Node<T> cur = sentry;
		while (cur.next != null) {
			cur = cur.next;
		}
		return cur;
	}

	private Node<T> findPre(Node<T> node) {
		if (node == null) {
			throw new NullPointerException() ; 
		}
		Node<T> cur = sentry;
		while (cur.next != null) {
			if (cur.next.equals(node)) {
				return cur;
			}
			cur = cur.next;
		}
		return null;
	}
	private Node<T> findPre(T data) {
		if (data == null) {
			throw new NullPointerException() ; 
		}
		Node<T> cur = sentry;
		while (cur.next != null) {
			if (cur.next.data.equals(data)) { 
				return cur; 
			}
			cur = cur.next;
		}
		return null;
	}

	public void remove(Node<T> node) {
		Node<T> pre = findPre(node);
		if (pre == null)
			return;
		pre.next = node.next;

	}
	public void remove(T data) {
		Node<T> pre = findPre(data);
		if (pre == null)
			return;
		Node<T> cur = pre.next;
		if (cur == null)
			return;

		pre.next = cur.next;

	}

	public void print() {

		Node<T> node = this.sentry;
		while ((node = node.next) != null) {
			System.out.println("节点： " + node.data);
		}
		System.out.println("===================");
	}

	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}

	private class Itr implements Iterator<T> {
		private Node<T> prev = sentry;
		private Node<T> current = sentry;
		private Node<T> next = sentry.next;
		private boolean canRemove = false;

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			prev = current;
			current = next;
			next = next.next;
			canRemove = true;
			return current.data;
		}

		@Override
		public void remove() {
			if (!canRemove) {
				throw new IllegalStateException();
			}
			prev.next = next;
			current = prev;
			canRemove = false;
		}
	}
	public static void main(String args[]) {
		testModifyWhileIterating();
	}

	public static void testModifyWhileIterating() {

		SingleLinkedList<String> list = new SingleLinkedList<String>();

		list.addFirst("a");
		list.addFirst("b");
		list.addFirst("c");
		list.addFirst("d");
		list.addFirst("e");
		list.addFirst("f");

		// 直接迭代
//		Node<String> node = list.sentry;
//		while ((node = node.next) != null) {
//			System.out.println("节点： " + node.data);
//			if (node.data.equals("c")) {
//				list.addFirst("e");
//				list.remove(node);
//				list.remove("d");
//			}
//		}
//		System.out.println("===================");

		Iterator<String> iterator = list.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();
			System.out.println(string);
//			iterator.remove();
			if (string.equals("c")) {
				iterator.remove();
			}
		}
		System.out.println("===================");

		list.print();

	}
}
