package uk.ac.ebi.intact.app.internal.tasks.details;

import org.cytoscape.application.swing.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.details.factories.ShowDetailPanelTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.DetailPanel;

import java.awt.*;
import java.util.Properties;

public class ShowDetailPanelTask extends AbstractTask {
    final Manager manager;
    final ShowDetailPanelTaskFactory factory;
    final boolean show;

    public ShowDetailPanelTask(final Manager manager,
                               final ShowDetailPanelTaskFactory factory, boolean show) {
        this.manager = manager;
        this.factory = factory;
        this.show = show;
    }

    public static boolean isPanelRegistered(Manager manager) {
        CySwingApplication swingApplication = manager.utils.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

        return cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Intact") >= 0;
    }

    public void run(TaskMonitor monitor) {

        if (show)
            monitor.setTitle("Show results panel");
        else
            monitor.setTitle("Hide results panel");

        CySwingApplication swingApplication = manager.utils.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

        // If the panel is not already registered, create it
        if (show && cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Intact") < 0) {
            CytoPanelComponent2 panel = new DetailPanel(manager);

            // Register it
            manager.utils.registerService(panel, CytoPanelComponent.class, new Properties());

            if (cytoPanel.getState() == CytoPanelState.HIDE)
                cytoPanel.setState(CytoPanelState.DOCK);

        } else if (!show && cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Intact") >= 0) {
            int compIndex = cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Intact");
            Component panel = cytoPanel.getComponentAt(compIndex);
            if (panel instanceof CytoPanelComponent2) {
                // Unregister it
                manager.utils.unregisterService(panel, CytoPanelComponent.class);
                manager.utils.setDetailPanel(null);
            }
        }

        factory.reregister();
    }
}
