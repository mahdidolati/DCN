/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.List;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class RunMy {
    public static void main(String[] args) {
//        My m1 = new My(3);
//        m1.createSuperVms();
//        My m2 = m1.copy();
//        //m2.createSuperVms();
////        System.out.println("bw,conn: " + m1.getEBw() + " " + m1.getEConnNum());
////        System.out.println("bw,conn: " + m2.getEBw() + " " + m2.getEConnNum());
//        double serverCapacity = 80.0;
//        
//        DevicePowerUsageModel powModel = new DevicePowerUsageModel(100.0, 400.0, 0.0);
//        int serverNum = m1.bestServerNumber(powModel);
//        double total = m1.getTotalWork();
//        
//        double p1 = 0.0;
//        double p2 = 0.0;
//        
//        //distribute vms
//        //distribute super vms
//        List<Switch> edges = m1.edgeTraffic();
////        System.out.println(">>" + edges.size());
////        for(Switch s : edges) {
////            System.out.println("" + s.getSwitchTraffic().size() + "::::");
////            for(SwitchTraffic st : s.getSwitchTraffic())
////                System.out.print("" + st.j + ", " + st.superVm + "; ");
////            System.out.println("");
////        }
//        List<Switch> aggs = m1.getAgg(edges);
//        List<Switch> aggAux = m1.assignAgg();
////        System.out.println("SIZESIZESIZE: " + aggAux.size());
//        List<Switch> cores = m1.getCore(aggAux);
////        System.out.println("es: " + edges.size() + ", as: " + aggs.size() + ", cs: " + cores.size());
//        
//        List<List<Switch>> ret = m2.edgeTrafficTeir();
//        List<Switch> edges2 = ret.get(0);
////        System.out.println(">>" + edges2.size());
////        for(Switch s : edges2) {
////            System.out.println("" + s.getSwitchTraffic().size() + "::::");
////            for(SwitchTraffic st : s.getSwitchTraffic())
////                System.out.print("" + st.j + ", " + st.superVm + "; ");
////            System.out.println("");
////        }
//        List<Switch> aggs2 = m2.getAgg(edges2);
//        List<Switch> aggAux2 = ret.get(1);
//        List<Switch> cores2 = m1.getCore(aggAux2);
//        
//        
//        double ret1 = CalCulator.getConsumptionEdge(edges, powModel);
////        System.out.println("edge: " + ret1);
//        double ret2 = CalCulator.getConsumptionEdge(edges2, powModel);
////        System.out.println("edge: " + ret2);
//        p1 += ret1;
//        p2 += ret2;
//        
//        ret1 = CalCulator.getConsumptionAgg(aggs, powModel);
////        System.out.println("agg1: " + ret1);
//        p1 += ret1;
//        ret2 = CalCulator.getConsumptionAggOrCore(aggs2, powModel);
////        System.out.println("agg2: " + ret2);
//        p2 += ret2;
//        
//        ret1 = CalCulator.getConsumptionCoreBest(cores, powModel);
////        System.out.println("core: " + ret1);
//        ret2 = CalCulator.getConsumptionCoreBest(cores2, powModel);
////        System.out.println("core: " + ret2);
//        p1 += ret1;
//        p2 += ret2;
//        
//        ret1 = CalCulator.getConsumptionServer(total, serverNum, powModel);
////        System.out.println("srv: " + ret1);
//        p1 += ret1;
//        p2 += ret1;
        
//        System.out.println("a: " + p1 + " b:" + p2 + " p: " + 100.0*Math.abs(p1-p2)/p2 );
        
    }
}
