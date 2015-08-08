/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import GomoryHuP.CutProxy;
import GomoryHuP.GomoryHu;
import Sedgewick.FlowEdge;
import Sedgewick.FlowNetwork;
import Sedgewick.FordFulkerson;
import greendcn.generator.CpuUsage;
import greendcn.generator.TrafficGenerator;
import greendcn.generator.VmNumberGenerator;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author mahdi
 */

public class Impl {
    public static void main(String[] args) {
        Job j = new Job(new VmNumberGenerator(5), new TrafficGenerator(50.0, 1.0, 5), new CpuUsage());
        j.createSuperVm(10);
        j.print();
        //
        List<CutProxy> ret = GomoryHu.getGoHu(j.getSuperTrafficMatrix());
        for(CutProxy c : ret) {
            c.print();
//            System.out.println("++++");
        }
//        FlowNetwork G = new FlowNetwork(j.getSuperTrafficMatrix());
//        int s = 0;
//        int t = j.getSuperTrafficMatrix().size()-1;
//        FordFulkerson maxflow = new FordFulkerson(G, s, t);
//        System.out.println("Max flow from " + s + " to " + t);
//        for (int v = 0; v < G.V(); v++) {
//            for (FlowEdge e : G.adj(v)) {
//                if ((v == e.from()) && e.flow() > 0)
//                    System.out.println("   " + e);
//            }
//        }
//
//        // print min-cut
//        System.out.print("Min cut: ");
//        for (int v = 0; v < G.V(); v++) {
//            if (maxflow.inCut(v)) System.out.print(v + " ");
//        }
//        System.out.println();
//
//        System.out.println("Max flow value = " +  maxflow.value());
        
    }
}
 