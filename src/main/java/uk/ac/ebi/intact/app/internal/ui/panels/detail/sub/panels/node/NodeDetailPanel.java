package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node;

import com.google.common.collect.Comparators;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.NodeBasics;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.NodeDetails;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.NodeFeatures;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.NodeSchematic;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.identifiers.NodeIdentifiers;
import uk.ac.ebi.intact.app.internal.ui.panels.filters.FilterPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class NodeDetailPanel extends AbstractDetailPanel {
    private JPanel nodesPanel = null;
    private CollapsablePanel selectedNodes;
    private final EasyGBC layoutHelper = new EasyGBC();
    public volatile boolean selectionRunning;
    private final ConcurrentHashMap<Node, NodePanel> nodeToPanel = new ConcurrentHashMap<>();
    private final JPanel filtersPanel = new JPanel(new GridBagLayout());
    private final EasyGBC filterHelper = new EasyGBC();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    private final Map<Class<? extends Filter>, FilterPanel> filterPanels = new HashMap<>();
    private int maxSelectedNodeInfoShown;

    public NodeDetailPanel(final Manager manager) {
        super(manager, manager.option.MAX_SELECTED_NODE_INFO_SHOWN.getValue(), "nodes");
        maxSelectedNodeInfoShown = manager.option.MAX_SELECTED_NODE_INFO_SHOWN.getValue();
        init();
        revalidate();
        repaint();
    }


    private void init() {
        setLayout(new GridBagLayout());

        EasyGBC c = new EasyGBC();

        JPanel mainPanel = new JPanel();
        {
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.setBackground(UIColors.lightBackground);
            EasyGBC d = new EasyGBC();
            CollapsablePanel filters = new CollapsablePanel("Filters", filtersPanel, false);
            filters.setBackground(UIColors.lightBackground);
            mainPanel.add(filters, d.down().anchor("north").expandHoriz());
            mainPanel.add(createNodesPanel(), d.down().anchor("north").expandHoriz());
            mainPanel.add(Box.createVerticalGlue(), d.down().expandVert());
        }
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        scrollPane.getVerticalScrollBar().setBlockIncrement(10);
        add(scrollPane, c.down().anchor("west").expandBoth());
    }

    public void setupFilters(List<Filter<? extends Node>> nodeFilters) {
        for (Filter<? extends Node> filter : nodeFilters) {
            if (!filterPanels.containsKey(filter.getClass())) {
                FilterPanel<?> filterPanel = FilterPanel.createFilterPanel(filter, manager);
                if (filterPanel != null) {
                    filtersPanel.add(filterPanel, filterHelper.down().expandHoriz());
                    filterPanels.put(filter.getClass(), filterPanel);
                }
            } else {
                filterPanels.get(filter.getClass()).setFilter(filter);
            }
        }
        hideDisabledFilters();
    }

    public void hideDisabledFilters() {
        for (FilterPanel<?> filterPanel : filterPanels.values()) {
            filterPanel.setVisible(filterPanel.getFilter().isEnabled());
        }
    }

    private JPanel createNodesPanel() {
        nodesPanel = new JPanel();
        nodesPanel.setBackground(UIColors.lightBackground);
        nodesPanel.setLayout(new GridBagLayout());

        selectedNodes(CyTableUtil.getNodesInState(currentNetwork.getCyNetwork(), CyNetwork.SELECTED, true));

        nodesPanel.setAlignmentX(LEFT_ALIGNMENT);
        selectedNodes = new CollapsablePanel("Selected nodes info", nodesPanel, false);
        return selectedNodes;
    }


    public void networkChanged(Network newNetwork) {
        this.currentNetwork = newNetwork;
        selectedNodes(newNetwork.getSelectedCyNodes());
    }

    private Future<?> lastSelection;

    public void viewUpdated() {
        if (lastSelection != null) lastSelection.cancel(true);
        lastSelection = executor.submit(() -> {
            hideDisabledFilters();
            selectedNodes(currentNetwork.getSelectedCyNodes());
        });
    }


    public void selectedNodes(Collection<CyNode> cyNodes) {
        if (checkCurrentNetwork() && checkCurrentView()) {
            selectionRunning = true;

            maxSelectedNodeInfoShown = manager.option.MAX_SELECTED_NODE_INFO_SHOWN.getValue();
            List<Node> nodes = cyNodes.stream()
                    .map(currentNetwork::getNode)
                    .filter(Objects::nonNull)
                    .filter(node -> currentView.visibleNodes.contains(node))
                    .collect(Comparators.least(maxSelectedNodeInfoShown, Node::compareTo));

            for (Node node : nodes) {
                if (!selectionRunning) {
                    break;
                }

                nodeToPanel.computeIfAbsent(node, keyNode -> {
                    NodePanel nodePanel = new NodePanel(keyNode);
                    nodePanel.setAlignmentX(LEFT_ALIGNMENT);
                    nodesPanel.add(nodePanel, layoutHelper.anchor("west").down().expandHoriz());
                    return nodePanel;
                });

            }
            if (nodes.size() < maxSelectedNodeInfoShown) {
                nodesPanel.remove(limitExceededPanel);
            } else {
                limitExceededPanel.setLimit(maxSelectedNodeInfoShown);
                nodesPanel.add(limitExceededPanel, layoutHelper.anchor("west").down().expandHoriz());
            }
            HashSet<Node> unselectedNodes = new HashSet<>(nodeToPanel.keySet());
            unselectedNodes.removeAll(nodes);
            for (Node unselectedNode : unselectedNodes) {
                NodePanel nodePanel = nodeToPanel.get(unselectedNode);
                nodePanel.delete();
                nodesPanel.remove(nodePanel);
                nodeToPanel.remove(unselectedNode);
            }

            selectionRunning = false;
        }

        revalidate();
        repaint();
    }

    private class NodePanel extends CollapsablePanel {

        private final NodeFeatures nodeFeatures;
        final NodeDetails nodeDetails;
        final Node node;

        public NodePanel(Node node) {
            super("", !(selectedNodes == null || selectedNodes.collapseAllButton.isExpanded()));
            this.node = node;
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(UIColors.lightBackground);
            List<Feature> features = node.getFeatures();
            setHeader(new NodeSchematic(node, features, openBrowser));
            content.add(new NodeBasics(node, openBrowser));
            nodeFeatures = new NodeFeatures(node, features, openBrowser, true, null);
            content.add(nodeFeatures);
            content.add(new NodeIdentifiers(node, openBrowser));
            nodeDetails = new NodeDetails(node, openBrowser);
            content.add(nodeDetails);
        }

        public void delete() {
            nodeFeatures.deleteEdgeSelectionCheckboxes();
        }
    }
}



