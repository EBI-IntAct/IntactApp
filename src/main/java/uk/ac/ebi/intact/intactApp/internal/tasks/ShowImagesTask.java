package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowImagesTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ShowImagesTask extends AbstractTask implements ObservableTask {
    final IntactManager manager;
    final ShowImagesTaskFactory factory;
    final boolean show;
    @Tunable(description = "Network view to set enhanced labels on",
            // longDescription = StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
            // exampleStringValue = StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
            context = "nogui")
    public CyNetworkView view = null;
    CyNetworkView netView;

    public ShowImagesTask(final IntactManager manager, final boolean show,
                          final ShowImagesTaskFactory factory) {
        this.manager = manager;
        if (view != null)
            this.netView = view;
        else
            this.netView = null;
        this.factory = factory;
        this.show = show;
    }

    public ShowImagesTask(final IntactManager manager, final CyNetworkView netView,
                          final ShowImagesTaskFactory factory) {
        this.manager = manager;
        if (view != null)
            this.netView = view;
        else
            this.netView = netView;
        this.factory = factory;
        this.show = false;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Show/hide structure images");

        if (netView == null) {
            // Command version
            manager.setShowImage(show);
            CyNetwork network = manager.getCurrentNetwork();
            Collection<CyNetworkView> views =
                    manager.getService(CyNetworkViewManager.class).getNetworkViews(network);
            for (CyNetworkView view : views) {
                if (view.getRendererId().equals("org.cytoscape.ding")) {
                    netView = view;
                    break;
                }
            }
            netView.updateView();
            return;
        }

        if (manager.showImage()) {
            manager.setShowImage(false);
            if (ModelUtils.isStitchNetwork(netView.getModel()))
                ViewUtils.updateChemVizPassthrough(manager, netView, false);
        } else {
            manager.setShowImage(true);
            if (ModelUtils.isStitchNetwork(netView.getModel()))
                ViewUtils.updateChemVizPassthrough(manager, netView, true);
        }

        netView.updateView();
        factory.reregister();
        manager.updateControls();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R getResults(Class<? extends R> clzz) {
        if (clzz.equals(String.class)) {
            return (R) "";
        } else if (clzz.equals(JSONResult.class)) {
            JSONResult res = () -> "{}";
            return (R) res;
        }
        return null;
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return Arrays.asList(JSONResult.class, String.class);
    }


}
