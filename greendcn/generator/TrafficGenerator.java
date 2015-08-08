/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.generator;

import java.util.Date;
import java.util.Random;

/**
 *
 * @author mahdi
 */

public class TrafficGenerator implements Generator {

    private double mean, islandSize;
    private double stdDev;
    Random random;
    
    public TrafficGenerator(double m, double s, double i) {
        this.mean = m;
        this.stdDev = s;
        this.islandSize = i;
        random = new Random(new Date().getTime());
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
            return v*-1;
        }
        return v;
    }
    
    private double getDistributedMode() {
        return Math.random();
    }
    
    //
    private double getCentralizedMode() {
      return Math.random();
    }
    
}