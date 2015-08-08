/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import MyPackage.MyPack.My;
import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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
    
    public RunGreenDCNFrameWork() {
        bexps = new HashMap<>();
        mexps = new HashMap<>();
    }
    
    public void run() {
        //create super vms
        g.createSuperVms(this.serverPower);
        System.out.println("-------------[Green]---------------");
        System.out.println("TRAFFIC SUM: " + g.sumSuperTrafficMatrix());
        
        System.out.println("min server num obtainded: " + g.getServerNum() + ", for total: " + g.getTotalWork());
        //calculate k-cuts
        g.calculateCuts();
        
        List<List<Switch>> ret = g.assignSuperVmsToServers();
        System.out.println("" + ret.get(0).size() + " " + ret.get(1).size());
        //voroodi va khorooji har edge hast be server
        List<Switch> hostToEdge = ret.get(0);
//        if(edges.size() > FatTree.getK()*FatTree.getK()/2)
//            return;
        //voroodi va khorooji har edge be agg
        List<Switch> edgeToAgg = g.getAgg(hostToEdge);
        double aggtraffic = 0.0;
        for(Switch w : edgeToAgg) {
            for(SwitchTraffic st : w.getSwitchTraffic()) {
                aggtraffic += st.in;
                aggtraffic += st.out;
            }
        }
        System.out.println("-----> Aggregate traffic: " + aggtraffic);
//        System.out.println("GreenDCN: ");
//        for(Switch sw : aggs) {
//            for(SwitchTraffic st : sw.getSwitchTraffic()) {
//                System.out.println("" + st.in + " " + st.out);
//            }
//        }
        List<Switch> aggAux2 = ret.get(1);
        //voroodi khorooji har pod hast??
        List<Switch> pods = g.getCore(aggAux2);        
        
        Output output = new Output();
        
        CalCulator.mode = CalCulator.MIN;
        
        double ret1[];
        
        ret1 = CalCulator.getConsumptionEdge(hostToEdge, edgeToAgg, this.edgePower, this.linkPower);
        output.addVal(Output.EDGE, Output.NUMBER, ret1[0]);
        output.addVal(Output.EDGE, Output.VALUE, ret1[1]);
        
        ret1 = CalCulator.getAggConsumption(edgeToAgg, pods, this.aggPower, this.linkPower);
        output.addVal(Output.AGG, Output.NUMBER, ret1[0]);
        output.addVal(Output.AGG, Output.VALUE, ret1[1]);
     
        ret1 = CalCulator.getConsumptionCore(pods, this.edgePower, this.linkPower);
        output.addVal(Output.CORE, Output.NUMBER, ret1[0]);
        output.addVal(Output.CORE, Output.VALUE, ret1[1]);
     
        double srvVal = 0.0;
        for(Double d : g.getget()) {
            srvVal += this.serverPower.getConsumption(d);
        }
        
//        ret1 = CalCulator.getConsumptionServer(g.getTotalWork(), g.getServerNum(), this.serverPower);
//        output.addVal(Output.SERVER, Output.NUMBER, ret1[0]);
//        output.addVal(Output.SERVER, Output.VALUE, ret1[1]);
        System.out.println("" + srvVal);
        output.addVal(Output.SERVER, Output.NUMBER, g.getServerNum());
        output.addVal(Output.SERVER, Output.VALUE, srvVal);
        
//        System.out.println("set p: " + this.proportionality);
        LogUtil.LOGGER.log(Level.INFO, "DC Consumption Green: {0}", output.getDcConsumption());
        
//        this.mexps.put(this.proportionality, output);
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
