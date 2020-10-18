package com.playtime;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.PluginErrorPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Slf4j
public class PlayTimePanel extends PluginPanel {
    private final static Color BACKGROUND_COLOR = ColorScheme.DARK_GRAY_COLOR;
    private final static Color BUTTON_HOVER_COLOR = ColorScheme.DARKER_GRAY_HOVER_COLOR;

    private final PlayTimePlugin plugin;
    private JLabel label;

    public PlayTimePanel(final PlayTimePlugin plugin)
    {
        super(false);
        this.plugin = plugin;

        this.setBackground(ColorScheme.DARK_GRAY_COLOR);
        this.setLayout(new BorderLayout());

        showView();
    }

    public void showView()
    {
        this.removeAll();

        final PluginErrorPanel errorPanel = new PluginErrorPanel();
        errorPanel.setBorder(new EmptyBorder(10, 25, 10, 25));
        errorPanel.setContent("Play Time", "Time played");

        JPanel panel = new JPanel();
        label = new JLabel("Total time: ?");
        panel.add(label);

        JPanel panel2 = new JPanel();
        JButton button = new JButton("Reset count");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plugin.resetCounter();
            }
        });
        panel2.add(button);

        this.add(errorPanel, BorderLayout.NORTH);
        this.add(wrapContainer(panel), BorderLayout.CENTER);
        this.add(wrapContainer(panel2), BorderLayout.SOUTH);

        this.revalidate();
        this.repaint();
    }

    public void updateTime(String time) {
        label.setText("Total time: " + time);
    }

    private JScrollPane wrapContainer(final JPanel container)
    {
        final JPanel wrapped = new JPanel(new BorderLayout());
        wrapped.add(container, BorderLayout.NORTH);
        wrapped.setBackground(BACKGROUND_COLOR);

        final JScrollPane scroller = new JScrollPane(wrapped);
        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroller.setBackground(BACKGROUND_COLOR);

        return scroller;
    }
}
