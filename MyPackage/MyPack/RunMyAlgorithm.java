/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.List;
import utility.FatTree;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class RunMyAlgorithm {
    
    public static double run(My m1, DevicePowerUsageModel powModel, 
            DevicePowerUsageModel powModelServer, DevicePowerUsageModel powModelLink) {
        System.out.println("--------------MY-----------------");
        
        m1.createSuperVms(powModelServer);
        System.out.println("Sum of traffic in network : " + m1.sumSuperTrafficMatrix());
        
        double total = m1.getTotalWork();
        System.out.println("Minimum server req: " + m1.getServerNum() + ", for total work: " + total);
        //System.out.println("bw,conn: " + m1.getEBw() + " " + m1.getEConnNum());
        //------
//        
        int serverNum;
        serverNum = m1.bestServerNumber(powModel, powModelServer);
//        if(serverNum > (FatTree.getK()*FatTree.getK()*FatTree.getK()/4)-15)
//            serverNum -= 15;
        System.out.print(", what he says:" + serverNum);
        
        m1.createSuperVmsEachJobSeparate(serverNum, total);
        //------
        serverNum = m1.getServerNum();
        System.out.println(", what we achieve:" + serverNum);
//        int serverNum = m1.getServerNum();
        if(serverNum > FatTree.getK()*FatTree.getK()*FatTree.getK()/4) {
            System.out.println("more servers are needed.");
            return -1;
//            serverNum = FatTree.getK()*FatTree.getK()*FatTree.getK()/4;
//            return serverNum;
        }
        double p1[] = new double[3];
        
        for(int next=0; next<1; next+=4) {
            if(serverNum+next > FatTree.getK()*FatTree.getK()*FatTree.getK()/4)
                break;

            List<Switch> edges = m1.edgeTraffic();
            List<Switch> aggs = m1.getAgg(edges);
            double aggtraffic = 0.0;
            for(Switch w : aggs) {
                for(SwitchTraffic st : w.getSwitchTraffic()) {
                    aggtraffic += st.in;
                    aggtraffic += st.out;
                }
            }
            System.out.println("-----> Aggregate traffic: " + aggtraffic);
    //        System.out.println("My Alg: ");
    //        for(Switch sw : aggs) {
    //            for(SwitchTraffic st : sw.getSwitchTraffic()) {
    //                System.out.println("" + st.in + " " + st.out);
    //            }
    //        }
            List<Switch> aggAux = m1.assignSupersToPods();
            List<Switch> podToCore = m1.getCore(aggAux);

            CalCulator.mode = CalCulator.BEST;
            
            double ret1[];
            ret1 = CalCulator.getConsumptionEdge(edges, aggs, powModel, powModelLink);
            System.out.print("edge: " + ret1[1] + ", num: " + ret1[0]);
            p1[0] = ret1[0];
            p1[1] = ret1[1];

            ret1 = CalCulator.getAggConsumption(aggs, podToCore, powModel, powModelLink);
            System.out.print(", agg1: " + ret1[1] + ", num: " + ret1[0]);
            p1[0] += ret1[0];
            p1[1] += ret1[1];

            ret1 = CalCulator.getConsumptionCore(podToCore, powModel, powModelLink);
            System.out.print(", core: " + ret1[1] + ", num: " + ret1[0]);
            p1[0] += ret1[0];
            p1[1] += ret1[1];

            System.out.print(", net all: " + p1[1] + ", num: " + p1[0]);
            
            ret1 = CalCulator.getConsumptionServer(total, serverNum, powModelServer);
            if(ret1[1] == Double.NaN) {
                System.out.println("\n\n**NNNNNNNNNNNNAAAAAAnnnnnn: " + total + ", " + serverNum + " \n**\n\n");
            }
            System.out.println(", srv: " + ret1[1] + ", num: " + ret1[0]);
            p1[1] += ret1[1];
            p1[2] = ret1[0];

        }
        System.out.println("BEST power my: " + p1[1] + ", switchNum: " + p1[0] + ", serverNum: " + p1[2]);
        System.out.println("-------------- End MY-----------------");
        return p1[1];
   
    }
    
    public static double runServOptNetMin(My m1, DevicePowerUsageModel powModel, 
            DevicePowerUsageModel powModelServer, DevicePowerUsageModel powModelLink) {
        System.out.println("--------------MY Net MIn-----------------");
        m1.createSuperVms(powModelServer);
        System.out.println("NET SUM : " + m1.sumSuperTrafficMatrix());
        
        double total = m1.getTotalWork();
        System.out.println("Minimum server req: " + m1.getServerNum() + ", for total work: " + total);
        //System.out.println("bw,conn: " + m1.getEBw() + " " + m1.getEConnNum());
        //------
//        
        int serverNum;
        serverNum = m1.bestServerNumber(powModel, powModelServer);
//        System.out.print(", what he says:" + serverNucreateSuperVmsEachJobSeparate.createSuperVms(serverNum, total);
        //------
        serverNum = m1.getServerNum();
        System.out.println(", what we achieve:" + serverNum);
//        int serverNum = m1.getServerNum();
        if(serverNum > FatTree.getK()*FatTree.getK()*FatTree.getK()/4) {
            System.out.println("more servers are needed.");
            return -1;
//            serverNum = FatTree.getK()*FatTree.getK()*FatTree.getK()/4;
//            return serverNum;
        }
        double p1[] = new double[3];
        
        for(int next=0; next<1; next+=4) {
            if(serverNum+next > FatTree.getK()*FatTree.getK()*FatTree.getK()/4)
                break;

            List<Switch> edges = m1.edgeTraffic();
            List<Switch> aggs = m1.getAgg(edges);
    //        System.out.println("My Alg: ");
    //        for(Switch sw : aggs) {
    //            for(SwitchTraffic st : sw.getSwitchTraffic()) {
    //                System.out.println("" + st.in + " " + st.out);
    //            }
    //        }
            List<Switch> aggAux = m1.assignSupersToPods();
            List<Switch> cores = m1.getCore(aggAux);

            CalCulator.mode = CalCulator.MIN;
            
            double ret1[];
            ret1 = CalCulator.getConsumptionEdge(edges, aggs, powModel, powModelLink);
            System.out.print("edge: " + ret1[1]);
            p1[0] = ret1[0];
            p1[1] = ret1[1];

            ret1 = CalCulator.getAggConsumption(aggs, cores, powModel, powModelLink);
            System.out.print(", agg1: " + ret1[1]);
            p1[0] += ret1[0];
            p1[1] += ret1[1];

            ret1 = CalCulator.getConsumptionCore(cores, powModel, powModelLink);
            System.out.print(", core: " + ret1[1]);
            p1[0] += ret1[0];
            p1[1] += ret1[1];

            ret1 = CalCulator.getConsumptionServer(total, serverNum, powModelServer);
            if(ret1[1] == Double.NaN) {
                System.out.println("NNNNNNNNNNNNAAAAAAnnnnnn: " + total + ", " + serverNum + " ");
            }
            System.out.println(", srv: " + ret1[1]);
            p1[1] += ret1[1];
            p1[2] = ret1[0];

        }
        System.out.println("BEST power my: " + p1[1] + ", switchNum: " + p1[0] + ", serverNum: " + p1[2]);
        System.out.println("-------------- End MY-----------------");
        return p1[1];
    }
    
    public static double runServerRandomNetBest(My m1, DevicePowerUsageModel powModel, 
            DevicePowerUsageModel powModelServer, DevicePowerUsageModel powModelLink) {
        System.out.println("--------------MY Server Rand-----------------");
        m1.createSuperVmsElastic(powModelServer);
        System.out.println("NET SUM : " + m1.sumSuperTrafficMatrix());
        
        double total = m1.getTotalWork();
        System.out.println("Minimum server req: " + m1.getServerNum() + ", for total work: " + total);
        
        double p1[] = new double[3];
        
        List<List<Switch>> ret = m1.assignSuperVmsToServersElastic();

        int serverNum = m1.getServerNum();
        
        List<Switch> hostToEdge = ret.get(0);
        List<Switch> edgeToAgg = m1.getAgg(hostToEdge);
        List<Switch> aggAux = ret.get(1);
        List<Switch> podToCore = m1.getCore(aggAux);

        CalCulator.mode = CalCulator.BEST;
        
        double ret1[];
        ret1 = CalCulator.getConsumptionEdge(hostToEdge, edgeToAgg, powModel, powModelLink);
        System.out.print("edge: " + ret1[1]);
        p1[0] = ret1[0];
        p1[1] = ret1[1];

        ret1 = CalCulator.getAggConsumption(edgeToAgg, podToCore, powModel, powModelLink);
        System.out.print(", agg1: " + ret1[1]);
        p1[0] += ret1[0];
        p1[1] += ret1[1];

        ret1 = CalCulator.getConsumptionCore(podToCore, powModel, powModelLink);
        System.out.print(", core: " + ret1[1]);
        p1[0] += ret1[0];
        p1[1] += ret1[1];

        ret1 = CalCulator.getConsumptionServer(total, serverNum, powModelServer);
        if(ret1[1] == Double.NaN) {
            System.out.println("NNNNNNNNNNNNAAAAAAnnnnnn: " + total + ", " + serverNum + " ");
        }
        System.out.println(", srv: " + ret1[1]);
        p1[1] += ret1[1];
        p1[2] = ret1[0];

        
        System.out.println("BEST power my: " + p1[1] + ", switchNum: " + p1[0] + ", serverNum: " + p1[2]);
        System.out.println("-------------- End MY-----------------");
        return p1[1];
   
    }
    
    public static double runServerRandomNetMax(My m1, DevicePowerUsageModel powModel, 
            DevicePowerUsageModel powModelServer, DevicePowerUsageModel powModelLink) {
        System.out.println("--------------MY Server Rand-----------------");
        m1.createSuperVmsElastic(powModelServer);
        System.out.println("NET SUM : " + m1.sumSuperTrafficMatrix());
        
        double total = m1.getTotalWork();
        System.out.println("Minimum server req: " + m1.getServerNum() + ", for total work: " + total);
        
        double p1[] = new double[3];
        
        List<List<Switch>> ret = m1.assignSuperVmsToServersElastic();

        int serverNum = m1.getServerNum();
        
        List<Switch> edges = ret.get(0);
        List<Switch> aggAux = ret.get(1);
        List<Switch> aggs = m1.getAgg(edges);
        List<Switch> cores = m1.getCore(aggAux);

        CalCulator.mode = CalCulator.MIN;
        
        double ret1[];
        ret1 = CalCulator.getConsumptionEdge(edges, aggs, powModel, powModelLink);
        System.out.print("edge: " + ret1[1]);
        p1[0] = ret1[0];
        p1[1] = ret1[1];

        ret1 = CalCulator.getAggConsumption(aggs, cores, powModel, powModelLink);
        System.out.print(", agg1: " + ret1[1]);
        p1[0] += ret1[0];
        p1[1] += ret1[1];

        ret1 = CalCulator.getConsumptionCore(cores, powModel, powModelLink);
        System.out.print(", core: " + ret1[1]);
        p1[0] += ret1[0];
        p1[1] += ret1[1];

        ret1 = CalCulator.getConsumptionServer(total, serverNum, powModelServer);
        if(ret1[1] == Double.NaN) {
            System.out.println("NNNNNNNNNNNNAAAAAAnnnnnn: " + total + ", " + serverNum + " ");
        }
        System.out.println(", srv: " + ret1[1]);
        p1[1] += ret1[1];
        p1[2] = ret1[0];

        int allSwitchNum = FatTree.getK()*FatTree.getK()+FatTree.getK();
        System.out.println("--=-=-=-=-=-=-=-=-=-: " + powModel.getIdlePowerFor(allSwitchNum-(int)p1[0]));
        p1[1] = powModel.getIdlePowerFor(allSwitchNum-(int)p1[0]);
        
        System.out.println("BEST power my: " + p1[1] + ", switchNum: " + p1[0] + ", serverNum: " + p1[2]);
        System.out.println("-------------- End MY-----------------");
        return p1[1];
   
    }
}
