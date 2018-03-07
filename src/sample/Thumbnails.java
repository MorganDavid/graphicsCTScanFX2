package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static sample.Controller.GetImageData;

public class Thumbnails {
    static short cthead[][][]; //store the 3D volume data set
    static float cthead_equalised[][][];
    static short min, max;
    public static List<BufferedImage> arr;

    @FXML
    FlowPane flowPane;

    public void initialize() {
        cthead = Controller.cthead;
        cthead_equalised = Controller.cthead_equalised;
        min = Controller.min;
        max = Controller.max;

        arr = makeThumb(Controller.choice);

        int noOfThumbs = -1;
        if(Controller.choice.toUpperCase().equals("TOP")){
            noOfThumbs = cthead.length;
        }
        if(Controller.choice.toUpperCase().equals("SIDE")){
            noOfThumbs = cthead[0].length;
        }
        if(Controller.choice.toUpperCase().equals("FRONT")){
            noOfThumbs = cthead[0][0].length;
        }


        for(int i = 0; i < noOfThumbs; i++){

            //resize the image before adding it to view.
            BufferedImage imgToAdd = Resize.resize(arr.get(i),0.25f,0.25f);
            final int index = i;
            //Create the imageview and add it to the pane
            ImageView imv = new ImageView();
            imv.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    System.out.println(index);
                    openResizeWindow(index);
                }
            });

                Image img = SwingFXUtils.toFXImage(imgToAdd,null);
            imv.setImage(img);
            flowPane.getChildren().add(imv);
        }

    }

    public void openResizeWindow(int index) {
        Resize.index = index;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("resize.fxml"));

            Scene scene = new Scene(fxmlLoader.load(), 397, 397);
            Stage stage = new Stage();

            stage.setTitle("Resize View");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't open resize_nearestNeighbour window!");
        }
    }

    public List<BufferedImage> makeThumb(String view) {
        ArrayList<BufferedImage> imageArr = new ArrayList<>();

        //Get image dimensions, and declare loop variables based on view.
        int w=cthead[0][0].length,h=cthead[0].length, i, j, c;
        int d = cthead[0][0].length; // d = number of slices for this view.
        if(view.equals("FRONT")){//TODO: make sure these are right.
            h=cthead.length;
            d=cthead[0].length;
            w=cthead[0][0].length;
        }
        if(view.equals("TOP")){
            d=cthead.length;
            w=cthead[0][0].length;
            h=cthead[0].length;
        }
        if(view.equals("SIDE")){
            h=cthead.length;
            d=cthead[0][0].length;
            w=cthead[0].length;
        }

        //Works like an inverted mip. Runs through all slices in the top level loop, then the second two loops get each pixel for each slice. Then add that slice to the imageArr
        for(int y = 0; y < d; y++) {
            BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_3BYTE_BGR);
            byte[] data = GetImageData(image);
            float col;
            short datum = -1;
            for (j = 0; j < h; j++) {
                for (i = 0; i < w; i++) {
                    if(view.equals("FRONT")){
                        datum=cthead[j][y][i];
                    }
                    if(view.equals("TOP")){
                        datum=cthead[y][j][i];
                    }
                    if(view.equals("SIDE")){
                        datum=cthead[j][i][y];
                    }

                    col = (255.0f * ((float) datum - (float) min) / ((float) (max - min)));
                    for (c = 0; c < 3; c++) {
                        data[c + 3 * i + 3 * j * w] = (byte) col;
                    } // colour loop

                } // column loop
            } // row loop
            imageArr.add(image);
        }
        return imageArr;
    }



}
