package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowGlassBallEffectTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

public class ShowGlassBallEffectTask extends AbstractTask {
    final IntactManager manager;
    final ShowGlassBallEffectTaskFactory factory;
    @Tunable(description = "Network view to set enhanced labels on",
            // longDescription = StringToModel.CY_NETWORK_VIEW_LONG_DESCRIPTION,
            // exampleStringValue = StringToModel.CY_NETWORK_VIEW_EXAMPLE_STRING,
            context = "nogui")
    public CyNetworkView view = null;
    CyNetworkView netView;

    public ShowGlassBallEffectTask(final IntactManager manager, final CyNetworkView netView,
                                   final ShowGlassBallEffectTaskFactory factory) {
        this.manager = manager;
        if (view != null)
            this.netView = view;
        else
            this.netView = netView;
        this.factory = factory;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Enable/disable STRING glass balls effect");

        if (manager.showGlassBallEffect())
            manager.setShowGlassBallEffect(false);
        else
            manager.setShowGlassBallEffect(true);

        VisualMappingManager vmm = manager.getService(VisualMappingManager.class);
        CyNetworkViewManager netManager = manager.getService(CyNetworkViewManager.class);
        for (CyNetworkView currNetView : netManager.getNetworkViewSet()) {
            if (vmm.getVisualStyle(currNetView).getTitle().startsWith(ViewUtils.STYLE_NAME) || vmm
                    .getVisualStyle(currNetView).getTitle().startsWith(ViewUtils.STYLE_NAME_ORG)) {
                ViewUtils.updateGlassBallEffect(manager, vmm.getVisualStyle(currNetView),
                        currNetView.getModel(), manager.showGlassBallEffect());
            }
        }
        netView.updateView();
        factory.reregister();
        manager.updateControls();
    }
}
