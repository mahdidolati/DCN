/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import shared.AlgRunner;
import utility.FatTree;
import utility.LogUtil;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class RunMyAlgorithm {
    public Map<Double, Output> myExp;
    public Map<Double, Output> minExp;
    public Map<Double, Map<Integer, Output>> bestNumExp;
    private My m1;
    private DevicePowerUsageModel edgePower, aggPower, corePowModel, serverPower, linkPower;
    private double proportionality;
    public int mode;
    public static int ALL = 1;
    public static int BIN = 2;
    public static int GRE = 3;
    
    public RunMyAlgorithm() {
        this.myExp = new HashMap<>();
        this.minExp = new HashMap<>();
        this.bestNumExp = new HashMap<>();
    }
    
    public void run() {
//        System.out.println("--------------MY-----------------");
        
        double total = m1.getTotalWork();
        int minReq = (int)Math.ceil(total/serverPower.getMaxUsage());
        int maxAva = (int)(Math.pow(FatTree.getK(),3)/4);
//        m1.createServersWithAllJobs(minReq, total);
        m1.vmPlacementServerBasedSenderPriority(total/minReq, serverPower.getMaxUsage(), My.EXCEED_RETAIN);
        if(m1.servers.size() > maxAva)
            return;
        int bestNum = m1.bestServerNumberProbabilityBased(serverPower, edgePower, aggPower, corePowModel, linkPower);
//        m1.outss;
//        this.proportionality;
        if(this.bestNumExp.containsKey(this.proportionality) == false) {
            this.bestNumExp.put(this.proportionality, new HashMap<>());
        }
        for(Integer iii : m1.outss.keySet()) {
                if(this.bestNumExp.get(this.proportionality).containsKey(iii)) {
                    this.bestNumExp.get(this.proportionality).get(iii).addOutput(m1.outss.get(iii));
                }else{
                    this.bestNumExp.get(this.proportionality).put(iii, m1.outss.get(iii));
                }
            }
        
//        System.out.println("0: " + bestNum);
//        bestNum = minReq;
//        LogUtil.LOGGER.log(Level.INFO, "try {0}", bestNum);
        if(this.mode == RunMyAlgorithm.GRE) {
            m1.createSuperVmsElastic(total/bestNum);
//            if(m1.servers.size() > maxAva)
//                return;
        }else if(this.mode == RunMyAlgorithm.BIN) {
            m1.createSuperVmsBinaryMerge(total/bestNum);
//            if(m1.servers.size() > maxAva)
//                return;
        }else if(this.mode == RunMyAlgorithm.ALL) {
    //        m1.createServersWithAllJobs(bestNum, total);
            m1.vmPlacementServerBasedSenderPriority(total/bestNum, serverPower.getMaxUsage(), My.EXCEED_RETAIN);
        }
                
//        System.out.println("l: " + m1.servers.size());
//        m1.createServersWithAllJobs(bestNum, total);
        int numm = 0;
        double w = 0;
        for(Server s : m1.servers) {
            w += s.curLoad;
            for(JobMy j : s.hosted.keySet())
                numm += s.hosted.get(j).size();
        }
        System.out.println("numm" + numm + " " + w + " " + m1.getTotalWork());
//        LogUtil.LOGGER.log(Level.INFO, "got {0}", m1.servers.size());
        
        Output output = this.getConsumptionByServer(m1);
        
        this.addExp(myExp, proportionality, output);
    }
    
    private void addExp(Map<Double, Output> m, double d, Output o) {
        if(m.containsKey(d)) {
            m.get(d).addOutput(o);
        }else {
            m.put(d, o);
        }
    }
    
    public Output getConsumptionByServer(My m1) {
       
        List<Switch> hostToEdge = m1.getHostToEdgeByServer(m1.servers, FatTree.getK()/2);
        
        double added = 0.0;
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
        
        CalCulator.mode = CalCulator.MIN;
        algRunner = new AlgRunner();
        algRunner.setAggPower(aggPower);
        algRunner.setCorePowModel(corePowModel);
        algRunner.setEdgePower(edgePower);
        algRunner.setLinkPower(linkPower);
        algRunner.setServerPower(serverPower);
        Output outputNetMin = algRunner.getNutConsump(hostToEdge, edgeToAgg, pods);
        this.addExp(minExp, proportionality, outputNetMin);
        
        double iSrvVal = 0.0;
        for(Server server : m1.servers) {
            iSrvVal += this.serverPower.getConsumption(server.curLoad);
        }
        output.addVal(Output.SERVER, Output.NUMBER, m1.getServerNum());
        output.addVal(Output.SERVER, Output.VALUE, iSrvVal);
        output.addVal(Output.I_SERVER, Output.VALUE, iSrvVal);
        
        return output;
    }

    public Map<Double, Output> getMyExp() {
        return myExp;
    }

    public void setMyExp(Map<Double, Output> myExp) {
        this.myExp = myExp;
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

    public Map<Double, Output> getMinExp() {
        return minExp;
    }

    public void setMinExp(Map<Double, Output> minExp) {
        this.minExp = minExp;
    }
    
}
