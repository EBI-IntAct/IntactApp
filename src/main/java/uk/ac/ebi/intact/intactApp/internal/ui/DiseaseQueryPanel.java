package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.model.EntityIdentifier;
import uk.ac.ebi.intact.intactApp.internal.model.Species;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetDiseaseTermsTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.GetEnrichmentTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetStringIDsFromDiseasesTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowEnrichmentPanelTaskFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: [Optional] Improve non-gui mode
public class DiseaseQueryPanel extends JPanel implements TaskObserver {
    final IntactManager manager;
    IntactNetwork intactNetwork = null;
    IntactNetwork initialIntactNetwork = null;
    JTextField searchTerms;
    JPanel mainSearchPanel;
    // JComboBox<Species> speciesCombo;
    JButton importButton;
    JButton backButton;
    SearchOptionsPanel optionsPanel;
    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat intFormatter = new DecimalFormat("#0");

    EntityIdentifier diseaseEntity = null;
    List<EntityIdentifier> entityList;
    Species species = Species.getSpecies("Homo sapiens");
    // Species species;

    private int confidence = 40;
    private int additionalNodes = 100;

    private boolean ignore = false;

    public DiseaseQueryPanel(final IntactManager manager) {
        super(new GridBagLayout());
        this.manager = manager;
        species = manager.getDefaultSpecies();
        confidence = (int) (manager.getDefaultConfidence() * 100);
        additionalNodes = manager.getDefaultMaxProteins();
        init();
    }

    public DiseaseQueryPanel(final IntactManager manager, IntactNetwork intactNetwork) {
        this(manager, intactNetwork, null, 40, 100);
    }


    public DiseaseQueryPanel(final IntactManager manager, IntactNetwork intactNetwork, String query) {
        this(manager, intactNetwork, query, 40, 100);
    }

    public DiseaseQueryPanel(final IntactManager manager, IntactNetwork intactNetwork, String query, SearchOptionsPanel searchOptions) {
        this(manager, intactNetwork, query, searchOptions.getConfidence(), searchOptions.getAdditionalNodes());
        boolean loadEnrichment = searchOptions.getLoadEnrichment();
        optionsPanel.setLoadEnrichment(loadEnrichment);
    }

    public DiseaseQueryPanel(final IntactManager manager, IntactNetwork intactNetwork, String query, int confidence, int additionalNodes) {
        super(new GridBagLayout());
        this.manager = manager;
        this.intactNetwork = intactNetwork;
        this.initialIntactNetwork = intactNetwork;
        this.confidence = confidence;
        this.additionalNodes = additionalNodes;
        init();
        if (query != null)
            searchTerms.setText(query);
    }

    public void doImport() {
        importButton.doClick();
    }

    private void init() {
        // Create the surrounding panel
        setPreferredSize(new Dimension(800, 600));
        EasyGBC c = new EasyGBC();

        // Create the search list panel
        mainSearchPanel = createSearchPanel();
        add(mainSearchPanel, c.down().expandBoth().insets(5, 5, 0, 5));

        optionsPanel = new SearchOptionsPanel(manager, false, true, false);
        optionsPanel.setMinimumSize(new Dimension(400, 150));
        optionsPanel.setConfidence(confidence);
        optionsPanel.setAdditionalNodes(additionalNodes);
        add(optionsPanel, c.down().expandHoriz().insets(5, 5, 0, 5));


        // Add Query/Cancel buttons
        JPanel buttonPanel = createControlButtons();
        add(buttonPanel, c.down().expandHoriz().insets(0, 5, 5, 5));
    }

    JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setPreferredSize(new Dimension(600, 400));
        fillSearchPanel(searchPanel);
        return searchPanel;
    }

    void fillSearchPanel(JPanel searchPanel) {
        String ttText = "<html>Enter disease name or partial name e.g.:" +
                "<dl><dd>cancer (matches various cancers)</dd>" +
                "<dd>demen (matches several forms of dementia)</dd>" +
                "<dd>als (matches various forms of amyotrophic lateral sclerosis)</dd></dl></html>";
        EasyGBC c = new EasyGBC();

        JLabel searchLabel = new JLabel("Enter disease term:");
        searchLabel.setToolTipText(ttText);
        c.noExpand().anchor("northwest").insets(0, 5, 0, 5);
        searchPanel.add(searchLabel, c);
        searchTerms = new JTextField();
        searchTerms.setToolTipText(ttText);
        searchTerms.addActionListener(new InitialAction());
        c.down().expandHoriz().insets(5, 10, 5, 10);
        searchPanel.add(searchTerms, c);
        JLabel filler = new JLabel();
        c.down().expandBoth().insets(5, 10, 5, 10);
        searchPanel.add(filler, c);
    }

    void replaceSearchPanel() {
        mainSearchPanel.removeAll();
        mainSearchPanel.revalidate();
        mainSearchPanel.repaint();
        mainSearchPanel.setLayout(new GridBagLayout());
        fillSearchPanel(mainSearchPanel);
        mainSearchPanel.revalidate();
        mainSearchPanel.repaint();
    }

    JPanel createControlButtons() {
        JPanel buttonPanel = new JPanel();
        BoxLayout layout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
        buttonPanel.setLayout(layout);
        JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        backButton = new JButton(new AbstractAction("Back") {
            @Override
            public void actionPerformed(ActionEvent e) {
                intactNetwork.reset();
                replaceSearchPanel();
                importButton.setEnabled(true);
                backButton.setEnabled(false);
                importButton.setAction(new InitialAction());
                getParent().revalidate();
            }
        });
        backButton.setEnabled(false);

        importButton = new JButton(new InitialAction());

        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalGlue());
        // buttonPanel.add(Box.createRigidArea(new Dimension(10,0)));
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(importButton);
        return buttonPanel;
    }


    void importNetwork(EntityIdentifier entity) {
        if (intactNetwork == null)
            intactNetwork = new IntactNetwork(manager);
        int confidence = optionsPanel.getConfidence();
        int limit = optionsPanel.getAdditionalNodes();
        manager.execute(new TaskIterator(new GetStringIDsFromDiseasesTask(intactNetwork, species, limit,
                confidence, entity.getIdentifier(),
                entity.getPrimaryName())), this);
        cancel();
    }

    public void createResolutionPanel() {
        mainSearchPanel.removeAll();
        revalidate();
        mainSearchPanel.setLayout(new GridBagLayout());
        EasyGBC c = new EasyGBC();

        {
            String label = "<html><b>Multiple possible matches for term:</b> ";
            label += "Select the best matching disease from the table";
            label += "</html>";

            JLabel lbl = new JLabel(label);
            c.anchor("northeast").expandHoriz();
            mainSearchPanel.add(lbl, c);
        }

        ResolveDiseaseTermsTableModel tableModel;
        {
            JPanel annPanel = new JPanel(new GridBagLayout());
            EasyGBC ac = new EasyGBC();

            tableModel = new ResolveDiseaseTermsTableModel(entityList);
            final JTable table = new JTable(tableModel);

            table.setRowSelectionAllowed(false);

            JScrollPane tableScroller = new JScrollPane(table);
            ac.right().expandBoth().insets(0, 5, 0, 5);
            annPanel.add(tableScroller, ac);

            c.down().expandBoth().insets(5, 0, 5, 0);
            mainSearchPanel.add(annPanel, c);
        }

        importButton.setAction(new ResolvedAction(tableModel));
        backButton.setEnabled(true);

        revalidate();
        importButton.setEnabled(true);
    }

    public void cancel() {
        intactNetwork = initialIntactNetwork;
        if (intactNetwork != null) intactNetwork.reset();
        replaceSearchPanel();
        importButton.setEnabled(true);
        backButton.setEnabled(false);
        importButton.setAction(new InitialAction());
        ((Window) getRootPane().getParent()).dispose();
    }

    @Override
    public void allFinished(FinishStatus finishStatus) {
        //
        if (optionsPanel.getLoadEnrichment()) {
            GetEnrichmentTaskFactory tf = new GetEnrichmentTaskFactory(manager, true);
            ShowEnrichmentPanelTaskFactory showTf = manager.getShowEnrichmentPanelTaskFactory();
            tf.setShowEnrichmentPanelFactory(showTf);
            TunableSetter setter = manager.getService(TunableSetter.class);
            Map<String, Object> valueMap = new HashMap<>();
            valueMap.put("cutoff", 0.05);
            TaskIterator newIterator =
                    setter.createTaskIterator(tf.createTaskIterator(manager.getCurrentNetwork()), valueMap);
            // System.out.println("stringNetwork network = "+stringNetwork.getNetwork());
            manager.execute(newIterator);
        }
    }

    @Override
    public void taskFinished(ObservableTask task) {
    }


    class InitialAction extends AbstractAction implements TaskObserver {
        public InitialAction() {
            super("Import");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Start our task cascade
            if (intactNetwork == null)
                intactNetwork = new IntactNetwork(manager);

            int taxon = species.getTaxId(); // Only supported for human right now;
            String terms = searchTerms.getText();
            if (terms == null || terms.length() == 0) {
                JOptionPane.showMessageDialog(null, "No terms were entered -- nothing to search for",
                        "Nothing entered", JOptionPane.ERROR_MESSAGE);
                return;
            }
            manager.info("Getting disease identifiers for terms: " + terms);

            // Launch a task to get the annotations.
            manager.execute(new TaskIterator(new GetDiseaseTermsTask(manager, taxon, terms)), this);
        }

        @Override
        public void allFinished(FinishStatus finishStatus) {
        }

        @Override
        public void taskFinished(ObservableTask task) {
            if (!(task instanceof GetDiseaseTermsTask)) {
                return;
            }
            GetDiseaseTermsTask annTask = (GetDiseaseTermsTask) task;

            entityList = annTask.getMatchedTerms();

            final int taxon = annTask.getTaxon();
            if (entityList == null || entityList.size() == 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(null, "Your query returned no results",
                                "No results", JOptionPane.ERROR_MESSAGE);
                    }
                });
                return;
            }
            // Always create the resolution panel
            if (entityList.size() == 1) {
                diseaseEntity = entityList.get(0);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        importNetwork(diseaseEntity);
                    }
                });
            } else {
                createResolutionPanel();
            }
        }
    }

    class ResolvedAction extends AbstractAction {
        final ResolveDiseaseTermsTableModel model;

        public ResolvedAction(final ResolveDiseaseTermsTableModel model) {
            super("Import");
            this.model = model;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int taxon = species.getTaxId();

            diseaseEntity = model.getSelectedEntity();

            importNetwork(diseaseEntity);

        }
    }

    static class ResolveDiseaseTermsTableModel extends AbstractTableModel {
        final List<EntityIdentifier> entityList;
        final Boolean[] selected;
        String[] columns = {"Select", "Matched name", "Primary name", "Type", "Identifier"};

        public ResolveDiseaseTermsTableModel(final List<EntityIdentifier> entityList) {
            this.entityList = entityList;
            selected = new Boolean[entityList.size()];
            selected[0] = Boolean.TRUE;
            for (int i = 1; i < entityList.size(); i++)
                selected[i] = Boolean.FALSE;
        }

        public EntityIdentifier getSelectedEntity() {
            for (int i = 0; i < entityList.size(); i++) {
                if (selected[i])
                    return entityList.get(i);
            }
            return null;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public int getRowCount() {
            return entityList.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EntityIdentifier eid = entityList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return selected[rowIndex];
                case 1:
                    return eid.getMatchedName();
                case 2:
                    return eid.getPrimaryName();
                case 3:
                    return "Disease";
                case 4:
                    return eid.getIdentifier();
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                case 2:
                case 3:
                case 4:
                    return String.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex != 0) {
                return;
            }
            Boolean bool = (Boolean) value;
            if (bool.equals(Boolean.FALSE)) {
                selected[rowIndex] = bool;
                return;
            }

            for (int i = 0; i < getRowCount(); i++)
                if (i != rowIndex)
                    setValueAt(Boolean.FALSE, i, 0);
            selected[rowIndex] = Boolean.TRUE;
            fireTableDataChanged();
        }
    }

}