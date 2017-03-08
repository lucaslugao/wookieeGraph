package org.graphstream.ui.view;

import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.GraphRenderer;

/**
 * Created by Lucas on 3/3/2017.
 */
public class MyView extends DefaultView {

    public MyView(Viewer viewer, String identifier, GraphRenderer renderer) {
        super(viewer, identifier, renderer);
    }

    public void close(GraphicGraph graph) {
        if (renderer != null) {
            renderer.close();
        }

        graph.addAttribute("ui.viewClosed", getId());
        removeKeyListener(shortcuts);
        shortcuts.release();
        mouseClicks.release();

        openInAFrame(false);
    }

}
