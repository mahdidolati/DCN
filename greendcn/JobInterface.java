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
   
    public JobInterface(VmNumberGenerator vmGen, TrafficGenerator trGen, CpuUsage cpuGen, int centersNum) {
       this.trafficMatrix = new ArrayList<>();
       this.cpu = new ArrayList<>();
        if(vmGen == null)
            return;
       this.trafficMatrix = new ArrayList<>();
       this.cpu = new ArrayList<>();
       int vmNum = vmGen.getInt();
       this.trafficMatrix = this.getInslandModelTraffic(vmNum, trGen, centersNum);
       this.cpu = new ArrayList<>();
       for(int i=0; i<vmNum; ++i) {
           this.cpu.add(cpuGen.getDouble());
       }
    }

    public List<List<Double>> getInslandModelTraffic(int vmNum, TrafficGenerator trGen, int centersNum) {
        List<List<Double>> trmx = new ArrayList<>();
        for(int i=0; i<vmNum; ++i) {
            trmx.add(new ArrayList<>());
            for(int j=0; j<vmNum; ++j) {
                trmx.get(i).add(0.0);
            }
        }
        int clusterSize = (vmNum-centersNum)/centersNum;
        //int currentClusterCenter = 0;
        int currentMember = centersNum;
        for(int i=0; i<centersNum; ++i) {
            int beg = i*clusterSize+centersNum;
            this.connectCenterToCluster(trmx, i, beg, clusterSize, trGen);
        }
        for(int i=0; i<centersNum; ++i) {
            for(int j=i+1; j<centersNum; ++j) {
                if(i==j)
                    continue;
                trmx.get(i).set(j, trGen.getDouble());
                trmx.get(j).set(i, trmx.get(i).get(j));
            }
        }
        return trmx;
    }

    private void connectCenterToCluster(List<List<Double>> trmx, int centerId, int beg, 
            int clusterSize, TrafficGenerator trGen) {
        for(int j=beg; j<beg+clusterSize; ++j) {
            if(trmx.get(centerId).size() > j) {
                double v = trGen.getDouble();
                trmx.get(centerId).set(j, v);
                trmx.get(j).set(centerId, v);
            }
        }
        //each cluster if fully connnected or not
//        for(int j=beg; j<beg+clusterSize; ++j) {
//            for(int k=j+1; k<beg+clusterSize; ++k) {
//                if(j >= trmx.size() || k >= trmx.size())
//                    continue;
//                double v = trGen.getDouble()/10;
//                trmx.get(j).set(k, v);
//                trmx.get(k).set(j, v);
//            }
//        }
    }
    
    public List<List<Double>> getTrafficMatrix() {
        return trafficMatrix;
    }

    public void setTrafficMatrix(List<List<Double>> trafficMatrix) {
        this.trafficMatrix = trafficMatrix;
    }

    public List<Double> getCpu() {
        return cpu;
    }

    public void setCpu(List<Double> cpu) {
        this.cpu = cpu;
    }

    
    
}
