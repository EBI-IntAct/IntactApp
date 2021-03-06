package uk.ac.ebi.intact.app.internal.ui.components.labels;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

public class JLabel2D extends JTextField {
    public static final int EFFECT_PLAIN = 0;

    public static final int EFFECT_GRADIENT = 1;

    public static final int EFFECT_IMAGE = 2;

    public static final int EFFECT_IMAGE_ANIMATION = 3;

    public static final int EFFECT_COLOR_ANIMATION = 4;

    protected int effectIndex = EFFECT_PLAIN;

    protected double shearFactor = 0.0;

    protected Color outlineColor;

    protected int stroke = 0;

    protected GradientPaint gradient;

    protected Image foregroundImage;

    protected boolean isRunning = false;

    protected int m_xShift;

    public JLabel2D(String text, int alignment) {
        super(text);
        setHorizontalAlignment(alignment);
        setOpaque(false);
        setBorder(null);
        setEditable(false);
        setFocusable(false);
    }

    public void setEffectIndex(int e) {
        effectIndex = e;
        repaint();
    }

    public int getEffectIndex() {
        return effectIndex;
    }

    public void setShearFactor(double s) {
        shearFactor = s;
        repaint();
    }

    public double getShearFactor() {
        return shearFactor;
    }

    public void setOutlineColor(Color c) {
        outlineColor = c;
        repaint();
    }

    public Color getOutlineColor() {
        return outlineColor;
    }

    public void setStroke(int s) {
        stroke = s;
        repaint();
    }

    public int getStroke() {
        return stroke;
    }


    public void setGradient(GradientPaint g) {
        gradient = g;
        repaint();
    }

    public GradientPaint getGradient() {
        return gradient;
    }

    public void setForegroundImage(Image img) {
        foregroundImage = img;
        repaint();
    }

    public Image getForegroundImage() {
        return foregroundImage;
    }


    public void paintComponent(Graphics g) {
        Dimension d = getSize();
        Insets ins = getInsets();
        int x = ins.left;
        int y = ins.top;
        int w = d.width - ins.left - ins.right;
        int h = d.height - ins.top - ins.bottom;

        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, d.width, d.height);
        }
        paintBorder(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(getText(), getFont(), frc);

        AffineTransform shear = AffineTransform.getShearInstance(shearFactor, 0.0);
        Shape src = tl.getOutline(shear);
        Rectangle rText = src.getBounds();

        float xText = x - rText.x;
        switch (getHorizontalAlignment()) {
            case CENTER:
                xText = x + (w - rText.width) / 2f;
                break;
            case RIGHT:
                xText = x + (w - rText.width);
                break;
        }
        float yText = y + h / 2f + tl.getAscent() / 3;

        AffineTransform shift = AffineTransform.getTranslateInstance(xText, yText);
        Shape shp = shift.createTransformedShape(src);

        if (outlineColor != null) {
            g2.setColor(outlineColor);
            if (stroke != 0) {
                g2.setStroke(new BasicStroke(stroke,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            g2.draw(shp);
        }

        switch (effectIndex) {
            case EFFECT_GRADIENT:
                if (gradient == null)
                    break;
                g2.setPaint(gradient);
                g2.fill(shp);
                break;

            case EFFECT_IMAGE:
                fillByImage(g2, shp, 0);
                break;

            case EFFECT_COLOR_ANIMATION:
                g2.setColor(getForeground());
                g2.fill(shp);
                break;

            case EFFECT_IMAGE_ANIMATION:
                if (foregroundImage == null)
                    break;
                int wImg = foregroundImage.getWidth(this);
                if (m_xShift > wImg)
                    m_xShift = 0;
                fillByImage(g2, shp, m_xShift - wImg);
                break;

            default:
                g2.setColor(getForeground());
                g2.fill(shp);
                break;
        }
    }

    protected void fillByImage(Graphics2D g2, Shape shape, int xOffset) {
        if (foregroundImage == null)
            return;
        int wImg = foregroundImage.getWidth(this);
        int hImg = foregroundImage.getHeight(this);
        if (wImg <= 0 || hImg <= 0)
            return;
        g2.setClip(shape);
        Rectangle bounds = shape.getBounds();
        for (int xx = bounds.x + xOffset; xx < bounds.x + bounds.width; xx += wImg)
            for (int yy = bounds.y; yy < bounds.y + bounds.height; yy += hImg)
                g2.drawImage(foregroundImage, xx, yy, this);
    }


}
