import java.io.IOException;

/**
 * Created by Lucas on 2/24/2017.
 */

public class WookieeGraph {
    public static void main(String[] args){
        if(args.length != 3){
            System.err.println("Three parameters expected.");
            return;
        }
        String origin = args[0];
        Integer depth = 0;
        try {
            depth = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("\"" + args[1] + "\" is not a number.");
            return;
        }

        String outputFile = args[2];
        long startTime = System.nanoTime();
        BFSManager manager = new Manager(origin, depth, 100, CharacterNamesProvider.fromFile("charNames.csv"));//.fromWiki() ); //
        System.out.format("Elapsed time = %f seconds%n", (System.nanoTime() - startTime)/1000000000.0);

        startTime = System.nanoTime();
        manager.Crawl();
        System.out.format("Elapsed time = %f seconds%n", (System.nanoTime() - startTime)/1000000000.0);
        try {
            manager.writeSolutionToFile(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
