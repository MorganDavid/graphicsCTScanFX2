package sample;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Histogram {
    int[] histogram, mapping;
    int h=112,w=256;
    int grey_levels = 3365;

    public Histogram() {
        histogram = new int[grey_levels+1];
        mapping = new int[grey_levels+1];
    }

    public int[] equalise(){
        int t_i = 0;
        for (int i=0; i<grey_levels; i++) {
            t_i+=histogram[i];
            mapping[i]= Math.max(0,Math.round((grey_levels*t_i)/(h*w))-1);
        }
        return mapping;
    }

    public void buildHistogram(short[][][] cthead,int slice){
        short datum;
        for (int j=0; j<112; j++) {
            for (int i=0; i<256; i++) {

                datum=cthead[slice][j][i];

                System.out.println(datum);

                histogram[datum+1117]++;//+1117 to make negative values positive from values 0 to 3365
            } // column loop
        } // row loop
        System.out.println(Arrays.toString(histogram));
    }

    public int[] getHistogram() {
        return histogram;
    }
}
