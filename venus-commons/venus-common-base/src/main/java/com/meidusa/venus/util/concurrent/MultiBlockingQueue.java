package com.meidusa.venus.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.meidusa.toolkit.common.util.Tuple;

/**
 * 
 * @author Struct
 * 
 * @param <E>
 */
@SuppressWarnings("unchecked")
public class MultiBlockingQueue<E extends MultiQueueRunnable> extends AbstractQueue<E> implements BlockingQueue<E>, MultipleQueue {

    final Queue<Tuple<QueueConfig, BlockingQueue<E>>> waitingList = new LinkedList<Tuple<QueueConfig, BlockingQueue<E>>>();
    private MultiQueueManager<E> manager;

    /** Main lock guarding all access */
    final ReentrantLock lock;
    /** Condition for waiting takes */
    final Condition notEmpty;
    /** Condition for waiting puts */
    // private final Condition notFull;

    private int size = 0;
    private int capacity;

    public MultiBlockingQueue(MultiQueueManager<E> manager) {
        this(manager, Integer.MAX_VALUE);
    }

    public MultiBlockingQueue(MultiQueueManager<E> manager, int capacity) {
        lock = new ReentrantLock(false);
        notEmpty = lock.newCondition();
        this.capacity = capacity;
        this.manager = manager;
        // notFull = lock.newCondition();
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public int getWaitingQueueSize() {
        return this.waitingList.size();
    }

    public int drainTo(Collection<? super E> c) {
        int i = 0;
        for (Iterator<Tuple<QueueConfig, Queue<E>>> it = manager.getAll().iterator(); it.hasNext();) {
            Tuple<QueueConfig, Queue<E>> tuple = it.next();
            int count = tuple.right.size();
            if (tuple.right.removeAll(c)) {
                i += count;
            }
        }
        return i;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        int i = 0;
        int j = maxElements;
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
    }

    protected Tuple<QueueConfig, BlockingQueue<E>> takeQueue() throws InterruptedException {
        while (true) {
            try {
                /*
                 * if(size <=0){ notEmpty.await(); }
                 */
                Tuple<QueueConfig, BlockingQueue<E>> entry = waitingList.remove();
                if (entry == null) {
                    notEmpty.await();
                } else {
                    entry.getLeft().setInWaiting(false);
                    if (entry.right.size() > 0 && entry.left.getRunningSize() < entry.left.getMaxActive()) {
                        return entry;
                    }
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to non-interrupted thread
                throw ie;
            } catch (java.util.NoSuchElementException e) {
                try {
                    notEmpty.await();
                } catch (InterruptedException ie) {
                    notEmpty.signal();
                    throw ie;
                }
            }
        }
    }

    protected Tuple<QueueConfig, BlockingQueue<E>> pollQueue() {
        while (true) {
            try {
                Tuple<QueueConfig, BlockingQueue<E>> entry = waitingList.remove();
                if (entry == null) {
                    return null;
                } else {
                    entry.getLeft().setInWaiting(false);
                    if (entry.right.size() > 0 && entry.left.getRunningSize() < entry.left.getMaxActive()) {
                        return entry;
                    }
                }
            } catch (java.util.NoSuchElementException e) {
                return null;
            }
        }
    }

    protected Tuple<QueueConfig, BlockingQueue<E>> peakQueue() {
        while (true) {
            try {
                Tuple<QueueConfig, BlockingQueue<E>> entry = waitingList.peek();
                if (entry == null) {
                    return null;
                } else {
                    if (entry.right.size() > 0 && entry.left.getRunningSize() < entry.left.getMaxActive()) {
                        return entry;
                    }
                }
            } catch (java.util.NoSuchElementException e) {
                return null;
            }
        }
    }

    protected Tuple<QueueConfig, BlockingQueue<E>> pollQueue(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        while (true) {
            try {
                if (size <= 0) {
                    nanos = notEmpty.awaitNanos(nanos);
                }
                Tuple<QueueConfig, BlockingQueue<E>> entry = waitingList.remove();
                if (entry == null) {
                    if (nanos <= 0)
                        return null;
                    nanos = notEmpty.awaitNanos(nanos);
                } else {
                    entry.getLeft().setInWaiting(false);
                    if (entry.right.size() > 0 && entry.left.getRunningSize() < entry.left.getMaxActive()) {
                        return entry;
                    }
                }
            } catch (InterruptedException ie) {
                notEmpty.signal(); // propagate to non-interrupted thread
                throw ie;
            } catch (java.util.NoSuchElementException e) {
                try {
                    if (nanos <= 0)
                        return null;
                    nanos = notEmpty.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    notEmpty.signal(); // propagate to non-interrupted thread
                    return null;
                }
            }
        }
    }

    public boolean offer(E e) {
        Tuple tuple = manager.getQueueTuple(e);
        e.setQueue(this, tuple);
        boolean success = ((BlockingQueue) tuple.right).offer(e);
        if (success) {
            lock.lock();
            try {
                ++size;
                putToWaitingList(tuple, true);
            } finally {
                lock.unlock();
            }
        }
        return success;
    }

    private void putToWaitingList(Tuple<QueueConfig, BlockingQueue<E>> tuple, boolean needSignal) {
        if (tuple.left.getRunningSize() < tuple.left.getMaxActive() && tuple.right.size() > 0) {
            if (!tuple.left.isInWaiting()) {
                if (waitingList.add(tuple)) {
                    tuple.left.setInWaiting(true);
                    notEmpty.signal();
                }
            }
        }
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Tuple tuple = manager.getQueueTuple(e);
        e.setQueue(this, tuple);
        boolean success = ((BlockingQueue) tuple.right).offer(e, timeout, unit);
        if (success) {
            lock.lock();
            try {
                ++size;
                putToWaitingList(tuple, true);
            } finally {
                lock.unlock();
            }
        }
        return success;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            Tuple<QueueConfig, BlockingQueue<E>> tuple = null;
            tuple = pollQueue(timeout, unit);

            if (tuple == null) {
                return null;
            }

            E e = tuple.right.poll(timeout, unit);

            if (e != null) {
                --size;
                tuple.left.incrementAndGet();
            }

            putToWaitingList(tuple, false);
            return e;
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) throws InterruptedException {
        Tuple tuple = manager.getQueueTuple(e);
       
         e.setQueue(this, tuple);
        
        ((BlockingQueue) tuple.right).put(e);

        lock.lock();
        try {
            ++size;
            putToWaitingList(tuple, true);
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return capacity - this.size();
    }

    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        Tuple<QueueConfig, BlockingQueue<E>> tuple = null;
        try {
            tuple = takeQueue();

            E e = tuple.right.take();

            if (e != null) {
                --size;
                tuple.left.incrementAndGet();
            }

            putToWaitingList(tuple, false);
            return e;
        } finally {
            lock.unlock();
        }
    }

    public E peek() {
        throw new UnsupportedOperationException();
    }

    public E poll() {
        lock.lock();
        Tuple<QueueConfig, BlockingQueue<E>> tuple = null;
        try {
            tuple = pollQueue();

            if (tuple == null) {
                return null;
            }

            E e = tuple.right.poll();

            if (e != null) {
                --size;
                tuple.left.incrementAndGet();
            }

            putToWaitingList(tuple, false);

            return e;
        } finally {
            lock.unlock();
        }
    }

    public void finished(Tuple tuple, long start, long finished) {
        final Lock lock = this.lock;
        lock.lock();
        try {
            ((QueueConfig) tuple.left).decrementAndGet();
            ((QueueConfig) tuple.left).addExecutTime(finished - start, finished);
            this.putToWaitingList(tuple, true);

            /*
             * if(running == tuple.left.getMaxActive() && tuple.right.size()>0){ queue.add(tuple);
             * tuple.left.setInWaiting(true); notEmpty.signal(); }
             */
        } finally {
            lock.unlock();
        }
    }

}
