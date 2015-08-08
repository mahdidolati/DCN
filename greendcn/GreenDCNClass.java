/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import GomoryHuP.CutProxy;
import MyPackage.MyPack.JobMy;
import MyPackage.MyPack.My;
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
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class GreenDCNClass {
    public List<JobMy> jobs;
    public int jobNumber;
    
    //40, 100
    public GreenDCNClass() {
        this.jobNumber = 2;
        jobs = new ArrayList<>();
    }
    
    public double runAlg() {
        //this.createSuperVms(); 
        //find best n
//        int serverNum = this.bestServerNumber();
//        double total = this.getTotalWork();
//        //distribute vms
//        //distribute super vms
//        List<Switch> edges = this.edgeTraffic();
//        List<List<Switch>> ret = this.edgeTrafficTeir();
//        //
//        List<Switch> aggs = this.getAgg(edges);
//        List<Switch> aggs2 = this.getAgg(edges2);
//        //
//        List<Switch> aggAux = this.assignAgg();
//        System.out.println("SIZESIZESIZE: " + aggAux.size());
//        List<Switch> cores = this.getCore(aggAux);
//        System.out.println("es: " + edges.size() + ", as: " + aggs.size() + ", cs: " + cores.size());
//        //into pods
//        //obtain traffic
//        //return everything needed
//        DevicePowerUsageModel powModel = new DevicePowerUsageModel(100.0, 400.0, 1.0);
//        double p1 = 0.0;
//        double p2 = 0.0;
//        double ret = CalCulator.getConsumptionEdge(edges, powModel);
//        System.out.println("edge: " + ret);
//        p1 += ret;
//        p2 += ret;
//        
//        ret = CalCulator.getConsumptionAgg(aggs, powModel);
//        System.out.println("agg1: " + ret);
//        p1 += ret;
//        ret = CalCulator.getConsumptionAggOrCore(aggs, powModel);
//        System.out.println("agg2: " + ret);
//        p2 += ret;
//        
//        ret = CalCulator.getConsumptionCore(cores, powModel);
//        System.out.println("core: " + ret);
//        p1 += ret;
//        p2 += ret;
//        
//        ret = CalCulator.getConsumptionServer(total, serverNum, powModel);
//        System.out.println("srv: " + ret);
//        p1 += ret;
//        p2 += ret;
//        
//        System.out.println("a: " + p1 + " b:" + p2);
        
        return 0.0;
    }
    
    public List<Switch> getCore(List<Switch> pods) {      
        for(Switch sw : pods) {
//            Switch s = new Switch();
            SwitchTraffic st = new SwitchTraffic();
            st.in = 0.0;
            st.out = 0.0;
            for(JobMy j : this.jobs) {
                double [] res = j.getSwitchTrafficCore(sw);
                if(res == null)
                    continue;
                st.in += res[0];
                st.out += res[1];
            }
            sw.add(st);
//            core.add(s);
        }
        return pods;
    }
   
    public List<Switch> getAgg(List<Switch> edges) {   
        List<Switch> agg = new ArrayList<>();
        for(Switch sw : edges) {
            Switch s = new Switch();
            SwitchTraffic st = new SwitchTraffic();
            st.in = 0.0;
            st.out = 0.0;
            for(JobMy j : this.jobs) {
                double [] res = j.getSwitchTrafficAgg(sw);
                if(res == null)
                    continue;
//                System.out.println("HOOOOOR");
                st.in += res[0];
                st.out += res[1];
            }
            s.add(st);
            agg.add(s);
        }
        return agg;
    }
    
    public List<Switch> assignAgg() {
        List<Switch> edges = new ArrayList<>();
        int srv = FatTree.getK()*FatTree.getK()/4;
        int num = 0;
        Switch sw = new Switch();
        edges.add(sw);
        for(JobMy j : this.jobs) {
            for(int i=0; i<j.getNumSuper(); ++i) {
                if(num > srv) {
                    num = 0;
                    sw = new Switch();
                    edges.add(sw);
//                    System.out.println("++---------------------");
                }
                j.getSwitchForCore(i, sw);
                num++;
            }
        }
        return edges;
    }
    
    public List<Switch> edgeTraffic() {
        List<Switch> edges = new ArrayList<>();
        Switch sw = new Switch();
        edges.add(sw);
        int srv = FatTree.getK()/2;
        int num = 0;
        for(JobMy j : this.jobs) {
            for(int i=0; i<j.getNumSuper(); ++i) {
                if(num >= srv) {
                    num = 0;
                    sw = new Switch();
                    edges.add(sw);
//                    System.out.println("---------------------");
                }
                sw.add(j.getSwitchFor(i, sw));
                num++;
            }
        }
        return edges;
    }
    
    public boolean isAllJobsAssigned() {
        boolean finish = true;
        for(JobMy j : this.jobs)
            if(j.allAssigned() == false) {
                finish = false;
                break;
            }
        return finish;
    }
    
    public List<List<Switch>> assignSuperVmsToServersElastic() {
        List<List<Switch>> ret = new ArrayList<>();
        List<Switch> ed = new ArrayList<>();
        List<Switch> podRepresentative = new ArrayList<>();
        Switch p = new Switch();
        int edInPodSize = 0;
        Switch s = new Switch();
        ed.add(s);
        while(true) {
            if(this.isAllJobsAssigned())
                break;
            if(s.hasFree() == false) {
                s = new Switch();
                ed.add(s);
                edInPodSize++;
            }
            if(edInPodSize > FatTree.getK()/2) {
                p = new Switch();
                podRepresentative.add(p);
                edInPodSize = 0;
            }
            for(JobMy j : this.jobs) {
                if(j.allAssigned())
                    continue;
                if(s.hasFree()) {
                    s.add(j.newSwitchAdd(s, p));
                }else
                    break;
            }
        }
        ret.add(ed);
        ret.add(podRepresentative);
        return ret;
    }
    
    public List<List<Switch>> assignSuperVmsToServers() {
        List<List<Switch>> ret = new ArrayList<>();
        List<Switch> ed = new ArrayList<>();
        List<Switch> podRepresentative = new ArrayList<>();
        int HALF_K = FatTree.getK()/2;
        while(true) {
            boolean finish = true;
            for(JobMy j : this.jobs)
                if(j.allAssigned() == false) {
                    finish = false;
                    break;
                }
            if(finish == true)
                break;
            Switch pod = new Switch();
            podRepresentative.add(pod);
            List<Switch> edges = new ArrayList<>();
            for(int i=0; i<HALF_K; i++) {
                edges.add(new Switch());
            }
            int serv = 0;
            for(JobMy j : this.jobs) {
                if(j.unassignedSupers() > 0) {
                    //be tedade assign nashodehaye in job be in edges ezafe mishavad
                    //albate momkene hamash assign nashode bashe ke dore bad assign khahand shod
                    serv += j.unassignedSupers();
                    //------------??
                    j.assignSuperToSwitchTorMin(edges, pod);
                    if(serv >= HALF_K)
                        break;
                }
            }
            ed.addAll(edges);
        }
        ret.add(ed);
        ret.add(podRepresentative);
        return ret;
    }
    
    public List<List<Switch>> edgeTrafficTeir() {
        List<List<Switch>> ret = new ArrayList<>();
        List<Switch> edges = new ArrayList<>();
        List<Switch> pods = new ArrayList<>();
        ret.add(edges);
        ret.add(pods);
        Switch pod = null;
        List<Switch> l = new ArrayList<>();
        edges.addAll(l);
        int srv = FatTree.getK()/2;
        while(true) {
            boolean finish = true;
            for(JobMy j : this.jobs)
                if(j.allAssigned() == false) {
                    finish = false;
                    break;
                }
            if(finish == true)
                break;
            for(int i=0; i<srv; ++i) {
                l.add(new Switch());
            }
            pod = new Switch();
            pods.add(pod);
            while(true) {
                boolean f = true;
                for(int i=0; i<l.size(); ++i) {
                    if(l.get(i).getSwitchTraffic().size() < srv)
                        f = false;
                }
                if(f == true)
                    break;
                f = true;
                for(JobMy j : this.jobs)
                    if(j.allAssigned() == false) {
                        f = false;
                        break;
                    }
                if(f == true)
                    break;
                for(int i=0; i<l.size(); ++i) {
                    if(l.get(i).getSwitchTraffic().size() >= srv)
                        continue;
                    if(i != l.size()-1) {
                        for(JobMy j : this.jobs) {
                            if(l.get(i).getSwitchTraffic().size() < srv && j.allAssigned()==false) {
                                l.get(i).add(j.newSwitchAdd(l.get(i), pod));
                            } 
                        }
                    }else{
                        for(JobMy j : this.jobs) {
                            while(l.get(i).getSwitchTraffic().size() < srv && j.allAssigned()==false) {
                                l.get(i).add(j.newSwitchAdd(l.get(i), pod));
                            }
                        }
                    }
                }
            }
            edges.addAll(l);
        }
        return ret;
    }
    
    public int bestServerNumber(DevicePowerUsageModel powModel, DevicePowerUsageModel powModelServer) {
        
        ServerPowerCalc srvPow = new ServerPowerCalc(powModelServer);
        NetworkPowerCalc netPow = new NetworkPowerCalc(powModel);
        
        double totalWork = this.getTotalWork();
        double bw = this.getEBw();
        double vm = this.getEConnNum();
        
        //
        //configure and run this experiment
        NondominatedPopulation result = new Executor()
                        .withProblemClass(ServerNumProblem.class, 
                                srvPow, netPow, totalWork, powModelServer.getMaxUsage(), vm, bw, FatTree.getK())
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
                return Double.valueOf(solution.getVariable(0).toString()).intValue();
        }
        
        //no solution
        return -1;
    }
    
    public double getEBw() {
        double sum = 0.0;
        int num = 0;
        for(int i=0; i<jobs.size(); ++i) {
            sum += jobs.get(i).getSumBWSuper();
            num += jobs.get(i).getNumSuper();
        }
        return sum/(num*num);
    }
    
    public double getEConnNum() {
        int connNum = 0;
        int num = 0;
        for(int i=0; i<jobs.size(); ++i) {
            connNum += jobs.get(i).getSumConnNumber();
            num += jobs.get(i).getNumSuper();
        }
        return (double)connNum/(num*num);
    }
    
    public double getTotalWork() {
        double sum = 0.0;
        for(int i=0; i<jobs.size(); ++i) {
            sum += jobs.get(i).getTotalWork();
        }
        return sum;
    }
    
    public void generateJobs() {
        for(int i=0; i<jobNumber; ++i) {
//            JobMy j = new JobMy(new VmNumberGenerator(25), new TrafficGenerator(10.0, 1.0), new CpuUsage());
//            jobs.add(j);
        }
    }
    
    public void createSuperVms(DevicePowerUsageModel powModel) {
        for(JobMy j : this.jobs) {
            j.createSuperVm(powModel.getMaxUsage(), powModel);
//            System.out.println("agg size: " + j.getSuperTrafficMatrix().size());
        }
    }

    public void createSuperVmsElastic(DevicePowerUsageModel powModel) {
        for(JobMy j : this.jobs) {
            j.createSuperElastic(powModel.getMaxUsage());
        }
    }
    
    public GreenDCNClass copy() {
        GreenDCNClass m = new GreenDCNClass();
        m.jobNumber = this.jobNumber;
        m.jobs = new ArrayList<>();
        for(JobMy j : this.jobs) {
            m.jobs.add(j.copy());
        }
        return m;
    }

    void calculateCuts() {
        for(JobMy j : this.jobs) {
//            System.out.println("super green vm size: " + j.getSuperTrafficMatrix().size() + " " + j.getSuperVmCpu().size());
            j.calculateCuts();
        }
    }

    public int getServerNum() {
        int n = 0;
        for(JobMy j : this.jobs) {
            n += j.getSuperTrafficMatrix().size();
        }
        return n;
    }
    
    public double sumSuperTrafficMatrix() {
        double sum = 0;
        for(JobMy j : this.jobs) {
            sum += j.sumSuperTrafficMatrix();
        }
        return sum;
    }
    
    public List<Double> getget() {
        List<Double> ll = new ArrayList<>();
        for(JobMy j : this.jobs) {
            for(Double d : j.getSuperVmCpu()) {
//                System.out.print("" + d + " ");
                ll.add(d);
            }
//            System.out.println("");
        }
        return ll;
    }
    
}
