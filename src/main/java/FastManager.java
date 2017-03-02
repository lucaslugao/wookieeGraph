
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Lucas on 2/24/2017.
 */
public class FastManager implements BFSManager {
    private final Set<String> characterNames;
    private final DoubleBlockingQueue doubleBlockingQueue;
    private final StringBuilder solutionLines;
    private final int maxDepth;
    private final int poolSize;

    public FastManager(String graphOrigin, int depth, int poolSize, String charNamesPath) {
        this.solutionLines = new StringBuilder();
        this.doubleBlockingQueue = new DoubleBlockingQueue(poolSize);
        this.maxDepth = depth;
        this.poolSize = poolSize;
        this.doubleBlockingQueue.putInOrigin(graphOrigin);
        this.characterNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        try {
            for (String line : Files.readAllLines(Paths.get(charNamesPath), StandardCharsets.UTF_8)) {
                this.characterNames.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("charNames path not found, please use the SlowManager.");
        }
    }
    public void crawl() {
        Thread threads[] = new Thread[poolSize];

        Thread reporter = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    doubleBlockingQueue.printStatus();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        reporter.start();

        for (int i = 0; i < poolSize; i++) {
            threads[i] = new Thread(new FastCrawler(doubleBlockingQueue, characterNames), "FastCrawler #" + Integer.toString(i));
            threads[i].start();
        }

        for (int depth = 0; depth < maxDepth; depth++) {
            System.out.println("Depth = " + Integer.toString(depth));
            ArrayList<String> partialSolution = doubleBlockingQueue.swapAndDrain(false);
            appendToSolution(partialSolution, depth);
        }

        for (int i = 0; i < poolSize; i++)
            threads[i].interrupt();


        //doubleBlockingQueue.signalAll();
        System.out.println("Join");
        ArrayList<String> partialSolution = doubleBlockingQueue.swapAndDrain(true);
        appendToSolution(partialSolution, maxDepth);
        for (int i = 0; i < poolSize; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        reporter.interrupt();
        try {
            reporter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void appendToSolution(List<String> partialSolution, int depth){
        for (String solution : partialSolution)
            solutionLines.append(solution + ", " + Integer.toString(depth) + "\n");
    }

    public void writeSolutionToFile(String filename) throws IOException {
        System.out.printf("Wrote solution with %d lines!\n", solutionLines.toString().chars().filter(ch -> ch == '\n').count());
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
        outputWriter.write(solutionLines.toString());
        outputWriter.flush();
        outputWriter.close();
    }


}
