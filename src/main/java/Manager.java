
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
public class Manager implements BFSManager {
    private final Set<String> characterNames;
    private final LinkProvider linkProvider;
    private final StringBuilder solutionLines;
    private final int maxDepth;
    private final int poolSize;

    public Manager(String graphOrigin, int depth, int poolSize, String charNamesPath) {
        this.solutionLines = new StringBuilder();
        this.linkProvider = new LinkProvider(poolSize);
        this.maxDepth = depth;
        this.poolSize = poolSize;
        this.linkProvider.putInOrigin(graphOrigin);
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
                    linkProvider.printStatus();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        reporter.start();

        for (int i = 0; i < poolSize; i++) {
            threads[i] = new Thread(new Crawler(linkProvider, characterNames), "Crawler #" + Integer.toString(i));
            threads[i].start();
        }


        for (int depth = 1; depth <= maxDepth; depth++) {
            System.out.println("Depth = " + Integer.toString(depth));
            ArrayList<String> partialSolution = linkProvider.swapAndDrain();
            appendToSolution(partialSolution, depth);
        }

//        for (int i = 0; i < poolSize; i++)
//            threads[i].interrupt();

        linkProvider.Stop();
        System.out.println("Join");
        //ArrayList<String> partialSolution = linkProvider.swapAndDrain();
        //appendToSolution(partialSolution, maxDepth);
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
