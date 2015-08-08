/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import greendcn.GreenDCNClass;
import greendcn.ServerNumProblem;
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
public class My {
    List<JobMy> jobs;
    int jobNumber;
    
    //40, 100
    public My(int jn) {
        //was 80
        this.jobNumber = jn;
        jobs = new ArrayList<>();
    }
    
    public List<Switch> getCore(List<Switch> pods) {
//        List<Switch> core = new ArrayList<>();
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
                //System.out.println("HOOOOOR");
                st.in += res[0];
                st.out += res[1];
            }
            s.add(st);
            agg.add(s);
        }
        return agg;
    }
    
    public List<Switch> assignSupersToPods() {
        List<Switch> pods = new ArrayList<>();
        int srvNumInPod = FatTree.getK()*FatTree.getK()/4;
        int num = 0;
        Switch sw = new Switch();
        pods.add(sw);
        for(JobMy j : this.jobs) {
            for(int i=0; i<j.getNumSuper(); ++i) {
                if(num > srvNumInPod) {
                    num = 0;
                    sw = new Switch();
                    pods.add(sw);
                    //System.out.println("++---------------------");
                }
                j.getSwitchForCore(i, sw);
                num++;
            }
        }
        return pods;
    }
    
    public List<Switch> edgeTraffic() {
        List<Switch> edges = new ArrayList<>();
        Switch curEdge = new Switch();
        edges.add(curEdge);
        int srv = FatTree.getK()/2;
        int num = 0;
        for(JobMy j : this.jobs) {
            for(int i=0; i<j.getNumSuper(); ++i) {
                if(num >= srv) {
                    num = 0;
                    curEdge = new Switch();
                    edges.add(curEdge);
                    //System.out.println("---------------------");
                }
                curEdge.add(j.getSwitchFor(i, curEdge));
                num++;
            }
        }
        return edges;
    }
    
    private void sortTrafficMatrix() {
        for(JobMy j : this.jobs) {
            
        }
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
                                srvPow, netPow, totalWork, powModelServer.getMaxUsage(), vm, bw, FatTree.getK(), this.getServerNum())
                        .withAlgorithm("NSGAII")
                        .withMaxEvaluations(10000)
                        .run();

        //display the results
        //System.out.format("Objective1  Objective2%n");
        int rret = -1;
        for (Solution solution : result) {
                System.out.format("%s,,       %.4f,,       %.4f,,        %.4f,,%n",
                                solution.getVariable(0).toString(),
                                solution.getObjective(0),
                                srvPow.getC(Double.valueOf(solution.getVariable(0).toString()), totalWork),
                                solution.getObjective(0));
                rret = Double.valueOf(solution.getVariable(0).toString()).intValue();
                System.out.println("000000>>>>>GENETIC:: " + rret);
        }
        
        //no solution
        return rret;
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
    
    public void generateJobs(int vmNum, int islandSize, double mean) {
        for(int i=0; i<jobNumber; ++i) {
            JobMy j = new JobMy(new VmNumberGenerator(vmNum), new TrafficGenerator(mean, mean/2, islandSize), new CpuUsage());
            jobs.add(j);
        }
    }
    
    public void createSuperVms(DevicePowerUsageModel powModel) {
        for(JobMy j : this.jobs) {
//            j.createSuperVmMy(powModel.getMaxUsage());
            j.createSuperVmOneAtATime(powModel.getMaxUsage(), powModel);
        }
    }

    public void createSuperVmsElastic(DevicePowerUsageModel powModel) {
        for(JobMy j : this.jobs) {
            j.createSuperElastic(powModel.getMaxUsage());
        }
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
        podRepresentative.add(p);
        int edInPodSize = 0;
        System.out.println("DDDDDDD" + this.getServerNum());
        
        while(true) {
            if(this.isAllJobsAssigned())
                break;
            for(JobMy j : this.jobs) {
                if(j.allAssigned())
                    continue;
                Switch s = new Switch();
                ed.add(s);
                edInPodSize++;
                while(true) {
                    if(s.hasFree() == false) {
                        s = new Switch();
                        ed.add(s);
                        edInPodSize++;
                        if(edInPodSize > FatTree.getK()/2) {
                            p = new Switch();
                            podRepresentative.add(p);
                            edInPodSize = 0;
                        }
                    }
                    SwitchTraffic d = j.newSwitchAdd(s, p);
                    if(d != null)
                        s.add(d);
                    else
                        break;
                }
            }
        }
        ret.add(ed);
        ret.add(podRepresentative);
        return ret;
    }
    
    public double sumSuperTrafficMatrix() {
        double sum = 0;
        for(JobMy j : this.jobs) {
            sum += j.sumSuperTrafficMatrix();
        }
        return sum;
    }
    
    public My copy() {
        My m = new My(this.jobNumber);
        m.jobNumber = this.jobNumber;
        m.jobs = new ArrayList<>();
        for(JobMy j : this.jobs) {
            m.jobs.add(j.copy());
        }
        return m;
    }

    public GreenDCNClass copyToGreenDCN() {
        GreenDCNClass m = new GreenDCNClass();
        m.jobNumber = this.jobNumber;
        m.jobs = new ArrayList<>();
        for(JobMy j : this.jobs) {
            m.jobs.add(j.copy());
        }
        return m;
    }
    
    public void createSuperVmsEachJobSeparate(int serverNum, double total) {
        double serverCap = total / serverNum;
        
//        this.sortTrafficMatrix();
        
        for(JobMy j : this.jobs) {
//            j.createSuperVmMy(serverCap);
            j.sortTrafficMatrix();
            j.reset();
            j.createSuperVmMyOneAtATimeFillOne(serverCap);
        }
    }
    
    public void printSizeOfEachVm() {
        for(JobMy j : this.jobs) {
            j.printSizeOfVms();
            System.out.println("");
        }
    }

    public int getServerNum() {
        int n = 0;
        for(JobMy j : this.jobs) {
            n += j.getSuperTrafficMatrix().size();
        }
        return n;
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
