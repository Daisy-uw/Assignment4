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
    private Double[][] intensityMatrix = new Double[4000][25];
    private double SD[] = new double[3999];
    private HashMap<Integer, Integer> Cmap = new HashMap<>();
    private HashMap<Integer, Integer> Fmap = new HashMap<>();
    ArrayList<Integer> shot = new ArrayList<>();
    int picNo = 0;
    int imageCount = 1; // keeps up with the number of images displayed since the first page.
    int pageNo = 1;
    double Tb = 0;
    double Ts = 0;

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

        leftPanel.setBackground(Color.decode("#FFFAF5"));
        leftPanel.setLayout(gridLayout2);
        leftPanel.add(nextPage);
        leftPanel.add(previousPage);
        leftPanel.add(intensity);
        leftPanel.add(colorCode);
        leftPanel.add(intensityAndColor);

        setLocationRelativeTo(null);
        nextPage.addActionListener(new GUI.nextPageHandler());
        previousPage.addActionListener(new GUI.previousPageHandler());

        setSize(1100, 750);
        // this centers the frame on the screen
        setLocationRelativeTo(null);

        button = new JButton[4000];
        for (int i = 0; i < 4000; i++) {
            ImageIcon icon;
            icon = new ImageIcon(getClass().getResource("Frames/image" + i+ ".jpg"));

            if (icon != null) {
                button[i] = new JButton(icon);
                JButton btn = button[i];
                JCheckBox cb = new JCheckBox();
                btn.add(cb);
                cb.setVisible(false);
                button[i].addActionListener(new GUI.IconButtonHandler(i, icon));
                buttonOrder[i] = i;
            }
        }
        readIntensityFile();
        computeSD();
        setThreshold();
        findCSet();
        findFset();
        //getShot();
        displayFirstPage();
    }
    private void displayFirstPage() {
        int imageButNo = 0;
        panelBottom1.removeAll();
        for (int i = 1; i < 21; i++) {
            // System.out.println(button[i]);
            imageButNo = buttonOrder[i];
            panelBottom1.add(button[imageButNo]);
            imageCount++;
        }
        panelBottom1.revalidate();
        panelBottom1.repaint();

    }
    public void readIntensityFile() {
        // System.out.println("Hello");
        StringTokenizer token;
        Scanner read;
        Double intensityBin;
        String line = "";
        int lineNumber = 0;
        try{
            read =new Scanner(new File ("intensity.txt"));
            while (read.hasNextLine()){
                line = read.nextLine();
                token = new StringTokenizer(line, " ");
                int i = 0;

                while (token.hasMoreTokens()){
                    intensityBin = Double.parseDouble(token.nextToken());
                    intensityMatrix[lineNumber][i] = intensityBin;
                    i++;

                }
                lineNumber++;
            }
        } catch (FileNotFoundException EE) {
            System.out.println("The file intensity.txt does not exist");
        }

    }
    public void computeSD(){
        System.out.println("Compute SD -----------------");
        for(int i = 0; i < 3999; i++){
            SD[i] = dist(i);
        }
    }
    private int dist(int i){
        int sum = 0;
        for(int j = 0; j < 25; j++){
            sum += Math.abs(intensityMatrix[i+1][j] - intensityMatrix[i][j]);
        }
        return sum;
    }
    public void setThreshold(){
        // Tb = mean(SD) + std(SD) * 11
        // Ts = mean(SD) * 2

        //Step 1: find mean(SD)
        double sum = 0;
        for (int i = 0; i < 3999; i++){
            sum += SD[i];
        }
        double SD_mean = sum/SD.length;
        System.out.println();
        System.out.println("SD mean = " + SD_mean );

        //Step 2: find std(SD)
        double std = 0;
        for(int i = 0; i < 3999; i++){
            std += Math.pow(SD[i] - SD_mean, 2);
        }
        std = Math.sqrt(std/3998);
        System.out.println("std = " + std);

        //Step 3: Set Tb, Ts
        Tb = SD_mean + std * 11;
        Ts = SD_mean * 2;
        System.out.println("Tb = " + Tb);
        System.out.println("Ts = "+ Ts);
    }
    public void findCSet(){
        System.out.println("Find C Set---------------");
        //If SD[i] > Tb then cut start at i and end at i+1
        for(int i = 0; i < 3999; i++){
            if(SD[i] > Tb) {
                Cmap.put(i+1000, i+1+1000); //frame start from 1000
                System.out.print((i+1000)+ " ");
            }
        }
    }

    public void findFset(){
        System.out.println("\nFind F Set --------------");
        /*  Tor = 2
        *   If Ts <= SD[i] < Tb, consider it as potential start
        *   of a gradual transition. The end frame of the transition is
        *   detected when its next 2 consecutive values are lower than Ts
        *   or reaches a cut boundary
        */
        for(int i = 0; i < 3999; i++){
            if(SD[i] >= Ts && SD[i] < Tb){
                int count = 0;
                int Fe = i;
                int j = i+1;
                while(j < 3999){
                    if(SD[j] < Ts){
                        count++;
                        if(count == 2) break;
                    } else{
                        Fe = j;
                    }
                    j++;
                }
                if(i < Fe){
                    //Check if (i, Fe) is a valid gradual transition
                    if(isValidGT(i, Fe)){
                        Fmap.put(i + 1000, Fe + 1000);
                    }
                }
                i = j;
            }
        }
    }

    // This method is to check if an interval is
    // a real gradual transition
    private boolean isValidGT(int Fs, int Fe){
        double sum = 0;
        for(int i = Fs; i <= Fe; i++){
            sum += SD[i];
        }
        if(sum < Tb) return false;
        System.out.println("Fs = "+ Fs + " Fe = " + Fe );
        return true;
    }
    public void getShot(){
        //Store the index of first frame in each shot to map Shot
        //Show first frame of each shot: ( Ce ,Fs + 1) - the first frame of each shot
        //(Cs ,Fs) - the end frame of its previous shot)
        shot.add(1000); // adding the first frame
        shot.add(4999); //adding the last frame

    }

    private class nextPageHandler implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int imageButNo = 0;
            int endImage = imageCount + 20;
            if (endImage <= 4000) {
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
