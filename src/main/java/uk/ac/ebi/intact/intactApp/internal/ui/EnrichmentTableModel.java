package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm.TermCategory;

import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

public class EnrichmentTableModel extends AbstractTableModel {
    private String[] columnNames;
    private CyTable cyTable;
    private Long[] rowNames;

    public EnrichmentTableModel(CyTable cyTable, String[] columnNames) {
        this.columnNames = columnNames;
        this.cyTable = cyTable;
        initData();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return rowNames.length;
    }

    public int getAllRowCount() {
        return cyTable.getRowCount();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Long[] getRowNames() {
        return rowNames;
    }

    public Object getValueAt(int row, int col) {
        final String colName = columnNames[col];
        final Long rowName = rowNames[row];
        // swingColumns = new String[] { colShowChart, colName, colDescription, colFDR,
        // colGenesCount, colGenes, colGenesSUID };
        //if (colName.equals(EnrichmentTerm.colShowChart)) {
        //	return cyTable.getRow(rowName).get(colName, Boolean.class);
        //} else
        switch (colName) {
            case EnrichmentTerm.colChartColor:
                String hexColor = cyTable.getRow(rowName).get(colName, String.class);
                if (hexColor != null && !hexColor.equals(""))
                    return Color.decode(hexColor);
                else
                    return Color.WHITE;
            case EnrichmentTerm.colFDR:
                return cyTable.getRow(rowName).get(colName, Double.class);
            case EnrichmentTerm.colGenesBG:
                return cyTable.getRow(rowName).get(colName, Integer.class);
            case EnrichmentTerm.colGenesCount:
                return cyTable.getRow(rowName).get(colName, Integer.class);
            case EnrichmentTerm.colGenesCountOld:
                return cyTable.getRow(rowName).get(colName, Integer.class);
            case EnrichmentTerm.colIDPubl:
                return cyTable.getRow(rowName).get(colName, String.class);
            case EnrichmentTerm.colYear:
                return cyTable.getRow(rowName).get(colName, Integer.class);
            case EnrichmentTerm.colGenes:
                return cyTable.getRow(rowName).getList(colName, String.class);
            case EnrichmentTerm.colGenesOld:
                return cyTable.getRow(rowName).getList(colName, String.class);
            case EnrichmentTerm.colGenesSUID:
                return cyTable.getRow(rowName).getList(colName, Long.class);
            default:
                return cyTable.getRow(rowName).get(colName, String.class);
        }
    }

    public Object getValueAt(int row, String colName) {
        // final String colName = columnNames[col];
        final Long rowName = rowNames[row];
        // swingColumns = new String[] { colShownChart, colName, colDescription, colFDR,
        // colGenesCount, colGenes, colGenesSUID };
        //if (colName.equals(EnrichmentTerm.colShowChart)) {
        //	return cyTable.getRow(rowName).get(colName, Boolean.class);
        //} else
        switch (colName) {
            case EnrichmentTerm.colChartColor:
                String hexColor = cyTable.getRow(rowName).get(colName, String.class);
                if (hexColor != null && !hexColor.equals(""))
                    return Color.decode(hexColor);
                else
                    return Color.WHITE;
            case EnrichmentTerm.colFDR:
                return cyTable.getRow(rowName).get(colName, Double.class);
            case EnrichmentTerm.colGenesBG:
            case EnrichmentTerm.colGenesCount:
            case EnrichmentTerm.colGenesCountOld:
            case EnrichmentTerm.colYear:
                return cyTable.getRow(rowName).get(colName, Integer.class);
            case EnrichmentTerm.colGenes:
            case EnrichmentTerm.colGenesOld:
                return cyTable.getRow(rowName).getList(colName, String.class);
            case EnrichmentTerm.colGenesSUID:
                return cyTable.getRow(rowName).getList(colName, Long.class);
            default:
                return cyTable.getRow(rowName).get(colName, String.class);
        }
    }

    public Class<?> getColumnClass(int c) {
        final String colName = columnNames[c];
        // return cyTable.getColumn(colName).getClass();
        // if (colName.equals(EnrichmentTerm.colShowChart)) {
        //	return Boolean.class;
        //} else
        switch (colName) {
            case EnrichmentTerm.colChartColor:
                return Color.class;
            case EnrichmentTerm.colFDR:
                return Double.class;
            case EnrichmentTerm.colGenesBG:
            case EnrichmentTerm.colGenesCount:
            case EnrichmentTerm.colGenesCountOld:
            case EnrichmentTerm.colYear:
                return Integer.class;
            case EnrichmentTerm.colGenes:
            case EnrichmentTerm.colGenesOld:
            case EnrichmentTerm.colGenesSUID:
                return List.class;
            case EnrichmentTerm.colIDPubl:
            default:
                return String.class;
        }
    }

    public boolean isCellEditable(int row, int col) {
        // columnNames[col].equals(EnrichmentTerm.colShowChart) ||
        return columnNames[col].equals(EnrichmentTerm.colChartColor);
    }

    public void setValueAt(Object value, int row, int col) {
        final String colName = columnNames[col];
        final Long rowName = rowNames[row];
        // if (colName.equals(EnrichmentTerm.colShowChart)) {
        // 	if (cyTable.getColumn(EnrichmentTerm.colShowChart) == null) {
        //		cyTable.createColumn(EnrichmentTerm.colShowChart, Boolean.class, false);
        //	}
        //	cyTable.getRow(rowName).set(colName, value);
        // }
        if (colName.equals(EnrichmentTerm.colChartColor)) {
            if (cyTable.getColumn(EnrichmentTerm.colChartColor) == null) {
                cyTable.createColumn(EnrichmentTerm.colChartColor, String.class, false);
            }
            try {
                Color color = (Color) value;
                String hexColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(),
                        color.getBlue());
                cyTable.getRow(rowName).set(colName, hexColor);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        fireTableCellUpdated(row, col);
    }

    // Filter the table
    public void filter(List<TermCategory> categories, boolean removeRedundancy, double cutoff) {
        List<CyRow> rows = cyTable.getAllRows();
        Long[] rowArray = new Long[rows.size()];
        int i = 0;
        for (CyRow row : rows) {
            String termCategory = row.get(EnrichmentTerm.colCategory, String.class);
            if (categories.size() == 0 || inCategory(categories, termCategory)) {
                rowArray[i] = row.get(EnrichmentTerm.colID, Long.class);
                i++;
            }
        }

        // Now we have the categories of interest.  Remove redundancy if desired
        if (removeRedundancy)
            rowNames = removeRedundancy(rowArray, i, cutoff);
        else
            rowNames = Arrays.copyOf(rowArray, i);

        fireTableDataChanged();
    }

    private boolean inCategory(List<TermCategory> categories, String termName) {
        for (TermCategory tc : categories) {
            if (tc.getName().equals(termName))
                return true;
        }
        return false;
    }

    private Long[] removeRedundancy(Long[] rowArray, int length, double cutoff) {
        // Sort by pValue
        Long[] sortedArray = pValueSort(rowArray, length);

        // Initialize with the most significant term
        List<Long> currentTerms = new ArrayList<>();
        currentTerms.add(sortedArray[0]);
        for (int i = 1; i < length; i++) {
            if (jaccard(currentTerms, sortedArray[i]) < cutoff)
                currentTerms.add(sortedArray[i]);
        }
        return (currentTerms.toArray(new Long[1]));
    }

    private Long[] pValueSort(Long[] rowArray, int length) {
        // Already sorted, I think...
        return Arrays.copyOf(rowArray, length);
    }

    // Two versions of jaccard similarity calculation.  This one
    // looks at the maximum jaccard between the currently selected
    // terms and the new term.
    private double jaccard(List<Long> currentTerms, Long term) {
        double maxJaccard = 0;
        for (Long currentTerm : currentTerms)
            maxJaccard = Math.max(maxJaccard, jaccard(currentTerm, term));
        return maxJaccard;
    }

    // This version of the jaccard calculation returns the jaccard between
    // all currently selected nodes and the nodes of the new term.
    private double jaccard2(List<Long> currentTerms, Long term) {
        Set<Long> currentNodes = new HashSet<>();
        for (Long currentTerm : currentTerms) {
            List<Long> nodes = cyTable.getRow(currentTerm).getList(EnrichmentTerm.colGenesSUID, Long.class);
            currentNodes.addAll(nodes);
        }
        List<Long> newNodes = cyTable.getRow(term).getList(EnrichmentTerm.colGenesSUID, Long.class);
        return jaccard2(currentNodes, newNodes);
    }

    private double jaccard2(Set<Long> currentNodes, List<Long> newNodes) {
        int intersection = 0;
        for (Long cn : newNodes) {
            if (currentNodes.contains(cn))
                intersection++;
        }
        double j = ((double) intersection) / (double) (currentNodes.size() + newNodes.size() - intersection);
        return j;
    }

    private double jaccard(Long currentTerm, Long term) {
        List<Long> currentNodes = cyTable.getRow(currentTerm).getList(EnrichmentTerm.colGenesSUID, Long.class);
        List<Long> newNodes = cyTable.getRow(term).getList(EnrichmentTerm.colGenesSUID, Long.class);

        int intersection = 0;
        for (Long cn : currentNodes) {
            if (newNodes.contains(cn))
                intersection++;
        }
        double j = ((double) intersection) / (double) (currentNodes.size() + newNodes.size() - intersection);
        return j;
    }

    private void initData() {
        List<CyRow> rows = cyTable.getAllRows();
        // Object[][] data = new Object[rows.size()][EnrichmentTerm.swingColumns.length];
        rowNames = new Long[rows.size()];
        int i = 0;
        for (CyRow row : rows) {
            rowNames[i] = row.get(EnrichmentTerm.colID, Long.class);
            i++;
        }
    }
}