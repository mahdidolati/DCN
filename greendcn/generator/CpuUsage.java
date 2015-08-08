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

public class CpuUsage implements Generator {

    @Override
    public int getInt() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Random r;
    
    public CpuUsage() {
        r = new Random(new Date().getTime());
    }
    
    @Override
    public double getDouble() {
        //5,8,13,16,20,26,35
        return (35-5)* r.nextFloat() + 5;
//        return 10.0;
        //return Math.random()*5+3;
    }
    
}
