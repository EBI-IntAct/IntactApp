package uk.ac.ebi.intact.app.internal.ui.components.legend;

import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.legend.shapes.Ball;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;


public class NodeColorPicker extends JPanel {
    protected String taxId;
    protected String descriptor;
    protected Color currentColor;
    protected EditableBall editableBall;
    protected boolean definedSpecies;
    protected final Manager manager;

    private final List<ColorChangedListener> listeners = new ArrayList<>();
    protected Font italicFont = new Font(getFont().getName(), Font.ITALIC, getFont().getSize());

    public NodeColorPicker(Manager manager) {
        this.manager = manager;
        setBackground(UIColors.lightBackground);
        setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
        addColorChangedListener(manager.style.settings);
    }

    public NodeColorPicker(Manager manager, String taxId, String descriptor, Color currentColor, boolean definedSpecies) {
        this(manager);
        this.taxId = taxId;
        this.descriptor = descriptor;
        this.currentColor = currentColor;
        this.definedSpecies = definedSpecies;
        init();
    }

    public void setCurrentColor(Color color) {
        currentColor = color;
        editableBall.setColor(color);
    }

    private void init() {
        editableBall = new EditableBall(currentColor, 30);
        editableBall.setBackground(UIColors.lightBackground);
        add(editableBall, BorderLayout.WEST);

        JLabel label = new JLabel(descriptor, SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBackground(UIColors.lightBackground);
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        if (definedSpecies) {
            label.setFont(italicFont);
        }
        add(label, BorderLayout.CENTER);

    }

    public boolean isDefinedSpecies() {
        return definedSpecies;
    }

    public void addColorChangedListener(ColorChangedListener listener) {
        listeners.add(listener);
    }

    public static class ColorChangedEvent extends AWTEvent {
        public final Color newColor;
        public final NodeColorPicker source;

        public ColorChangedEvent(NodeColorPicker source, int id, Color newColor) {
            super(source, id);
            this.source = source;
            this.newColor = newColor;
        }

        @Override
        public NodeColorPicker getSource() {
            return source;
        }
    }

    public interface ColorChangedListener extends EventListener {
        void colorChanged(ColorChangedEvent colorChangedEvent);
    }

    protected class EditableBall extends Ball {
        public MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Color selectedColor = JColorChooser.showDialog(NodeColorPicker.this, "Choose " + descriptor + " colors", currentColor);
                if (selectedColor != null) {
                    currentColor = selectedColor;
                    editableBall.setColor(currentColor);
                    for (ColorChangedListener listener : listeners) {
                        listener.colorChanged(new ColorChangedEvent(NodeColorPicker.this, ActionEvent.ACTION_PERFORMED, currentColor));
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        };

        public EditableBall(Color color, int diameter) {
            super(color, diameter);
            addMouseListener(mouseListener);
            setToolTipText("Change color");
        }
    }

    public String getTaxId() {
        return taxId;
    }

    public String getDescriptor() {
        return descriptor;
    }
}
