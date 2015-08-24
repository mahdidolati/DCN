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

public class CpuUsage implements Generator {

    List<Double> forDebug;
    int counter;
    
    @Override
    public int getInt() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Random r;
    
    public CpuUsage() {
        r = new Random(new Date().getTime());
        double[] t = new double[]{13,8,13,6,11,6,14,11,14,12,6,9,5,7,5,10,5,11};
        forDebug = new ArrayList<>();
        for(int jj=0; jj<t.length; ++jj) {
            forDebug.add(t[jj]);
        }
        counter = 0;
    }
    
    @Override
    public double getDouble() {
        //5,8,13,16,20,26,35
//        double v = this.forDebug.get(counter);
//        counter++;
//        return v;
        return (15-5)* r.nextFloat() + 5;
//        return 10.0;
        //return Math.random()*5+3;
    }
    
}
