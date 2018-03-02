package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.DuplicateFormatFlagsException;
import java.util.Vector;

import static sample.Controller.image1;

public class Resize {
    @FXML private ImageView imageView;
    @FXML private Slider sldRX;
    @FXML private Slider sldRY;
    @FXML private CheckBox chkBilinear;
    public static int index = -1;

    private static BufferedImage thisImage;


    public void initialize(){
        System.out.println("index is: " + index);
        //if we have oppened from thumbnail
        if(index == -1){
            thisImage=image1;
        }else{
            thisImage=Thumbnails.arr.get(index);
        }

        sldRX.valueProperty().addListener((observable, oldValue, newValue) -> {
            BufferedImage im;
            if(!chkBilinear.isSelected()) {
                im = resize(thisImage,(float) sldRX.getValue(), (float) sldRY.getValue());
            }else{
                im = bilinear(thisImage, (float) sldRX.getValue(), (float) sldRY.getValue());
            }
            Image img = SwingFXUtils.toFXImage(im, null);
            imageView.setFitHeight(-1);
            imageView.setFitWidth(-1);
            imageView.setImage(img);
        });
        sldRY.valueProperty().addListener((observable, oldValue, newValue) -> {
            BufferedImage im;
            if(!chkBilinear.isSelected()) {
                im = resize(thisImage,(float) sldRX.getValue(), (float) sldRY.getValue());
            }else{
                im = bilinear(thisImage, (float) sldRX.getValue(), (float) sldRY.getValue());
            }

            Image img = SwingFXUtils.toFXImage(im, null);
            imageView.setFitHeight(-1);
            imageView.setFitWidth(-1);
            imageView.setImage(img);
        });


    }

    public static BufferedImage resize(BufferedImage image,float resizeXFactor, float resizeYFactor){
            int newHeight = (int) (image.getHeight() * resizeYFactor);
            int newWidth = (int) (image.getWidth() * resizeXFactor);
            System.out.println("resizing to: " + newHeight + " by " + newWidth);
            //Get image dimensions, and declare loop variables
            int w = image.getWidth(), h = image.getHeight(), i, j, c;
            //Obtain pointer to data for fast processing
            byte[] data = Controller.GetImageData(image);

            BufferedImage imageToReturn = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_3BYTE_BGR);

            byte[] newData = Controller.GetImageData(imageToReturn);

            //Shows how to loop through each pixel and colour
            //Try to always use j for loops in y, and i for loops in x
            //as this makes the code more readable
            for (j = 0; j < newHeight; j++) {
                for (i = 0; i < newWidth ; i++) {
                    float y = (float) (j * image.getHeight() / newHeight);
                    float x = (float) (i * image.getWidth() / newWidth);

                    for (c = 0; c < 3; c++) {
                        //x and y are where we get the colour from.
                        //System.out.println("newData Length: " + newData.length);
                        int col =  data[(int) (c + 3 * x + 3 * y * w)];
                        newData[c + 3 * i + 3 * j * newWidth] =  (byte)col;
                    } // colour loop
                } // column loop
            } // row loop

            return imageToReturn;
    }

    /**
     * one dimensional interpolation. Can be used for x or y, just imagine the y being x everywhere.
     * @return
     */
    public float interpolate(float v1, float v2, int y1, int y2, int    y){
       // Vector2D v = ( v1.add((v2.subtract(v1))) ).multiplyScalar((y-v1.getY())/(v2.getY()-v1.getY()));
        float v = (v1+(v2-v1)*(y-y1)/(y2-y1));
        return v;
    }

    public BufferedImage bilinearInterpolation(BufferedImage newImage,float resizeXFactor, float resizeYFactor){
        float j,i,c;

        byte[] data = Controller.GetImageData(newImage);

        for (j = 0; j < newImage.getHeight()-1-resizeYFactor; j+=resizeYFactor) {
            for (i = 0; i < newImage.getWidth()-1-resizeXFactor; i+=resizeXFactor) {
                for(int z = 0; z<resizeXFactor; z++){
                    for (c = 0; c < 3; c++) {
                        float v1 = data[(int) (c + 3 * i + 3 * j * newImage.getWidth())]&255;
                        float v2 = data[(int) (c + 3 * (i+resizeXFactor) + 3 * j * newImage.getWidth())]&255;
                        float col = interpolate(v1,v2,(int)i,(int)(i+resizeXFactor), (int) (z+i));

                        if(c==0 && j<550 && i<550) System.out.println("i: " + i + " j: " + j + " z: " + z + " v1: " + v1 + " v2: " + v2 + " interpolated byte: " + (byte)col);
                        data[(int) (0 + 3 * (z+i) + 3 * j * newImage.getWidth())]=(byte)col;
                    }

                }

                for(int z = 0; z<resizeYFactor; z++){
                    for (c = 0; c < 3; c++) {
                        float v1 = data[(int) (c + 3 * i + 3 * j * newImage.getWidth())]&255;
                        float v2 = data[(int) (c + 3 * i + 3 * (j + resizeYFactor) * newImage.getWidth())]&255;
                        float col = interpolate(v1, v2, (int)j, (int) (j + resizeYFactor), (int)(z + j));
                        data[(int) (c + 3 * i + 3 * (j + z) * newImage.getWidth())] = (byte) (col);
                    }
                }
            }
        }
        //Second stage of interpolation, move through everything that isn't in the first grid.
        for (j = 0; j < newImage.getHeight()-resizeYFactor-1; j++) {
            for (i = 0; i < newImage.getWidth(); i++) {
                if(j%resizeYFactor==0 && i%resizeXFactor!=0) {
                    for(int z = 0; z<resizeYFactor; z++){
                        for (c = 0; c < 3; c++) {
                            float v1 = data[(int) (c + 3 * i + 3 * j * newImage.getWidth())]&255;
                            float v2 = data[(int) (c + 3 * i + 3 * (j + resizeYFactor) * newImage.getWidth())]&255;
                            float col = interpolate(v1, v2, (int)j, (int) (j + resizeYFactor), (int)(z + j));
                            data[(int) (c + 3 * i + 3 * (j + z) * newImage.getWidth())] = (byte) (col);
                        }
                    }
                }
            }
        }


        return newImage;
    }

    public BufferedImage bilinear(BufferedImage image,float resizeXFactor, float resizeYFactor){
        float newHeight = (int) (image.getHeight() * resizeYFactor);
        float newWidth = (int) (image.getWidth() * resizeXFactor);
        System.out.println("resizing to: " + newHeight + " by " + newWidth);
        //Get image dimensions, and declare loop variables
        float w = image.getWidth(), h = image.getHeight(), i, j, c;
        //Obtain pointer to data for fast processing
        byte[] data = Controller.GetImageData(image);

        BufferedImage imageToReturn = new BufferedImage((int)newWidth, (int)newHeight, BufferedImage.TYPE_3BYTE_BGR);

        byte[] newData = Controller.GetImageData(imageToReturn);

        for (j = 0; j < image.getHeight(); j++) {
            for (i = 0; i < image.getWidth(); i++) {
                for (c = 0; c < 3; c++) {
                    float col = data[(int) (c + 3 * i + 3 * j * w)];
                    newData[(int) (c + 3 * (int)(i*resizeXFactor) + 3 * (int)j*Math.round(resizeYFactor) * newWidth)] = (byte) col;
                }
            }
        }

        return bilinearInterpolation(imageToReturn, resizeXFactor, resizeYFactor);
    }

}
