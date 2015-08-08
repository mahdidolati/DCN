/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn.power;

import java.util.List;
import java.util.logging.Level;
import utility.FatTree;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;
import java.util.logging.Logger;
/**
 *
 * @author mahdi
 */
public class CalCulator {
    public static int mode = 0;
    public final static int BEST = 1;
    public final static int MIN = 2;
    
    private static int getNumber(DevicePowerUsageModel swFabricPow, double l, int max) {
        if(mode == BEST) {
//            System.out.println(">>" + swFabricPow.getBestFor(l, max) + " " + swFabricPow.getMinFor(l, max));
            return swFabricPow.getBestFor(l, max);
        } else
            return swFabricPow.getMinFor(l, max);
    }
    
    public static double[] getConsumptionEdge(List<Switch> sws, List<Switch> edgeToAgg, 
            DevicePowerUsageModel swFabricPow, DevicePowerUsageModel linkPow) {
        double pow = 0.0;
        for(int i=0; i<sws.size(); ++i) {
            Switch sw = sws.get(i);
            Switch sw2 = edgeToAgg.get(i);
            double edgeLoad = 0;
            for(SwitchTraffic st : sw.getSwitchTraffic()) {
                edgeLoad += st.in;
                edgeLoad += st.out;
                pow += linkPow.getDynamicConsumption(st.in);
                pow += linkPow.getDynamicConsumption(st.out);
//                LOGGER.info("edge link usage: " + st.in + ", " + st.out);
            }
            for(SwitchTraffic st : sw2.getSwitchTraffic()) {
                edgeLoad += (st.in + st.out);
            }
//            LOGGER.log(Level.INFO, "edge fabric usage: {0}", edgeLoad);
            pow += swFabricPow.getConsumption(edgeLoad);
        }
        double ret[] = new double[2];
        ret[0] = sws.size();
        ret[1] = pow;
//        System.out.println("edge num: " + sws.size() + ", pow: " + pow);
        return ret;
    }
    
    public static double[] getAggConsumption(List<Switch> sws, List<Switch> pods,
            DevicePowerUsageModel swFabricPow, DevicePowerUsageModel linkPow) {
        double out[] = new double[2];
        out[0] = 0;
        out[1] = 0;
        int podNum = FatTree.getK() / 2;
        int podCounter = 0;
        double trValIn = 0;
        double trValOut = 0.0;
        int podIndex = 0;
        for(int i=0; i<sws.size(); ++i) {
            if(podCounter == podNum || i == sws.size()-1) {
                double podToCoreLoad = 0.0;
                for(SwitchTraffic pst : pods.get(podIndex).getSwitchTraffic()){
                    podToCoreLoad += (pst.in + pst.out);
                }
                double allLoad = podToCoreLoad + trValIn + trValOut;
                int b = getNumber(swFabricPow, allLoad, FatTree.getK()/2);
//                int m = swFabricPow.getMinFor(allLoad, FatTree.getK()/2);
//                System.out.println("best: " + b + ", min: " + d.getMinFor(trValIn+trValOut, FatTree.getK()/2));
                if(b != 0) {
                    out[0] += b;
//                    LOGGER.log(Level.INFO, "agg fabric usage: {0}", ((allLoad)/b));
                    out[1] += swFabricPow.getConsumption(allLoad, b);
                    out[1] += (b*podCounter)*linkPow.getDynamicConsumption(trValIn/(b*podCounter));
                    out[1] += (b*podCounter)*linkPow.getDynamicConsumption(trValOut/(b*podCounter));
//                    System.out.println(">>><<<<>>>><<<<: " + d.getConsumption(trVal, b) + ", " + d.getConsumption(trVal, m));
                }
                podCounter = 0;
                trValIn = 0;
            }
            for(SwitchTraffic st : sws.get(i).getSwitchTraffic()) {
                trValIn += st.in;
                trValOut += st.out;
            }
            podCounter++;
        }
        return out;
    }
    
    public static double[] getConsumptionCore(List<Switch> sws, 
            DevicePowerUsageModel swFabric, DevicePowerUsageModel linkPow) {
        double pow[] = new double[2];
        pow[0] = pow[1] = 0;
        int maxN = 0;
        double tr = 0;
        double trOut = 0.0;
        for(Switch sw : sws) {
            for(SwitchTraffic st : sw.getSwitchTraffic()) {
                tr += st.in;
                trOut += st.out;
            }
        }
        int b = getNumber(swFabric, tr+trOut, FatTree.getK());
        if(b == 0) {
            pow[0] = 0;
            pow[1] = 0;
        }else {
            pow[0] = b;
            pow[1] = swFabric.getConsumption(tr+trOut, b);
            pow[1] += sws.size()*b*linkPow.getDynamicConsumption(tr/(sws.size()*b));
            pow[1] += sws.size()*b*linkPow.getDynamicConsumption(trOut/(sws.size()*b));
        }
//        System.out.println("best core num: " + maxN + ", pow: " + pow);
        return pow;
    }
    
    public static double[] getConsumptionServer(double total, int size, DevicePowerUsageModel d) {
        double out[] = new double[2];
        if(size == 0) {
            out[0] = 0;
            out[1] = 0;
        }else{
            out[0] = size;
            out[1] = size * d.getConsumption(total/size);
        }
        return out; 
    }
    
}