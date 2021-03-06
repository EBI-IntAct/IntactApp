package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.EvidenceViewTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.labels.SelectableLabel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;
import static uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils.groupElementsInPanel;

public class NodeFeatures extends AbstractNodeAttribute {
    private final static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private final List<Feature> features;
    private final boolean showFeatureEdge;
    private final SummaryEdge summaryEdge;
    private Map<FeatureClassifier.FeatureClass, List<Feature>> classification;

    public NodeFeatures(Node node, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, SummaryEdge summaryEdge) {
        this(node, features, openBrowser, showFeatureEdge, summaryEdge, lightBackground);
    }

    public NodeFeatures(Node node, List<Feature> features, OpenBrowser openBrowser, boolean showFeatureEdge, SummaryEdge summaryEdge, Color background) {
        super("Features summary", node, openBrowser);
        this.features = features;
        this.showFeatureEdge = showFeatureEdge;
        this.summaryEdge = summaryEdge;
        this.setBackground(background);
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(lightBackground);
        executor.execute(this::fillReportedFeatures);
    }

    private void fillReportedFeatures() {
        classification = FeatureClassifier.classify(features);
        boolean empty = true;
        for (FeatureClassifier.FeatureClass featureClass : FeatureClassifier.root) {
            CollapsablePanel featurePanel = recursivelyBuildFeatures(featureClass);
            if (featurePanel != null) {
                empty = false;
                content.add(featurePanel);
            }
        }
        if (empty) {
            setVisible(false);
        }

    }

    private CollapsablePanel recursivelyBuildFeatures(FeatureClassifier.FeatureClass featureClass) {
        if (featureClass instanceof FeatureClassifier.InnerFeatureClass) {
            FeatureClassifier.InnerFeatureClass innerFeatureClass = (FeatureClassifier.InnerFeatureClass) featureClass;
            List<CollapsablePanel> subFeaturePanels = new ArrayList<>();
            for (FeatureClassifier.FeatureClass subFeatureClass : innerFeatureClass.subClasses) {
                CollapsablePanel subFeaturePanel = recursivelyBuildFeatures(subFeatureClass);
                if (subFeaturePanel != null) {
                    subFeaturePanels.add(subFeaturePanel);
                }
            }
            if (classification.containsKey(innerFeatureClass.nonDefinedLeaf)) {
                subFeaturePanels.add(createFeatureList(innerFeatureClass.nonDefinedLeaf, classification.get(innerFeatureClass.nonDefinedLeaf)));
            }
            if (!subFeaturePanels.isEmpty()) {
                VerticalPanel featureListPanel = new VerticalPanel(getBackground());
                for (CollapsablePanel subFeaturePanel : subFeaturePanels) {
                    featureListPanel.add(subFeaturePanel);
                }
                CollapsablePanel collapsablePanel = new CollapsablePanel("", featureListPanel, false);
                collapsablePanel.setHeader(getFeaturePanelTitle(featureClass));
                return collapsablePanel;
            }
            return null;
        } else if (classification.containsKey(featureClass)) {
            return createFeatureList(featureClass, classification.get(featureClass));
        } else {
            return null;
        }
    }

    private LinePanel getFeaturePanelTitle(FeatureClassifier.FeatureClass featureClass) {
        LinePanel title = new LinePanel(getBackground());
        if (featureClass.identifier != null) {
            title.add(new JLink(featureClass.name, featureClass.identifier.getUserAccessURL(), openBrowser));
        } else {
            title.add(new SelectableLabel(featureClass.name));
        }
        title.add(new SelectableLabel(String.format(" (%d)", getFeatureCount(featureClass))));
        return title;
    }

    private int getFeatureCount(FeatureClassifier.FeatureClass featureClass) {
        if (featureClass instanceof FeatureClassifier.InnerFeatureClass) {
            FeatureClassifier.InnerFeatureClass innerFeatureClass = (FeatureClassifier.InnerFeatureClass) featureClass;
            return innerFeatureClass.subClasses.stream().mapToInt(this::getFeatureCount).sum() + getFeatureCount(innerFeatureClass.nonDefinedLeaf);
        } else {
            if (classification.containsKey(featureClass)) return classification.get(featureClass).size();
            return 0;
        }
    }

