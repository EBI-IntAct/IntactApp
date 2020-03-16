package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.model.*;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm.TermCategory;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.EnrichmentSettingsTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.ExportEnrichmentTableTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.FilterEnrichmentTableTask;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TextIcon;
import uk.ac.ebi.intact.intactApp.internal.utils.ViewUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

// import org.jcolorbrewer.ColorBrewer;

public class EnrichmentCytoPanel extends JPanel
        implements CytoPanelComponent2, ListSelectionListener, ActionListener, RowsSetListener, TableModelListener {

    public final static String showTable = TermCategory.ALL.getTable();
    private static final Icon chartIcon = new ImageIcon(
            EnrichmentCytoPanel.class.getResource("/images/chart20.png"));
    private static final Icon icon = new TextIcon(IconUtils.ENRICH_LAYERS, IconUtils.getIconFont(20.0f), IconUtils.STRING_COLORS, 14, 14);
    final IntactManager manager;
    final CyColorPaletteChooserFactory colorChooserFactory;
    final Font iconFont;
    final String colEnrichmentTerms = "enrichmentTerms";
    final String colEnrichmentTermsPieChart = "enrichmentTermsPieChart";
    final String colEnrichmentPieChart = "enrichmentPieChart";
    final String butSettingsName = "Network-specific enrichment panel settings";
    final String butFilterName = "Filter enrichment table";
    final String butDrawChartsName = "Draw charts using default color palette";
    final String butResetChartsName = "Reset charts";
    final String butAnalyzedNodesName = "Select all analyzed nodes";
    final String butExportTableDescr = "Export enrichment table";
    Map<String, JTable> enrichmentTables;
    JPanel topPanel;
    JPanel mainPanel;
    JScrollPane scrollPane;
    boolean clearSelection = false;
    // JComboBox<String> boxTables;
    List<String> availableTables;
    // boolean createBoxTables = true;
    JButton butSettings;
    JButton butDrawCharts;
    JButton butResetCharts;
    JButton butAnalyzedNodes;
    JButton butExportTable;
    JButton butFilter;
    JLabel labelPPIEnrichment;
    JLabel labelRows;
    JMenuItem menuItemReset;
    JPopupMenu popupMenu;
    EnrichmentTableModel tableModel;

    public EnrichmentCytoPanel(IntactManager manager, boolean noSignificant) {
        this.manager = manager;
        enrichmentTables = new HashMap<>();
        this.setLayout(new BorderLayout());
        IconManager iconManager = manager.getService(IconManager.class);
        colorChooserFactory = manager.getService(CyColorPaletteChooserFactory.class);
        iconFont = iconManager.getIconFont(22.0f);
        initPanel(noSignificant);
    }

    public String getIdentifier() {
        return "uk.ac.ebi.intact.intactApp.Enrichment";
    }

    public Component getComponent() {
        return this;
    }

    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.SOUTH;
    }

    public String getTitle() {
        return "STRING Enrichment";
    }

    public Icon getIcon() {
        return icon;
    }

    public EnrichmentTableModel getTableModel() {
        return tableModel;
    }

    // table selection handler
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;

        CyNetwork network = manager.getCurrentNetwork();
        if (network == null)
            return;
        // TODO: clear table selection when switching
        JTable table = enrichmentTables.get(showTable);
        if (table.getSelectedColumnCount() == 1 && table.getSelectedRow() > -1) {
            if (table.getSelectedColumn() != EnrichmentTerm.chartColumnCol) {
                // Only clear the network selection if it's our first selected row
                if (table.getSelectedRowCount() == 1)
                    clearNetworkSelection(network);
                for (int row : table.getSelectedRows()) {
                    Object cellContent =
                            table.getModel().getValueAt(table.convertRowIndexToModel(row),
                                    EnrichmentTerm.nodeSUIDColumn);
                    if (cellContent instanceof List) {
                        List<Long> nodeIDs = (List<Long>) cellContent;
                        for (Long nodeID : nodeIDs) {
                            network.getDefaultNodeTable().getRow(nodeID).set(CyNetwork.SELECTED, true);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int column = e.getColumn();
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null)
            return;

        updateLabelRows();
        // if (tableModel.getRowCount() != tableModel.getAllRowCount()) {
        //	System.out.println("Table got filtered from " + tableModel.getAllRowCount() + " to " + tableModel.getRowCount() + " rows." );
        // }

        if (column != EnrichmentTerm.chartColumnCol)
            return;
        // int row = e.getFirstRow();
        // TableModel model = (TableModel)e.getSource();
        // String columnName = model.getColumnName(column);
        // Object data = model.getValueAt(row, column);
        Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
        if (preselectedTerms.size() > 0) {
            ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
        }
    }

    // TODO: make this network-specific
    public void actionPerformed(ActionEvent e) {
        // if (e.getSource().equals(boxTables)) {
        // if (boxTables.getSelectedItem() == null) {
        // return;
        // }
        // // System.out.println("change selected table");
        // showTable = (String) boxTables.getSelectedItem();
        // // TODO: do some cleanup for old table?
        // createBoxTables = false;
        // initPanel();
        // createBoxTables = true;
        // } else
        TaskManager<?, ?> tm = manager.getService(TaskManager.class);
        CyNetwork network = manager.getCurrentNetwork();
        if (e.getSource().equals(butDrawCharts)) {
            resetCharts();
            // do something fancy here...
            // piechart: attributelist="test3" colorlist="modulated" showlabels="false"
            Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
            if (preselectedTerms.size() == 0) {
                preselectedTerms = getAutoSelectedTopTerms(manager.getTopTerms(network));
            }
            AvailableCommands availableCommands = manager
                    .getService(AvailableCommands.class);
            if (!availableCommands.getNamespaces().contains("enhancedGraphics")) {
                JOptionPane.showMessageDialog(null,
                        "Charts will not be displayed. You need to install enhancedGraphics from the App Manager or Cytoscape App Store.",
                        "No results", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
        } else if (e.getSource().equals(butResetCharts)) {
            // reset colors and selection
            resetCharts();
        } else if (e.getSource().equals(butAnalyzedNodes)) {
            List<CyNode> analyzedNodes = ModelUtils.getEnrichmentNodes(network);
            if (network == null || analyzedNodes == null)
                return;
            for (CyNode node : analyzedNodes) {
                network.getDefaultNodeTable().getRow(node.getSUID()).set(CyNetwork.SELECTED, true);
                // System.out.println("select node: " + nodeID);
            }
        } else if (e.getSource().equals(butFilter)) {
            // filter table
            tm.execute(new TaskIterator(new FilterEnrichmentTableTask(manager, this)));
        } else if (e.getSource().equals(butSettings)) {
            tm.execute(new TaskIterator(new EnrichmentSettingsTask(manager)));
        } else if (e.getSource().equals(butExportTable)) {
            if (network != null)
                tm.execute(new TaskIterator(new ExportEnrichmentTableTask(manager, network, this, ModelUtils.getEnrichmentTable(manager, network,
                        TermCategory.ALL.getTable()))));
        } else if (e.getSource().equals(menuItemReset)) {
            // System.out.println("reset color now");
            Component c = (Component) e.getSource();
            JPopupMenu popup = (JPopupMenu) c.getParent();
            JTable table = (JTable) popup.getInvoker();
            // System.out.println("action listener: " + table.getSelectedRow() + " : " + table.getSelectedColumn());
            if (table.getSelectedRow() > -1) {
                resetColor(table.getSelectedRow());
            }
        }
    }

    public void initPanel(boolean noSignificant) {
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null)
            return;
        initPanel(network, noSignificant);
    }

    public void initPanel(CyNetwork network, boolean noSignificant) {
        this.removeAll();

        Set<CyTable> currTables = ModelUtils.getEnrichmentTables(manager, network);
        availableTables = new ArrayList<>();
        for (CyTable currTable : currTables) {
            createJTable(currTable, ModelUtils.isCurrentDataVersion(network));
            availableTables.add(currTable.getTitle());
        }
        if (noSignificant) {
            mainPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Enrichment retrieval returned no results that met the criteria.",
                    SwingConstants.CENTER);
            mainPanel.add(label, BorderLayout.CENTER);
            this.add(mainPanel, BorderLayout.CENTER);
        } else if (availableTables.size() == 0) {
            mainPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("No enrichment has been retrieved for this network.",
                    SwingConstants.CENTER);
            mainPanel.add(label, BorderLayout.CENTER);
            this.add(mainPanel, BorderLayout.CENTER);
            // return;
        } else {
            // Collections.sort(availableTables);
            // boxTables = new JComboBox<String>(availableTables.toArray(new String[0]));
            // if (createBoxTables) {
            //	if (availableTables.contains(EnrichmentTerm.termTables[0])) {
            //		showTable = EnrichmentTerm.termTables[0];
            //	} else {
            //		showTable = availableTables.get(0);
            //	}
            //}
            // boxTables.setSelectedItem(showTable);
            // boxTables.addActionListener(this);

            JPanel buttonsPanelLeft = new JPanel(new GridLayout(1, 3));
            butFilter = new JButton(IconManager.ICON_FILTER);
            butFilter.setFont(iconFont);
            butFilter.addActionListener(this);
            butFilter.setToolTipText(butFilterName);
            butFilter.setBorderPainted(false);
            butFilter.setContentAreaFilled(false);
            butFilter.setFocusPainted(false);
            butFilter.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            butDrawCharts = new JButton(chartIcon);
            butDrawCharts.addActionListener(this);
            butDrawCharts.setToolTipText(butDrawChartsName);
            butDrawCharts.setBorderPainted(false);
            butDrawCharts.setContentAreaFilled(false);
            butDrawCharts.setFocusPainted(false);
            butDrawCharts.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2));


            butResetCharts = new JButton(IconManager.ICON_CIRCLE_O);
            butResetCharts.setFont(iconFont);
            butResetCharts.addActionListener(this);
            butResetCharts.setToolTipText(butResetChartsName);
            butResetCharts.setBorderPainted(false);
            butResetCharts.setContentAreaFilled(false);
            butResetCharts.setFocusPainted(false);
            butResetCharts.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 20));

            buttonsPanelLeft.add(butFilter);
            buttonsPanelLeft.add(butDrawCharts);
            buttonsPanelLeft.add(butResetCharts);

            JPanel buttonsPanelRight = new JPanel(new GridLayout(1, 3));
            butAnalyzedNodes = new JButton(IconManager.ICON_CHECK_SQUARE_O);
            butAnalyzedNodes.addActionListener(this);
            butAnalyzedNodes.setFont(iconFont);
            butAnalyzedNodes.setToolTipText(butAnalyzedNodesName);
            butAnalyzedNodes.setBorderPainted(false);
            butAnalyzedNodes.setContentAreaFilled(false);
            butAnalyzedNodes.setFocusPainted(false);
            butAnalyzedNodes.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            butExportTable = new JButton(IconManager.ICON_SAVE);
            butExportTable.addActionListener(this);
            butExportTable.setFont(iconFont);
            butExportTable.setToolTipText(butExportTableDescr);
            butExportTable.setBorderPainted(false);
            butExportTable.setContentAreaFilled(false);
            butExportTable.setFocusPainted(false);
            butExportTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

            butSettings = new JButton(IconManager.ICON_COG);
            butSettings.setFont(iconFont);
            butSettings.addActionListener(this);
            butSettings.setToolTipText(butSettingsName);
            butSettings.setBorderPainted(false);
            butSettings.setContentAreaFilled(false);
            butSettings.setFocusPainted(false);
            butSettings.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 20));

            buttonsPanelRight.add(butAnalyzedNodes);
            buttonsPanelRight.add(butExportTable);
            buttonsPanelRight.add(butSettings);

            JPanel panelMiddle = new JPanel(new BorderLayout());
            Double ppiEnrichment = ModelUtils.getPPIEnrichment(network);
            labelPPIEnrichment = new JLabel();
            if (ppiEnrichment != null) {
                labelPPIEnrichment = new JLabel("PPI Enrichment: " + ppiEnrichment.toString());
                labelPPIEnrichment.setToolTipText(
                        "<html>If the PPI enrichment is less or equal 0.05, your proteins have more interactions among themselves <br />"
                                + "than what would be expected for a random set of proteins of similar size, drawn from the genome. Such <br />"
                                + "an enrichment indicates that the proteins are at least partially biologically connected, as a group.</html>");
            }
            panelMiddle.add(labelPPIEnrichment, BorderLayout.WEST);
            // get the table
            JTable currentTable = enrichmentTables.get(showTable);
            // System.out.println("show table: " + showTable);
            if (tableModel != null) {
                tableModel.filter(manager.getCategoryFilter(network), manager.getRemoveOverlap(network), manager.getOverlapCutoff(network));
            }

            labelRows = new JLabel("");
            updateLabelRows();
            labelRows.setHorizontalAlignment(JLabel.RIGHT);
            Font labelFont = labelRows.getFont();
            labelRows.setFont(labelFont.deriveFont((float) (labelFont.getSize() * 0.8)));
            panelMiddle.add(labelRows, BorderLayout.EAST);

            topPanel = new JPanel(new BorderLayout());
            topPanel.add(buttonsPanelLeft, BorderLayout.WEST);
            topPanel.add(panelMiddle, BorderLayout.CENTER);
            topPanel.add(buttonsPanelRight, BorderLayout.EAST);
            // topPanel.add(boxTables, BorderLayout.EAST);
            this.add(topPanel, BorderLayout.NORTH);

            mainPanel = new JPanel(new BorderLayout());
            scrollPane = new JScrollPane(currentTable);
            mainPanel.setLayout(new GridLayout(1, 1));
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            this.add(mainPanel, BorderLayout.CENTER);

            // JScrollPane scrollPane2 = new JScrollPane(currentTable);
            // scrollPane.setViewportView(jTable);
            // scrollPane2.add(currentTable);

            // subPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(),
            // showTable, TitledBorder.CENTER, TitledBorder.TOP));
            // subPanel.add(scrollPane2);
            // mainPanel.add(subPanel, BorderLayout.CENTER);
        }

        this.revalidate();
        this.repaint();
    }

    private void createJTable(CyTable cyTable, boolean currentVersion) {
        if (currentVersion)
            tableModel = new EnrichmentTableModel(cyTable, EnrichmentTerm.swingColumnsEnrichment);
        else
            tableModel = new EnrichmentTableModel(cyTable, EnrichmentTerm.swingColumnsEnrichmentOld);
        JTable jTable = new JTable(tableModel);
        TableColumnModel tcm = jTable.getColumnModel();
        tcm.removeColumn(tcm.getColumn(EnrichmentTerm.nodeSUIDColumn));
        tcm.getColumn(EnrichmentTerm.fdrColumn).setCellRenderer(new DecimalFormatRenderer());
        // jTable.setDefaultEditor(Object.class, null);
        // jTable.setPreferredScrollableViewportSize(jTable.getPreferredSize());
        jTable.setFillsViewportHeight(true);
        jTable.setAutoCreateRowSorter(true);
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jTable.getSelectionModel().addListSelectionListener(this);
        jTable.getModel().addTableModelListener(this);
        jTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
        CyNetwork network = manager.getCurrentNetwork();
        jTable.setDefaultEditor(Color.class, new ColorEditor(manager, this, colorChooserFactory, network));
        popupMenu = new JPopupMenu();
        menuItemReset = new JMenuItem("Remove color");
        menuItemReset.addActionListener(this);
        popupMenu.add(menuItemReset);
        jTable.setComponentPopupMenu(popupMenu);
        jTable.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());
                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());
                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }
                }
            }
        });
        // jTable.addMouseListener(new TableMouseListener(jTable));

        enrichmentTables.put(cyTable.getTitle(), jTable);
    }

    private void clearNetworkSelection(CyNetwork network) {
        List<CyNode> nodes = network.getNodeList();
        clearSelection = true;
        for (CyNode node : nodes) {
            if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
                network.getRow(node).set(CyNetwork.SELECTED, false);
            }
        }
        clearSelection = false;
    }

    public void handleEvent(RowsSetEvent rse) {
        CyNetworkManager networkManager = manager.getService(CyNetworkManager.class);
        CyNetwork selectedNetwork = null;
        if (rse.containsColumn(CyNetwork.SELECTED)) {
            Collection<RowSetRecord> columnRecords = rse.getColumnRecords(CyNetwork.SELECTED);
            for (RowSetRecord rec : columnRecords) {
                CyRow row = rec.getRow();
                if (row.toString().contains("FACADE"))
                    continue;
                Long networkID = row.get(CyNetwork.SUID, Long.class);
                Boolean selectedValue = (Boolean) rec.getValue();
                if (selectedValue && networkManager.networkExists(networkID)) {
                    selectedNetwork = networkManager.getNetwork(networkID);
                }
            }
        }
        if (selectedNetwork != null) {
            initPanel(selectedNetwork, false);
            return;
        }
        // experimental: clear term selection when all network nodes are unselected
        CyNetwork network = manager.getCurrentNetwork();
        JTable currentTable = enrichmentTables.get(showTable);
        if (!clearSelection && network != null && currentTable != null) {
            List<CyNode> nodes = network.getNodeList();
            for (CyNode node : nodes) {
                if (network.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
                    return;
                }
            }
            currentTable.clearSelection();
        }
    }

    public void resetColor(int currentRow) {
        JTable currentTable = enrichmentTables.get(showTable);
        // currentRow = currentTable.getSelectedRow();
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null || tableModel == null)
            return;
        CyTable enrichmentTable = ModelUtils.getEnrichmentTable(manager, network,
                TermCategory.ALL.getTable());
        Color color = (Color) currentTable.getModel().getValueAt(
                currentTable.convertRowIndexToModel(currentRow),
                EnrichmentTerm.chartColumnCol);
        String termName = (String) currentTable.getModel().getValueAt(
                currentTable.convertRowIndexToModel(currentRow),
                EnrichmentTerm.nameColumn);
        if (color == null || termName == null)
            return;

        //currentTable.getModel().setValueAt(Color.OPAQUE, currentTable.convertRowIndexToModel(currentRow),
        //		EnrichmentTerm.chartColumnCol);
        for (CyRow row : enrichmentTable.getAllRows()) {
            if (enrichmentTable.getColumn(EnrichmentTerm.colName) != null
                    && row.get(EnrichmentTerm.colName, String.class) != null
                    && row.get(EnrichmentTerm.colName, String.class).equals(termName)) {
                row.set(EnrichmentTerm.colChartColor, "");
            }
        }
        tableModel.fireTableDataChanged();

        // re-draw charts if the user changed the color
        Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
        if (preselectedTerms.size() > 0)
            ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
    }

    public void resetCharts() {
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null || tableModel == null)
            return;

        CyTable nodeTable = network.getDefaultNodeTable();
        // replace columns
        ModelUtils.replaceListColumnIfNeeded(nodeTable, String.class,
                EnrichmentTerm.colEnrichmentTermsNames);
        ModelUtils.replaceListColumnIfNeeded(nodeTable, Integer.class,
                EnrichmentTerm.colEnrichmentTermsIntegers);
        ModelUtils.replaceColumnIfNeeded(nodeTable, String.class,
                EnrichmentTerm.colEnrichmentPassthrough);

        // remove colors from table?
        CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
                TermCategory.ALL.getTable());
        if (currTable == null || currTable.getRowCount() == 0) {
            return;
        }
        for (CyRow row : currTable.getAllRows()) {
            if (currTable.getColumn(EnrichmentTerm.colChartColor) != null
                    && row.get(EnrichmentTerm.colChartColor, String.class) != null
                    && !row.get(EnrichmentTerm.colChartColor, String.class).equals("")) {
                row.set(EnrichmentTerm.colChartColor, "");
            }
        }
        // initPanel();
        tableModel.fireTableDataChanged();
    }

    public void drawCharts() {
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null)
            return;

        resetCharts();
        Map<EnrichmentTerm, String> preselectedTerms = getUserSelectedTerms();
        if (preselectedTerms.size() == 0) {
            preselectedTerms = getAutoSelectedTopTerms(manager.getTopTerms(network));
        }
        ViewUtils.drawCharts(manager, preselectedTerms, manager.getChartType(network));
    }

    public void updateLabelRows() {
        if (tableModel == null)
            return;
        String labelTxt = "";
        if (tableModel.getAllRowCount() != tableModel.getRowCount()) {
            labelTxt = tableModel.getRowCount() + " rows (" + tableModel.getAllRowCount() + " before filtering)";
            // System.out.println("filtered:" + labelTxt);
        } else {
            labelTxt = tableModel.getAllRowCount() + " rows";
            // System.out.println("total rows: " + labelTxt);
        }
        if (labelRows != null)
            labelRows.setText(labelTxt);
    }

    private Map<EnrichmentTerm, String> getUserSelectedTerms() {
        Map<EnrichmentTerm, String> selectedTerms = new LinkedHashMap<>();
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null)
            return selectedTerms;

        // Set<CyTable> currTables = ModelUtils.getEnrichmentTables(manager, network);
        // for (CyTable currTable : currTables) {
        CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
                TermCategory.ALL.getTable());
        // currTable.getColumn(EnrichmentTerm.colShowChart) == null ||
        if (currTable == null || currTable.getRowCount() == 0) {
            return selectedTerms;
        }
        for (CyRow row : currTable.getAllRows()) {
            if (currTable.getColumn(EnrichmentTerm.colChartColor) != null
                    && row.get(EnrichmentTerm.colChartColor, String.class) != null
                    && !row.get(EnrichmentTerm.colChartColor, String.class).equals("")
                    && !row.get(EnrichmentTerm.colChartColor, String.class).equals("#ffffff")) {
                String selTerm = row.get(EnrichmentTerm.colName, String.class);
                if (selTerm != null) {
                    EnrichmentTerm enrTerm = new EnrichmentTerm(selTerm, 0,
                            row.get(EnrichmentTerm.colDescription, String.class),
                            row.get(EnrichmentTerm.colCategory, String.class), -1.0, -1.0,
                            row.get(EnrichmentTerm.colFDR, Double.class), row.get(EnrichmentTerm.colGenesBG, Integer.class));
                    enrTerm.setNodesSUID(row.getList(EnrichmentTerm.colGenesSUID, Long.class));
                    selectedTerms.put(enrTerm, row.get(EnrichmentTerm.colChartColor, String.class));
                }
            }
        }
        // System.out.println(selectedTerms);
        return selectedTerms;
    }

    private Map<EnrichmentTerm, String> getAutoSelectedTopTerms(int termNumber) {
        Map<EnrichmentTerm, String> selectedTerms = new LinkedHashMap<>();
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null || tableModel == null)
            return selectedTerms;

        CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
                TermCategory.ALL.getTable());
        if (currTable == null || currTable.getRowCount() == 0) {
            return selectedTerms;
        }

        // List<CyRow> rows = currTable.getAllRows();
        Color[] colors = manager.getEnrichmentPalette(network).getColors(manager.getTopTerms(network));
        Long[] rowNames = tableModel.getRowNames();
        for (int i = 0; i < manager.getTopTerms(network); i++) {
            if (i >= rowNames.length)
                continue;
            CyRow row = currTable.getRow(rowNames[i]);
            String selTerm = row.get(EnrichmentTerm.colName, String.class);
            if (selTerm != null) {
                EnrichmentTerm enrTerm = new EnrichmentTerm(selTerm, 0,
                        row.get(EnrichmentTerm.colDescription, String.class),
                        row.get(EnrichmentTerm.colCategory, String.class), -1.0, -1.0,
                        row.get(EnrichmentTerm.colFDR, Double.class), row.get(EnrichmentTerm.colGenesBG, Integer.class));
                enrTerm.setNodesSUID(row.getList(EnrichmentTerm.colGenesSUID, Long.class));
                String color = String.format("#%02x%02x%02x", colors[i].getRed(), colors[i].getGreen(),
                        colors[i].getBlue());
                row.set(EnrichmentTerm.colChartColor, color);
                selectedTerms.put(enrTerm, color);
            }
        }
        // initPanel();
        tableModel.fireTableDataChanged();
        return selectedTerms;
    }

    public CyTable getFilteredTable() {
        //Map<EnrichmentTerm, String> selectedTerms = new LinkedHashMap<EnrichmentTerm, String>();
        CyNetwork network = manager.getCurrentNetwork();
        if (network == null || tableModel == null)
            //return selectedTerms;
            return null;

        CyTable currTable = ModelUtils.getEnrichmentTable(manager, network,
                TermCategory.ALL.getTable());

        if (currTable == null || currTable.getRowCount() == 0) {
            return null;
        }

        CyTableFactory tableFactory = manager.getService(CyTableFactory.class);
        CyTableManager tableManager = manager.getService(CyTableManager.class);
        CyTable filtTable = tableFactory.createTable(TermCategory.ALLFILTERED.getTable(), EnrichmentTerm.colID, Long.class, false,
                true);
        filtTable.setSavePolicy(SavePolicy.DO_NOT_SAVE);
        tableManager.addTable(filtTable);
        ModelUtils.setupEnrichmentTable(filtTable);

        Long[] rowNames = tableModel.getRowNames();
        for (Long rowName : rowNames) {
            CyRow row = currTable.getRow(rowName);
            CyRow filtRow = filtTable.getRow(rowName);
            filtRow.set(EnrichmentTerm.colName, row.get(EnrichmentTerm.colName, String.class));
            filtRow.set(EnrichmentTerm.colIDPubl, "");
            filtRow.set(EnrichmentTerm.colYear, row.get(EnrichmentTerm.colYear, Integer.class));
            filtRow.set(EnrichmentTerm.colDescription, row.get(EnrichmentTerm.colDescription, String.class));
            filtRow.set(EnrichmentTerm.colCategory, row.get(EnrichmentTerm.colCategory, String.class));
            filtRow.set(EnrichmentTerm.colFDR, row.get(EnrichmentTerm.colFDR, Double.class));
            filtRow.set(EnrichmentTerm.colGenesBG, row.get(EnrichmentTerm.colGenesBG, Integer.class));
            filtRow.set(EnrichmentTerm.colGenesCount, row.get(EnrichmentTerm.colGenesCount, Integer.class));
            filtRow.set(EnrichmentTerm.colGenes, row.getList(EnrichmentTerm.colGenes, String.class));
            filtRow.set(EnrichmentTerm.colGenesSUID, row.getList(EnrichmentTerm.colGenesSUID, Long.class));
            filtRow.set(EnrichmentTerm.colNetworkSUID, row.get(EnrichmentTerm.colNetworkSUID, Long.class));
            // filtRow.set(EnrichmentTerm.colShowChart, false);
            filtRow.set(EnrichmentTerm.colChartColor, "");
        }
        return filtTable;
    }

    static class DecimalFormatRenderer extends DefaultTableCellRenderer {
        private static final DecimalFormat formatter = new DecimalFormat("0.#####E0");

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            try {
                if (value != null && (double) value < 0.001) {
                    value = formatter.format(value);
                }
            } catch (Exception ex) {
                // ignore and return original value
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                    column);
        }
    }
}