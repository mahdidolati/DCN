/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import greendcn.generator.CpuUsage;
import greendcn.generator.TrafficGenerator;
import greendcn.generator.VmNumberGenerator;
import greendcn.power.DevicePowerUsageModel;
import greendcn.power.NetworkPowerCalc;
import greendcn.power.ServerPowerCalc;
import java.util.ArrayList;
import java.util.List;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import utility.FatTree;

/**
 *
 * @author mahdi
 */
public class Simul {
    private static double getBw(List<Job> jobs) {
        double sum = 0.0;
        double num = 0.0;
        for(Job j : jobs) {    
            List<List<Double>> l = j.getSuperTrafficMatrix();
            for(List<Double> row : l) {
                for(Double val : row) {
                    sum += val;
                    num++;
                }
            }
        }
        return sum/num;
    }
    
    private static double getCpu(List<Job> jobs) {
        double sum = 0.0;
        double num = 0.0;
        for(Job j : jobs) {   
            for(Double val : j.getSuperVmCpu()) {
                sum += val;
                num++;
            }
        }
        return sum/num;
    }
    
    private static double getTotalCpu(List<Job> jobs) {
        double sum = 0.0;
        for(Job j : jobs) {   
            for(Double val : j.getSuperVmCpu()) {
                sum += val;
            }
        }
        return sum;
    }
    
    public static void main(String[] args) {
        double serverCapacity = 80.0;
        List<Job> jobs = new ArrayList<>();
        int jobNumber = 40;
        for(int i=0; i<jobNumber; ++i) {
            Job j = new Job(new VmNumberGenerator(10), new TrafficGenerator(50.0, 1.0, 10), new CpuUsage());
            j.createSuperVm(serverCapacity);
            jobs.add(j);
        }
        //
        int k = 15;
        double totalWork = Simul.getTotalCpu(jobs);
        double cpu = Simul.getCpu(jobs);
        double bw = Simul.getBw(jobs);
        DevicePowerUsageModel powModel = new DevicePowerUsageModel(100.0, 400.0, 1.0, FatTree.getK());
        ServerPowerCalc srvPow = new ServerPowerCalc(powModel);
        NetworkPowerCalc netPow = new NetworkPowerCalc(powModel);
        //
        //configure and run this experiment
        NondominatedPopulation result = new Executor()
                        .withProblemClass(ServerNumProblem.class, 
                                srvPow, netPow, totalWork, serverCapacity, cpu, bw/100000, k)
                        .withAlgorithm("NSGAII")
                        .withMaxEvaluations(10000)
                        .run();

        //display the results
//        System.out.format("Objective1  Objective2%n");
        for (Solution solution : result) {
//                System.out.format("%s,,       %.4f,,       %.4f,,        %.4f,,%n",
//                                solution.getVariable(0).toString(),
//                                solution.getObjective(0),
//                                srvPow.getC(Double.valueOf(solution.getVariable(0).toString()), totalWork),
//                                solution.getObjective(1));
        }
        
    }
}
