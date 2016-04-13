/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.meidusa.venus.util.concurrent;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.meidusa.toolkit.common.util.Tuple;

@SuppressWarnings("unchecked")
public class MultiLinkedBlockingQueue<E extends MultiQueueRunnable> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable, MultipleQueue {
    private static final long serialVersionUID = -6903933977591709194L;

    /**
     * Linked list node class
     */
    static class Node<E> {
        E item;
        /**
         * One of: - the real successor Node - this Node, meaning the successor is head.next - null, meaning there is no
         * successor (this is the last node)
         */

        Node<E> next;

        Node(E x) {
            item = x;
        }
    }

    /** The capacity bound, or Integer.MAX_VALUE if none */
    private final int capacity;

    /** Current number of elements */
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicInteger nodeCount = new AtomicInteger(0);

    /** Head of linked list */
    private transient Node<Tuple<QueueConfig, Queue<E>>> head;

    /** Tail of linked list */
    private transient Node<Tuple<QueueConfig, Queue<E>>> last;

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /** Lock held by put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();

    /** Wait queue for waiting puts */
    private final Condition notFull = putLock.newCondition();

    private MultiQueueManager<E> manager;

    public void setManager(MultiQueueManager<E> manager) {
        this.manager = manager;
    }

    private Map<String, Node<Tuple<QueueConfig, Queue<E>>>> nodeMap = new HashMap<String, Node<Tuple<QueueConfig, Queue<E>>>>();

    /**
     * Signals a waiting take. Called only from put/offer (which do not otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Signals a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * Creates a node and links it at end of queue.
     * 
     * @param x the item
     */
    private void enqueue(E x) {
        Tuple tuple = manager.getQueueTuple(x);
        x.setQueue(this, tuple);
        ((Queue) tuple.right).add(x);
        putTupleIntoQueue(tuple);
    }

    private void putTupleIntoQueue(Tuple<QueueConfig, Queue<E>> tuple) {
        // putToWaitingList(tuple,true);
        String name = ((QueueConfig) tuple.left).getName();
        if (tuple.left.getRunningSize() < tuple.left.getMaxActive() && tuple.right.size() > 0) {
            if (!tuple.left.isInWaiting()) {
                Node<Tuple<QueueConfig, Queue<E>>> node = new Node<Tuple<QueueConfig, Queue<E>>>(tuple);// nodeMap.get(name);
                /*
                 * if(node == null){ synchronized (nodeMap) { node = nodeMap.get(name); if(node == null){ node = new
                 * Node<Tuple<QueueConfig,Queue<E>>>(tuple); nodeMap.put(name, last); } } }
                 */

                last = last.next = node;

                nodeCount.getAndIncrement();
                tuple.left.setInWaiting(true);
                if (!this.takeLock.isHeldByCurrentThread()) {
                    this.signalNotEmpty();
                } else {
                    notEmpty.signal();
                }
            }
        }
    }

    /**
     * Removes a node from head of queue.
     * 
     * @return the node
     */
    private E dequeue() {
        // assert takeLock.isHeldByCurrentThread();
        Tuple<QueueConfig, Queue<E>> item = null;
        E x = null;
        while (nodeCount.get() > 0) {
            Node<Tuple<QueueConfig, Queue<E>>> h = head;
            Node<Tuple<QueueConfig, Queue<E>>> first = h.next;
            head = first;
            item = first.item;
            item.left.setInWaiting(false);
            this.nodeCount.decrementAndGet();
            first.item = null;
            if (item.left.getRunningSize() < item.left.getMaxActive() && item.right.size() > 0) {
                try {
                    x = item.right.poll();
                } catch (NoSuchElementException e) {
                    return null;
                }
                break;
            }
        }
        return x;

    }

    /**
     * Lock to prevent both puts and takes.
     */
    void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlock to allow both puts and takes.
     */
    void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    /**
     * Tells whether both locks are held by current thread.
     */
    boolean isFullyLocked() {
        return (putLock.isHeldByCurrentThread() && takeLock.isHeldByCurrentThread());
    }

