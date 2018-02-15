package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

import static sample.Controller.image1;

public class Resize {
    @FXML private ImageView imageView;
    private static boolean isResizing = false;

    public void initialize(){
        BufferedImage im = resize(image1,2f,2f);

        Image img = SwingFXUtils.toFXImage(im, null);

        imageView.setImage(img);

    }

    public BufferedImage resize(BufferedImage image,float resizeXFactor, float resizeYFactor){
        if(!isResizing) {
            float newHeight = image.getHeight() * resizeYFactor;
            float newWidth = image.getWidth() * resizeXFactor;

            //Get image dimensions, and declare loop variables
            int w = image.getWidth(), h = image.getHeight(), i, j, c;
            //Obtain pointer to data for fast processing
            byte[] data = Controller.GetImageData(image);

            BufferedImage imageToReturn = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_3BYTE_BGR);

            byte[] newData = Controller.GetImageData(imageToReturn);

            float col;
            short datum = -1;
            //Shows how to loop through each pixel and colour
            //Try to always use j for loops in y, and i for loops in x
            //as this makes the code more readable
            for (j = 0; j < newHeight-1 ; j++) {
                for (i = 0; i < newWidth-1 ; i++) {
                    for (c = 0; c < 3; c++) {
                        //x and y are where we get the colour from.
                        float y = (float)j * (float)image.getHeight() / (float)newHeight;
                        float x = (float)i * (float)image.getWidth() / (float)newWidth;
                        col = data[c + 3 * ((int) x) + 3 * ((int) y) * w];
                        System.out.println("newData Length: " + newData.length);
                        newData[c + 3 * i + 3 * j * w] = (byte) col;
                    } // colour loop
                } // column loop
            } // row loop

            return imageToReturn;

        }else {
            //Return this if we are already resizing.
            BufferedImage alreadyRunning = new BufferedImage(-1, -1, -1);
            System.out.println("already resing");
            return alreadyRunning;
        }
    }


    public void setStageAndSetupListeners(Stage stage) {
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if(!Double.isNaN(oldVal.doubleValue())&&!Double.isNaN(newVal.doubleValue())) {

             /*   Double xScale = (Double) oldVal / (Double) newVal;

                BufferedImage im = resize(image1,xScale.floatValue(),1);
                if(im.getHeight()!=-1) {
                    Image img = SwingFXUtils.toFXImage(im, null);
                    imageView.setFitHeight(img.getHeight());
                    imageView.setFitWidth(img.getWidth());
                    imageView.setImage(img);
                }*/
            }
        });

      /*  stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if(!Double.isNaN(oldVal.doubleValue())&&!Double.isNaN(newVal.doubleValue())) {
                Double yScale = (Double) oldVal / (Double) newVal;

                BufferedImage im = resize(image1,1,yScale.floatValue());
                Image img = SwingFXUtils.toFXImage(im,null);
                imageView.setImage(img);



                System.out.println("y:" + yScale);
            }

        });*/
    }
}
