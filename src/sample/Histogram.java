package sample;

import javax.naming.ldap.Control;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Histogram {
    static float[] histogram, mapping;
    static int grey_levels = Controller.max-Controller.min;

    public Histogram() {
        histogram = new float[grey_levels+1];
        mapping = new float[grey_levels+1];

        for(int i = 0; i < grey_levels; i++){
            histogram[i] = 0;
            mapping[i] = 0;
        }
    }

    static public float[] equalise(){
        short[][][] cthead = Controller.cthead;
        float size = cthead[0].length*cthead[0][0].length*cthead.length;
        float t_i = 0;
        for (int i=0; i<=grey_levels; i++) {
            t_i+=histogram[i];
            mapping[i] = 255*(t_i/(size));
            System.out.println(t_i);
        }
        System.out.println("eq: "+Arrays.toString(mapping));
        return mapping;
    }

    //Builds a histogram from the whole data set and puts the result in the histgoram variable.
    static public void buildHistogram(short[][][] cthead){
        short datum;
        for (int j=0; j<cthead.length; j++) {
            for (int i=0; i<cthead[0].length; i++) {
                for(int z=0; z<cthead[0][0].length; z++) {
                    datum = cthead[j][z][i];

                    histogram[datum + (Math.abs(Controller.min))]++;//+1117 to make negative values positive from values 0 to 3365
                }
            } // column loop
        } // row loop
        System.out.println(Arrays.toString(histogram));
    }

    static public float getMapping(int in){
        return mapping[in];
    }

    static public float[] getHistogram() {
        return histogram;
    }
}
