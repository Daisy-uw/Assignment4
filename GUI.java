import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;

import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.*;

public class GUI extends JFrame {
    private JLabel photographLabel = new JLabel(); // container to hold a large
    private JButton[] button; // creates an array of JButtons
    private int[] buttonOrder; // creates an array to keep up with the image order
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout3;

    private JPanel panelBottom1;
    private JPanel rightPanel;
    private JPanel leftPanel;
    // private JPanel bigPhotoPanel;
    private int[][] intensityMatrix = new int[4000][25];
    private int SD[] = new int[3999];
    private HashMap<Integer, Integer> Cmap = new HashMap<>();
    private HashMap<Integer, Integer> Fmap = new HashMap<>();
    ArrayList<Integer> shots = new ArrayList<>();
    ArrayList<BufferedImage> frames = new ArrayList<>();
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
        setTitle("Icon Demo: Please Select a Frame");
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

        JButton playShot = new JButton("Play Shot");
        playShot.setForeground(Color.decode("#E079C6"));


        leftPanel.setBackground(Color.decode("#fbf5ff"));
        leftPanel.setLayout(gridLayout2);
        leftPanel.add(playShot);

        setLocationRelativeTo(null);
        playShot.addActionListener(new GUI.PlayShotHandler());

        setSize(1100, 750);
        // this centers the frame on the screen
        setLocationRelativeTo(null);


        readIntensityFile();
        computeSD();
        setThreshold();
        findCSet();
        findFset();
        getShot();
        getFirstFrames();
        saveImages();
        button = new JButton[frames.size()];
        buttonOrder = new int[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            ImageIcon icon;
            icon = new ImageIcon(getClass().getResource("Frames/image" + i+ ".jpg"));

            if (icon != null) {
                button[i ] = new JButton(icon);
                JButton btn = button[i];
                JCheckBox cb = new JCheckBox();
                btn.add(cb);
                cb.setVisible(false);
                button[i].addActionListener(new GUI.IconButtonHandler(i, icon));
                buttonOrder[i] = i;
            }
        }
        displayFirstPage();
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
    private class PlayShotHandler implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            int start_frame = shots.get(picNo); //first frame of the shot is the selected image
            int end_frame = Fmap.get(start_frame); // last frame of the shot
            System.out.println("Start frame = " + start_frame);
            System.out.println("End frame = " + end_frame);

            // Load the video file and check if it is opened
            VideoCapture capture = new VideoCapture(VideoFrameReader.videoPath);
            if (!capture.isOpened()) {
                System.err.println("Could not open video file.");
                System.exit(1);
            }

            //Set position of the video to start frame
            capture.set(Videoio.CAP_PROP_POS_FRAMES, start_frame);

            /*//Create a new window to show the shot
            HighGui.namedWindow("Video", HighGui.WINDOW_NORMAL);

           // Release the resources
            Mat mat = new Mat();
            for(int i = start_frame; i <=end_frame; i++){
                capture.read(mat);
                if(mat.empty()) break;
                HighGui.imshow("Video", mat);
                HighGui.waitKey(10);
            }
            capture.release();
            HighGui.destroyAllWindows();*/

            // Create a JFrame to display the video
            JFrame frame = new JFrame("Video Player");

            // Create a JLabel to hold the video frames
            JLabel label = new JLabel();
            frame.add(label);

            // Set the size of the JFrame
            frame.setSize(640, 480);

            // Show the JFrame
            frame.setVisible(true);

            // Loop through the video frames
            for (int i = start_frame; i <= end_frame; i++) {

                // Read the next frame from the video
                Mat frameMat = new Mat();
                capture.read(frameMat);

                // Resize the frame to fit the JLabel
                Mat resizedMat = new Mat();
                Size size = new Size(label.getWidth(), label.getHeight());
                Imgproc.resize(frameMat, resizedMat, size);

                MatOfByte matOfByte = new MatOfByte();
                Imgcodecs.imencode(".jpg", resizedMat, matOfByte);
                byte[] data = matOfByte.toArray();

                // Create a BufferedImage from the byte array
                BufferedImage image = null;
                try {
                    image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(data));
                } catch (java.io.IOException image_reading_err) {
                    image_reading_err.printStackTrace();
                }

                // Set the JLabel's icon to the converted BufferedImage
                ImageIcon icon = new ImageIcon(image);
                label.setIcon(icon);

