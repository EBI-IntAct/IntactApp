package uk.ac.ebi.intact.intactApp.internal.ui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Map;

class SelectEverythingAction extends AbstractAction {
    final Map<String, ResolveTableModel> tableModelMap;


    public SelectEverythingAction(final Map<String, ResolveTableModel> tableModelMap) {
        super("Select Everything");
        this.tableModelMap = tableModelMap;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (String term : tableModelMap.keySet()) {
            ResolveTableModel rtm = tableModelMap.get(term);
            for (int i = 0; i < rtm.getRowCount(); i++) {
                rtm.setValueAt(true, i, 0);
            }
        }
    }
}
