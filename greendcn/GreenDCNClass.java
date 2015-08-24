/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package greendcn;

import GomoryHuP.CutProxy;
import MyPackage.MyPack.JobMy;
import MyPackage.MyPack.My;
import MyPackage.MyPack.ReturnTrCpu;
import MyPackage.MyPack.Server;
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
            Server ser = new Server();
            for(Server server : sw.getServers()) {
                ser.merge(server.getCopy());
            }
            SwitchTraffic st = ser.getInOut();
            sw.add(st);
        }
        return pods;
    }
   
    public List<Switch> getAgg(List<Switch> edges) {   
        List<Switch> agg = new ArrayList<>();
        for(Switch sw : edges) {
            Server server = new Server();
            Switch s = new Switch();
            for(Server serv : sw.getServers()) {
                //cheraaaaa?????
                Server tServ = serv.getCopy();
                server.merge(tServ);
//                s.addServer(serv);
            }
            s.addServer(server);
            SwitchTraffic st = server.getInOut();
//            for(JobMy j : this.jobs) {
//                double [] res = j.getSwitchTrafficAgg(sw, s);
//                if(res == null)
//                    continue;
////                System.out.println("HOOOOOR");
//                st.in += res[0];
//                st.out += res[1];
//            }
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
    
    public List<List<Switch>> assignSuperVmsToServers(double maxLoad) {
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
            for(JobMy j : this.jobs) {
                if(j.unassignedSupers() > 0) {
                    //be tedade assign nashodehaye in job be in edges ezafe mishavad
                    //albate momkene hamash assign nashode bashe ke dore bad assign khahand shod
//                    serv += j.unassignedSupers();
                    //------------??
                    j.assignSuperToSwitchTorMin(edges, pod, maxLoad);
                    //sooti ha tabaq tabaq
                    
//                    if(serv >= HALF_K*HALF_K)
//                        break;
                }
            }
            for(Switch hasLoad : edges) {
                if(!hasLoad.getServers().isEmpty()) {
                    ed.add(hasLoad);
                }
            }
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
    
    public void createSuperVms2(double serverMaxUsage) {
        for(JobMy j : this.jobs) {
            j.createSuperVm(serverMaxUsage, null);
        }
    }
    
    public void createSuperVms(DevicePowerUsageModel powModel) {
        this.servers = new ArrayList<>();
        int num = 0;
        for(JobMy j : this.jobs) {
            j.createSuperVm(powModel.getMaxUsage(), powModel);
            num += j.cpu.size();
//            this.servers.addAll(j.servers);
        }
        System.out.println(":(((" + num);
    }

    public void createSuperVmsElastic(DevicePowerUsageModel powModel) {
        double serverCap = powModel.getMaxUsage();
        for(JobMy j : this.jobs) {
            j.createSuperElastic(powModel.getMaxUsage());
        }
        servers = new ArrayList<>();
        for(JobMy jj : this.jobs) {
            servers.addAll(jj.servers);
        }
        boolean hadAssign;
        do{
            hadAssign = false;
            for(int i=0; i<servers.size(); ++i) {
                for(int j=0; j<servers.size(); ++j) {
                    if(i == j)
                        continue;
                    if(servers.get(i).curLoad+servers.get(j).curLoad>serverCap)
                        continue;
                    servers.get(i).merge(servers.get(j));
                    servers.get(i).curLoad += servers.get(j).curLoad;
                    servers.remove(j);
                    hadAssign = true;
                    break;
                }
            }
        }while(hadAssign);
        
        int nm = 0;
        for(Server server : this.servers) {
            for(JobMy j : server.hosted.keySet()) {
                nm += server.hosted.get(j).size();
            }
        }
//        System.out.println("--------------------------------------------------------" + nm);
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

    public void calculateCuts() {
        for(JobMy j : this.jobs) {
//            System.out.println("super green vm size: " + j.getSuperTrafficMatrix().size() + " " + j.getSuperVmCpu().size());
            j.calculateCuts();
        }
    }

    public int getServerNum() {
        return this.servers.size();
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
        int num = 0;
        for(Server server : this.servers) {
            if(server.deleted)
                System.err.println("getget:Reporting deleted server!");
            ll.add(server.curLoad);
            for(JobMy j : server.hosted.keySet()) {
                num += server.hosted.get(j).size();
            }
        }
        System.out.println("getget:" + num);
        return ll;
    }

    public List<Server> servers;
    
    public void consolidate(double serverCap) {
        servers = new ArrayList<>();
        Server curServer;
        for(JobMy jj : this.jobs) {
            servers.addAll(jj.servers);
//            for(int i=0; i<jj.getSuperVmCpu().size();++i) {
//                curServer = new Server(serverCap);
//                ReturnTrCpu rtc = new ReturnTrCpu();
//                rtc.index = i;
//                rtc.cpu = jj.getSuperVmCpu().get(i);
//                rtc.j = jj;
//                curServer.addCandidate(rtc);
//                servers.add(curServer);
//            }
        }
//        boolean hadAssign;
//        do{
//            hadAssign = false;
//            for(int i=0; i<servers.size(); ++i) {
//                double between = -1;
//                for(int j=0; j<servers.size(); ++j) {
//                    if(i == j)
//                        continue;
//                    if(servers.get(i).deleted || servers.get(j).deleted)
//                        continue;
//                    if(servers.get(i).curLoad+servers.get(j).curLoad>serverCap)
//                        continue;
////                    double curBetween = servers.get(i).getBetween(servers.get(j));
//                    servers.get(i).merge(servers.get(j));
//                    servers.get(i).curLoad += servers.get(j).curLoad;
//                    servers.get(j).deleted = true;
//                    hadAssign = true;
//                }
//            }
//        }while(hadAssign);
    }

    public double averageServerUtilization2() {
        double sum = 0.0;
        double sum2 = 0.0;
        double num = 0;
        for(Server srv : this.servers) {
            sum += srv.curLoad;
            sum2 += Math.pow(srv.curLoad, 2);
            num++;
        }
        double stdDev = Math.sqrt(sum2/num-Math.pow(sum/num, 2));
        return sum/num;
    }
    
    public double stdDevServerUtilization2() {
        double sum = 0.0;
        double sum2 = 0.0;
        double num = 0;
        for(Server srv : this.servers) {
            sum += srv.curLoad;
            sum2 += Math.pow(srv.curLoad, 2);
            num++;
        }
        double stdDev = Math.sqrt(sum2/num-Math.pow(sum/num, 2));
        return stdDev;
    }

    public List<Switch> getHostToEdgeByServer(List<Server> srvs, int groupSize) {
        int num = 0;
        Switch sw = new Switch();
        List<Switch> sws = new ArrayList<>();
        for(Server srv : srvs) {
            if(num >= groupSize) {
                sws.add(sw);
                sw = new Switch();
                num = 0;
            }
            sw.add(srv.getInOut());
            sw.addServer(srv);
            num++;
        }
        if(sw.getServers().isEmpty() == false)
            sws.add(sw);
        return sws;
    }
    
    public List<Switch> getEdgeToAggByServer(List<Switch> hostToEdge, int groupSize) {
        List<Switch> copyyy = new ArrayList<>();
        for(Switch sww : hostToEdge) {
            copyyy.add(sww.getCopy());
        }
        hostToEdge = copyyy;
        
        List<Server> srvs = new ArrayList<>();
        for(Switch sw : hostToEdge) {
            Server srv = new Server();
            for(Server oSrv : sw.getServers()) {            
                srv.merge(oSrv);
            }
            srvs.add(srv);
        }
        
        
        List<Switch> toRet = getHostToEdgeByServer(srvs, groupSize);
        
        return toRet;
    }
    
    public List<Switch> getInterPodByServer(List<Switch> edgeToAgg, int groupSize) {
        List<Switch> rr = getEdgeToAggByServer(edgeToAgg, groupSize);
        for(Switch sw : rr) {
            boolean f = true;
            Server ss = null;
            for(Server s : sw.getServers()) {
                if(f) {
                    f = false;
                    ss = s;
                }else{
                    ss.merge(s.getCopy());
                    ss.curLoad += s.curLoad;
                }
            }
            List<Server> l = new ArrayList<>();
            l.add(ss);
            sw.setServers(l);
        }
        return rr;
    }


    public void fillServers() {
       int num = 0;
        for(JobMy j : this.jobs) {
           for(Server s : j.servers) {
               if(s.deleted == false) {
                   this.servers.add(s);
               }
           }
       }
        double v= 0;
        for(Server s : this.servers) {
            v += s.curLoad;
            for(JobMy j : s.hosted.keySet()) {
                num += s.hosted.get(j).size();
            }
        }
        System.out.println("bad az por kardan: " + num + " " + v);
    }

    
}