                // Wait for a specified amount of time between each frame
                try {
                    Thread.sleep(40); // 25 frames per second
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

            }
        }
    }
    private void displayFirstPage() {
        int imageButNo = 0;
        panelBottom1.removeAll();
        for (int i = 0; i < frames.size(); i++) {
            imageButNo = buttonOrder[i];
            panelBottom1.add(button[imageButNo]);
            imageCount++;
        }
        panelBottom1.revalidate();
        panelBottom1.repaint();

    }
    public void readIntensityFile() {
        StringTokenizer token;
        Scanner read;
        int intensityBin;
        String line = "";
        int lineNumber = 0;
        try{
            read =new Scanner(new File ("intensity.txt"));
            while (read.hasNextLine()){
                line = read.nextLine();
                token = new StringTokenizer(line, " ");
                int i = 0;

                while (token.hasMoreTokens()){
                    intensityBin = Integer.parseInt(token.nextToken());
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
        for(int i = 0; i < SD.length; i++){
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
        int sum = 0;
        for (int i = 0; i < SD.length; i++){
            sum += SD[i];
        }
        int SD_mean = sum/SD.length;
        System.out.println("SD mean = " + SD_mean);

        //Step 2: find std(SD)
        double std = 0;
        for(int i = 0; i < SD.length; i++){
            std += Math.pow(SD[i] - SD_mean, 2);
        }
        std = Math.sqrt(std/(SD.length));
        System.out.println("std = " + std);

        //Step 3: Set Tb, Ts
        Tb = SD_mean + std * 11;
        Ts = SD_mean * 2;
        System.out.println("Tb = " + Tb);
        System.out.println("Ts = " + Ts);
    }
    public void findCSet(){
        //If SD[i] >= Tb then cut start at i and end at i+1
        System.out.println("--------Ce values---------");
        for(int i = 0; i < SD.length; i++){
            if(SD[i] >= Tb) {
                Cmap.put(i+1000, i+1+1000); //frame start from 1000
                System.out.println((i+1+1000));
            }
        }

    }

    public void findFset(){
        /*  Tor = 2
        *   If Ts <= SD[i] < Tb, consider it as potential start
        *   of a gradual transition. The end frame of the transition is
        *   detected when its next 2 consecutive values are lower than Ts
        *   or reaches a cut boundary
        */
        Map<Integer, Integer> temp = new HashMap<>();
        for(int i = 0; i < SD.length; i++){
            if(SD[i] >= Ts && SD[i] < Tb){

                int Fe = i;
                int j = i+1;
                while(j < SD.length){
                    if(SD[j] >= Tb) break;
                    else if(SD[j] < Ts){
                        if(j+ 1 >= SD.length || SD[j+1] < Ts )
                            break;
                    } else{
                        Fe = j;
                    }
                    j++;
                }
                temp.put(i, Fe);
                i = j;
            }
        }

        // For each Fs_candi and Fe_candi, check if it is a valid pair
        // then put on the Fmap.
        System.out.println("---------Fs + 1 values-------");
        for(int Fs: temp.keySet()){
            int Fe = temp.get(Fs);
            if(isValidGT(Fs, Fe)){
                Fmap.put(Fs + 1000, Fe + 1000);
                System.out.println((Fs + 1000 + 1));
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
        return true;
    }
    public void getShot(){
        //Store the index of first frame in each shot to ArrayList Shot
        //Show first frame of each shot: ( Ce ,Fs + 1) - the first frame of each shot
        //(Cs ,Fs) - the end frame of its previous shot)
        shots.add(1000); // adding the first frame
        for(int frame: Cmap.keySet()){
            shots.add(frame + 1);
            //System.out.println(frame+1);
        }
        for(int frame: Fmap.keySet()){
            shots.add(frame + 1);
            //System.out.println(frame+1);
        }
        Collections.sort(shots);

        //Update the Fmap to store the first and last frame of each shot
        // so when user chooses a frame (first frame), the program will
        // play the shot (from the first frame to last frame)
        Fmap.clear();
        for(int i = 0; i < shots.size() -1; i++){
            Fmap.put(shots.get(i), shots.get(i+1)-1);
        }
        //adding the last shot
        Fmap.put(shots.get(shots.size()-1),4999); // the last frame is 4999
    }
    private void getFirstFrames(){
        String videoPath = VideoFrameReader.videoPath;
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Create a VideoCapture object to read from the video file
        VideoCapture videoCapture = new VideoCapture(videoPath);

        // Check if the VideoCapture object was successfully opened
        if (!videoCapture.isOpened()) {
            System.out.println("Error opening video file");
            return;
        }else{
            Mat mat = new Mat();

            //get frames that have index stored in `shots` from the video
            int count = 0;
            int i = 0;
            while (videoCapture.read(mat) && i < shots.size()) {
                count++;
                if(count == shots.get(i)){
                    BufferedImage image = VideoFrameReader.MatToImage.convertMatToImage(mat);
                    frames.add(image);
                    i++;
                }
            }
        }
        System.out.println(shots.size() == frames.size());

    }
    private void saveImages(){
        File folder = new File ("Frames");
        if(!folder.exists()){
            folder.mkdirs();
        }
        for(int i = 0; i < shots.size(); i++){

            File outputFile = new File(folder, "image" + i + ".jpg");
            try {
                ImageIO.write(frames.get(i), "jpg", outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
