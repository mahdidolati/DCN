/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package run;

import ElasticTree.RunElasticAlgorithm;
import MyPackage.MyPack.My;
import MyPackage.MyPack.MyAlgorithmStateSpace;
import MyPackage.MyPack.RunMyAlgorithm;
import MyPackage.MyPack.Server;
import greendcn.GreenDCNClass;
import greendcn.RunGreenDCNFrameWork;
import greendcn.power.DevicePowerUsageModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.fife.ui.rsyntaxtextarea.modes.RubyTokenMaker;
import utility.FatTree;
import utility.LogUtil;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */

public class Run1 {
    public static void main(String[] args) throws IOException {
        //b' for each device :((((((((((((((
        for(int centerNum=3; centerNum<10; ++centerNum) {
            LogUtil.LOGGER.log(Level.INFO, "cenNum {0}", centerNum);
            Map<String, Double> res = new HashMap<>();

            double swUsage = 300.0;
            double linkUsage = 5.0;
            double swFabricUsage = swUsage - (FatTree.getK()*linkUsage);
            double srvUsage = 2*swUsage;

            FileWriter bruteForce = new FileWriter(new File(""+centerNum+".different_server_size_brute.csv"));
            FileWriter algsCompare = new FileWriter(new File(""+centerNum+".me_green_elastic.csv"));
            FileWriter myCompare = new FileWriter(new File(""+centerNum+".my_compare.csv"));
            FileWriter bestNumExp = new FileWriter(new File(""+centerNum+".bestNumExp.csv"));
            FileWriter fw3 = new FileWriter(new File(""+centerNum+".tt.csv"));
            double serverCapacity = new Server().maxCap;
            int maxRound = 5;
            int minVmNum = 5000;
            int maxVimNum = 5001;
            int islandSize = 3*FatTree.getK();
            int trStep = 10;
            int propStep = 10;
            double propInit = 0.87;
            double propInc = 0.02;
            double multTraffic = 200.0;
            double edgeLinkMaxUsage = 10000;
            double aggLinkMaxUsage = edgeLinkMaxUsage;
            double coreLinkMaxUsage = aggLinkMaxUsage;
            MyAlgorithmStateSpace stateSpace = new MyAlgorithmStateSpace();
            RunGreenDCNFrameWork greenFramework = new RunGreenDCNFrameWork();
            RunElasticAlgorithm elasticAlg = new RunElasticAlgorithm();
            RunMyAlgorithm myAlg = new RunMyAlgorithm();
            myAlg.mode = RunMyAlgorithm.ALL;
            RunMyAlgorithm myAlg2 = new RunMyAlgorithm();
            myAlg2.mode = RunMyAlgorithm.BIN;
            RunMyAlgorithm myAlg3 = new RunMyAlgorithm();
            myAlg3.mode = RunMyAlgorithm.GRE;
            Map<Double, List<Map<Double, Output>>> typeHaa = new HashMap<>();

            for(int round=0; round<maxRound; ++round) {
                LogUtil.LOGGER.log(Level.INFO, "Round: {0}", round);
                greenFramework.isCalced = false;
                for(int vmNum=minVmNum; vmNum<maxVimNum; vmNum+=trStep) {
                    double proportionality = propInit;
                    My m2 = new My(minVmNum/islandSize);
                    m2.generateJobs(islandSize, centerNum, multTraffic);

                    for(int i=0; i<propStep; ++i) {
                        proportionality -= propInc;
    //                    proportionality = 0.68;
                        My e = m2.copy();
    //                    GreenDCNClass g = m.copyToGreenDCN();
    //                    GreenDCNClass e = m.copyToGreenDCN();
                        My m = m2.copy();
                        stateSpace.setM1(m);    
                        My myTemp3 = m2.copy();
                        myAlg.setM1(myTemp3);
                        myAlg2.setM1(m2.copy());
                        myAlg3.setM1(m2.copy());
                        GreenDCNClass g = m2.copyToGreenDCN();
                        greenFramework.setG(g);
                        GreenDCNClass elas = m2.copyToGreenDCN();
                        elasticAlg.setG(elas);

                        DevicePowerUsageModel edgePowModel = new DevicePowerUsageModel(edgeLinkMaxUsage, swFabricUsage, proportionality, FatTree.getK());
                        DevicePowerUsageModel aggPowModel = new DevicePowerUsageModel(aggLinkMaxUsage, swFabricUsage, proportionality, FatTree.getK());
                        DevicePowerUsageModel corePowModel = new DevicePowerUsageModel(coreLinkMaxUsage, swFabricUsage, proportionality, FatTree.getK());
                        DevicePowerUsageModel serverPowModel = new DevicePowerUsageModel(serverCapacity, srvUsage, proportionality, 1);
                        DevicePowerUsageModel powModelLink = new DevicePowerUsageModel(edgeLinkMaxUsage, linkUsage, proportionality, 1);
                        double pm, gm, em;
                        pm = 0.0;
                        gm = 0.0;
                        em = 0.0;

                        if(typeHaa.containsKey(proportionality) == false) {
                            Map<Double, Output> vmPlacement1 = new HashMap<>();
                            Map<Double, Output> vmPlacement2 = new HashMap<>();
                            Map<Double, Output> vmPlacement3 = new HashMap<>();
                            Map<Double, Output> vmPlacement4 = new HashMap<>();
                            List<Map<Double, Output>> ssdr = new ArrayList<>();
                            ssdr.add(vmPlacement1);
                            ssdr.add(vmPlacement2);
                            ssdr.add(vmPlacement3);
                            ssdr.add(vmPlacement4);
                            typeHaa.put(proportionality, ssdr);
                        }
    //                    vmPlacementPerformance(m2, serverCapacity, serverPowModel, typeHaa.get(proportionality));

                        elasticAlg.setAggPower(aggPowModel);
                        elasticAlg.setCorePowModel(corePowModel);
                        elasticAlg.setEdgePower(edgePowModel);
                        elasticAlg.setServerPower(serverPowModel);
                        elasticAlg.setLinkPower(powModelLink);
                        elasticAlg.setProportionality(proportionality);
                        elasticAlg.run();
    //                    stateSpace.setEdgePower(edgePowModel);
    //                    stateSpace.setAggPower(aggPowModel);
    //                    stateSpace.setCorePowModel(corePowModel);
    //                    stateSpace.setLinkPower(powModelLink);
    //                    stateSpace.setServerPower(serverPowModel);
    //                    stateSpace.setProportionality(proportionality);
    //                    stateSpace.run();
                        greenFramework.setEdgePower(edgePowModel);
                        greenFramework.setAggPower(aggPowModel);
                        greenFramework.setCorePowModel(corePowModel);
                        greenFramework.setLinkPower(powModelLink);
                        greenFramework.setServerPower(serverPowModel);
                        greenFramework.setProportionality(proportionality);
                        greenFramework.run();
                        myAlg.setEdgePower(edgePowModel);
                        myAlg.setCorePowModel(corePowModel);
                        myAlg.setAggPower(aggPowModel);
                        myAlg.setLinkPower(powModelLink);
                        myAlg.setServerPower(serverPowModel);
                        myAlg.setProportionality(proportionality);
                        myAlg.run();
                        myAlg2.setEdgePower(edgePowModel);
                        myAlg2.setCorePowModel(corePowModel);
                        myAlg2.setAggPower(aggPowModel);
                        myAlg2.setLinkPower(powModelLink);
                        myAlg2.setServerPower(serverPowModel);
                        myAlg2.setProportionality(proportionality);
                        myAlg2.run();
                        myAlg3.setEdgePower(edgePowModel);
                        myAlg3.setCorePowModel(corePowModel);
                        myAlg3.setAggPower(aggPowModel);
                        myAlg3.setLinkPower(powModelLink);
                        myAlg3.setServerPower(serverPowModel);
                        myAlg3.setProportionality(proportionality);
                        myAlg3.run();
    //                    LogUtil.LOGGER.log(Level.INFO, "\t\t\t\t\t\t***dc: {0}, {1}, {2}, {3}***", new Double[]{
    //                        proportionality,
    //                        (elasticAlg.mexps.get(proportionality).getDcConsumption()-
    //                                myAlg.myExp.get(proportionality).getDcConsumption())/
    //                                elasticAlg.mexps.get(proportionality).getDcConsumption(),
    //                        (greenFramework.mexps.get(proportionality).getDcConsumption()-
    //                                myAlg.myExp.get(proportionality).getDcConsumption())/
    //                                greenFramework.mexps.get(proportionality).getDcConsumption(),
    //                        (myAlg.myExp.get(proportionality).getDcConsumption()-
    //                                stateSpace.bexps.get(proportionality).getDcConsumption())/
    //                            myAlg.myExp.get(proportionality).getDcConsumption()
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "nw: {0}, {1}, {2}, {3}", new Double[]{
    //                        proportionality,
    //                        (elasticAlg.mexps.get(proportionality).getNetworkConsumption()-
    //                                myAlg.myExp.get(proportionality).getNetworkConsumption())/
    //                                elasticAlg.mexps.get(proportionality).getNetworkConsumption(),
    //                        (greenFramework.mexps.get(proportionality).getNetworkConsumption()-
    //                                myAlg.myExp.get(proportionality).getNetworkConsumption())/
    //                                greenFramework.mexps.get(proportionality).getNetworkConsumption(),
    //                        (myAlg.myExp.get(proportionality).getNetworkConsumption()-
    //                                stateSpace.bexps.get(proportionality).getNetworkConsumption())/
    //                            myAlg.myExp.get(proportionality).getNetworkConsumption()
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "src: {0}, {1}, {2}, {3}", new Double[]{
    //                        proportionality,
    //                        (elasticAlg.mexps.get(proportionality).getStat(Output.SERVER, Output.VALUE)-
    //                                myAlg.myExp.get(proportionality).getStat(Output.SERVER, Output.VALUE))/
    //                                elasticAlg.mexps.get(proportionality).getStat(Output.SERVER, Output.VALUE),
    //                        (greenFramework.mexps.get(proportionality).getStat(Output.SERVER, Output.VALUE)-
    //                                myAlg.myExp.get(proportionality).getStat(Output.SERVER, Output.VALUE))/
    //                                greenFramework.mexps.get(proportionality).getStat(Output.SERVER, Output.VALUE),
    //                        (myAlg.myExp.get(proportionality).getStat(Output.SERVER, Output.VALUE)-
    //                                stateSpace.bexps.get(proportionality).getStat(Output.SERVER, Output.VALUE))/
    //                            myAlg.myExp.get(proportionality).getStat(Output.SERVER, Output.VALUE)
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "srv: {0}, {1}, {2}", new Double[]{
    //                        myAlg.myExp.get(proportionality).getStat(Output.SERVER, Output.VALUE),
    //                        greenFramework.mexps.get(proportionality).getStat(Output.SERVER, Output.VALUE),
    //                        elasticAlg.mexps.get(proportionality).getStat(Output.SERVER, Output.VALUE)
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "edg: {0}, {1}, {2}", new Double[]{
    //                        myAlg.myExp.get(proportionality).getStat(Output.EDGE, Output.VALUE),
    //                        greenFramework.mexps.get(proportionality).getStat(Output.EDGE, Output.VALUE),
    //                        elasticAlg.mexps.get(proportionality).getStat(Output.EDGE, Output.VALUE)
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "agg: {0}, {1}, {2}", new Double[]{
    //                        myAlg.myExp.get(proportionality).getStat(Output.AGG, Output.VALUE),
    //                        greenFramework.mexps.get(proportionality).getStat(Output.AGG, Output.VALUE),
    //                        elasticAlg.mexps.get(proportionality).getStat(Output.AGG, Output.VALUE)
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "core: {0}, {1}, {2}", new Double[]{
    //                        myAlg.myExp.get(proportionality).getStat(Output.CORE, Output.VALUE),
    //                        greenFramework.mexps.get(proportionality).getStat(Output.CORE, Output.VALUE),
    //                        elasticAlg.mexps.get(proportionality).getStat(Output.CORE, Output.VALUE)
    //                    });
    //                    LogUtil.LOGGER.log(Level.INFO, "{0}, {1}, {2}", new Double[]{
    //                        myAlg.myExp.get(proportionality).getStat(Output.SERVER, Output.NUMBER),
    //                        greenFramework.mexps.get(proportionality).getStat(Output.SERVER, Output.NUMBER),
    //                        elasticAlg.mexps.get(proportionality).getStat(Output.SERVER, Output.NUMBER)
    //                    });
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

    //        Run1.writeBruteForceResults(bruteForce, maxRound, propInit, propStep, propInc, stateSpace);
            Run1.writeCompareMethod(propInit, stateSpace, elasticAlg, greenFramework, myAlg, algsCompare, propStep, propInc);
            Run1.writeCompareMyMethod(propInit, myAlg, myAlg2, myAlg3, myCompare, propStep, propInc);
            Run1.writeBestNumExp(propInit, propInc, propStep, myAlg, bestNumExp);
    //        double proportionality = propInit;
    //        for(int i=0; i<propStep; ++i) {
    //            proportionality -= propInc;
    //            List<Map<Double, Output>> tempThiss = typeHaa.get(proportionality);
    //            fw3.write(""+proportionality+"\r\n");
    //            
    //            writeOneLineOfStatForVmPlacement(tempThiss, fw3, Output.SERVER, Output.VALUE);
    //            writeOneLineOfStatForVmPlacement(tempThiss, fw3, Output.EDGE, Output.ALL_TRAFFIC);
    //            writeOneLineOfStatForVmPlacement(tempThiss, fw3, Output.SERVER, Output.NUMBER);
    //            writeOneLineOfStatForVmPlacement(tempThiss, fw3, Output.AGG, Output.NUMBER);
    //            writeOneLineOfStatForVmPlacement(tempThiss, fw3, Output.CORE, Output.NUMBER);
    //        }
    //        fw3.close();
        }
    }

    private static void writeOneLineOfStatForVmPlacement(List<Map<Double, Output>> tempThiss, 
            FileWriter fw3, String device, String stat) 
            throws IOException {
        for(double percentFull=0.4; percentFull<=1.0; percentFull+=0.1) {
            boolean avval = true;
            for(int ii=0; ii<3; ++ii) {
                double ssv = tempThiss.get(ii).get(percentFull).getStat(device, stat);
                if(avval)
                    fw3.write("" + percentFull + ", ");
                avval = false;
                fw3.write("" + ssv + ", ");
            }
            fw3.write("\r\n");
        }
    }

    private static void vmPlacementPerformance(My m2, 
            double serverCapacity, DevicePowerUsageModel serverPowModel, List<Map<Double, Output>> ret) {
        
        My myVmPlacement = m2.copy();
        GreenDCNClass greenVmPlacement = m2.copyToGreenDCN();
        GreenDCNClass elasticVmPlacement = m2.copyToGreenDCN();
        
        double serverCapDiff = serverCapacity;
        Map<Double, Output> vmPlacement1 = ret.get(0);
        Map<Double, Output> vmPlacement2 = ret.get(1);
        Map<Double, Output> vmPlacement3 = ret.get(2);
        Map<Double, Output> vmPlacement4 = ret.get(3);
        for(double percentFull=0.4; percentFull<=1.0; percentFull+=0.1) {
            if(vmPlacement1.containsKey(percentFull) == false) {
                vmPlacement1.put(percentFull, new Output());
                vmPlacement2.put(percentFull, new Output());
                vmPlacement3.put(percentFull, new Output());
                vmPlacement4.put(percentFull, new Output());
            }
//          System.out.println("how much to full: " + percentFull);
            serverCapDiff = serverCapacity * percentFull;
            double srvConsump, netAll, num;
            //
            myVmPlacement.vmPlacementServerBasedSenderPriority(serverCapDiff, serverCapacity, My.EXCEED_RETAIN);
            srvConsump = getServreConsumption(myVmPlacement.servers, serverPowModel);
            netAll = getAllNetTraffic(myVmPlacement.servers);
            vmPlacement1.get(percentFull).addVal(Output.SERVER, Output.VALUE, srvConsump);
            vmPlacement1.get(percentFull).addVal(Output.EDGE, Output.ALL_TRAFFIC, netAll);
            vmPlacement1.get(percentFull).addVal(Output.SERVER, Output.NUMBER, myVmPlacement.servers.size());
            vmPlacement1.get(percentFull).addVal(Output.AGG, Output.NUMBER, myVmPlacement.averageServerUtilization());
            vmPlacement1.get(percentFull).addVal(Output.CORE, Output.NUMBER, myVmPlacement.stdDevServerUtilization());
            
            System.out.println("m: "
                    + myVmPlacement.servers.size() + " "
                    + srvConsump + " "
                    + netAll + " "
                    );
            
            DevicePowerUsageModel sPowModel = new DevicePowerUsageModel(serverCapDiff, 0,0,1); 
            
            elasticVmPlacement.createSuperVmsElastic(sPowModel);
            srvConsump = getServreConsumption(elasticVmPlacement.servers, serverPowModel);
            netAll = getAllNetTraffic(elasticVmPlacement.servers);
            vmPlacement2.get(percentFull).addVal(Output.SERVER, Output.VALUE, srvConsump);
            vmPlacement2.get(percentFull).addVal(Output.EDGE, Output.ALL_TRAFFIC, netAll);
            vmPlacement2.get(percentFull).addVal(Output.SERVER, Output.NUMBER, elasticVmPlacement.servers.size());
            vmPlacement2.get(percentFull).addVal(Output.AGG, Output.NUMBER, elasticVmPlacement.averageServerUtilization2());
            vmPlacement2.get(percentFull).addVal(Output.CORE, Output.NUMBER, elasticVmPlacement.stdDevServerUtilization2());
            
            System.out.println("e: "
                    + elasticVmPlacement.servers.size() + " "
                    + srvConsump + " "
                    + netAll + " "
                    );
            
            greenVmPlacement.createSuperVms(sPowModel);
//            greenVmPlacement.calculateCuts();
            greenVmPlacement.consolidate(serverCapDiff);
//            greenVmPlacement.assignSuperVmsToServers(serverCapDiff);
//            greenVmPlacement.fillServers();
            srvConsump = getServreConsumption(greenVmPlacement.servers, serverPowModel);
            netAll = getAllNetTraffic(greenVmPlacement.servers);
            vmPlacement3.get(percentFull).addVal(Output.SERVER, Output.VALUE, srvConsump);
            vmPlacement3.get(percentFull).addVal(Output.EDGE, Output.ALL_TRAFFIC, netAll);
            vmPlacement3.get(percentFull).addVal(Output.SERVER, Output.NUMBER, greenVmPlacement.servers.size());
            vmPlacement3.get(percentFull).addVal(Output.AGG, Output.NUMBER, greenVmPlacement.averageServerUtilization2());
            vmPlacement3.get(percentFull).addVal(Output.CORE, Output.NUMBER, greenVmPlacement.stdDevServerUtilization2());
            
            System.out.println("g: "
                    + greenVmPlacement.servers.size() + " "
                    + srvConsump + " "
                    + netAll + " "
                    );
            
        }
    }

    private static double getSuperVmConsumption(GreenDCNClass greenVmPlacement, DevicePowerUsageModel serverPowModel) {
        double val = 0.0;
        for(double d : greenVmPlacement.getget()) {
            val += serverPowModel.getConsumption(d);
        }
        return val;
    }
    
    private static double getServreConsumption(List<Server> servers, DevicePowerUsageModel serverPowModel) {
        double val = 0.0;
        for(Server server : servers) {
            val += serverPowModel.getConsumption(server.curLoad);
        }
        return val;
    }
    
    private static double getAllNetTraffic(List<Server> servers) {
        double val = 0.0;
        for(Server server : servers) {
            SwitchTraffic st = server.getInOut();
            val += st.in;
            val += st.out;
        }
        return val;
    }
    
    private static double getAllNetTrafficSuper(List<Server> servers) {
        double val = 0.0;
        for(Server server : servers) {
            SwitchTraffic st = server.getInOutSuper();
            val += st.in;
            val += st.out;
        }
        return val;
    }
    
    private static void writeCompareMyMethod(double propInit, RunMyAlgorithm myAlg, RunMyAlgorithm myAlg2,
            RunMyAlgorithm myAlg3, FileWriter algsCompare, 
            int propStep, double propInc) throws IOException {
        double proportionality = propInit;
        Map<Double, Output> myExp, myExp2, myExp3;
        myExp = myAlg.getMyExp();
        myExp2 = myAlg2.getMyExp();
        myExp3 = myAlg3.getMyExp();
        algsCompare.write(",");
        for(int i=0; i<propStep; ++i) {
            proportionality -= propInc;
            algsCompare.write(""+proportionality+", ");
        }
        algsCompare.write("\r\n");
        Run1.writeForAllProps(algsCompare, myExp, Output.DC, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.NETWORK, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.SERVER, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.SERVER, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.EDGE, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.AGG, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.DC, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.NETWORK, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.SERVER, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.SERVER, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.EDGE, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp2, Output.AGG, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.DC, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.NETWORK, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.SERVER, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.SERVER, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.EDGE, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp3, Output.AGG, Output.NUMBER, propInit, propStep, propInc);
        algsCompare.close();
    }
    
    private static void writeCompareMethod(double propInit, MyAlgorithmStateSpace stateSpace, RunElasticAlgorithm elasticAlg,
            RunGreenDCNFrameWork greenFramework, RunMyAlgorithm myAlg, FileWriter algsCompare, 
            int propStep, double propInc) throws IOException {
        double proportionality = propInit;
        Map<Double, Output> bexp, mexp, gexp, myExp, minExp;
        bexp = elasticAlg.mexps;
        mexp = stateSpace.mexps;
        gexp = greenFramework.mexps;
        myExp = myAlg.getMyExp();
        minExp = myAlg.getMinExp();
        algsCompare.write(",");
        for(int i=0; i<propStep; ++i) {
            proportionality -= propInc;
            algsCompare.write(""+proportionality+", ");
        }
        algsCompare.write("\r\n");
        Run1.writeForAllProps(algsCompare, bexp, Output.DC, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, bexp, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, bexp, Output.NETWORK, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, bexp, Output.SERVER, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, bexp, Output.SERVER, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, bexp, Output.EDGE, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, bexp, Output.AGG, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.DC, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.NETWORK, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.SERVER, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.SERVER, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.EDGE, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, gexp, Output.AGG, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.DC, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.NETWORK, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.SERVER, Output.VALUE, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.SERVER, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.EDGE, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, myExp, Output.AGG, Output.NUMBER, propInit, propStep, propInc);
        Run1.writeForAllProps(algsCompare, minExp, Output.NETWORK, Output.VALUE, propInit, propStep, propInc);
        algsCompare.close();
    }

    private static void writeForAllProps(FileWriter algsCompare, Map<Double, Output> bexp,
            String d, String s, double propInit, double propStep, double propInc) throws IOException {
        double proportionality = propInit;
        algsCompare.write("" + d + ":" + s + ",");
        for(int i=0; i<propStep; ++i) {
            proportionality -= propInc;
            algsCompare.write("" + bexp.get(proportionality).getStat(d, s) + ", ");
        }
        algsCompare.write("\r\n");
    }
    
    private static void writeBruteForceResults(FileWriter bruteForce, double maxRound,
            double propInit, double propStep, double propInc, MyAlgorithmStateSpace stateSpace) throws IOException {
        double proportionality = propInit;
        for(int i=0; i<propStep; ++i) {
            proportionality -= propInc;
            Map<Double, List<Output>> bexpAll = stateSpace.nBexpsAll;
            bexpAll.get(proportionality).sort(new Comparator<Output>() {
                @Override
                public int compare(Output o1, Output o2) {
                    double s1 = o1.getStat(Output.SERVER, Output.NUMBER);
                    double s2 = o2.getStat(Output.SERVER, Output.NUMBER);
                    if(s1 < s2)
                        return -1;
                    if(s1 > s2)
                        return 1;
                    return 0;
                }
            });
            
            List<Output> commed = null;
            if(maxRound > 25) {
                commed = Run1.averageData2(bexpAll, proportionality);
            }else
                commed = bexpAll.get(proportionality);
            
            for(Output out : commed) {
                bruteForce.write(""+out.getStat(Output.SERVER, Output.NUMBER)+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+(out.getNetworkConsumption()+out.getStat(Output.SERVER, Output.VALUE))+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+out.getStat(Output.SERVER, Output.VALUE)+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+out.getStat(Output.I_SERVER, Output.VALUE)+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+out.getNetworkConsumption()+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+out.getNetworkNumber()+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+out.getStat(Output.AGG, Output.NUMBER)+", ");
            }
            bruteForce.write("\r\n");
            for(Output out : commed) {
                bruteForce.write(""+out.getStat(Output.EDGE, Output.NUMBER)+", ");
            }
            bruteForce.write("\r\n");
        }
        
        bruteForce.close();
    }
    
    private static List<Output> averageData(Map<Double, List<Output>> bexpAll, double proportionality) {
        int con = 0;
        int com = 30;
        List<Output> commed = new ArrayList<>();
        Output oo = new Output();
        for(Output out : bexpAll.get(proportionality)) {
            if(con < com) {
                oo.addOutput(out);
                con++;
            }else {
                con = 0;
                commed.add(oo);
                oo = new Output();
            }
        }
        if(oo.getVals().keySet().size() > 0) {
            commed.add(oo);
        }
        return commed;
    }

    private static List<Output> averageData2(Map<Double, List<Output>> bexpAll, double proportionality) {
        List<Output> commed = new ArrayList<>();
        for(int i=10; i<bexpAll.get(proportionality).size()-10; i++) {
            Output out = new Output();
            for(int j=-10; j<11; ++j) {
                out.addOutput(bexpAll.get(proportionality).get(i+j));
            }
            commed.add(out);
        }
        return commed;
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

    private static void writeBestNumExp(double propInit, double propInc, int propStep, 
            RunMyAlgorithm myAlg, FileWriter bestNumExp) throws IOException {
        double proportionality = propInit;
        for(int i=0; i<propStep; ++i) {
            proportionality -= propInc;
            bestNumExp.write(""+proportionality+"\r\n");
            for(int iii : myAlg.bestNumExp.get(proportionality).keySet()) {
                bestNumExp.write("" + iii +",");
            }
            bestNumExp.write("\r\n");
            for(int iii : myAlg.bestNumExp.get(proportionality).keySet()) {
                bestNumExp.write("" + myAlg.bestNumExp.get(proportionality).get(iii).getStat(Output.DC, Output.VALUE)+",");
            }
            bestNumExp.write("\r\n");
            for(int iii : myAlg.bestNumExp.get(proportionality).keySet()) {
                bestNumExp.write("" + myAlg.bestNumExp.get(proportionality).get(iii).getStat(Output.SERVER, Output.VALUE)+",");
            }
            bestNumExp.write("\r\n");
            for(int iii : myAlg.bestNumExp.get(proportionality).keySet()) {
                bestNumExp.write("" + myAlg.bestNumExp.get(proportionality).get(iii).getStat(Output.NETWORK, Output.VALUE)+",");
            }
            bestNumExp.write("\r\n");
        }
        bestNumExp.close();
    }

}
