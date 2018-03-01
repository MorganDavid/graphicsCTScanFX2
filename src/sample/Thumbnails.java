package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class Thumbnails {
    static BufferedImage[] images;
    static short cthead[][][]; //store the 3D volume data set
    static float cthead_equalised[][][];
    static short min, max;

    @FXML ImageView imageView;

    public void initialize() {
        cthead = Controller.cthead;
        cthead_equalised = Controller.cthead_equalised;
        min = Controller.min;
        max = Controller.max;
        images = new BufferedImage[113];
        imageView.setFitWidth(-1);
        imageView.setFitHeight(-1);

        for(int s = 0; s<112; s++){
            image1 = getSlice(s, image1);

        }

        image1 = getSlice(image1);
        Image img = SwingFXUtils.toFXImage(image1,null);
        imageView.setImage(img);

    }

    /**
     * Gets the slice at the specified index.
     * @param slice slice to get
     * @param image The image reference to put the image in.
     * @return the final image.
     */
    private BufferedImage getSlice(int slice, BufferedImage image){
        //Get image dimensions, and declare loop variables
        int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
        //Obtain pointer to data for fast processing
        byte[] data = Controller.GetImageData(image);
        float col;
        float datum;
            for (j = 0; j < 255; j++) {
                for (i = 0; i < 255; i++) {
                    datum = cthead[slice][j][i];

                    col = (255.0f * ((float) datum - (float) min) / ((float) (max - min)));

                    for (c = 0; c < 3; c++) {
                        //and now we are looping through the bgr components of the pixel
                        //set the colour component c of pixel (i,j)
                        data[c + 3 * (i+slice*64) + 3 * (j+slice*64) * w] = (byte) col;
                    } // colour loop
                } // column loop
            } // row loop

        // System.out.println(Arrays.toString(hist.getHistogram()));
        return image;
    }

}
