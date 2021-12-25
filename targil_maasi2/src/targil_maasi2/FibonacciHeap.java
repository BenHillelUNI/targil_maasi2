package targil_maasi2;

/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
	private static int totalLinks = 0;
	private static int totalCuts = 0;
	
	private int numOfTrees = 0;
	private int numOfMarked = 0;
	private int length = 0;
	
	private HeapNode min;
	private HeapNode first;
	
   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty.
    *   
    */
    public boolean isEmpty()
    {
    	return this.min == null;
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * The added key is assumed not to already belong to the heap.  
    * 
    * Returns the newly created node.
    */
    public HeapNode insert(int key)
    {
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
    public void deleteMin()
    {
    	// nothing to delete
    	if (this.isEmpty()) {
    		return;
    	}
    	
    	// decrease length
    	this.length--;
    	//delete the min root and add his children to the heap - O(d), when d = min.degree
    	
    	// remove min root connections to other nodes
    	this.removeMin();
    	
    	// update min
    	this.updateMinProcess();
    	
    	// successive linking - O(log(n))
    	HeapNode[] arr = this.successiveLinking();
    	
    	// rebuild the heap - O(log(n))
    	this.rebuildHeapFromArray(arr);
    }

   /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty.
    *
    */
    public HeapNode findMin()
    {
    	return this.min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Melds heap2 with the current heap.
    *
    */
    public void meld (FibonacciHeap heap2)
    {
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
    public int size()
    {
    	return this.length;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
    * Note: The size of of the array depends on the maximum order of a tree, and an empty heap returns an empty array.
    * 
    */
    public int[] countersRep()
    {
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
    * Deletes the node x from the heap.
	* It is assumed that x indeed belongs to the heap.
    *
    */
    public void delete(HeapNode x) 
    {
    	// decrease key to be min
    	this.decreaseKey(x, x.key-this.min.getKey()+1);
    	
    	// delete it
    	this.deleteMin();
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {
    	// decrease the key
    	x.setKey(x.getKey() - delta);
    	
    	// perform cascading cut if needed
    	if (x.getKey() < x.getParent().getKey()) {
    		this.cascadingCut(x);
    	}
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * 
    * In words: The potential equals to the number of trees in the heap
    * plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {
    	return this.numOfTrees + 2 * this.numOfMarked;
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the
    * run-time of the program. A link operation is the operation which gets as input two
    * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
    * tree which has larger value in its root under the other tree.
    */
    public static int totalLinks()
    {    
    	return totalLinks;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the
    * run-time of the program. A cut operation is the operation which disconnects a subtree
    * from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return totalCuts;
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
    * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
    *  
    * ###CRITICAL### : you are NOT allowed to change H. 
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {    
        int[] arr = new int[k];
        FibonacciHeap minHeap = new FibonacciHeap();
        minHeap.insert(H.min.getKey());
        return arr; // should be replaced by student code
    }
    
    
    //OUR FUNCTIONS =====================================================================================
    
    private HeapNode link(HeapNode x,HeapNode y) {
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
    
    private void connect(HeapNode x,HeapNode y ) {
    	// connect to nodes back and forth
    	x.setNext(y);
    	y.setPrev(x);
    }
    
    private void addTree(HeapNode node) {
    	// insert node to beginning of tree
    	this.insertNode(node);
    	
    	// update fields
    	if(this.min.getKey() > node.getKey()) {
    		this.min = node;
    	}
    	
    	this.numOfTrees++;
    }
    
    private void insertNode(HeapNode node) {
    	// connect node to beginning of tree
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
    	if(x.isMark()) {
    		x.setMark(false);
    		this.numOfMarked--;
    	}
    	
    	// remove child from parent
    	y.setRank(y.getRank()-1);
    	if (x.getNext() == x) {
    		y.setChild(null);
    	}
    	else {
    		y.setChild(x.getNext());
    		x.getPrev().setNext(x.getNext());
    		x.getNext().setPrev(x.getPrev());
    	}
    	
    	// update fields
    	totalCuts++;
    }
    
    private void cascadingCut(HeapNode x) {
    	// cut subtree of node and add it to the heap
    	this.cut(x);
    	this.addTree(x);
    	
    	if (x.getParent().getParent() != null) {
    		// check if parent is marked
    		if (!x.getParent().isMark()) {
    			x.getParent().setMark(true);
    			this.numOfMarked++;
    		}
    		else {
    			// continue cascading cut to parent
    			cascadingCut(x.getParent());
    		}
    	}
    }
    
    private void removeMin() {
    	// remove min connections to nodes
    	HeapNode firstChild = this.min.getChild();
    	HeapNode lastChild = firstChild.getPrev();
    	this.min.getPrev().setNext(firstChild);
    	firstChild.setPrev(this.min.getPrev());
    	this.min.getNext().setPrev(lastChild);
    	lastChild.setNext(this.min.getNext());
    }
    
    private void updateMinProcess() {
    	int currMin = Integer.MAX_VALUE;
    	HeapNode currNode = this.min.getNext();
    	int count = 0;
    	
    	// find smallest key
    	while (count < this.numOfTrees) {
    		if (currMin > currNode.getKey()) {
    			currMin = currNode.getKey();
    		}
    		currNode = currNode.getNext();
    	}
    	
    	// update min
    	this.min = currNode;
    }
    
    private HeapNode[] successiveLinking() {
    	HeapNode[] arr = new HeapNode[this.numOfTrees];
    	HeapNode curr = this.first;
    	
    	// link trees so that there is no two trees at the same rank
    	for (int i = 0; i < numOfTrees; i++) {
    		int rank = curr.getRank();
    		if(arr[rank] == null) {
    			arr[rank] = curr;
    		}
    		else {
    			arr[rank+1] = link(curr, arr[rank]);
    			arr[rank] = null;
    		}
    	}
    	
    	return arr;
    }
    
    private void rebuildHeapFromArray(HeapNode[] arr) {
    	HeapNode curr = this.first;
    	this.numOfTrees = 0;
    	
    	// rebuild heap from array after successive linking
    	for (HeapNode tree : arr) {
    		if (tree != null && tree != this.first) {
    			this.numOfTrees++;
    			this.connect(curr, tree);
    			curr = tree;
    		}
    	}
    	this.connect(curr, this.first);
    }
    
    private int[] createCounterRepsArray() {
    	int[] arr = new int[this.numOfTrees];
    	HeapNode currNode = this.min;
    	int maxOrder = 0;
    	do {
    		if (arr[currNode.getRank()] == 0) {
    			maxOrder++;
    		}
    		arr[currNode.getRank()]++;
    	} while (currNode != this.min);
    	
    	// copy to array in correct size
    	int[] sizedArr = new int[maxOrder];
    	for (int i = 0; i < arr.length; i++) {
    		if (arr[i] != 0) {
        		sizedArr[i] = arr[i];
    		}
    	}
    	
    	return sizedArr;
    }
    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in another file. 
    *  
    */
    public static class HeapNode{

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
			if (this.child == null) {
				this.child = newChild;
			}
			else {
				HeapNode childNext = this.child.getNext();
				this.child.setNext(newChild);
				childNext.setPrev(newChild);
				newChild.setNext(childNext);
				newChild.setPrev(this.child);
			}
			this.rank++;
		}
		
		//getters and setters: =========================================
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
    	//getters and setters: =========================================
    }
}
