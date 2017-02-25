import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Lucas on 2/24/2017.
 */

public class wookieeGraph {
    public static void main(String[] args){
        if(args.length != 3){
            System.err.println("Three parameter expected.");
        }
        String origin = args[0];
        Integer depth = 0;
        try {
            depth = Integer.parseInt(args[1]);

        } catch (NumberFormatException e) {
            System.err.println("\"" + args[1] + "\" is not a number.");
        }

        String outputFile = args[2];

        Manager manager = new Manager(origin, depth, 10 );
        long startTime = System.nanoTime();
        manager.crawl();
        long estimatedTime = System.nanoTime() - startTime;

        System.out.format("Elapsed time = %f seconds%n", estimatedTime/1000000000.0);
        try {
            manager.writeSolutionToFile(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