    private CollapsablePanel createFeatureList(FeatureClassifier.FeatureClass featureClass, List<Feature> features) {
        VerticalPanel featureListPanel = new VerticalPanel(getBackground());
        if (showFeatureEdge) {
            groupElementsInPanel(featureListPanel, getBackground(), features, feature -> feature.type, openBrowser,
                    (featureTypePanel, featuresOfType) -> groupElementsInPanel(featureTypePanel, getBackground(), featuresOfType, feature -> feature.name,
                            (featureNamePanel, featuresOfName) -> {
                                for (Feature feature : featuresOfName) {
                                    List<EvidenceEdge> featureEdges = feature.getEdges();
                                    for (EvidenceEdge featureEdge : featureEdges) {
                                        LinePanel line = new LinePanel(getBackground());
                                        if (summaryEdge == null) { // Node features
                                            Node otherNode;
                                            if (node.equals(featureEdge.source)) {
                                                otherNode = featureEdge.target;
                                            } else if (node.equals(featureEdge.target)) {
                                                otherNode = featureEdge.source;
                                            } else {
                                                continue;
                                            }
                                            line.add(new SelectEdgeButton(featureEdge));
                                            line.add(new SelectableLabel("Observed on edge with " + otherNode.name + " (" + featureEdge.ac + ")"));
                                        } else { // Summary edge features
                                            if (summaryEdge.isSummarizing(featureEdge)) {
                                                line.add(LinkUtils.createEvidenceEdgeLink(openBrowser, featureEdge));
                                            }
                                        }
                                        featureNamePanel.add(line);
                                    }
                                }
                            }
                    )
            );
        } else { // Evidence edge features
            for (Feature feature : features) {
                LinePanel line = new LinePanel(getBackground());
                line.add(new SelectableLabel(feature.type + " (" + feature.name + ")"));
                line.add(Box.createHorizontalGlue());
                featureListPanel.add(line);
            }
        }

        CollapsablePanel collapsablePanel = new CollapsablePanel("", featureListPanel, true);
        collapsablePanel.setHeader(getFeaturePanelTitle(featureClass));
        return collapsablePanel;
    }

    public static Map<EvidenceEdge, List<SelectEdgeButton>> edgeToCheckBoxes = new HashMap<>();
    private final Map<SelectEdgeButton, EvidenceEdge> checkBoxes = new HashMap<>();

    private class SelectEdgeButton extends JCheckBox implements ItemListener {
        private boolean silenceListener = false;
        private final EvidenceEdge edge;

        public SelectEdgeButton(EvidenceEdge edge) {
            this.edge = edge;
            setSelected(edge.edgeRow.get(CyNetwork.SELECTED, Boolean.class));
            setToolTipText("Select edge");
            checkBoxes.put(this, edge);
            CollectionUtils.addToGroups(edgeToCheckBoxes, this, selectEdgeButton -> edge);
            addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (silenceListener) return;
            Manager manager = edge.getNetwork().manager;
            NetworkView currentView = manager.data.getCurrentNetworkView();
            if (currentView != null && currentView.getType() == NetworkView.Type.SUMMARY) {
                manager.utils.execute(new EvidenceViewTaskFactory(manager, true).createTaskIterator());
            }

            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            edge.getNetwork().getCyNetwork().getRow(edge.cyEdge).set(CyNetwork.SELECTED, selected);

            for (SelectEdgeButton checkBox : edgeToCheckBoxes.get(edge)) {
                checkBox.silenceListener = true;
                checkBox.setSelected(selected);
                checkBox.silenceListener = false;
            }
        }
    }

    public void deleteEdgeSelectionCheckboxes() {
        for (Map.Entry<SelectEdgeButton, EvidenceEdge> entry : checkBoxes.entrySet()) {
            JCheckBox selectCheckBox = entry.getKey();
            EvidenceEdge edge = entry.getValue();
            List<SelectEdgeButton> edgeCheckBoxes = edgeToCheckBoxes.get(edge);
            edgeCheckBoxes.remove(selectCheckBox);
            if (edgeCheckBoxes.isEmpty()) {
                edgeToCheckBoxes.remove(edge);
            }
        }
    }
}
