package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.node;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorLegendEditor;
import uk.ac.ebi.intact.app.internal.ui.components.legend.NodeColorPicker;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.legend.panels.AbstractLegendPanel;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions.Taxons;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

public class NodeColorLegendPanel extends AbstractLegendPanel {
    public final Map<Long, NodeColorPicker> colorPickers = new HashMap<>();
    private static final ImageIcon add = IconUtils.createImageIcon("/Buttons/add.png");
    private final JButton addNodeColorButton = new JButton(add);
    private final JPanel addNodeColorPanel = new JPanel();

    public NodeColorLegendPanel(Manager manager, Network currentNetwork, NetworkView currentView) {
        super("<html>Node Color <em>~ Species</em></html>", manager, currentNetwork, currentView);
        createNodeColorLegend(Taxons.getSpecies());
        addSeparator();
        createNodeColorLegend(List.of(Taxons.CHEMICAL_SYNTHESIS));
        createUserDefinedNodeColors();
        addSeparator();
        createNodeColorLegend(Taxons.getKingdoms());
    }


    private void createNodeColorLegend(List<Taxons> taxons) {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setBackground(UIColors.lightBackground);

        taxons.forEach((taxon) -> {
            Map<Long, Paint> reference = (taxon.isSpecies) ? StyleMapper.taxIdToPaint : StyleMapper.kingdomColors;
            NodeColorPicker nodeColorPicker = new NodeColorPicker(taxon.descriptor, (Color) reference.get(taxon.taxId), taxon.isSpecies);
            nodeColorPicker.addColorChangedListener(e -> {
                manager.style.updateStylesColorScheme(taxon.taxId, e.newColor, true);
                reference.put(taxon.taxId, e.newColor);
            });
            colorPickers.put(taxon.taxId, nodeColorPicker);
            panel.add(nodeColorPicker, layoutHelper.down().anchor("west").expandHoriz());
        });
        content.add(panel, layoutHelper.down().anchor("west").expandHoriz());
    }

    private void createUserDefinedNodeColors() {
        JPanel userDefinedSpeciesPanel = new JPanel(new GridBagLayout());
        content.add(userDefinedSpeciesPanel, layoutHelper.down().expandHoriz());
        addNodeColorPanel.setBackground(UIColors.lightBackground);
        addNodeColorPanel.setLayout(new FlowLayout(FlowLayout.LEFT,4,2));

        addNodeColorButton.addActionListener(e -> {
            userDefinedSpeciesPanel.add(new NodeColorLegendEditor(currentNetwork, addNodeColorPanel), layoutHelper.down().expandHoriz().anchor("west"));
            if (currentNetwork.getNonDefinedTaxon().isEmpty())
                addNodeColorPanel.setVisible(false);
            revalidate();
            repaint();
        });

        addNodeColorButton.setBackground(UIColors.lightBackground);
        addNodeColorButton.setOpaque(true);
        addNodeColorButton.setBorder(new EmptyBorder(0, 0, 0, 0));
        addNodeColorPanel.add(addNodeColorButton);

        JLabel label = new JLabel("Add new node color legend");
        label.setBorder(new EmptyBorder(0, 4, 0, 0));
        addNodeColorPanel.add(label, layoutHelper);
        addNodeColorPanel.setVisible(false);
        new Thread(() -> {
            while (StyleMapper.speciesNotReady())
                TimeUtils.sleep(200);
            addNodeColorPanel.setVisible(true);
        }).start();

        content.add(addNodeColorPanel, layoutHelper.anchor("west").down().noExpand());
    }

    @Override
    public void filterCurrentLegend() {
        executor.execute(() -> {
            Set<Long> networkTaxIds = currentNetwork.getTaxIds();

            for (Long taxId : colorPickers.keySet()) {
                colorPickers.get(taxId).setVisible(
                        networkTaxIds.contains(taxId) ||
                                (StyleMapper.taxIdToChildrenTaxIds.containsKey(taxId) && CollectionUtils.anyCommonElement(networkTaxIds, StyleMapper.taxIdToChildrenTaxIds.get(taxId)))
                );
            }
            addNodeColorPanel.setVisible(!currentNetwork.getNonDefinedTaxon().isEmpty());
        });
    }
}
