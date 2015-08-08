/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.power;

import java.util.logging.Level;
import utility.FatTree;
import utility.LogUtil;

/**
 *
 * @author mahdi
 */
public class DevicePowerUsageModel {
    double alpha;
    double beta;
    double maxUsage;
    double maxPower;
    double proportionality;
    int portNum;
    
    public DevicePowerUsageModel(double mu, double mp, double pr, int portNum) {
        System.out.println(">>>>>>>>>>>>>>>>>"+mu*portNum);
        this.beta = mp * (1.0-pr);
        this.alpha = (mp*pr)/(mu*mu*portNum*portNum);
        this.maxUsage = mu;
        this.maxPower = mp;
        this.proportionality = pr;
        this.portNum = portNum;
//      System.out.println("model: " + this.alpha + ", " + this.beta + ", " + this.maxUsage + ", " + this.maxPower);
    }
    
    public double getConsumption(double load) {
        //System.out.println("MAX USAGE:::::: " + load);
        if(load > this.maxUsage*this.portNum) {
//            load = this.maxUsage*this.portNum;
//            System.out.println("MAX USAGE:::::: " + load);
        }
        return this.getDynamicConsumption(load)+this.beta;
    }
    
    public double getDynamicConsumption(double load) {
//        System.out.println("MAX USAGE:::::: " + load + " " + this.maxUsage*this.portNum);
        if(load > this.maxUsage*this.portNum) {
            LogUtil.LOGGER.log(Level.INFO, "usage is more than allowed: {0} {1}", new String[]{""+load,""+this.maxUsage*this.portNum});
//            load = this.maxUsage*this.portNum;
//            System.out.println("MAX USAGE:::::: " + load + " " + this.maxUsage*this.portNum);
        }
        return this.alpha*Math.pow(load, 2.0);
    }
    
    public int getBestFor(double load, int max) {
//        System.out.println("L:" + load);
        if(this.beta == 0)
            return Integer.MAX_VALUE;
        if(load == 0)
            return 0;
//        System.out.println("(alpha,beta): " + this.alpha + ", " + this.beta + ", " + Math.sqrt(this.alpha/this.beta) + ", " + load + ", " + Math.sqrt(this.alpha/this.beta)*load);
        double nn = Math.sqrt(this.alpha/this.beta)*load;
//        System.out.println("........:::: " + nn);
        int nn_l = (int)Math.floor(nn);
        int nn_h = (int)Math.ceil(nn);
        double cons_l = this.getConsumption(load, nn_l);
        double cons_h = this.getConsumption(load, nn_h);
        int out;
        if(cons_h < cons_l)
            out = nn_h;
        else
            out = nn_l;
        int min = this.getMinFor(load, max);
//        System.out.println("best sw: " + min + " " + out + " " + max);
//        System.out.println("'''''''''''::::: " +  out);
//        System.out.println("'''''''''''::::: " + this.getConsumption(load, min));    
        if(out < min) {
            return min;
        }else if(out > max)
            return max;
        else
            return out;
    }
    
    public int getMinFor(double load, int max) {
        if(load == 0)
            return 0;
//        System.out.println("ll: " + (load/(this.maxUsage*this.portNum)));
        int m = (int)Math.ceil(load/(this.maxUsage*this.portNum));
        if(max < m)
            return max;
        return m;
    }
     
    public double getConsumption(double val, int num) {
        if(num == 0 || val == 0)
            return 0;
        double p = num*this.beta;
        p += (num*this.getDynamicConsumption(val/num));
        return p;
    }
   
    public double getMaxUsage() {
        return this.maxUsage*this.portNum;
    }
    
    public double getIdlePowerFor(int num) {
        return num*this.beta;
    }
    
}
