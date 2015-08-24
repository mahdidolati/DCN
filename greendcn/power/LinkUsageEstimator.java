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
public class LinkUsageEstimator {
    private DevicePowerUsageModel model;
    
    public LinkUsageEstimator(DevicePowerUsageModel model) {
        this.model = model;
    }
    
    public double getConsumption(int n, double totalWork, double serverCap, double vm, double bw, int k) {
        double edge = (n-1)*Math.pow(vm, 2.0)*bw;
        edge = 4*n*model.alpha*edge*edge + 2*n*model.beta/k;
        double agg = (1/2)*k*(n-(k/2))*Math.pow(vm, 2.0)*bw;
        agg = 4*n*model.alpha*agg*agg*2/k*2/k + 2*n*model.beta/k;
        double core = (1/4)*k*k*(n-(k*k/4))*Math.pow(vm, 2.0)*bw;
        core = 4*n*model.alpha*core*core*+k*model.beta;
        
        return edge+agg+core;
    }
}
