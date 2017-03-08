import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.MyViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by Lucas on 3/3/2017.
 */
public class GraphStreamVisualization implements VisualizationProvider {
    private final Graph graph;
    private final Thread viewerThread;
    private final JFrame frame;
    private boolean isRunning;


    public GraphStreamVisualization() {
        System.setProperty("sun.java2d.opengl", "True");
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
        this.graph = new SingleGraph("StarWars");
        this.graph.addAttribute("ui.antialias");
        this.graph.addAttribute("ui.quality");
        this.graph.addAttribute("ui.stylesheet", "url('res/stylesheet.css')");
        this.graph.setStrict(false);
        this.graph.setAutoCreate(true);
        this.isRunning = false;

        this.frame = new JFrame("Star Wars: Social Graph");

        this.viewerThread = new Thread(() -> {
            Viewer graphViewer = new MyViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD, 1000 / 60);
            graphViewer.enableAutoLayout();

            try {
                frame.setIconImage(ImageIO.read(new File("images/yoda.png")));
            } catch (IOException e) {
                System.out.println("Failed to load Yoda icon.");
            }

            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setSize(2 * dim.width / 3, 2 * dim.height / 3);
            frame.setBackground(Color.BLACK);
            frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            View view = graphViewer.addDefaultView(false);
            frame.add((Component) view);
            frame.setVisible(true);
            frame.requestFocus();

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    e.getWindow().setVisible(false);
                    e.getWindow().dispose();
                    graphViewer.close();
                }
            });
        });
        this.viewerThread.setName("GraphStream viewer thread");
    }

    public void addEdge(String a, String b, String label) {
        if (isRunning) {
            graph.addEdge(a + "-" + b, a, b);
            graph.getEdge(a + "-" + b).addAttribute("ui.class", label);
        }
    }

    public void startViewer() {
        isRunning = true;
        viewerThread.start();
    }

    public void stopViewer(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (frame.isVisible())
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }
}
