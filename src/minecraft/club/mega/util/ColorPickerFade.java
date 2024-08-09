package club.mega.util;

import java.awt.*;
import javax.swing.*;

public class ColorPickerFade extends JPanel {
    private Color color;
    private JSlider redSlider;
    private JSlider greenSlider;
    private JSlider blueSlider;
    private JSlider alphaSlider;

    public ColorPickerFade(Color initialColor, int x, int y, int width, int height) {
        this.color = initialColor;

        setLayout(null);

        redSlider = createSlider("Red", color.getRed(), 10, 10, width - 20);
        greenSlider = createSlider("Green", color.getGreen(), 10, 40, width - 20);
        blueSlider = createSlider("Blue", color.getBlue(), 10, 70, width - 20);
        alphaSlider = createSlider("Alpha", color.getAlpha(), 10, 100, width - 20);

        add(redSlider);
        add(greenSlider);
        add(blueSlider);
        add(alphaSlider);

        setSize(width, height);
        setLocation(x, y);

        setVisible(true);
    }

    private JSlider createSlider(String name, int initialValue, int x, int y, int width) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 255, initialValue);
        slider.setName(name);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBounds(x, y, width, 30);
        slider.addChangeListener(e -> updateColor());
        return slider;
    }

    private void updateColor() {
        color = new Color(redSlider.getValue(), greenSlider.getValue(), blueSlider.getValue(), alphaSlider.getValue());
        repaint();
    }

    public Color getSelectedColor() {
        return color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(color);
        g.fillRect(10, 140, getWidth() - 20, 30);
    }
}
