/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.generator;

import java.util.Random;

/**
 *
 * @author mahdi
 */

public class VmNumberGenerator implements Generator {

    private int K;
    
    public VmNumberGenerator(int K) {
        this.K = K;
    }
    
    @Override
    public int getInt() {
        return this.K;
//        Random r = new Random();
//        int ret = (int) (r.nextGaussian()*0.5*K + K);
//        if(ret == 0)
//            return 2;
//        else
//            return ret*2;
    }

    @Override
    public double getDouble() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}