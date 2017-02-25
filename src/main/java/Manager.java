import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Lucas on 2/24/2017.
 */
public class Manager {
    private final DoubleBlockingQueue doubleBlockingQueue;
    private final StringBuilder solutionLines;
    private final int maxDepth;
    private final int poolSize;

    public Manager(String graphOrigin, int depth, int poolSize) {
        this.solutionLines = new StringBuilder();
        this.doubleBlockingQueue = new DoubleBlockingQueue();
        this.maxDepth = depth;
        this.poolSize = poolSize;
        this.doubleBlockingQueue.putInOrigin(graphOrigin);
    }

    public void crawl() {
        Thread threads[] = new Thread[poolSize];

        for (int i = 0; i < poolSize; i++) {
            threads[i] = new Thread(new Crawler(doubleBlockingQueue));
            threads[i].run();
        }

        for (int depth = 0; depth < maxDepth; depth++) {
            ArrayList<String> partialSolution = doubleBlockingQueue.swapAndDrain();
            for (String solution : partialSolution)
                solutionLines.append(solution + ", " + Integer.toString(depth) + "\r\n");
        }

        for (int i = 0; i < poolSize; i++)
            threads[i].interrupt();
        doubleBlockingQueue.signalAll();
    }

    public void writeSolutionToFile(String filename) throws IOException {
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
        outputWriter.write(solutionLines.toString());
        outputWriter.flush();
        outputWriter.close();
    }


}
