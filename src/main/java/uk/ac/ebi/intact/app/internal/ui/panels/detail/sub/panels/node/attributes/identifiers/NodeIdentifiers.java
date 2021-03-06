package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.identifiers;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.AbstractNodeAttribute;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.identifiers.panels.IdentifierPanelFactory;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class NodeIdentifiers extends AbstractNodeAttribute {

    private final List<Identifier> identifiers;

    public NodeIdentifiers(Node node, OpenBrowser openBrowser) {
        this("Identifiers", node, openBrowser, node.getIdentifiers());
    }

    public NodeIdentifiers(String title, Node node, OpenBrowser openBrowser, List<Identifier> identifiers) {
        super(title, node, openBrowser);
        this.identifiers = identifiers;
        fillContent();
    }

    protected void fillContent() {
        if (identifiers.isEmpty()) {
            this.setVisible(false);
            return;
        }

        Map<String, List<Identifier>> dbToIdentifiers = CollectionUtils.groupBy(identifiers, identifier -> identifier.database.value);

        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        dbToIdentifiers.values().stream()
                .sorted(Comparator.comparing(identifiersOfDb -> identifiersOfDb.get(0).database.value))
                .forEach(identifiersOfDb -> content.add(IdentifierPanelFactory.createPanel(identifiersOfDb, openBrowser)));
    }
}
