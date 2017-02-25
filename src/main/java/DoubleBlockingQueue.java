/**
 * Created by Paulo on 2/24/2017.
 */

import com.sun.deploy.util.BlackList;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.locks.*;

public class DoubleBlockingQueue {

    private ArrayList <String> partialSolution;
    private ArrayList <String> origin;
    private ArrayList <String> destiny;

    private int currentWorkers;

    private Lock originLock ;
    private Lock destinyLock ;
    private Lock solutionLock ;

    private Condition notEmpty; // The origin is empty, the threads must wait until the manager swaps
    private Condition stepOver; // The step is not over yet, the manager must wait

    private HashSet<String> blackList;

    public DoubleBlockingQueue() {
        this.origin = new ArrayList<String>();
        this.destiny = new ArrayList<String>();
        this.partialSolution = new ArrayList<String>();

        this.currentWorkers = 0;//this.headOrigin = this.headDestiny = 0; // head at zero means that there is no element at the queue

        this.originLock = new ReentrantLock ();
        this.destinyLock = new ReentrantLock ();
        this.solutionLock = new ReentrantLock();

        this.notEmpty = originLock.newCondition();
        this.stepOver = originLock.newCondition();

        this.blackList = new HashSet<String>();
    }

    public void putInSolution (String s) {
        solutionLock.lock();
        try {
            partialSolution.add(s);
        }
        finally {
            solutionLock.unlock();
        }
    }

    public String getFromOrigin() {
        originLock.lock();
        try {
            while (origin.size() == 0) // If the origin queue is empty, the threads trying to get links are set to sleep
                notEmpty.awaitUninterruptibly();

            currentWorkers++;
            int head = origin.size() - 1;
            String link = origin.remove(head);
            return link;
        }
        finally {
            originLock.unlock();
        }
    }

    public void putInOrigin (String s) {
        originLock.lock();
        try {
            origin.add(s);
        }
        finally {
            originLock.unlock();
        }
    }

    public void putInDestiny(List<String> list){
        destinyLock.lock();
        try {
            for (String s : list) {
                if (!blackList.contains(s)) { // We just add new items to the destiny queue
                    blackList.add(s);
                    destiny.add(s);
                }
            }
            currentWorkers--;
            originLock.lock();
            try {
                if (currentWorkers == 0 && origin.size() == 0) { // We don`t need a lock to read the headOrigin, even though it is modified at getFromOrigin,
                    stepOver.signal();                        // because once headOrigin is zero it c
                }
            }
            finally {
                originLock.unlock();
            }
        }
        finally {
            destinyLock.unlock();
        }
    }

    public List<String> swapDrain() {
        originLock.lock();
        destinyLock.lock();
        try {
            while (origin.size() > 0 && currentWorkers > 0) {// If the origin queue is not empty, the step is not over
                stepOver.awaitUninterruptibly();
            }

            // Swaping Queues
            this.origin = destiny;
            this.destiny = new ArrayList<String>();

            // Sending Signal
            notEmpty.signalAll();

            // Returning partial solution
            ArrayList<String> auxList = new ArrayList<String>(partialSolution);
            partialSolution = new ArrayList<String>();
            return auxList;

        }
        finally {
            originLock.unlock();
            destinyLock.unlock();
        }
    }

    public void printStatus () {
        System.out.println("***   ***   ***   ***   ***   ***   ***   ***   ***");
        System.out.println("OriginSize: " + origin.size() + "; DestinyOrigin: " + destiny.size() + "; CurrentWorkers: " + currentWorkers);

        System.out.println("BlackList: ");
        for (String s : blackList)  {
            System.out.println(s);
        }

    }

    public void signAll () {
        notEmpty.signalAll();
    }

    public static void main (String[] args) {
        DoubleBlockingQueue testQueue = new DoubleBlockingQueue();

        testQueue.putInOrigin("Yoda1");
        testQueue.putInOrigin("Yoda2");
        testQueue.putInOrigin("Yoda3");
        testQueue.putInOrigin("Yoda4");

        testQueue.printStatus ();

        String s1 = testQueue.getFromOrigin();
        String s2 = testQueue.getFromOrigin();
        String s3 = testQueue.getFromOrigin();
        String s4 = testQueue.getFromOrigin();

        testQueue.printStatus ();

        ArrayList<String> input = new ArrayList<String> ();
        input.add("Yoda1");
        testQueue.putInDestiny(input);

        input.add("Yoda2");
        testQueue.putInDestiny(input);

        input.add("Yoda3");
        testQueue.putInDestiny(input);

        input.add("Yoda4");
        testQueue.putInDestiny(input);
        //Thread robot1 = new Thread();

        testQueue.printStatus ();
    }
}
