/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import utility.FatTree;
import utility.LogUtil;
import utility.Output;
import utility.Switch;

/**
 *
 * @author mahdi
 */
public class MyAlgorithmStateSpace {
    private My m1;
    private DevicePowerUsageModel edgePower, aggPower, corePowModel, serverPower, linkPower;
    private double proportionality;
//    public Map<String, Output> exps;
    public Map<Double, Output> bexps;
    public Map<Double, Output> mexps;
    public Map<Double, List<Output>> bexpsAll;
    
    public MyAlgorithmStateSpace() {
        bexps = new HashMap<>();
        mexps = new HashMap<>();
        this.bexpsAll = new HashMap<>();
    }
    
    public MyAlgorithmStateSpace(My m1) {
        this();
        this.m1 = m1;
    }
    
    public MyAlgorithmStateSpace(My m1, DevicePowerUsageModel edgePower, DevicePowerUsageModel aggPower,
            DevicePowerUsageModel corePowModel, DevicePowerUsageModel powModelServer, 
            DevicePowerUsageModel powModelLink, double proportionality) {
        this();
        this.m1 = m1;
        this.edgePower = edgePower;
        this.linkPower = powModelLink;
        this.serverPower = powModelServer;
        this.aggPower = aggPower;
        this.corePowModel = corePowModel;
        this.proportionality = proportionality;
    }
    
    public void run() throws IOException {
        System.out.println("--------------MY State Space-----------------");
        
        double total = m1.getTotalWork();
        double lowerBoundOfServerNum = Math.ceil(total / serverPower.getMaxUsage());
        System.out.println("Minimum server req: " + lowerBoundOfServerNum + ", for total work: " + total);
        
        double totoalServerAvailable = Math.pow(FatTree.getK(), 3)/4.0;
        double sn = lowerBoundOfServerNum;
        Output bestCase = null;
        Output netMin = null;
        
        while(true) {
            if(sn > totoalServerAvailable) {
                break;
            }
            if(this.serverPower.getMaxUsage() < total/sn) {
                System.out.println("" + sn + ", I, I, I\n");
            }else{
                m1.createSuperVmsEachJobSeparate((int)sn, total);
                
                double tsn = m1.getServerNum();
                LogUtil.LOGGER.log(Level.INFO, " try {0}", sn);
                LogUtil.LOGGER.log(Level.INFO, " got {0}", tsn);
//                System.out.println("Size of one server: " + (total/sn) + ", " + tsn);
//                m1.printSizeOfEachVm();
                
                if(tsn >= sn) {
//                    LogUtil.LOGGER.log(Level.INFO, " got sthng {0}", tsn);
                    sn = tsn;
                    CalCulator.mode = CalCulator.BEST;
                    Output tempOut = this.getConsumption(m1);
                    this.addAllExps(bexpsAll, this.proportionality, tempOut);
                    if(bestCase == null)
                        bestCase = tempOut;
                    else
                        if(bestCase.getDcConsumption() > tempOut.getDcConsumption())
                            bestCase = tempOut;
                    CalCulator.mode = CalCulator.MIN;
                    tempOut = this.getConsumption(m1);
                    if(netMin == null)
                        netMin = tempOut;
                    else
                        if(netMin.getDcConsumption() > tempOut.getDcConsumption())
                            netMin = tempOut;
                }
            }
            sn += 20;
        }
        LogUtil.LOGGER.log(Level.INFO, "DC Consumption Best: {0}", bestCase.getDcConsumption());
//        this.addExp(this.bexps, this.proportionality, bestCase);
//        this.addExp(this.mexps, this.proportionality, netMin);
    }
    
    private void addExp(Map<Double, Output> m, double d, Output o) {
        if(m.containsKey(d)) {
            m.get(d).addOutput(o);
        }else {
            m.put(d, o);
        }
    }
    
    private void addAllExps(Map<Double, List<Output>> m, double d, Output o) {
        if(m.containsKey(d)) {
            m.get(d).add(o);
        }else {
            List<Output> l = new ArrayList<>();
            l.add(o);
            m.put(d, l);
        }
    }
    
