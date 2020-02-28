package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.application.swing.*;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowPublicationsPanelTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.ui.PublicationsCytoPanel;

import java.awt.*;
import java.util.Properties;

public class ShowPublicationsPanelTask extends AbstractTask {
    final IntactManager manager;
    final boolean show;
    final boolean noSignificant;
    final ShowPublicationsPanelTaskFactory factory;

    public ShowPublicationsPanelTask(final IntactManager manager,
                                     ShowPublicationsPanelTaskFactory factory, boolean show, boolean noSignificant) {
        this.manager = manager;
        this.factory = factory;
        this.show = show;
        this.noSignificant = noSignificant;
    }

    public static boolean isPanelRegistered(IntactManager sman) {
        CySwingApplication swingApplication = sman.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);

        return cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications") >= 0;
    }

    public void run(TaskMonitor monitor) {
        if (show)
            monitor.setTitle("Show publications panel");
        else
            monitor.setTitle("Hide publications panel");

        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);

        // If the panel is not already registered, create it
        if (show && cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications") < 0) {
            CytoPanelComponent2 panel = new PublicationsCytoPanel(manager, noSignificant);

            // Register it
            manager.registerService(panel, CytoPanelComponent.class, new Properties());
            manager.registerService(panel, RowsSetListener.class, new Properties());

            if (cytoPanel.getState() == CytoPanelState.HIDE)
                cytoPanel.setState(CytoPanelState.DOCK);

            cytoPanel.setSelectedIndex(
                    cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications"));

        } else if (show && cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications") >= 0) {
            PublicationsCytoPanel panel = (PublicationsCytoPanel) cytoPanel.getComponentAt(
                    cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications"));
            panel.initPanel(noSignificant);
        } else if (!show && cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications") >= 0) {
            int compIndex = cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Publications");
            Component panel = cytoPanel.getComponentAt(compIndex);
            if (panel instanceof CytoPanelComponent2) {
                // Unregister it
                manager.unregisterService(panel, CytoPanelComponent.class);
                manager.unregisterService(panel, RowsSetListener.class);
            }
        }

        factory.reregister();
    }
}
