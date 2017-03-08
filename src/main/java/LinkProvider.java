/**
 * Created by Paulo on 2/24/2017.
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LinkProvider {

    private ArrayList<String> origin;
    private ArrayList<String> destiny;

    private int waitingWorkers;
    private final int nThreads;

    private final Lock originLock;
    private final Lock destinyLock;

    private final Condition notEmpty; // The origin is empty, the threads must wait until the manager swaps
    private final Condition stepOver; // The step is not over yet, the manager must wait

    private final HashSet<String> visited;

    private boolean isStopped = false;


    private final VisualizationProvider visualization;
    private int counter = 0;

    public LinkProvider(int nThreads, VisualizationProvider visualization) {
        this.origin = new ArrayList<>();
        this.destiny = new ArrayList<>();

        this.waitingWorkers = 0;
        this.nThreads = nThreads;

        this.originLock = new ReentrantLock();
        this.destinyLock = new ReentrantLock();

        this.notEmpty = originLock.newCondition();
        this.stepOver = originLock.newCondition();

        this.visited = new HashSet<>();

        this.visualization = visualization;
    }

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

    public void Stop() {
        originLock.lock();
        try {
            isStopped = true;
            notEmpty.signalAll();
        } finally {
            originLock.unlock();
        }
    }

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

    public void putInDestiny(String origin, List<String> list) {
        destinyLock.lock();
        try {
            for (String s : list) {
                if (!visited.contains(s)) { // We just add new items to the destiny queue
                    visited.add(s);
                    destiny.add(s);
                    if (visualization != null)
                        visualization.addEdge(origin, s, "Level" + Integer.toString(counter % 2));
                }
            }
        } finally {
            destinyLock.unlock();
        }
    }

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
                counter++;
                return new ArrayList<>(origin);

            } finally {
                destinyLock.unlock();
            }

        } finally {
            originLock.unlock();
        }
    }

    public void printStatus() {
        originLock.lock();
        destinyLock.lock();
        try {
            System.out.printf("OriginSize: %6d | DestinySize: %6d | WaitingWorkers: %3d | PagesVisited: %6d\n", origin.size(), destiny.size(), waitingWorkers, visited.size());
        } finally {
            destinyLock.unlock();
            originLock.unlock();
        }
    }
}
