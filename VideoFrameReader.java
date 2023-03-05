import org.opencv.core.*;
import org.opencv.videoio.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class VideoFrameReader {
    double histogram[] = new double[26];
    double intensityMatrix[][] = new double[4000][26];
    private ArrayList<BufferedImage> frames = new ArrayList<>();
    static String videoPath = Paths.get(".").toAbsolutePath().normalize().toString() + "\\20020924_juve_dk_02a.avi";
   public VideoFrameReader(){
       getFrames();
       saveImages();
       readIntensity();
       System.out.println(intensityMatrix[0][0]);
       writeIntensity();
   }
   private void writeIntensity(){
       try {
           String path = Paths.get(".").toAbsolutePath().normalize().toString();
           String file_name = path + "/intensity.txt";
           FileWriter file = new FileWriter(file_name);
           BufferedWriter line = new BufferedWriter(file);

           for (int i = 0; i < 4000; i++) {
               for (int j = 0; j < 26; j++) {
                   line.write(String.valueOf(intensityMatrix[i][j]) + " ");
               }
               line.newLine();
           }

           line.close();
           file.close();
           System.out.println("intensity.txt file written successfully");
       } catch (IOException e) {
           System.out.println("Cannot create intensity.txt file" + e.getMessage());
       }
   }
    private void readIntensity(){
        for(int i = 0; i < frames.size(); i++){
            BufferedImage image = frames.get(i);
            int height = image.getHeight();
            int width = image.getWidth();
            countBin(image, height, width);
            for(int bin = 0; bin < 26; bin++){
                intensityMatrix[i][bin] = histogram[bin];
            }
        }
    }
    private void countBin(BufferedImage image, int height, int width) {
        histogram[0] = height * width;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color color = new Color(image.getRGB(i, j));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();

                // calculate the intensity and the bin number
                // store it into the intensityBins matrix
                double intensity = 0.2999 * red + 0.587 * green + 0.114 * blue;
                int bin = (int) intensity / 10 + 1;
                if (bin > 25) {
                    bin = 25;
                }
                histogram[bin]++;
            }
        }
    }

    /* This method is to read the video and get images from this
     * video and save all the images into 'frames' array list
     */
    private void getFrames(){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // Create a VideoCapture object to read from the video file
        VideoCapture videoCapture = new VideoCapture(videoPath);

        // Check if the VideoCapture object was successfully opened
        if (!videoCapture.isOpened()) {
            System.out.println("Error opening video file");
            return;
        }else{
            Mat mat = new Mat();

            //get frames 1000 to 4999 from the video
            int count = 0;
            while (videoCapture.read(mat)) {
                count++;
                if(count >= 1000 && count <= 4999){
                    BufferedImage image = MatToImage.convertMatToImage(mat);
                    frames.add(image);
                }
            }
        }
    }
    private void saveImages(){
        File folder = new File ("Frames");
        if(!folder.exists()){
            folder.mkdirs();
        }
        for(int i = 0; i < 4000; i++){

            File outputFile = new File(folder, "image" + i + ".jpg");
            try {
                ImageIO.write(frames.get(i), "jpg", outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*  This class is to convert a Mat object into
    *   an BufferredImage object
    */
    private static class MatToImage {
        static {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        }

        public static BufferedImage convertMatToImage(Mat mat) {
            int type = BufferedImage.TYPE_BYTE_GRAY;
            if (mat.channels() > 1) {
                type = BufferedImage.TYPE_3BYTE_BGR;
            }
            BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
            byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            if (pixels.length != mat.total() * mat.channels()) {
                throw new RuntimeException("Unexpected buffer size: " + pixels.length);
            }
            mat.get(0, 0, pixels);
            return image;
        }
    }
    public static void main(String[] args) {
        // Load the OpenCV native library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        new VideoFrameReader();

    }
}
