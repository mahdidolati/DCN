
package shared;

import MyPackage.MyPack.JobMy;
import MyPackage.MyPack.Server;
import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.List;
import utility.FatTree;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mahdi
 */

public class AlgRunner {
    private DevicePowerUsageModel edgePower, aggPower, corePowModel, serverPower, linkPower;
    
    public Output getNutConsump(List<Switch> hostToEdge, List<Switch> edgeToAgg, List<Switch> pods) {    
        
        this.addOutOfDc(hostToEdge, edgeToAgg, pods);
        
        Output output = new Output();
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

        return output;
    }

    public void addOutOfDc(List<Switch> hostToEdge, List<Switch> edgeToAgg, List<Switch> pods) {
        double v = 0;
        double ad;
        ad = addAux(hostToEdge, v);
        ad += addAux(edgeToAgg, v);
        ad += addAux(pods, v);
//        System.out.println("added: " + ad);
    }

    private double addAux(List<Switch> hostToEdge, double v) {
        double added = 0;
        for(Switch sw : hostToEdge) {
            for(Server sv : sw.getServers()) {
                for(JobMy j : sv.hosted.keySet()) {
                    for(int hh : sv.hosted.get(j)) {
//                        added++;
                        if(sw.getSts().isEmpty())
                            throw new NullPointerException();
                        for(SwitchTraffic st : sw.getSts()) {
                            st.in += (v/sw.getSts().size());
                            st.out += (v/sw.getSts().size());
                            added += 2*(v/sw.getSts().size());
                        }
                    }
                }
            }
        }
        return added;
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
        
    
    
}
