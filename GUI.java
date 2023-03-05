import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.DoubleToIntFunction;

import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.*;

public class GUI extends JFrame {
    private JLabel photographLabel = new JLabel(); // container to hold a large
    private JButton[] button; // creates an array of JButtons
    private int[] buttonOrder = new int[4000]; // creates an array to keep up with the image order
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout3;

    private JPanel panelBottom1;
    private JPanel rightPanel;
    private JPanel leftPanel;
    // private JPanel bigPhotoPanel;
    private Double[][] intensityMatrix = new Double[4000][26];
    int picNo = 0;
    int imageCount = 1; // keeps up with the number of images displayed since the first page.
    int pageNo = 1;

    public static void main(String args[]) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GUI app = new GUI();
                app.setVisible(true);
            }
        });
    }
    public GUI(){
        setTitle("Icon Demo: Please Select an Image");
        panelBottom1 = new JPanel();
        rightPanel = new JPanel();
        leftPanel = new JPanel();
        // main photo grid
        gridLayout1 = new GridLayout(4, 5, 5, 5);
        // left panel button grid
        gridLayout2 = new GridLayout(15, 1, 15, 25);
        // right panel grid
        gridLayout3 = new GridLayout(2, 1, 5, 5);

        // set frame size
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // leftPanel.setBackground(Color.GREEN);
        // rightPanel.setBackground(Color.MAGENTA);
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
        leftPanel.setPreferredSize(new Dimension(getWidth() / 5, getHeight()));
        rightPanel.setPreferredSize(new Dimension(getWidth() * 4 / 5, getHeight()));

        rightPanel.setLayout(gridLayout3);
        rightPanel.setBackground(Color.decode("#FFFFFF"));
        panelBottom1.setLayout(gridLayout1);
        rightPanel.add(photographLabel);
        rightPanel.add(panelBottom1);

        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton previousPage = new JButton("Previous Page");
        previousPage.setForeground(Color.decode("#AC422A"));
        JButton nextPage = new JButton("Next Page");
        nextPage.setForeground(Color.decode("#AC422A"));
        JButton intensity = new JButton("Intensity");
        intensity.setForeground(Color.decode("#79B5E0"));
        JButton colorCode = new JButton("Color Code");
        colorCode.setForeground(Color.decode("#E18A8A"));
        JButton intensityAndColor = new JButton("Intensity + Color");
        intensityAndColor.setForeground(Color.decode("#66C698"));
        JCheckBox relavantBtn = new JCheckBox("relevant");
        relavantBtn.setForeground(Color.decode("#BB76BC"));

        leftPanel.setBackground(Color.decode("#FFFAF5"));
        leftPanel.setLayout(gridLayout2);
        leftPanel.add(nextPage);
        leftPanel.add(previousPage);
        leftPanel.add(intensity);
        leftPanel.add(colorCode);
        leftPanel.add(intensityAndColor);
        leftPanel.add(relavantBtn);

        setLocationRelativeTo(null);
        nextPage.addActionListener(new GUI.nextPageHandler());
        previousPage.addActionListener(new GUI.previousPageHandler());

        setSize(1100, 750);
        // this centers the frame on the screen
        setLocationRelativeTo(null);

        button = new JButton[4000];
        for (int i = 0; i < 4000; i++) {
            ImageIcon icon;
            icon = new ImageIcon(getClass().getResource("Frames/" + (i+ 1000)+ ".jpg"));

            if (icon != null) {
                button[i] = new JButton(icon);
                JButton btn = button[i];
                JCheckBox cb = new JCheckBox();
                btn.add(cb);
                cb.setVisible(false);
                button[i].addActionListener(new GUI.IconButtonHandler(i, icon));
                buttonOrder[i] = i+1000;
            }
        }
    }
    private class nextPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int endImage = imageCount + 20;
            if (endImage <= 101) {
                panelBottom1.removeAll();
                for (int i = imageCount; i < endImage; i++) {
                    imageButNo = buttonOrder[i];
                    panelBottom1.add(button[imageButNo]);
                    imageCount++;

                }

                panelBottom1.revalidate();
                panelBottom1.repaint();
            }
        }
    }
    private class previousPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int startImage = imageCount - 40;
            int endImage = imageCount - 20;
            if (startImage >= 1) {
                panelBottom1.removeAll();
                /*
                 * The for loop goes through the buttonOrder array starting with the startImage
                 * value
                 * and retrieves the image at that place and then adds the button to the
                 * panelBottom1.
                 */
                for (int i = startImage; i < endImage; i++) {
                    imageButNo = buttonOrder[i];
                    panelBottom1.add(button[imageButNo]);
                    imageCount--;

                }

                panelBottom1.revalidate();
                panelBottom1.repaint();
            }
        }

    }
    private class IconButtonHandler implements ActionListener {
        int pNo = 0;
        ImageIcon iconUsed;

        IconButtonHandler(int i, ImageIcon j) {
            pNo = i;
            iconUsed = j; // sets the icon to the one used in the button
        }

        public void actionPerformed(ActionEvent e) {
            photographLabel.setIcon(iconUsed);
            picNo = pNo;
        }

    }
}
