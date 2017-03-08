/**
 * Created by Lucas on 3/3/2017.
 */
public interface VisualizationProvider {
    void addEdge(String a, String b, String label);

    void startViewer();

    void stopViewer(int waitTime);
}
