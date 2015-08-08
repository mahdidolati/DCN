/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ElasticTree;

import greendcn.GreenDCNClass;
import greendcn.power.CalCulator;
import greendcn.power.DevicePowerUsageModel;
import java.util.List;
import utility.FatTree;
import utility.Switch;

/**
 *
 * @author mahdi
 */
public class RunElasticAlgorithm {
    public static double run(GreenDCNClass g, DevicePowerUsageModel powModel, 
            DevicePowerUsageModel powModelServer, DevicePowerUsageModel powModelLink) {
        //create super vms
        System.out.println("------------ELASTIC----------");
        g.createSuperVmsElastic(powModelServer);
        System.out.println("Sum of traffic in network: " + g.sumSuperTrafficMatrix());
        List<List<Switch>> ret = g.assignSuperVmsToServersElastic();
        //voroodi va khorooji har edge hast be server
        List<Switch> edges = ret.get(0);
//        if(edges.size() > FatTree.getK()*FatTree.getK()/2)
//            return -1;
        //voroodi va khorooji har edge be agg
        List<Switch> aggs2 = g.getAgg(edges);
        List<Switch> aggAux2 = ret.get(1);
        //voroodi khorooji har pod hast??
        List<Switch> cores2 = g.getCore(aggAux2);        
        
        double p2[] = new double[3];
    
        double ret1[];
     
        CalCulator.mode = CalCulator.MIN;
        
        ret1 = CalCulator.getConsumptionEdge(edges, aggs2, powModel, powModelLink);
        System.out.print("edge pow: " + ret1[1] + ", num: " + ret1[0]);
        p2[0] = ret1[0];
        p2[1] = ret1[1];
        
        ret1 = CalCulator.getAggConsumption(aggs2, cores2, powModel, powModelLink);
        System.out.print(", agg: " + ret1[1] + ", num: " + ret1[0]);
        p2[0] += ret1[0];
        p2[1] += ret1[1];
     
        ret1 = CalCulator.getConsumptionCore(cores2, powModel, powModelLink);
        System.out.print(", core: " + ret1[1] + ", num: " + ret1[0]);
        p2[0] += ret1[0];
        p2[1] += ret1[1];
     
        System.out.print(", network power: " + p2[1] + ", all switch num: " + p2[0]);
        
        ret1 = CalCulator.getConsumptionServer(g.getTotalWork(), g.getServerNum(), powModelServer);
        System.out.println(", srv: " + ret1[1]);
        p2[1] += ret1[1];
        p2[2] = ret1[0];
        
        System.out.println("" + "switchNum: " + p2[0] + ", serverNum: " + p2[2]);
        System.out.println("------------ELASTIC END----------");
        return p2[1];
    }
}
