import java.io.IOException;

/**
 * Created by Lucas on 3/1/2017.
 */
public interface BFSManager {
    void crawl();
    void writeSolutionToFile(String path) throws IOException;
}
