/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import greendcn.generator.CpuUsage;
import greendcn.generator.TrafficGenerator;
import greendcn.generator.VmNumberGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mahdi
 */
public class JobInterface {
    protected List<List<Double>> trafficMatrix;
    protected List<Double> cpu;
   
    public JobInterface(VmNumberGenerator vmGen, TrafficGenerator trGen, CpuUsage cpuGen) {
       this.trafficMatrix = new ArrayList<>();
       this.cpu = new ArrayList<>();
        if(vmGen == null)
            return;
       this.trafficMatrix = new ArrayList<>();
       this.cpu = new ArrayList<>();
       int vmNum = vmGen.getInt();
       this.trafficMatrix = this.getInslandModelTraffic(vmNum, trGen);
       this.cpu = new ArrayList<>();
       for(int i=0; i<vmNum; ++i) {
           this.cpu.add(cpuGen.getDouble());
       }
    }

    public List<List<Double>> getInslandModelTraffic(int vmNum, TrafficGenerator trGen) {
        List<List<Double>> trmx = new ArrayList<>();
        for(int i=0; i<vmNum; ++i) {
            trmx.add(new ArrayList<>());
        }
        for(int i=0; i<vmNum; ++i) {
            for(int j=0; j<vmNum; ++j) {
                if(i == j)
                    trmx.get(i).add(0.0);
                else {
                    trmx.get(i).add(trGen.getDouble());
                }
            }
        }
        return trmx;
    }
    
}
