package targil_maasi2;

/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap {
	private static int totalLinks = 0;
	private static int totalCuts = 0;

	private int numOfTrees = 0;
	private int numOfMarked = 0;
	private int length = 0;

	private HeapNode min;
	private HeapNode first;

	public HeapNode getFirst() {
		return this.first;
	}

	/**
	 * public boolean isEmpty()
	 *
	 * Returns true if and only if the heap is empty.
	 * 
	 */
	public boolean isEmpty() {
		return this.min == null;
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts
	 * it into the heap. The added key is assumed not to already belong to the heap.
	 * 
	 * Returns the newly created node.
	 */
	public HeapNode insert(int key) {
		// initialize new node
		HeapNode newNode = new HeapNode(key);
		
		// add node as new tree
		this.addTree(newNode);

		// update fields
		this.length++;

		return newNode;
	}

	/**
	 * public void deleteMin()
	 *
	 * Deletes the node containing the minimum key.
	 *
	 */
	public void deleteMin() {
		// nothing to delete
		if (this.isEmpty()) {
			return;
		}

		// remove min root connections to other nodes
		this.removeMin();

		// decrease length
		this.length--;
		if (this.isEmpty()) {
			return;
		}

		// update min
		this.updateMinProcess();

		// successive linking - O(log(n))
		HeapNode[] arr = this.successiveLinking();

		// rebuild heap
		this.rebuildHeapFromArray(arr);
	}

	/**
	 * public HeapNode findMin()
	 *
	 * Returns the node of the heap whose key is minimal, or null if the heap is
	 * empty.
	 *
	 */
	public HeapNode findMin() {
		return this.min;
	}

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Melds heap2 with the current heap.
	 *
	 */
	public void meld(FibonacciHeap heap2) {
		HeapNode ourLast = this.first.getPrev();
		HeapNode theirFirst = heap2.first;
		HeapNode theirLast = theirFirst.getPrev();

		// connect heap at the end
		ourLast.setNext(theirFirst);
		theirFirst.setPrev(ourLast);
		this.first.setPrev(theirLast);
		theirLast.setNext(this.first);

		// update fields
		if (heap2.min.getKey() < this.min.getKey()) {
			this.min = heap2.min;
		}

		this.length += heap2.length;
		this.numOfTrees += heap2.numOfTrees;
		this.numOfMarked += heap2.numOfMarked;
	}

	/**
	 * public int size()
	 *
	 * Returns the number of elements in the heap.
	 * 
	 */
	public int size() {
		return this.length;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return an array of counters. The i-th entry contains the number of trees of
	 * order i in the heap. Note: The size of of the array depends on the maximum
	 * order of a tree, and an empty heap returns an empty array.
	 * 
	 */
	public int[] countersRep() {
		// return empty array if heap is empty
		if (this.isEmpty()) {
			return new int[0];
		}

		// create array of counts
		return this.createCounterRepsArray();
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap. It is assumed that x indeed belongs to the
	 * heap.
	 *
	 */
	public void delete(HeapNode x) {
		HeapNode currMin = this.min;
		
		// decrease key to be min
		this.decreaseKey(x, x.key - this.min.getKey() + 2);
		
		// delete it
		this.deleteMin();
		
		if (x != currMin) {
			this.min = currMin;
		}
	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * Decreases the key of the node x by a non-negative value delta. The structure
	 * of the heap should be updated to reflect this change (for example, the
	 * cascading cuts procedure should be applied if needed).
	 */
	public void decreaseKey(HeapNode x, int delta) {
		// decrease the key
		x.setKey(x.getKey() - delta);
		
		// perform cascading cut if needed
		if (x.getParent() != null) {
			if (x.getKey() < x.getParent().getKey()) {
				this.cascadingCut(x);
			}
		}
		
		// update min
		if (x.getKey() < this.min.getKey()) {
			this.min = x;
		}
	}

	/**
	 * public int potential()
	 *
	 * This function returns the current potential of the heap, which is: Potential
	 * = #trees + 2*#marked
	 * 
	 * In words: The potential equals to the number of trees in the heap plus twice
	 * the number of marked nodes in the heap.
	 */
	public int potential() {
		return this.numOfTrees + 2 * this.numOfMarked;
	}

	/**
	 * public static int totalLinks()
	 *
	 * This static function returns the total number of link operations made during
	 * the run-time of the program. A link operation is the operation which gets as
	 * input two trees of the same rank, and generates a tree of rank bigger by one,
	 * by hanging the tree which has larger value in its root under the other tree.
	 */
	public static int totalLinks() {
		return totalLinks;
	}

	/**
	 * public static int totalCuts()
	 *
	 * This static function returns the total number of cut operations made during
	 * the run-time of the program. A cut operation is the operation which
	 * disconnects a subtree from its parent (during decreaseKey/delete methods).
	 */
	public static int totalCuts() {
		return totalCuts;
	}

	/**
	 * public static int[] kMin(FibonacciHeap H, int k)
	 *
	 * This static function returns the k smallest elements in a Fibonacci heap that
	 * contains a single tree. The function should run in O(k*deg(H)). (deg(H) is
	 * the degree of the only tree in H.)
	 * 
	 * ###CRITICAL### : you are NOT allowed to change H.
	 */
	public static int[] kMin(FibonacciHeap H, int k) {
		int[] arr = new int[k];
		FibonacciHeap minHeap = new FibonacciHeap();
		minHeap.insertCopy(H.min);
		// save the current parent pointer
		HeapNodeCopy parent = new HeapNodeCopy(H.min);
		for (int i = 0; i < k; i++) {
			// add all of the children of the smallest node in minHeap
			HeapNode child = parent.getOriginal().getChild();
			for (int j = 0; j < parent.getOriginal().getRank(); j++) {
				minHeap.insertCopy(child);
				child = child.getNext();
			}
			arr[i] = parent.getKey();
			minHeap.deleteMin();
			parent = (HeapNodeCopy) minHeap.findMin();
		}
		return arr; // should be replaced by student code
	}

	// OUR FUNCTIONS
	// =====================================================================================

	private HeapNodeCopy insertCopy(HeapNode node) {
		// initialize new node
		HeapNodeCopy newNode = new HeapNodeCopy(node);

		// add node as new tree
		this.addTree(newNode);

		// update fields
		this.length++;

		return newNode;
	}

	private HeapNode link(HeapNode x, HeapNode y) {
		// determine larger key
		if (x.getKey() < y.getKey()) {
			HeapNode z = y;
			y = x;
			x = z;
		}

		// link the nodes
		y.addChild(x);
		x.setParent(y);

		// update fields
		totalLinks++;

		return y;
	}

	private void connect(HeapNode x, HeapNode y) {
		// connect to nodes back and forth
		x.setNext(y);
		y.setPrev(x);
	}

	private void addTree(HeapNode node) {
		// insert node to beginning of tree
		this.insertNode(node);

		// update fields
		if (this.min.getKey() > node.getKey()) {
			this.min = node;
		}

		this.numOfTrees++;
	}

	private void insertNode(HeapNode node) {
		// connect node to beginning of tree

		if (this.isEmpty() || this.numOfTrees == 0) {
			this.first = node;
			this.min = node;
			this.first.setNext(node);
			this.first.setPrev(node);
			return;
		}

		HeapNode newNode = node;
		HeapNode last = this.first.getPrev();
		newNode.setNext(this.first);
		this.first.setPrev(newNode);
		newNode.setPrev(last);
		last.setNext(newNode);
		this.first = newNode;
	}

	private void cut(HeapNode x) {
		HeapNode y = x.getParent();
		x.setParent(null);

		// unmark node if marked
		if (x.isMark()) {
			x.setMark(false);
			this.numOfMarked--;
		}

		// remove child from parent
		y.setRank(y.getRank() - 1);
		if (x.getNext() == x) {
			y.setChild(null);
		} else {
			y.setChild(x.getNext());
			x.getPrev().setNext(x.getNext());
			x.getNext().setPrev(x.getPrev());
		}

		// update fields
		totalCuts++;
	}

	private void cascadingCut(HeapNode x) {
		// cut subtree of node and add it to the heap
		HeapNode parent = x.getParent();
		
		this.cut(x);
		this.addTree(x);

		if (parent != null && parent.getParent() != null) {
			// check if parent is marked
			if (!parent.isMark()) {
				parent.setMark(true);
				this.numOfMarked++;
			} else {
				// continue cascading cut to parent
				cascadingCut(parent);
			}
		}
	}

	private void removeMin() {
		HeapNode minNode = this.min;
		
		// min is the only node
		if (this.length == 1) {
			this.first = null;
			this.min = null;
			this.numOfTrees = 0;
			return;
		}
		
		// min doesn't have a child
		if (minNode.getChild() == null) {
			this.numOfTrees--;
			minNode.getPrev().setNext(minNode.getNext());
			minNode.getNext().setPrev(minNode.getPrev());
			if (this.first == minNode) {
				this.first = minNode.getNext();
			}
			return;
		}
		

		HeapNode child = minNode.getChild();
		
		// min has one child
		if (minNode.getRank() == 1) {
			if (this.first == minNode) {
				this.first = child;
			}
			child.setNext(minNode.getNext());
			child.setPrev(minNode.getPrev());
			minNode.getNext().setPrev(child);
			minNode.getPrev().setNext(child);
			child.setParent(null);
			return;
		}
		
		this.numOfTrees--;
		// min has multiple children
		
		if (this.first == minNode) {
			this.first = child;
		}
		
		if (minNode.getNext() != minNode) {
			HeapNode lastChild = child.getPrev();
			child.setPrev(minNode.getPrev());
			minNode.getPrev().setNext(child);
			lastChild.setNext(minNode.getNext());
			minNode.getNext().setPrev(lastChild);
		}
		
		int children = minNode.getRank();
		for (int i = 0; i < children; i++) {
			child.setParent(null);
			child = child.getNext();
			this.numOfTrees++;
		}
	}

	private void updateMinProcess() {
		int currMin = Integer.MAX_VALUE;
		HeapNode currNode = this.first;
		int count = 0;

		// find smallest key
		while (count <= this.numOfTrees) {
			if (currMin > currNode.getKey()) {
				currMin = currNode.getKey();
				this.min = currNode;
			}
			currNode = currNode.getNext();
			count++;
		}
	}

	private HeapNode[] successiveLinking() {
		HeapNode[] arr = new HeapNode[(int) (Math.log(this.length) / Math.log(2)) + 1];
		HeapNode curr = this.first;

		// link trees so that there is no two trees at the same rank
		for (int i = 0; i < this.numOfTrees; i++) {
			HeapNode next = curr.getNext();
			curr.setNext(curr);
			curr.setPrev(curr);
			while (arr[curr.getRank()] != null) {
				curr = link(curr, arr[curr.getRank()]);
				arr[curr.getRank() - 1] = null;
			}
			arr[curr.getRank()] = curr;
			curr = next;
		}

		return arr;
	}

	private void rebuildHeapFromArray(HeapNode[] arr) {
		HeapNode curr = null;
		this.numOfTrees = 0;

		// rebuild heap from array after successive linking
		for (HeapNode tree : arr) {
			if (tree != null) {
				if (curr == null) {
					curr = tree;
					curr.setNext(curr);
					curr.setPrev(curr);
					this.first = curr;
				} else {
					this.connect(curr, tree);
					curr = tree;
				}
				this.numOfTrees++;
			}
		}
		if (curr != null) {
			connect(curr, this.first);
		}

	}

	private int[] createCounterRepsArray() {
		int[] arr = new int[(int)(Math.log(this.length)/Math.log(2))+1];
		HeapNode currNode = this.min;
		int maxOrder = 0;
		do {
			if (arr[currNode.getRank()] == 0) {
				maxOrder++;
			}
			arr[currNode.getRank()]++;
			currNode = currNode.getNext();
		} while (currNode != this.min);

		return arr;
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap (for example
	 * HeapNode), do it in this file, not in another file.
	 * 
	 */
	public static class HeapNode {

		private int key;
		private int rank;
		private boolean mark;
		private HeapNode child;
		private HeapNode next;
		private HeapNode prev;
		private HeapNode parent;

		public HeapNode(int key) {
			this.key = key;
			this.next = this;
			this.prev = this;
			this.mark = false;
		}

		public void addChild(HeapNode newChild) {
			HeapNode child = this.getChild();
			if (child == null) {
				this.setChild(newChild);
			} else {
				this.child = newChild;
				newChild.setNext(child);
				newChild.setPrev(child.getPrev());
				child.setPrev(newChild);
				newChild.getPrev().setNext(newChild);
			}
			this.rank++;
		}

		// getters and setters: =========================================
		public int getRank() {
			return rank;
		}

		public void setRank(int rank) {
			this.rank = rank;
		}

		public boolean isMark() {
			return mark;
		}

		public void setMark(boolean mark) {
			this.mark = mark;
		}

		public HeapNode getChild() {
			return child;
		}

		public void setChild(HeapNode child) {
			this.child = child;
		}

		public HeapNode getNext() {
			return next;
		}

		public void setNext(HeapNode next) {
			this.next = next;
		}

		public HeapNode getPrev() {
			return prev;
		}

		public void setPrev(HeapNode prev) {
			this.prev = prev;
		}

		public HeapNode getParent() {
			return parent;
		}

		public void setParent(HeapNode parent) {
			this.parent = parent;
		}

		public void setKey(int key) {
			this.key = key;
		}

		public int getKey() {
			return this.key;
		}
		// getters and setters: =========================================
	}

	private static class HeapNodeCopy extends HeapNode {

		private HeapNode original;

		public HeapNodeCopy(HeapNode original) {
			super(original.getKey());
			this.original = original;
			// TODO Auto-generated constructor stub
		}

		public HeapNode getOriginal() {
			return original;
		}
	}
}
