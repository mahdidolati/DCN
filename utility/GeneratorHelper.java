/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package utility;

import greendcn.generator.CpuUsage;
import greendcn.generator.TrafficGenerator;
import greendcn.generator.VmNumberGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mahdi
 */
public class GeneratorHelper {
    
    
    private static VmNumberGenerator vmGen;
    private static TrafficGenerator trGen;
    private static CpuUsage cpuGen;
    
    public static List<List<Double>> getTraffic() {
       List<List<Double>> trafficMatrix;
       trafficMatrix = new ArrayList<>();
    
       int vmNum = vmGen.getInt();
       List<Double> t = null;
       for(int i=0; i<vmNum; ++i) {
           t = new ArrayList<>();
           for(int j=0; j<vmNum; ++j) {
               if(i==j)
                   t.add(0.0);
               else
                   t.add(trGen.getDouble());
           }
           trafficMatrix.add(t);
       }
       
       return trafficMatrix;
    }
    
    public static List<Double> getCpu(VmNumberGenerator vmGen, greendcn.generator.TrafficGenerator trGen, CpuUsage cpuGen) {
       List<Double> cpu;
       
       cpu = new ArrayList<>();
       
       int vmNum = vmGen.getInt();
       
       for(int i=0; i<vmNum; ++i) {
           cpu.add(cpuGen.getDouble());
       }
       
       return cpu;
    }
}
