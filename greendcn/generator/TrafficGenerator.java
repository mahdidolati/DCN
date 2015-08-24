/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.generator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 *
 * @author mahdi
 */

public class TrafficGenerator implements Generator {

    private double mean, islandSize;
    private double stdDev;
    Random random;
    List<Double> forDebug;
    int counter; 
    
    public TrafficGenerator(double m, double s, double i) {
        this.mean = m;
        this.stdDev = s;
        this.islandSize = i;
        random = new Random(new Date().getTime());
        forDebug = new ArrayList<>();
        double[] t = new double[]{223,218,124,283,219,315,119,191,179,185,275,272,209,201,201,196,82,152};
        for(int jj=0; jj<t.length; ++jj) {
            forDebug.add(t[jj]);
        }
        counter = 0;
    }
    
    @Override
    public int getInt() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getDouble() {
        double r = random.nextGaussian();
        double v = (this.stdDev*r)+this.mean;
        if(v < 0) {
//            System.out.println("Gen" + r + " " + this.stdDev + " " + this.mean);
            return 0.0;
        }
        return v;
//        double v = this.forDebug.get(counter);
//        counter++;
//        return v;
    }
    
    private double getDistributedMode() {
        return Math.random();
    }
    
    //
    private double getCentralizedMode() {
      return Math.random();
    }
    
}