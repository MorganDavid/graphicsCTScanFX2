package sample;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Histogram {
    static float[] histogram, mapping;
    int h=112,w=256;
    static int grey_levels = 3365;

    public Histogram() {
        histogram = new float[grey_levels+1];
        mapping = new float[grey_levels+1];

        for(int i = 0; i < grey_levels; i++){
            histogram[i] = 0;
            mapping[i] = 0;
        }
    }

    static public float[] equalise(){
        float size = 256*256*113;
        float t_i = 0;
        for (int i=0; i<=grey_levels; i++) {
            t_i+=histogram[i];
            //mapping[i] = Math.max(0,(3365*t_i)/(256*256*113));
            mapping[i] = 255*(t_i/(size));
            System.out.println(t_i);
        }
        System.out.println("eq: "+Arrays.toString(mapping));
        return mapping;
    }

    static public void buildHistogram(short[][][] cthead){
        short datum = -1;
        for (int j=0; j<cthead.length; j++) {
            for (int i=0; i<cthead[0].length; i++) {
                for(int z=0; z<cthead[0][0].length; z++) {
                    datum = cthead[j][z][i];

               //     System.out.println(datum);

                    histogram[datum + 1117]++;//+1117 to make negative values positive from values 0 to 3365
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