    /**
     * Creates a <tt>MultiLinkedBlockingQueue</tt> with a capacity of {@link Integer#MAX_VALUE}.
     */
    public MultiLinkedBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a <tt>MultiLinkedBlockingQueue</tt> with the given (fixed) capacity.
     * 
     * @param capacity the capacity of this queue
     * @throws IllegalArgumentException if <tt>capacity</tt> is not greater than zero
     */
    public MultiLinkedBlockingQueue(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException();
        this.capacity = capacity;
        last = head = new Node<Tuple<QueueConfig, Queue<E>>>(null);
    }

    /**
     * Creates a <tt>MultiLinkedBlockingQueue</tt> with a capacity of {@link Integer#MAX_VALUE}, initially containing
     * the elements of the given collection, added in traversal order of the collection's iterator.
     * 
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public MultiLinkedBlockingQueue(Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        final ReentrantLock putLock = this.putLock;
        putLock.lock(); // Never contended, but necessary for visibility
        try {
            int n = 0;
            for (E e : c) {
                if (e == null)
                    throw new NullPointerException();
                if (n == capacity)
                    throw new IllegalStateException("Queue full");
                enqueue(e);
                ++n;
            }
            count.set(n);
        } finally {
            putLock.unlock();
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE
    /**
     * Returns the number of elements in this queue.
     * 
     * @return the number of elements in this queue
     */
    public int size() {
        return count.get();
    }

    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.
    /**
     * Returns the number of additional elements that this queue can ideally (in the absence of memory or resource
     * constraints) accept without blocking. This is always equal to the initial capacity of this queue less the current
     * <tt>size</tt> of this queue.
     * 
     * <p>
     * Note that you <em>cannot</em> always tell if an attempt to insert an element will succeed by inspecting
     * <tt>remainingCapacity</tt> because it may be the case that another thread is about to insert or remove an
     * element.
     */
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if necessary for space to become available.
     * 
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public void put(E e) throws InterruptedException {
        if (e == null)
            throw new NullPointerException();
        // Note: convention in all put/take/etc is to preset local var
        // holding count negative to indicate failure unless set.
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;

        putLock.lockInterruptibly();
        try {
            /*
             * Note that count is used in wait guard even though it is not protected by lock. This works because count
             * can only decrease at this point (all other puts are shut out by lock), and we (or some other waiting put)
             * are signalled if it ever changes from capacity. Similarly for all other uses of count in other wait
             * guards.
             */
            while (count.get() == capacity) {
                notFull.await();
            }
            enqueue(e);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            putLock.unlock();
        }

        if (c == 0)
            signalNotEmpty();
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if necessary up to the specified wait time for
     * space to become available.
     * 
     * @return <tt>true</tt> if successful, or <tt>false</tt> if the specified waiting time elapses before space is
     *         available.
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {

        if (e == null)
            throw new NullPointerException();
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {

                if (nanos <= 0)
                    return false;
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(e);
            c = count.getAndIncrement();
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue if it is possible to do so immediately without exceeding
     * the queue's capacity, returning <tt>true</tt> upon success and <tt>false</tt> if this queue is full. When using a
     * capacity-restricted queue, this method is generally preferable to method {@link BlockingQueue#add add}, which can
     * fail to insert an element only by throwing an exception.
     * 
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
        if (e == null)
            throw new NullPointerException();
        final AtomicInteger count = this.count;
        if (count.get() == capacity)
            return false;
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                enqueue(e);
                c = count.getAndIncrement();
                if (c + 1 < capacity)
                    notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0)
            signalNotEmpty();
        return c >= 0;
    }

    public E take() throws InterruptedException {
        E x = null;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0 || this.nodeCount.get() == 0 || (x = dequeue()) == null) {
                notEmpty.await();
            }

            x.taked();
            c = count.getAndDecrement();
            if (c > 1 && this.nodeCount.get() > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }

        final Lock lock = this.putLock;
        lock.lock();
        try {
            Tuple tuple = this.manager.getQueueTuple(x);
            this.putTupleIntoQueue(tuple);
        } finally {
            lock.unlock();
        }

        if (c == capacity)
            signalNotFull();
        return x;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0)
                    return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }

    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0)
            return null;
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = dequeue();
                c = count.getAndDecrement();
                if (c > 1)
                    notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity)
            signalNotFull();
        return x;
    }

    public E peek() {
        throw new UnsupportedOperationException();
    }

    /*
     * Unlinks interior Node p with predecessor trail.
     */
    void unlink(Node<E> p, Node<E> trail) {
        // assert isFullyLocked();
        // p.next is not changed, to allow iterators that are
        // traversing p to maintain their weak-consistency guarantee.
        /*
         * p.item = null; trail.next = p.next; if (last == p) last = trail; if (count.getAndDecrement() == capacity)
         * notFull.signal();
         */
    }

    /**
     * Removes a single instance of the specified element from this queue, if it is present. More formally, removes an
     * element <tt>e</tt> such that <tt>o.equals(e)</tt>, if this queue contains one or more such elements. Returns
     * <tt>true</tt> if this queue contained the specified element (or equivalently, if this queue changed as a result
     * of the call).
     * 
     * @param o element to be removed from this queue, if present
     * @return <tt>true</tt> if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        if (o == null)
            return false;
        fullyLock();
        try {
            /*
             * for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) { if (o.equals(p.item)) {
             * unlink(p, trail); return true; } } return false;
             */

            Tuple<QueueConfig, Queue<E>> tuple = manager.getQueueTuple((Named) o);
            return tuple.right.remove(o);

        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in proper sequence.
     * 
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this queue. (In other words, this
     * method must allocate a new array). The caller is thus free to modify the returned array.
     * 
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     * 
     * @return an array containing all of the elements in this queue
     */
    /*
     * public Object[] toArray() { fullyLock(); try { int size = count.get(); Object[] a = new Object[size]; int k = 0;
     * for (Node<E> p = head.next; p != null; p = p.next) a[k++] = p.item; return a; } finally { fullyUnlock(); } }
     */

    /**
     * Returns an array containing all of the elements in this queue, in proper sequence; the runtime type of the
     * returned array is that of the specified array. If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the specified array and the size of this queue.
     * 
     * <p>
     * If this queue fits in the specified array with room to spare (i.e., the array has more elements than this queue),
     * the element in the array immediately following the end of the queue is set to <tt>null</tt>.
     * 
     * <p>
     * Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs.
     * Further, this method allows precise control over the runtime type of the output array, and may, under certain
     * circumstances, be used to save allocation costs.
     * 
     * <p>
     * Suppose <tt>x</tt> is a queue known to contain only strings. The following code can be used to dump the queue
     * into a newly allocated array of <tt>String</tt>:
     * 
     * <pre>
     * String[] y = x.toArray(new String[0]);
     * </pre>
     * 
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to <tt>toArray()</tt>.
     * 
     * @param a the array into which the elements of the queue are to be stored, if it is big enough; otherwise, a new
     *            array of the same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of
     *             every element in this queue
     * @throws NullPointerException if the specified array is null
     */
    // @SuppressWarnings("unchecked")
    /*
     * public <T> T[] toArray(T[] a) { fullyLock(); try { int size = count.get(); if (a.length < size) a =
     * (T[])java.lang.reflect.Array.newInstance (a.getClass().getComponentType(), size); int k = 0; for (Node<E> p =
     * head.next; p != null; p = p.next) a[k++] = (T)p.item; if (a.length > k) a[k] = null; return a; } finally {
     * fullyUnlock(); } }
     */

    public String toString() {
        fullyLock();
        try {
            return super.toString();
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue. The queue will be empty after this call returns.
     */
    /*
     * public void clear() { fullyLock(); try { for (Node<E> p, h = head; (p = h.next) != null; h = p) { h.next = h;
     * p.item = null; } head = last; // assert head.item == null && head.next == null; if (count.getAndSet(0) ==
     * capacity) notFull.signal(); } finally { fullyUnlock(); } }
     */

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        int i = 0;
        int j = maxElements;
        boolean signalNotFull = false;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            for (Iterator<Tuple<QueueConfig, Queue<E>>> it = manager.getAll().iterator(); it.hasNext();) {
                Tuple<QueueConfig, Queue<E>> tuple = it.next();
                int count = tuple.right.size();
                if (tuple.right.removeAll(c)) {
                    i += count;
                    j -= count;
                    if (j <= 0)
                        break;
                }
            }
            return i;
        } finally {
            takeLock.unlock();
            if (signalNotFull)
                signalNotFull();
        }
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence. The returned <tt>Iterator</tt> is a
     * "weakly consistent" iterator that will never throw {@link ConcurrentModificationException}, and guarantees to
     * traverse elements as they existed upon construction of the iterator, and may (but is not guaranteed to) reflect
     * any modifications subsequent to construction.
     * 
     * @return an iterator over the elements in this queue in proper sequence
     */
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    public void finished(Tuple tuple, long start, long finished) {
        final Lock lock = this.putLock;
        lock.lock();

        try {
            // decrease running size
            ((QueueConfig) tuple.left).decrementAndGet();

            // Execution time statistics
            ((QueueConfig) tuple.left).addExecutTime(finished - start, finished);

            // increase waiting queue size
            this.putTupleIntoQueue(tuple);
        } finally {
            lock.unlock();
        }

    }

    public static void main(String[] args) throws Exception {

        class DefaultQueueConfigManager extends DefaultMultiQueueManager {
            final List list = new ArrayList<Tuple<QueueConfig, Queue>>();
            int maxThread;
            private MultiBlockingQueueExecutor executor = null;

            public DefaultQueueConfigManager(int maxThread) {
                this.maxThread = maxThread;
            }

            public Tuple<QueueConfig, Queue> newTuple(Named named) {
                Tuple<QueueConfig, Queue> tuple = super.newTuple(named);
                list.add(tuple);
                adjustMaxActive(tuple);
                return tuple;
            }

            public Queue createQueue(QueueConfig config) {
                return new LinkedBlockingQueue(config.getMaxQueue());
            }

            public QueueConfig getConfig(Named named) {
                QueueConfig config = new QueueConfig();
                config.setMaxQueue(1000000);
                config.setName(named.getName());
                return config;
            }

            public int getIdleSize() {
                if (executor == null) {
                    return 0;
                }
                return maxThread - executor.getRunningSize();
            }

            private void adjustMaxActive(Tuple<QueueConfig, Queue> tuple) {
                int maxActive = 0;
                if (tuple.left.getAverageLatencyTime() <= 10) {
                    maxActive = (int) (0.9 * maxThread);
                } else if (tuple.left.getAverageLatencyTime() < 100) {
                    maxActive = (int) (0.8 * maxThread);
                } else if (tuple.left.getAverageLatencyTime() <= 1000) {
                    maxActive = (int) (0.5 * maxThread) + getIdleSize();
                } else if (tuple.left.getAverageLatencyTime() <= 5000) {
                    maxActive = (int) (0.2 * maxThread) + getIdleSize();
                } else if (tuple.left.getAverageLatencyTime() <= 10000) {
                    maxActive = (int) ((0.1 * maxThread) + (0.7 * getIdleSize()));
                } else {
                    maxActive = (int) ((0.05 * maxThread) + (0.5 * getIdleSize()));
                }

                if (tuple.left.getMaxActive() > 0 && this.getIdleSize() <= 0.05 * maxThread && tuple.left.getRunningSize() >= 0.9 * tuple.left.getMaxActive()) {
                    maxActive = (int) (maxActive - 0.1 * maxThread);
                }

                if (maxActive == 0) {
                    maxActive = 1;
                } else if (maxActive >= maxThread) {
                    maxActive = (int) (0.9 * maxThread);
                    if (maxActive == 0) {
                        maxActive = 1;
                    }
                }

                tuple.left.setMaxActive(maxActive);
            }

            public void init() {
                new Thread() {
                    {
                        this.setDaemon(true);
                        this.setName("endPoint-Thread-adjust--" + Thread.currentThread().getName());
                    }

                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(5 * 1000L);
                            } catch (InterruptedException e) {
                            }
                            if (list.size() < 1)
                                continue;
                            List<Tuple<QueueConfig, Queue>> temp = new ArrayList<Tuple<QueueConfig, Queue>>();
                            temp.addAll(list);
                            System.out.println("-----" + Thread.currentThread().getName() + ",total=" + maxThread + ",idle=" + getIdleSize() + "----------");
                            int totalRunning = 0;
                            PriorityQueue<QueueConfig> queue = new PriorityQueue<QueueConfig>(temp.size(), new Comparator<QueueConfig>() {
                                public int compare(QueueConfig o1, QueueConfig o2) {
                                    return (int) (o2.getAverageLatencyTime() - o1.getAverageLatencyTime());
                                }

                            });
                            for (Iterator<Tuple<QueueConfig, Queue>> it = temp.iterator(); it.hasNext();) {
                                Tuple<QueueConfig, Queue> tuple = it.next();
                                System.out.println("name=" + tuple.left.getName() + ", running=" + tuple.left.getRunningSize() + ", maxThread="
                                        + tuple.left.getMaxActive() + ", latency=" + tuple.left.getAverageLatencyTime() + ", size=" + tuple.right.size()
                                        + ",inQueue=" + tuple.left.isInWaiting());
                                // adjustMaxActive(tuple);

                                if (tuple.left.getRunningSize() > 0 && tuple.left.getAverageLatencyTime() > 0) {
                                    queue.add(tuple.left);
                                }
                            }

                            /*
                             * if(getIdleSize() < 0.1 * DefaultQueueConfigManager.this.maxThread){ QueueConfig config =
                             * null; int targetIdleSize = (int)(0.1 * DefaultQueueConfigManager.this.maxThread);
                             * if(targetIdleSize == 0){ targetIdleSize = 1; } while(queue.size()>0 && (config =
                             * queue.remove()) != null && targetIdleSize > 0){ if(config.getRunningSize() >
                             * targetIdleSize){ int thisDown = (int)(targetIdleSize/(queue.size()+1));
                             * config.setMaxActive(config.getRunningSize() - thisDown); targetIdleSize = targetIdleSize
                             * - thisDown; } } }
                             */

                        }
                    }

                }.start();
            }
        }

        final long start = System.currentTimeMillis();
        final AtomicLong producerCounter = new AtomicLong();
        final AtomicLong consumerCounter = new AtomicLong();
        final Tuple<Long, Long> timer = new Tuple<Long, Long>(System.currentTimeMillis(), System.currentTimeMillis());
        final MultiLinkedBlockingQueue wrapper = new MultiLinkedBlockingQueue();
        final int maxActive = 10;
        DefaultQueueConfigManager manager = new DefaultQueueConfigManager(maxActive);
        wrapper.setManager(manager);
        manager.init();
        for (int i = 0; i < 10; i++) {
            final int j = i;
            new Thread() {
                {
                    this.setName("producer-" + j);
                }

                public void run() {
                    for (int count = 0; count < 100000; count++) {
                        try {
                            wrapper.put(new MultiQueueRunnable() {

                                @Override
                                public void doRun() {
                                    /*
                                     * if(j == 5 || j == 6 || j == 7|| j == 8){ try { Thread.sleep(1000l); } catch
                                     * (InterruptedException e) { e.printStackTrace(); } }
                                     */
                                }

                                public String getName() {
                                    return "abcde" + j;
                                }

                            });
                            long in = producerCounter.incrementAndGet();

                            if (in % 100000 == 0) {
                                System.out.println("in<--,count=" + in + ",total=" + (System.currentTimeMillis() - timer.left) + ",size=" + wrapper.size()
                                        + ",consumerCounter=" + consumerCounter.get());
                                timer.left = System.currentTimeMillis();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

        }

        for (int i = 0; i < 10; i++) {
            final int j = i;
            new Thread() {
                {
                    this.setName("consumer-" + j);
                }

                public void run() {
                    // long start = System.currentTimeMillis();
                    while (true) {
                        MultiQueueRunnable entry;
                        try {
                            entry = wrapper.take();
                            long count = consumerCounter.incrementAndGet();
                            // System.out.println(entry.getName());
                            entry.run();
                            if (count % 100000 == 0) {
                                System.out
                                        .println("out-->,count=" + count + ",total=" + (System.currentTimeMillis() - timer.right) + ",size=" + wrapper.size());
                                timer.right = System.currentTimeMillis();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        System.out.println(System.currentTimeMillis() - start);

    }

}
