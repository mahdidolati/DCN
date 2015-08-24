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
import shared.AlgRunner;
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
    public Map<Double, List<Output>> nBexpsAll;
    public Map<Double, Output> myExp;
    
    public MyAlgorithmStateSpace() {
        bexps = new HashMap<>();
        mexps = new HashMap<>();
        this.bexpsAll = new HashMap<>();
        this.nBexpsAll = new HashMap<>();
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
//        System.out.println("--------------MY State Space-----------------");
        
        double total = m1.getTotalWork();
        double lowerBoundOfServerNum = Math.ceil(total / serverPower.getMaxUsage());
//        System.out.println("Minimum server req: " + lowerBoundOfServerNum + ", for total work: " + total);
        
        double totoalServerAvailable = Math.pow(FatTree.getK(), 3)/4.0;
        double sn = lowerBoundOfServerNum;
        Output bestCase = null;
        Output netMin = null;
        double tsn = 0;
        double ttsn = -1;
        
        while(true) {
            if(sn > totoalServerAvailable) {
                break;
            }
//            System.out.println("" + sn);
//            LogUtil.LOGGER.log(Level.INFO, "ttry: {0}", sn);
                m1.createServersWithAllJobs((int)sn, total);
//                m1.vmPlacementServerBasedSenderPriority(total/sn, serverPower.getMaxUsage(), 0);
//                LogUtil.LOGGER.log(Level.INFO, "ggot: {0}", m1.servers.size());
//                int rret = m1.bestServerNumber(serverPower, edgePower, aggPower, corePowModel, linkPower);
//                LogUtil.LOGGER.log(Level.INFO, "--> ret: {0}", rret);
//                System.out.println(">>>>" + m1.servers.size() + " " + hToE.size() + " " + eToA.size() + " " + interPod.size());
//                m1.createSuperVmsEachJobSeparate((int)sn, total);
//                System.out.println("cap: " + (total/sn));
//                for(Server srv : m1.servers) {
//                    System.out.print("" + srv.curLoad + " ");
//                }
//                System.out.println("");
                ttsn = tsn;
                tsn = m1.getServerNum();
//                LogUtil.LOGGER.log(Level.INFO, " try {0}", sn);
//                LogUtil.LOGGER.log(Level.INFO, " got {0}", tsn);
//                LogUtil.LOGGER.log(Level.INFO, "other got {0}", m1.servers.size());
//                System.out.println("Size of one server: " + (total/sn) + ", " + tsn);
//                m1.printSizeOfEachVm();
    
                if(tsn > totoalServerAvailable) {
                    LogUtil.LOGGER.log(Level.INFO, "reached server capacity...");
                    break;
                }
                

//                    LogUtil.LOGGER.log(Level.INFO, " got sthng {0}", tsn);
                    CalCulator.mode = CalCulator.BEST;
                    Output tempOut;// = this.getConsumption(m1);
                    tempOut = this.getConsumptionByServer(m1);
                    Output nTempOut = this.getConsumptionByServer(m1);
                    this.addAllExps(bexpsAll, this.proportionality, tempOut);
                    this.addAllExps(nBexpsAll, this.proportionality, nTempOut);
                    if(bestCase == null)
                        bestCase = nTempOut;
                    else
                        if(bestCase.getDcConsumption() > nTempOut.getDcConsumption())
                            bestCase = nTempOut;
                    CalCulator.mode = CalCulator.MIN;
                    tempOut = this.getConsumptionByServer(m1);
                    if(netMin == null)
                        netMin = tempOut;
                    else
                        if(netMin.getDcConsumption() > tempOut.getDcConsumption())
                            netMin = tempOut;

            
            sn += 10000;
        }
//        if(bestCase == null)
//            throw new NullPointerException();
        if(bestCase != null)
            this.addExp(bexps, proportionality, bestCase);
//        LogUtil.LOGGER.log(Level.INFO, "DC Consumption Best: {0}", bestCase.getDcConsumption());
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
//        LogUtil.LOGGER.log(Level.INFO, "--");
        if(m.containsKey(d)) {
            m.get(d).add(o);
        }else {
            List<Output> l = new ArrayList<>();
            l.add(o);
            m.put(d, l);
        }
    }
    
    private Output getConsumptionByServer(My m1) {
        
        
        List<Switch> hostToEdge = m1.getHostToEdgeByServer(m1.servers, FatTree.getK()/2);
        List<Switch> edgeToAgg = m1.getEdgeToAggByServer(hostToEdge, 1);
        List<Switch> pods = m1.getInterPodByServer(edgeToAgg, FatTree.getK()/2);
        
        CalCulator.mode = CalCulator.BEST;
        AlgRunner algRunner = new AlgRunner();
        algRunner.setAggPower(aggPower);
        algRunner.setCorePowModel(corePowModel);
        algRunner.setEdgePower(edgePower);
        algRunner.setLinkPower(linkPower);
        algRunner.setServerPower(serverPower);
        Output output = algRunner.getNutConsump(hostToEdge, edgeToAgg, pods);
        
        double srvVal = m1.servers.size()*this.serverPower.getConsumption(m1.getTotalWork()/m1.servers.size());
        double iSrvVal = 0.0;
        for(Server server : m1.servers) {
//            srvVal += this.serverPower.getConsumption(server.curLoad);
            iSrvVal += this.serverPower.getConsumption(server.curLoad);
        }
//        System.out.println("" + srvVal);
        output.addVal(Output.SERVER, Output.NUMBER, m1.getServerNum());
        output.addVal(Output.SERVER, Output.VALUE, iSrvVal);
        output.addVal(Output.I_SERVER, Output.VALUE, srvVal);
        
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
