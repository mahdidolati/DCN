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
    double cpuAvg;
    
    public ServerPowerCalc(DevicePowerUsageModel model, double cpuAvg) {
        this.model = model;
        this.cpuAvg = cpuAvg;
    }
    
    public double getConsumption(int n, double totalWork) {
        if(totalWork/n < this.cpuAvg)
            return (
                    (totalWork/this.cpuAvg) * this.model.getConsumption(this.cpuAvg)
                    +
                    (n-(totalWork/this.cpuAvg)) * this.model.getConsumption(0)
                    );
        else
            return n * this.model.getConsumption(totalWork/n);
    }
    
    public double getC(double n, double totalWork) {
//        if(totalWork/n < this.cpuAvg)
//            return (
//                    (totalWork/this.cpuAvg) * this.model.getConsumption(this.cpuAvg)
//                    +
//                    (n-(totalWork/this.cpuAvg)) * this.model.getConsumption(0)
//                    );
//        else
        return n * this.model.getConsumption(totalWork/n);
    }

}
