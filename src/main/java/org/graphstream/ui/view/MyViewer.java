package org.graphstream.ui.view;

import org.graphstream.graph.Graph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.swingViewer.DefaultView;
import org.graphstream.ui.swingViewer.ViewPanel;

import java.awt.event.ActionEvent;

/**
 * Created by Lucas on 3/3/2017.
 */
public class MyViewer extends Viewer {
    /**
     * The graph or source of graph events is in another thread or on another
     * machine, but the pipe already exists. The graphic graph displayed by this
     * viewer is created.
     *
     * @param source The source of graph events.
     */
    public MyViewer(ProxyPipe source) {
        super(source);
    }

    /**
     * We draw a pre-existing graphic graph. The graphic graph is maintained by
     * its creator.
     *
     * @param graph THe graph to draw.
     */
    public MyViewer(GraphicGraph graph) {
        super(graph);
    }

    /**
     * New viewer on an existing graph. The viewer always run in the Swing
     * thread, therefore, you must specify how it will take graph events from
     * the graph you give. If the graph you give will be accessed only from the
     * Swing thread use ThreadingModel.GRAPH_IN_GUI_THREAD. If the graph you
     * use is accessed in another thread use
     * ThreadingModel.GRAPH_IN_ANOTHER_THREAD. This last scheme is more powerful
     * since it allows to run algorithms on the graph in parallel with the
     * viewer.
     *
     * @param graph          The graph to render.
     * @param threadingModel
     * @param delay          Delay between frames (in ms)
     */
    public MyViewer(Graph graph, ThreadingModel threadingModel, int delay) {
        super(graph, threadingModel);
        this.delay = delay;
    }

    /**
     * Called on a regular basis by the timer. Checks if some events occurred
     * from the graph pipe or from the layout pipe, and if the graph changed,
     * triggers a repaint. Never call this method, it is called by a Swing Timer
     * automatically.
     */
    public void actionPerformed(ActionEvent e) {
        synchronized (views) {
            if (pumpPipe != null)
                pumpPipe.pump();

            if (layoutPipeIn != null)
                layoutPipeIn.pump();

            if (graph != null) {
                boolean changed = graph.graphChangedFlag();

                if (changed) {
                    computeGraphMetrics();

                    for (View view : views.values())
                        view.display(graph, changed);
                }

                graph.resetGraphChangedFlag();
            }
        }
    }

    /**
     * Close definitively this viewer and all its views.
     */
    public void close() {
        synchronized (views) {
            disableAutoLayout();

            for (View view : views.values())
                view.close(graph);

            timer.stop();
            timer.removeActionListener(this);

            if (pumpPipe != null)
                pumpPipe.removeSink(graph);
            if (sourceInSameThread != null)
                sourceInSameThread.removeSink(graph);

            graph = null;
            pumpPipe = null;
            sourceInSameThread = null;
            timer = null;
        }
    }

    /**
     * Build the default graph view and insert it. The view identifier is
     * {@link #DEFAULT_VIEW_ID}. You can request the view to be open in its own
     * frame.
     *
     * @param openInAFrame It true, the view is placed in a frame, else the view is only
     *                     created and you must embed it yourself in your application.
     */
    public ViewPanel addDefaultView(boolean openInAFrame) {
        synchronized (views) {
            DefaultView view = new MyView(this, DEFAULT_VIEW_ID,
                    newGraphRenderer());
            addView(view);

            if (openInAFrame)
                view.openInAFrame(true);

            return view;
        }
    }
}
