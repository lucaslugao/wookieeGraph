import java.io.IOException;

/**
 * Created by Lucas on 3/1/2017.
 */
interface BFSManager {
    void Crawl();
    void writeSolutionToFile(String path) throws IOException;
}
