/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import greendcn.power.NetworkPowerCalc;
import greendcn.power.ServerPowerCalc;
import java.util.logging.Level;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;
import utility.LogUtil;

/**
 *
 * @author mahdi
 */

public class ServerNumProblem extends AbstractProblem {

    int maxServerAvail;
    ServerPowerCalc spc;
    NetworkPowerCalc npc;
    double totalWork, serverCap, hTOe, eTOa, aTOc;
    int k, minS, maxSe;
    
    public ServerNumProblem(ServerPowerCalc spc, NetworkPowerCalc npc, 
            double totalWork, double serverCap, 
            double hTOe, double eTOa, double aTOc, int k, int minS) {
        super(1, 1);
        this.maxServerAvail = k*k*k/4;
        this.spc = spc;
        this.npc = npc;
        this.totalWork = totalWork;
        this.serverCap = serverCap;
        this.hTOe = hTOe;
        this.eTOa = eTOa;
        this.aTOc = aTOc;
        this.k = k;
        this.minS = minS;
        this.maxSe = k*k*k/4;
//        System.out.println("data for optimization with genetic algorithm:");
//        System.out.println("k: " + k + "\ntotal work: " + totalWork + "\nserver capacity: " + serverCap);
//        System.out.println("vm: " + vm + "\nbw: " + bw + "\nminimum server: " + minS);
//        System.out.println("max server available: "+this.maxServerAvail+ ", total work: " + this.totalWork + ", servercap: " + this.serverCap);
    }
    
    @Override
    public void evaluate(Solution solution) {
        int[] x = EncodingUtils.getInt(solution);
        if(x[0] > maxSe || x[0] < minS)
            solution.setObjective(0, Double.MAX_VALUE);
        else {
            double f1 = this.spc.getConsumption(x[0], totalWork);
            double f2 = this.npc.getConsumption(x[0], totalWork, serverCap, hTOe, eTOa, aTOc, k);
            solution.setObjective(0, f1+f2);
//            LogUtil.LOGGER.log(Level.INFO, "n, srv, net: {0} {1} {2}", new Double[]{(double)x[0],f1,f2});
        }
    }

    
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(1, 1);
//        System.out.println(">>>>>>>>"+minReq);
        solution.setVariable(0, EncodingUtils.newInt(this.minS, this.maxServerAvail));
        return solution;
    }
    
}
