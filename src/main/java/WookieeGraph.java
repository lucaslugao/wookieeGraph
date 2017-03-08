import java.io.IOException;
import java.util.Set;

/**
 * Created by Lucas on 2/24/2017.
 */

public class WookieeGraph {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.printf("At least three parameters expected, %d given.\n", args.length);
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
        VisualizationProvider visualization = new DummyVisualization();

        String outputFile = args[2];
        if (args.length >= 4) {
            switch (args[3].toLowerCase()) {
                case "gs":
                case "graphstream":
                    visualization = new GraphStreamVisualization();
                    break;
            }
        }

        long startTime = System.nanoTime();
        Set<String> characterNames = CharacterNamesProvider.fromFile("charNames.csv");//.fromWiki();//
        System.out.format("Elapsed time = %f seconds%n", (System.nanoTime() - startTime) / 1000000000.0);

        Manager manager = new Manager(origin, depth, 200, characterNames, visualization);//.fromWiki() ); //

        visualization.startViewer();

        startTime = System.nanoTime();
        manager.Crawl();
        System.out.format("Elapsed time = %f seconds%n", (System.nanoTime() - startTime) / 1000000000.0);

        //visualization.stopViewer(5000);
        try {
            manager.writeSolutionToFile(outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The end.");
    }
}
