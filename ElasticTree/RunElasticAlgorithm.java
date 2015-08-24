/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ElasticTree;

import MyPackage.MyPack.JobMy;
import MyPackage.MyPack.Server;
import greendcn.GreenDCNClass;
import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import shared.AlgRunner;
import utility.FatTree;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class RunElasticAlgorithm {
    private DevicePowerUsageModel edgePower, aggPower, corePowModel, serverPower, linkPower;
    private double proportionality;
    public Map<Double, Map<Double, Output>> bexps;
    public Map<Double, Output> mexps;
    private GreenDCNClass g;
    public boolean isCalced;
    public RunElasticAlgorithm() {
        bexps = new HashMap<>();
        mexps = new HashMap<>();
    }
    
    public void run() {
        
        g.createSuperVmsElastic(serverPower);
        
        List<Switch> hostToEdge = g.getHostToEdgeByServer(g.servers, FatTree.getK()/2);
        
        List<Switch> edgeToAgg = g.getEdgeToAggByServer(hostToEdge, 1);
        
        List<Switch> pods = g.getInterPodByServer(edgeToAgg, FatTree.getK()/2);
       
        
        CalCulator.mode = CalCulator.MIN;
        AlgRunner algRunner = new AlgRunner();
        algRunner.setAggPower(aggPower);
        algRunner.setCorePowModel(corePowModel);
        algRunner.setEdgePower(edgePower);
        algRunner.setLinkPower(linkPower);
        algRunner.setServerPower(serverPower);
        Output output = algRunner.getNutConsump(hostToEdge, edgeToAgg, pods);

        double srvVal = 0.0;
        for(Server server : g.servers) {
            srvVal += this.serverPower.getConsumption(server.curLoad);
        }

        output.addVal(Output.SERVER, Output.NUMBER, g.getServerNum());
        output.addVal(Output.SERVER, Output.VALUE, srvVal);
        
        this.addExp(mexps, proportionality, output);
    }

    private void addExp(Map<Double, Output> m, double d, Output o) {
        if(m.containsKey(d)) {
            m.get(d).addOutput(o);
        }else {
            m.put(d, o);
        }
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

    public boolean isIsCalced() {
        return isCalced;
    }

    public void setIsCalced(boolean isCalced) {
        this.isCalced = isCalced;
    }
    
    
    
}
