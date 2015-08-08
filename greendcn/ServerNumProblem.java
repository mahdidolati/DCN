/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import greendcn.power.NetworkPowerCalc;
import greendcn.power.ServerPowerCalc;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

/**
 *
 * @author mahdi
 */

public class ServerNumProblem extends AbstractProblem {

    int maxServerAvail;
    ServerPowerCalc spc;
    NetworkPowerCalc npc;
    double totalWork, serverCap, vm, bw;
    int k, minS;
    
    public ServerNumProblem(ServerPowerCalc spc, NetworkPowerCalc npc, 
            double totalWork, double serverCap, 
            double vm, double bw, int k, int minS) {
        super(1, 1);
        this.maxServerAvail = k*k*k/4;
        this.spc = spc;
        this.npc = npc;
        this.totalWork = totalWork;
        this.serverCap = serverCap;
        this.vm = vm;
        this.bw = bw;
        this.k = k;
        this.minS = minS;
        System.out.println("data for optimization with genetic algorithm:");
        System.out.println("k: " + k + "\ntotal work: " + totalWork + "\nserver capacity: " + serverCap);
        System.out.println("vm: " + vm + "\nbw: " + bw + "\nminimum server: " + minS);
//        System.out.println("max server available: "+this.maxServerAvail+ ", total work: " + this.totalWork + ", servercap: " + this.serverCap);
    }
    
    @Override
    public void evaluate(Solution solution) {
        int[] x = EncodingUtils.getInt(solution);
        double f1 = this.spc.getConsumption(x[0], totalWork);
        double f2 = this.npc.getConsumption(x[0], totalWork, serverCap, vm, bw, k);
        solution.setObjective(0, f1+f2);
        //solution.setObjective(1, f2);
    }
    
    @Override
    public Solution newSolution() {
        Solution solution = new Solution(1, 1);
//        System.out.println(">>>>>>>>"+minReq);
        solution.setVariable(0, EncodingUtils.newInt(this.minS, this.maxServerAvail));
        return solution;
    }
    
}
