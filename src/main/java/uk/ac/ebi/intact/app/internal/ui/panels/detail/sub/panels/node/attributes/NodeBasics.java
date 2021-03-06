package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.labels.SelectableLabel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;

import javax.swing.*;
import java.awt.*;

import static uk.ac.ebi.intact.app.internal.io.DbIdentifiersToLink.getFancyDatabaseName;
import static uk.ac.ebi.intact.app.internal.io.DbIdentifiersToLink.getLink;

public class NodeBasics extends AbstractNodeAttribute {

    private LinePanel graphDescription;

    public NodeBasics(Node node, OpenBrowser openBrowser) {
        super(null, node, openBrowser);
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        if (node.description != null) content.add(new SelectableLabel(node.description));
        content.add(new JLink(getFancyDatabaseName(node.preferredIdentifier) + " · " + node.preferredIdentifier.id, getLink(node.preferredIdentifier), openBrowser));
        graphDescription = new LinePanel(getBackground());
        graphDescription.add(new JLink(StringUtils.capitalize(node.typeName), node.type.id.getUserAccessURL(), openBrowser));
        if (node.species != null) {
            graphDescription.add(new SelectableLabel(" of " + node.species + " - TaxId: "));
            graphDescription.add(LinkUtils.createSpecieLink(openBrowser, node.taxId));
            graphDescription.add(Box.createHorizontalGlue());
        }
        content.add(graphDescription);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (graphDescription != null)
            graphDescription.setBackground(bg);
    }
}
