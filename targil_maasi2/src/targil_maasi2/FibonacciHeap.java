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
	private int numOfTrees,numOfMarked;
	private HeapNode min;
	private HeapNode first;
	private int length = 0;
	
   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty.
    *   
    */
    public boolean isEmpty()
    {
    	return this.min == null; // should be replaced by student code
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
    	this.numOfTrees++;
    	this.length++;
    	HeapNode newNode = new HeapNode(key);
    	HeapNode last = this.first.getPrev();
    	newNode.setNext(this.first);
    	this.first.setPrev(newNode);
    	newNode.setPrev(last);
    	last.setNext(newNode);
    	this.first = newNode;
    	//update min if needed
    	if(this.min.getKey() > newNode.getKey()) {
    		this.min = newNode;
    	}
    	return newNode; // should be replaced by student code
    }

   /**
    * public void deleteMin()
    *
    * Deletes the node containing the minimum key.
    *
    */
    public void deleteMin()
    {
    	if(this.isEmpty()) {
    		return;
    	}
    	//decrease length
    	this.length--;
    	//delete the min root and add his children to the heap - O(d), when d = min.degree
    	
    	HeapNode firstChild = this.min.getChild();
    	HeapNode lastChild = firstChild.getPrev();
    	this.min.getPrev().setNext(firstChild);
    	firstChild.setPrev(this.min.getPrev());
    	this.min.getNext().setPrev(lastChild);
    	lastChild.setNext(this.min.getNext());
    	
    	//update min
    	int currMin = Integer.MAX_VALUE;
    	HeapNode currNode = this.min.getNext();
    	int numOfTrees = 0;
    	while(currNode.getKey() != currMin) {
    		if(currMin > currNode.getKey()) {
    			currMin = currNode.getKey();
    			numOfTrees = 0;
    		}
    		currNode = currNode.getNext();
    		numOfTrees++;
    		
    	}
    	this.min = currNode;
    	//successive linking - O(log(n))
    	HeapNode[] arr = new HeapNode[(int)Math.log(this.length)+1];
    	HeapNode curr = this.min;
    	for(int i=0;i<numOfTrees;i++) {
    		int rank = curr.getRank();
    		if(arr[rank] == null) {
    			arr[rank] = curr;
    		}else {
    			arr[rank+1] = link(curr, arr[rank]);
    			arr[rank] = null;
    		}
    	}
    	
    	//rebuild the heap - O(log(n))
    	HeapNode aviv = this.min;
    	this.numOfTrees = 0;
    	for(HeapNode ben : arr) {
    		if(ben != null && ben != this.min) {
    			this.numOfTrees++;
    			connect(aviv,ben);
    			aviv = ben;
    		}
    	}
    	connect(aviv,this.min); 	
    }

   /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty.
    *
    */
    public HeapNode findMin()
    {
    	return min;// should be replaced by student code
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
    	
    	ourLast.setNext(theirFirst);
    	theirFirst.setPrev(ourLast);
    	this.first.setPrev(theirLast);
    	theirLast.setNext(this.first);
    	
    	if(heap2.min.getKey() < this.min.getKey()) {
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
    	return this.length; // should be replaced by student code
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
    	int[] arr = new int[(int)Math.log(length)+1];
    	HeapNode currNode = this.min;
    	do {
    		arr[currNode.getRank()]++;
    	}while(currNode != this.min);
        return arr; //	 to be replaced by student code
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
    	this.decreaseKey(x, x.key-this.min.getKey()+1);
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
    	x.setKey(x.getKey()-delta);
    	if(x.getKey()<x.getParent().getKey()) {
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
    	return this.numOfTrees +2*this.numOfMarked; // should be replaced by student code
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
    	return totalLinks; // should be replaced by student code
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
    	return totalCuts; // should be replaced by student code
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
    	if(x.getKey() < y.getKey()) {
    		HeapNode z = y;
    		y = x;
    		x = z;
    	}
    	y.addChild(x);
    	x.setParent(y);
    	
    	totalLinks++;
    	return y;
    }
    
    private void connect(HeapNode x,HeapNode y ) {
    	x.setNext(y);
    	y.setPrev(x);
    }
    
    private void heapInsert(HeapNode node) {
    	this.numOfTrees++;
    	HeapNode newNode = node;
    	HeapNode last = this.first.getPrev();
    	newNode.setNext(this.first);
    	this.first.setPrev(newNode);
    	newNode.setPrev(last);
    	last.setNext(newNode);
    	this.first = newNode;
    	//update min if needed
    	if(this.min.getKey() > newNode.getKey()) {
    		this.min = newNode;
    	}
    }
    
    private void cut(HeapNode x) {
    	totalCuts++;
    	HeapNode y = x.getParent();
    	x.setParent(null);
    	if(x.isMark()) {
    		this.numOfMarked--;
    		x.setMark(false);
    	}
    	y.setRank(y.getRank()-1);
    	if(x.getNext() == x) {
    		y.setChild(null);
    	}else {
    		y.setChild(x.getNext());
    		x.getPrev().setNext(x.getNext());
    		x.getNext().setPrev(x.getPrev());
    	}
    }
    
    private void cascadingCut(HeapNode x) {
    	this.cut(x);
    	this.heapInsert(x);
    	if(x.getParent().getParent() != null) {
    		if(!x.getParent().isMark()) {
    			x.getParent().setMark(true);
    			this.numOfMarked++;
    		}else {
    			cascadingCut(x.getParent());
    		}
    	}
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
			if(this.child == null) {
				this.child = newChild;
			}else {
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
