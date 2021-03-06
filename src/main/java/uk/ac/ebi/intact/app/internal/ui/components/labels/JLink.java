package uk.ac.ebi.intact.app.internal.ui.components.labels;

import org.cytoscape.util.swing.OpenBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class JLink extends JTextField {
    private static final long serialVersionUID = 8273875024682878518L;
    private final OpenBrowser openBrowser;
    private URI uri;
    private boolean visited = false;
    private final boolean bold;
    private static final Color baseColor = new Color(56, 76, 148);
    private static final Color visitedColor = new Color(39, 25, 91);
    private static final Color hoverColor = new Color(129, 20, 20);

    private static final Font DEFAULT_FONT;
    private static final Font BOLD_FONT;

    static {
        Font font = UIManager.getFont("Label.font");
        font =(font != null) ? font : new Font("SansSerif", Font.PLAIN, 11);
        Map<TextAttribute, Object> attributes =  new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
        DEFAULT_FONT = font.deriveFont(attributes);
        BOLD_FONT = DEFAULT_FONT.deriveFont(Font.BOLD);
    }

    public JLink(String text, String uri, final OpenBrowser openBrowser) {
        this(text, uri, openBrowser, false);
    }

    public JLink(String text, String uri, final OpenBrowser openBrowser, boolean bold) {
        super();
        this.openBrowser = openBrowser;
        this.bold = bold;
        setup(text, URI.create(uri));
    }

    public void setup(String t, URI u) {
        uri = u;
        setText(t);
        setToolTipText(uri.toString());

        setBackground(null);
        setEditable(false);
        setOpaque(false);
        setBorder(null);
        setForeground(baseColor);

        setFont(bold ? BOLD_FONT : DEFAULT_FONT);

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                open(uri);
                visited = true;
                setForeground(visitedColor);
            }

            public void mouseEntered(MouseEvent e) {
                // setText(text,false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setForeground(hoverColor);

            }

            public void mouseExited(MouseEvent e) {
                // setText(text,true);
                setCursor(Cursor.getDefaultCursor());
                if (!visited) {
                    setForeground(baseColor);
                } else {
                    setForeground(visitedColor);
                }
            }
        });
    }

    private void open(URI uri) {
        openBrowser.openURL(uri.toString());
    }

}