    private Output getConsumption(My m1) {
        
        Output output = new Output();
        
        List<Switch> hostToEdge = m1.edgeTraffic();
        List<Switch> edgeToAgg = m1.getAgg(hostToEdge);
        
        List<Switch> pods = m1.assignSupersToPods();
        pods = m1.getCore(pods);

//        System.out.println("" + hostToEdge.size() + " " + edgeToAgg.size() + " " + pods.size());
//        for(Switch sw : hostToEdge) {
//            System.out.println("h to e: " + sw);
//        }
//        for(Switch sw : edgeToAgg) {
//            System.out.println("e to a: " + sw);
//        }
//        for(Switch sw : pods) {
//            System.out.println("p: " + sw);
//        }
        
        double ret1[];
        ret1 = CalCulator.getConsumptionEdge(hostToEdge, edgeToAgg, edgePower, linkPower);
        output.addVal(Output.EDGE, Output.NUMBER, ret1[0]);
        output.addVal(Output.EDGE, Output.VALUE, ret1[1]);

        ret1 = CalCulator.getAggConsumption(edgeToAgg, pods, aggPower, linkPower);
        output.addVal(Output.AGG, Output.NUMBER, ret1[0]);
        output.addVal(Output.AGG, Output.VALUE, ret1[1]);

        ret1 = CalCulator.getConsumptionCore(pods, corePowModel, linkPower);
        output.addVal(Output.CORE, Output.NUMBER, ret1[0]);
        output.addVal(Output.CORE, Output.VALUE, ret1[1]);

        int serverNum = m1.getServerNum();
        
        double srvVal = 0.0;
        for(Double d : m1.getget()) {
            srvVal += this.serverPower.getConsumption(d);
        }
//        System.out.println("" + srvVal);
        output.addVal(Output.SERVER, Output.NUMBER, m1.getServerNum());
        output.addVal(Output.SERVER, Output.VALUE, srvVal);
        
//        ret1 = CalCulator.getConsumptionServer(m1.getTotalWork(), serverNum, serverPower);
//        if(ret1[1] == Double.NaN) {
//            System.out.println("\n\n**NNNNNNNNNNNNAAAAAAnnnnnn: " + m1.getTotalWork() + ", " + serverNum + " \n**\n\n");
//        }
//        output.addVal(Output.SERVER, Output.NUMBER, ret1[0]);
//        output.addVal(Output.SERVER, Output.VALUE, ret1[1]);
        
        return output;
        
    }
    
    public My getM1() {
        return m1;
    }

    public void setM1(My m1) {
        this.m1 = m1;
    }

    public DevicePowerUsageModel getEdgePower() {
        return edgePower;
    }

    public void setEdgePower(DevicePowerUsageModel edgePower) {
        this.edgePower = edgePower;
    }

    public DevicePowerUsageModel getAggPower() {
        return aggPower;
    }

    public void setAggPower(DevicePowerUsageModel aggPower) {
        this.aggPower = aggPower;
    }

    public DevicePowerUsageModel getCorePowModel() {
        return corePowModel;
    }

    public void setCorePowModel(DevicePowerUsageModel corePowModel) {
        this.corePowModel = corePowModel;
    }

    public DevicePowerUsageModel getServerPower() {
        return serverPower;
    }

    public void setServerPower(DevicePowerUsageModel serverPower) {
        this.serverPower = serverPower;
    }

    public DevicePowerUsageModel getLinkPower() {
        return linkPower;
    }

    public void setLinkPower(DevicePowerUsageModel linkPower) {
        this.linkPower = linkPower;
    }

    public double getProportionality() {
        return proportionality;
    }

    public void setProportionality(double proportionality) {
        this.proportionality = proportionality;
    }

    public Map<Double, Output> getBexps() {
        return bexps;
    }

    public void setBexps(Map<Double, Output> bexps) {
        this.bexps = bexps;
    }

    public Map<Double, Output> getMexps() {
        return mexps;
    }

    public void setMexps(Map<Double, Output> mexps) {
        this.mexps = mexps;
    }
    
    public Map<Double, Map<Double, Output>> averageStat(Map<Double,Map<Double,Output>> exps) {
        Map<Double,Map<Double,Output>> t = new HashMap<>();
        for(Double propor : exps.keySet()) {
            double sAvg = 0.0;
            Output output = new Output();
            for(Double serverNum : exps.get(propor).keySet()) {
                sAvg += serverNum;
                for(String d : exps.get(propor).get(serverNum).getVals().keySet()) {
                    for(String stat : exps.get(propor).get(serverNum).getVals().get(d).keySet()) {
                        output.addVal(d, stat, exps.get(propor).get(serverNum).getVals().get(d).get(stat));
                    }
                }
            }
            Map<Double, Output> tt = new HashMap<>();
            tt.put(sAvg/exps.get(propor).keySet().size(), output);
            t.put(propor, tt);
        }
        return t;
    }
    
}
