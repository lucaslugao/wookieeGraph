/**
 * Created by Paulo on 2/24/2017.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.locks.*;

public class DoubleBlockingQueue {

    private ArrayList<String> partialSolution;
    private ArrayList<String> origin;
    private ArrayList<String> destiny;

    private int waitingWorkers;
    private final int nThreads;

    private Lock originLock;
    private Lock destinyLock;
    private Lock solutionLock;

    private Condition notEmpty; // The origin is empty, the threads must wait until the manager swaps
    private Condition stepOver; // The step is not over yet, the manager must wait

    private HashSet<String> blackList;

    public DoubleBlockingQueue(int nThreads) {
        this.origin = new ArrayList<String>();
        this.destiny = new ArrayList<String>();
        this.partialSolution = new ArrayList<String>();

        this.waitingWorkers = 0;
        this.nThreads = nThreads;

        this.originLock = new ReentrantLock();
        this.destinyLock = new ReentrantLock();
        this.solutionLock = new ReentrantLock();

        this.notEmpty = originLock.newCondition();
        this.stepOver = originLock.newCondition();

        this.blackList = new HashSet<String>();
    }

    public void putInSolution(String s) {
        solutionLock.lock();
        try {
            partialSolution.add(s);
        } finally {
            solutionLock.unlock();
        }
    }

    public String getFromOrigin() {
        originLock.lock();
        try {
            while (origin.size() == 0) {// If the origin queue is empty, the threads trying to get links are set to sleep
                waitingWorkers++;

                if (waitingWorkers == nThreads)
                    stepOver.signal();

                notEmpty.awaitUninterruptibly();

                waitingWorkers--;
            }

            return origin.remove(origin.size() - 1);
        } finally {
            originLock.unlock();
        }
    }

    public void putInOrigin(String s) {
        originLock.lock();
        destinyLock.lock();
        try {
            boolean emptyBefore = (origin.size() == 0);

            if (! blackList.contains(s)) {
                blackList.add(s);
                origin.add(s);
                if (emptyBefore) notEmpty.signalAll();
            }

        } finally {
            originLock.unlock();
            destinyLock.unlock();
        }
    }

    public void putInDestiny(List<String> list) {
        destinyLock.lock();
        try {
            for (String s : list) {
                if (!blackList.contains(s)) { // We just add new items to the destiny queue
                    blackList.add(s);
                    destiny.add(s);
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
                this.destiny = new ArrayList<String>();

                // Sending Signal
                notEmpty.signalAll();

                // Returning partial solution
                ArrayList<String> auxList = new ArrayList<String>(partialSolution);
                partialSolution = new ArrayList<String>();
                return auxList;
            } finally {
                destinyLock.unlock();
            }

        } finally {
            originLock.unlock();
        }
    }

    public void printStatus() {
        System.out.println("***   ***   ***   ***   ***   ***   ***   ***   ***");
        System.out.println("OriginSize: " + origin.size() + "; DestinyOrigin: " + destiny.size() + "; CurrentWorkers: " + waitingWorkers);

        System.out.println("BlackList: ");
        for (String s : blackList) {
            System.out.println(s);
        }

    }

    public void signalAll() {
        originLock.lock();
        try {
            notEmpty.signalAll();
        }
        finally {
            originLock.unlock();
        }
    }

    public static void main(String[] args) {
        DoubleBlockingQueue testQueue = new DoubleBlockingQueue(1);

        testQueue.putInOrigin("Yoda1");
        testQueue.putInOrigin("Yoda2");
        testQueue.putInOrigin("Yoda3");
        testQueue.putInOrigin("Yoda4");

        testQueue.printStatus();

        String s1 = testQueue.getFromOrigin();
        String s2 = testQueue.getFromOrigin();
        String s3 = testQueue.getFromOrigin();
        String s4 = testQueue.getFromOrigin();

        testQueue.printStatus();

        ArrayList<String> input = new ArrayList<String>();
        input.add("Yoda1");
        testQueue.putInDestiny(input);

        input.add("Yoda2");
        testQueue.putInDestiny(input);

        input.add("Yoda3");
        testQueue.putInDestiny(input);

        input.add("Yoda4");
        testQueue.putInDestiny(input);

        testQueue.printStatus();
    }
}
