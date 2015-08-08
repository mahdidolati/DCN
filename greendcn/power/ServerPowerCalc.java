/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.power;

/**
 *
 * @author mahdi
 */
public class ServerPowerCalc {
    
    DevicePowerUsageModel model;
    
    public ServerPowerCalc(DevicePowerUsageModel model) {
        this.model = model;
    }
    
    public double getConsumption(int n, double totalWork) {
        return n * this.model.getConsumption(totalWork/n);
    }
    
    public double getC(double n, double totalWork) {
        return n * this.model.getConsumption(totalWork/n);
    }

}
