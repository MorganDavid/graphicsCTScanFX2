package sample;


import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Arrays;

public class Controller {


    @FXML private Slider topSlider;
    @FXML private Slider frontSlider;
    @FXML private Slider sideSlider;

    @FXML private ChoiceBox choiceBoxMip;

    @FXML private ImageView topView;
    @FXML private ImageView sideView;
    @FXML private ImageView frontView;

    @FXML private CheckBox chkEqualise;

    static BufferedImage image1, image2, image3; //storing the image in memory
    static short cthead[][][]; //store the 3D volume data set
    static float cthead_equalised[][][];
    static short min, max; //min/max value in the 3D volume data set
    public static String choice = "ERROR";

    public void initialize() throws IOException {
        File file = new File("CThead");

        //Create a BufferedImage to store the image data
        image1=new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
        image2=new BufferedImage(256, 112, BufferedImage.TYPE_3BYTE_BGR);
        image3=new BufferedImage(256, 112, BufferedImage.TYPE_3BYTE_BGR);

        //Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find the equivalent in Java)
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        int i, j, k; //loop through the 3D data set

        min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
        short read; //value read in
        int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around

        cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
        cthead_equalised = new float[113][256][256]; //allocate the memory - note this is fixed for this data set
        //loop through the data reading it in
        for (k=0; k<113; k++) {
            for (j=0; j<256; j++) {
                for (i=0; i<256; i++) {
                    //because the Endianess is wrong, it needs to be read byte at a time and swapped
                    b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types (C++ is so much easier!)
                    b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types (C++ is so much easier!)
                    read=(short)((b2<<8) | b1); //and swizzle the bytes around
                    if (read<min) min=read; //update the minimum
                    if (read>max) max=read; //update the maximum
                    cthead[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
                }
            }
        }

        image1=new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
        image2=new BufferedImage(256, 112, BufferedImage.TYPE_3BYTE_BGR);
        image3=new BufferedImage(256, 112, BufferedImage.TYPE_3BYTE_BGR);

        topSlider.setMax(cthead.length-1);
        sideSlider.setMax(cthead[0][0].length-1);
        frontSlider.setMax(cthead[0].length-1);

        makeEventListeners();

        Histogram hist = new Histogram();
        hist.buildHistogram(cthead);
        hist.equalise();
        for (j=0; j<cthead.length; j++) {
            for (i = 0; i < cthead[0].length; i++) {
                for (int z = 0; z < cthead[0][0].length; z++) {
                    float s = hist.getMapping(cthead[j][i][z]+1117);
                    cthead_equalised[j][i][z] = (s);
                }
            }
        }

    }

    private void makeEventListeners(){
        topSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            image1 = getSlice((int)topSlider.getValue(),"TOP", image1);
            Image img = SwingFXUtils.toFXImage(image1,null);
            topView.setImage(img);
        });
        sideSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            image2 = getSlice((int)sideSlider.getValue(),"SIDE" ,image2);
            Image img = SwingFXUtils.toFXImage(image2,null);
            sideView.setImage(img);
        });
        frontSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            image3 = getSlice((int)frontSlider.getValue(),"FRONT" ,image3);
            Image img = SwingFXUtils.toFXImage(image3,null);
            frontView.setImage(img);
        });

    }

    public void openThumbnail(){
        choice = choiceBoxMip.getValue().toString().toUpperCase();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("thumbnails.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 1000,600);
            Stage stage = new Stage();

            //get status of choice box

            stage.setTitle("Thumbnail View");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't open Thumbnail window!");
        }
    }

    public void mipButton(){
        String choice = choiceBoxMip.getValue().toString().toUpperCase();

        if(choice.equals("FRONT")){
            image3 = MIP(image3,choice);
            Image img = SwingFXUtils.toFXImage(image3,null);
            frontView.setImage(img);
        }
        if(choice.equals("TOP")){
            image1 = MIP(image1,choice);
            Image img = SwingFXUtils.toFXImage(image1,null);
            topView.setImage(img);
        }
        if(choice.equals("SIDE")){
            image2 = MIP(image2,choice);
            Image img = SwingFXUtils.toFXImage(image2,null);
            sideView.setImage(img);
        }

    }

    /**
     * Gets the slice at the specified index.
     * @param slice slice to get
     * @param view TOP, FRONT, or SIDE. returns different views.
     * @param image The image reference to put the image in.
     * @return the final image.
     */
    private BufferedImage getSlice(int slice, String view, BufferedImage image){
        //Get image dimensions, and declare loop variables
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        //Obtain pointer to data for fast processing
        byte[] data = GetImageData(image);
        float col;
        float datum=-1;
        boolean isEqualising = chkEqualise.isSelected();
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                //set which slice we loop through based on the view paramter.
                if(view.equals("FRONT")){
                    if(!isEqualising) {
                        datum = cthead[j][slice][i];
                    }else{
                        datum = cthead_equalised[j][slice][i];
                    }
                }
                if(view.equals("TOP")){
                    if(!isEqualising) {
                        datum = cthead[slice][j][i];
                    }else{
                        datum = cthead_equalised[slice][j][i];
                    }
                }
                if(view.equals("SIDE")){
                    if(!isEqualising) {
                        datum = cthead[j][i][slice];
                    }else{
                        datum = cthead_equalised[j][i][slice];
                    }
                }
                //If we are equalising, the data is already converted to rgb, so no need to do it again.
                if(chkEqualise.isSelected()){
                    col=datum;
                }else {
                    col = (255.0f * ((float) datum - (float) min) / ((float) (max - min)));
                }
                for (c=0; c<3; c++) {
                    data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop
        return image;
    }

    /*
        This function will return a pointer to an array
        of bytes which represent the image data in memory.
        Using such a pointer allows fast access to the image
        data for processing (rather than getting/setting
        individual pixels)
    */
    public static byte[] GetImageData(BufferedImage image) {
        WritableRaster WR=image.getRaster();
        DataBuffer DB=WR.getDataBuffer();
        if (DB.getDataType() != DataBuffer.TYPE_BYTE)
            throw new IllegalStateException("That's not of type byte");

        return ((DataBufferByte) DB).getData();
    }

    /*
        This function shows how to carry out an operation on an image.
        It obtains the dimensions of the image, and then loops through
        the image carrying out the copying of a slice of data into the
		image.
    */
    public BufferedImage MIP(BufferedImage image, String view) {
        //Get image dimensions, and declare loop variables
        int w=image.getWidth(), h=image.getHeight(), i, j, c;
        //Obtain pointer to data for fast processing
        byte[] data = GetImageData(image);
        float col;
        short datum;
        int depth = -1;

        //Sets how far the ray should go back into the data based on the view selected.
        if (view.equals("FRONT")) {
            depth = cthead[0][0].length;
        }
        if (view.equals("SIDE")) {
            depth = cthead[0][0].length;
        }
        if (view.equals("TOP")) {
            depth = cthead.length;
        }

        //First two loops through every pixel on each slice, the internal loop finds the maximum of that pixel.
        for (j=0; j<h; j++) {
            for (i=0; i<w; i++) {
                short myMaximum = Short.MIN_VALUE;

                for(int y = 0; y < depth; y++){
                    short z = -1;
                    if(view.equals("FRONT")){
                        z=cthead[j][y][i];
                    }
                    if(view.equals("TOP")){
                        z=cthead[y][j][i];
                    }
                    if(view.equals("SIDE")){
                        z=cthead[j][i][y];
                    }

                    if(z > myMaximum){
                        myMaximum = z;
                    }
                }

                datum = myMaximum;

                col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                for (c=0; c<3; c++) {
                    //and now we are looping through the bgr components of the pixel
                    //set the colour component c of pixel (i,j)
                    data[c+3*i+3*j*w]=(byte) col;
                } // colour loop
            } // column loop
        } // row loop

        return image;
    }

    public void openResizeWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("resize.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 397, 397);
            Stage stage = new Stage();

            stage.setTitle("Resize View");
            stage.setScene(scene);
            stage.show();
         /*   Resize r = new Resize();
            r.resize_nearestNeighbour(getSlice(76,"TOP",image1),2,2);*/
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't open resize_nearestNeighbour window!");
        }
    }




}
