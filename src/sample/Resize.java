package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

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
        //Index will be -1 if oppened from the main menu, if oppened from thumbnail view the index will be equal to the index we want to display.
        if(index == -1){
            thisImage=image1;
        }else{
            thisImage=Thumbnails.arr.get(index);
            index=-1;
        }

        //initialise the view.
        BufferedImage im2 = resize(thisImage, (int) sldRX.getValue(), (int) sldRY.getValue());

        Image img2 = SwingFXUtils.toFXImage(im2, null);
        imageView.setFitHeight(-1);
        imageView.setFitWidth(-1);
        imageView.setImage(img2);

        sldRX.valueProperty().addListener((observable, oldValue, newValue) -> {
            BufferedImage im;
            if(!chkBilinear.isSelected()) {
                im = resize(thisImage,(float) sldRX.getValue(), (float) sldRY.getValue());
            }else{
                System.out.println(sldRX.getValue());
                im = bilinear(thisImage, (int) sldRX.getValue(), (int) sldRY.getValue());
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
                im = bilinear(thisImage, (int) sldRX.getValue(), (int) sldRY.getValue());
            }

            Image img = SwingFXUtils.toFXImage(im, null);
            imageView.setFitHeight(-1);
            imageView.setFitWidth(-1);
            imageView.setImage(img);
        });
    }

    //Nearest neighbour resizing.
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
                    //(x,y) is the pixel on the original image that we should set the pixel value for this (j,i) on the new image.
                    float y = (float) (j * image.getHeight() / newHeight);
                    float x = (float) (i * image.getWidth() / newWidth);

                    for (c = 0; c < 3; c++) {
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
    public static float interpolate(float v1, float v2, int y1, int y2, int    y){
       // Vector2D v = ( v1.add((v2.subtract(v1))) ).multiplyScalar((y-v1.getY())/(v2.getY()-v1.getY()));
        float v = (v1+(v2-v1)*(y-y1)/(y2-y1));
        return v;
    }

    private static BufferedImage bilinearInterpolation(BufferedImage newImage,float resizeXFactor, float resizeYFactor){
        float j,i,c;

        byte[] data = Controller.GetImageData(newImage);

        for (j = 0; j < newImage.getHeight()-1-resizeYFactor; j+=resizeYFactor) {
            for (i = 0; i < newImage.getWidth()-1-resizeXFactor; i+=resizeXFactor) {
                //Interpolate between every horizontal pixel.
                for(int z = 0; z<resizeXFactor; z++) {
                    for (c = 0; c < 3; c++) {
                        /*
                        //Attempting to make floats work.
                         float v1 = data[(int) (c + 3 * i + 3 * j * newImage.getWidth())] & 255;
                        float v2 = data[(int) (c + 3 * (i + resizeXFactor) + 3 * j * newImage.getWidth())] & 255;
                        byte v1blue = (byte) ((byte) ((byte) v1 << 24) & 255);
                        byte v1green = (byte) ((byte) ((byte) v1 << 12) &255);
                        byte v1red = (byte) ((byte) ((byte) v1 << 8) &255);

                        byte v2blue = (byte) ((byte) ((byte) v2 << 24) & 255);
                        byte v2green = (byte) ((byte) ((byte) v2 << 12) &255);
                        byte v2red = (byte) ((byte) ((byte) v2 << 8) &255);


                        byte blue = (byte) interpolate(v1blue, v2blue, (int) i, (int) (i + resizeXFactor), (int) (z + i));
                        byte green = (byte) interpolate(v1green, v2green, (int) i, (int) (i + resizeXFactor), (int) (z + i));
                        byte red = (byte) interpolate(v1red, v2red, (int) i, (int) (i + resizeXFactor), (int) (z + i));

                        //if (c == 0 && j < 550 && i < 550) System.out.println("i: " + i + " j: " + j + " z: " + z + " v1: " + v1 + " v2: " + v2 + " interpolated byte: " + (byte) col);

                        data[(int) (0 + 3 * (z + i) + 3 * j * newImage.getWidth())] = blue;
                        data[(int) (1 + 3 * (z + i) + 3 * j * newImage.getWidth())] = green;
                        data[(int) (2 + 3 * (z + i) + 3 * j * newImage.getWidth())] = red;
                        data[(int) (2 + 3 * (z + i) + 3 * j * newImage.getWidth())] = red;
                        byte v1blue = (byte) ((byte) ((byte) v1 << 24) & 255);
                        byte v1green = (byte) ((byte) ((byte) v1 << 12) &255);
                        byte v1red = (byte) ((byte) ((byte) v1 << 8) &255);

                         */
                        float v1 = data[(int) (c + 3 * i + 3 * j * newImage.getWidth())] & 255;
                        float v2 = data[(int) (c + 3 * (i + resizeXFactor) + 3 * j * newImage.getWidth())] & 255;

                        float col = interpolate(v1, v2, (int) i, (int) (i + resizeXFactor), (int) (z + i));

                        //if (c == 0 && j < 550 && i < 550) System.out.println("i: " + i + " j: " + j + " z: " + z + " v1: " + v1 + " v2: " + v2 + " interpolated byte: " + (byte) col);

                        data[(int) (c + 3 * (z + i) + 3 * j * newImage.getWidth())] = (byte) col;
                    }

                }
                //Interpolate between every vertical pixel.
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

    //Sets up a grid of pixels with resizeXFactor between every horizontal pixel and resizeYFactor between every vertical.
    public static BufferedImage bilinear(BufferedImage image,float resizeXFactor, float resizeYFactor){
        float newHeight = (int) (image.getHeight() * resizeYFactor);
        float newWidth = (int) (image.getWidth() * resizeXFactor);
        //Get image dimensions, and declare loop variables
        float w = image.getWidth(), h = image.getHeight(), i, j, c;
        //Obtain pointer to data for fast processing
        byte[] data = Controller.GetImageData(image);

        BufferedImage imageToReturn = new BufferedImage((int)newWidth, (int)newHeight, BufferedImage.TYPE_3BYTE_BGR);

        byte[] newData = Controller.GetImageData(imageToReturn);

        for (j = 0; j < image.getHeight()-resizeYFactor; j++) {
            for (i = 0; i < image.getWidth()-resizeXFactor; i++) {
                for (c = 0; c < 3; c++) {
                    float col = data[(int) (c + 3 * i + 3 * j * w)]&255;
                    newData[(int) (c + 3 * (int)(i*resizeXFactor) + 3 * (int)(j*resizeYFactor) * newWidth)] = (byte) col;
                }
            }
        }
        //Give this grid of pixels to the 2d interpolation function.
        return bilinearInterpolation(imageToReturn, resizeXFactor, resizeYFactor);
    }

}
