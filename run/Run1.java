/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package run;

import MyPackage.MyPack.My;
import MyPackage.MyPack.MyAlgorithmStateSpace;
import MyPackage.MyPack.RunMyAlgorithm;
import greendcn.GreenDCNClass;
import greendcn.RunGreenDCNFrameWork;
import greendcn.power.DevicePowerUsageModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import utility.FatTree;
import utility.LogUtil;
import utility.Output;

/**
 *
 * @author mahdi
 */

public class Run1 {
    public static void main(String[] args) throws IOException {
        //b' for each device :((((((((((((((
        Map<String, Double> res = new HashMap<>();
        
        double swUsage = 300.0;
        double linkUsage = 5.0;
        double swFabricUsage = swUsage - (FatTree.getK()*linkUsage);
        double srvUsage = swUsage;
        
        FileWriter fw = new FileWriter(new File("out_5.csv"));
        int serverCapacity = 40;
        int maxRound = 15;
        int minVmNum = 1000;
        int maxVimNum = 1001;
        int islandSize = FatTree.getK()*2;
        int trStep = 10;
        int propStep = 10;
        double propInit = 0.95;
        double propInc = 0.03;
        double multTraffic = 50.0;
        double edgeLinkMaxUsage = 5000;
        double aggLinkMaxUsage = 2*edgeLinkMaxUsage;
        double coreLinkMaxUsage = 2*aggLinkMaxUsage;
        MyAlgorithmStateSpace stateSpace = new MyAlgorithmStateSpace();
        RunGreenDCNFrameWork greenFramework = new RunGreenDCNFrameWork();
        
        for(int round=0; round<maxRound; ++round) {
            LogUtil.LOGGER.log(Level.INFO, "Round: {0}", round);
            for(int vmNum=minVmNum; vmNum<maxVimNum; vmNum+=trStep) {
                double proportionality = propInit;
                My m2 = new My(minVmNum/islandSize);
                m2.generateJobs(islandSize, islandSize, multTraffic);
                
                for(int i=0; i<propStep; ++i) {
                    proportionality -= propInc;
                    
                    My e = m2.copy();
//                    GreenDCNClass g = m.copyToGreenDCN();
//                    GreenDCNClass e = m.copyToGreenDCN();
                    My m = m2.copy();
                    stateSpace.setM1(m);
                    GreenDCNClass g = m2.copyToGreenDCN();
                    greenFramework.setG(g);
                    
                    DevicePowerUsageModel edgePowModel = new DevicePowerUsageModel(edgeLinkMaxUsage, swFabricUsage, proportionality, FatTree.getK());
                    DevicePowerUsageModel aggPowModel = new DevicePowerUsageModel(aggLinkMaxUsage, swFabricUsage, proportionality, FatTree.getK());
                    DevicePowerUsageModel corePowModel = new DevicePowerUsageModel(coreLinkMaxUsage, swFabricUsage, proportionality, FatTree.getK());
                    DevicePowerUsageModel powModelServer = new DevicePowerUsageModel(serverCapacity, srvUsage, proportionality, 1);
                    DevicePowerUsageModel powModelLink = new DevicePowerUsageModel(edgeLinkMaxUsage, linkUsage, proportionality, 1);
                    double pm, gm, em;
                    pm = 0.0;
                    gm = 0.0;
                    em = 0.0;

                    stateSpace.setEdgePower(edgePowModel);
                    stateSpace.setAggPower(aggPowModel);
                    stateSpace.setCorePowModel(corePowModel);
                    stateSpace.setLinkPower(powModelLink);
                    stateSpace.setServerPower(powModelServer);
                    stateSpace.setProportionality(proportionality);
                    stateSpace.run();
                    greenFramework.setEdgePower(edgePowModel);
                    greenFramework.setAggPower(aggPowModel);
                    greenFramework.setCorePowModel(corePowModel);
                    greenFramework.setLinkPower(powModelLink);
                    greenFramework.setServerPower(powModelServer);
                    greenFramework.setProportionality(proportionality);
//                    greenFramework.run();
//                    pm = RunMyAlgorithm.run(m, powModel, powModelServer, powModelLink);
//                    gm = RunMyAlgorithm.runServOptNetMin(g, powModel, powModelServer, powModelLink);
//                    em = RunMyAlgorithm.runServerRandomNetBest(e, powModel, powModelServer, powModelLink);
//                    em = RunMyAlgorithm.runServerRandomNetMax(e, powModel, powModelServer);
//                    pm += RunMyAlgorithm.run(m, powModel, powModelServer, powModelLink);
//                    gm += RunGreenDCNFrameWork.run(g, powModel, powModelServer, powModelLink);
//                    em += RunElasticAlgorithm.run(e, powModel, powModelServer, powModelLink);
                }
            }
        }
        
        double proportionality = propInit;
//        fw.write(", ");
        for(int i=0; i<propStep; ++i) {
            proportionality -= propInc;
            Map<Double, Output> bexp, mexp, gexp;
            Map<Double, List<Output>> bexpAll = stateSpace.bexpsAll;
            bexp = stateSpace.bexps;
            mexp = stateSpace.mexps;
            gexp = greenFramework.mexps;
//            diffNumSrv(stateSpace, proportionality, fw);
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getStat(Output.SERVER, Output.NUMBER)+", ");
            }
            fw.write("\r\n");
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getDcConsumption()+", ");
            }
            fw.write("\r\n");
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getStat(Output.SERVER, Output.VALUE)+", ");
            }
            fw.write("\r\n");
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getNetworkConsumption()+", ");
            }
            fw.write("\r\n");
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getNetworkNumber()+", ");
            }
            fw.write("\r\n");
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getStat(Output.AGG, Output.NUMBER)+", ");
            }
            fw.write("\r\n");
            for(Output out : bexpAll.get(proportionality)) {
                fw.write(""+out.getStat(Output.EDGE, Output.NUMBER)+", ");
            }
            fw.write("\r\n");
//            fw.write("b, " + bexp.get(proportionality).toString());
//            fw.write(", ");
//            fw.write("m, " + mexp.get(proportionality).toString());
//            fw.write(", ");
//            fw.write("g, " + gexp.get(proportionality).toString());
//            fw.write("\r\n");
        }
        
        fw.close();
    }

    private static void diffNumSrv(MyAlgorithmStateSpace stateSpace, double proportionality, FileWriter fw) throws IOException {
        boolean cap = true;
        Map<Double, List<Output>> allExps = stateSpace.bexpsAll;
        allExps.get(proportionality).sort(new Comparator<Output>() {
            @Override
            public int compare(Output o1, Output o2) {
                double io1 = o1.getStat(Output.SERVER, Output.NUMBER);
                double io2 = o2.getStat(Output.SERVER, Output.NUMBER);
                if(io1 < io2) {
                    return -1;
                }else if(io2 < io1) {
                    return 1;
                }else
                    return 0;
            }
        });
        if(cap) {
            for(Output ou : allExps.get(proportionality)) {
                fw.write("" + ou.getStat(Output.SERVER, Output.NUMBER) + ", ");
            }
            cap = false;
        }
        fw.write("\r\n");
        fw.write(""+proportionality+",");
        for(Output ou : allExps.get(proportionality)) {
            fw.write("" + ou.getDcConsumption()+", ");
        }
    }

}
