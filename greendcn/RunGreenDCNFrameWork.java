/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import MyPackage.MyPack.JobMy;
import MyPackage.MyPack.My;
import MyPackage.MyPack.Server;
import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
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
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */

public class RunGreenDCNFrameWork {
    
    private DevicePowerUsageModel edgePower, aggPower, corePowModel, serverPower, linkPower;
    private double proportionality;
    public Map<Double, Map<Double, Output>> bexps;
    public Map<Double, Output> mexps;
    private GreenDCNClass g;
    public boolean isCalced;
    public RunGreenDCNFrameWork() {
        bexps = new HashMap<>();
        mexps = new HashMap<>();
    }
    List<Switch> hostToEdge;
    List<Switch> edgeToAgg;
    List<Switch> pods;
    public void run() {
    
        isCalced = true;
        g.createSuperVms(this.serverPower);
        g.calculateCuts();
        List<List<Switch>> ret = g.assignSuperVmsToServers(this.serverPower.getMaxUsage());
        g.fillServers();
        hostToEdge = ret.get(0);
        edgeToAgg = g.getAgg(hostToEdge);
        List<Switch> aggAux2 = ret.get(1);
        pods = g.getCore(aggAux2);   
        
        CalCulator.mode = CalCulator.MIN;
        AlgRunner algRunner = new AlgRunner();
        algRunner.setAggPower(aggPower);
        algRunner.setCorePowModel(corePowModel);
        algRunner.setEdgePower(edgePower);
        algRunner.setLinkPower(linkPower);
        algRunner.setServerPower(serverPower);
        Output output = algRunner.getNutConsump(hostToEdge, edgeToAgg, pods);
     
        double srvVal = 0.0;
        for(Double d : g.getget()) {
            srvVal += this.serverPower.getConsumption(d);
        }
        
        output.addVal(Output.SERVER, Output.NUMBER, g.getServerNum());
        output.addVal(Output.SERVER, Output.VALUE, srvVal);
        
        this.addExp(this.mexps, this.proportionality, output);
    }
    
    private void addExp(Map<Double, Output> m, double d, Output o) {
        if(m.containsKey(d)) {
            m.get(d).addOutput(o);
        }else {
            m.put(d, o);
        }
    }

    public Map<Double, Map<Double, Output>> averageStat(Map<Double,Map<Double,Output>> exps) {
        Map<Double,Map<Double,Output>> t = new HashMap<>();
        for(Double propor : exps.keySet()) {
            double sAvg = 0.0;
            Output output = new Output();
            for(Double serverNum : exps.get(propor).keySet()) {
                sAvg += serverNum;
//                System.out.println("----------------------");
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

    public Map<Double, Map<Double, Output>> getBexps() {
        return bexps;
    }

    public void setBexps(Map<Double, Map<Double, Output>> bexps) {
        this.bexps = bexps;
    }

    public Map<Double, Output> getMexps() {
        return mexps;
    }

    public void setMexps(Map<Double, Output> mexps) {
        this.mexps = mexps;
    }

    public GreenDCNClass getG() {
        return g;
    }

    public void setG(GreenDCNClass g) {
        this.g = g;
    }
}
