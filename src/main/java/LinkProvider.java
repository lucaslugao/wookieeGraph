/**
 * Created by Paulo on 2/24/2017.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.locks.*;

/**
 * LinkProvider is the class that consists of useful data
 * structures and methods to the implementation of a parallel
 * BFS. It contains two thread safe stacks and methods to
 * perform the synchronization of multiple threads working in
 * the task of performing a search of a graph in breadth.
 */
public class LinkProvider {

    /**
     * Thread safe stack of Strings.
     */
    private ArrayList<String> origin;
    /**
     * Thread safe stack of Strings.
     */
    private ArrayList<String> destiny;

    /**
     * Counts the number of threads waiting for a signal in
     * the condition notEmpty.
     */
    private int waitingWorkers;
    /**
     * Number of threads accessing an object of this class.
     */
    private final int nThreads;

    /**
     * Lock that protect the stack origin. This lock is associated
     * to the two condition.
     */
    private final Lock originLock;
    /**
     * Lock that protect the stack destiny.
     */
    private final Lock destinyLock;

    /**
     * Condition associated to the lock originLock. All the threads
     * waiting for a signal in this condition tried to get a String
     * from origin and found it empty.
     */
    private final Condition notEmpty; // The origin is empty, the threads must wait until the manager swaps
    /**
     * Condition associated to the lock originLock. At most one thread
     * is waiting for a signal in this condition.
     */
    private final Condition stepOver; // The step is not over yet, the manager must wait

    /**
     * Set of all the Strings once added to any of the stacks.
     */
    private final HashSet<String> visited;

    /**
     * Flag that forces the threads to get a null string if they call
     * the getFromOrigin once the flag is set to true.
     */
    private boolean isStopped = false;

    /**
     * Constructor.
     *
     * @param nThreads (required) number of threads
     * using the data structure. Can take positive values.
     */
    public LinkProvider(int nThreads) {
        this.origin = new ArrayList<>();
        this.destiny = new ArrayList<>();

        this.waitingWorkers = 0;
        this.nThreads = nThreads;

        this.originLock = new ReentrantLock();
        this.destinyLock = new ReentrantLock();

        this.notEmpty = originLock.newCondition();
        this.stepOver = originLock.newCondition();

        this.visited = new HashSet<>();
    }

    /**
     * Get a String from the stack origin.
     *
     * This method tries to get a string from the origin stack, if this stack is
     * empty the current thread is set to wait for a signal in the condition
     * notEmpty. When the number of threads waiting is equal to nThreads this
     * methods send a signal in the condition stepOver.
     * @return a string from the top of the stack.
     */
    public String getFromOrigin() {
        originLock.lock();
        try {

            while (origin.size() == 0 && !isStopped) {// If the origin queue is empty, the threads trying to get links are set to sleep
                waitingWorkers++;

                if (waitingWorkers == nThreads)
                    stepOver.signal();

                notEmpty.awaitUninterruptibly();

                waitingWorkers--;
            }

            if (isStopped)
                return null;

            return origin.remove(origin.size() - 1);
        } finally {
            originLock.unlock();
        }
    }

    /**
     * Stop the threads waiting for a signal.
     *
     * Allows the threads waiting for a signal in notEmpty to exit from the
     * getFromOrigin method, even when the stack is empty, by receiving a null.
     * string.
     */
    public void Stop() {
        originLock.lock();
        try {
            isStopped = true;
            notEmpty.signalAll();
        } finally {
            originLock.unlock();
        }
    }

    /**
     * Put a String in the stack origin.
     *
     * This methods add a String in the top of the stack origin if the set
     * visited does not contain this String.
     * @param s String to add in the stack.
     */
    public void putInOrigin(String s) {
        originLock.lock();
        destinyLock.lock();
        try {
            boolean emptyBefore = (origin.size() == 0);

            if (!visited.contains(s)) {
                visited.add(s);
                origin.add(s);
                if (emptyBefore) notEmpty.signalAll();
            }

        } finally {
            originLock.unlock();
            destinyLock.unlock();
        }
    }

    /**
     * Put a list of Strings in the stack destiny.
     *
     * This methods iterates over each String in the list and add it in the top
     * of the stack destiny if the set visited does not contain this String.
     * @param list list of Strings to add in the stack.
     */
    public void putInDestiny(List<String> list) {
        destinyLock.lock();
        try {
            for (String s : list) {
                if (!visited.contains(s)) { // We just add new items to the destiny queue
                    visited.add(s);
                    destiny.add(s);
                }
            }
        } finally {
            destinyLock.unlock();
        }
    }

    /**
     * Transfer the Strings of the stack destiny to the stack origin.
     *
     * This method transfer the Strings of the stack destiny to the
     * stack origin and send a signal to all the threads waiting in
     * the notEmpty condition.
     * @return the Strings stored in the stack destiny.
     */
    public ArrayList<String> swapAndDrain() {
        originLock.lock();
        try {
            while (origin.size() > 0 || waitingWorkers < nThreads) {// If the origin queue is not empty, the step is not over
                stepOver.awaitUninterruptibly();
            }
            destinyLock.lock();
            try {
                // Swaping Queues
                this.origin = destiny;
                this.destiny = new ArrayList<>();

                // Sending Signal
                notEmpty.signalAll();

                // Returning partial solution
                return new ArrayList<>(origin);

            } finally {
                destinyLock.unlock();
            }

        } finally {
            originLock.unlock();
        }
    }

    /**
     * Prints useful information about the current status of the data
     * structure in the console.
     */
    public void printStatus() {
        originLock.lock();
        destinyLock.lock();
        try {
            System.out.printf("OriginSize: %6d | DestinySize: %6d | WaitingWorkers: %3d | PagesVisited: %6d\n", origin.size(), destiny.size(), waitingWorkers, visited.size());
        }
        finally {
            destinyLock.unlock();
            originLock.unlock();
        }
    }
}
