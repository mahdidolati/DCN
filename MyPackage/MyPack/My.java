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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import utility.FatTree;
import utility.LogUtil;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class My {
    List<JobMy> jobs;
    int jobNumber;
    public List<Server> servers;
    //40, 100
    public My(int jn) {
        //was 80
        this.jobNumber = jn;
        jobs = new ArrayList<>();
        this.servers = new ArrayList<>();
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
   
//    public List<Switch> getAgg(List<Switch> edges) {
//        List<Switch> agg = new ArrayList<>();
//        for(Switch sw : edges) {
//            Switch s = new Switch();
//            SwitchTraffic st = new SwitchTraffic();
//            st.in = 0.0;
//            st.out = 0.0;
//            for(JobMy j : this.jobs) {
//                double [] res = j.getSwitchTrafficAgg(sw);
//                if(res == null)
//                    continue;
//                //System.out.println("HOOOOOR");
//                st.in += res[0];
//                st.out += res[1];
//            }
//            s.add(st);
//            agg.add(s);
//        }
//        return agg;
//    }
    
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
    
    public Map<Integer,Output> outss;
    
    public int bestServerNumberProbabilityBased(DevicePowerUsageModel serverPow, DevicePowerUsageModel edgePow, 
            DevicePowerUsageModel aggPow, DevicePowerUsageModel corePow, DevicePowerUsageModel linkPow
            ) {
        //if two speak how much speak
        double islandSize = 0.0;
        double eBw = 0.0;
        double connectionNumbers = 0.0;
        double allPairsNumber = 0.0;
        for(JobMy jm : this.jobs) {
            islandSize += jm.getCpu().size();
            for(List<Double> l : jm.getTrafficMatrix()) {
//                islandSize += l.size();
                for(Double d : l) {
                    if(d > 0) {
                        eBw += d;
                        connectionNumbers++;
                    }
                    allPairsNumber++;
                }
            }
        }
        islandSize /= this.jobs.size();
        eBw /= connectionNumbers;
        //what is the probability of speacking?
        double connectionProbability = connectionNumbers/allPairsNumber;
        
        //how many vms can stay on a server
        double eCpu = 0.0;
        double vmNumber = 0.0;
        for(JobMy jm : this.jobs) {
            for(double d : jm.getCpu()) {
                eCpu += d;
                vmNumber++;
            }
        }
        eCpu /= vmNumber;
        //prepare estimation
        ServerPowerCalc srvPow = new ServerPowerCalc(serverPow, eCpu);
        NetworkPowerCalc netPow = new NetworkPowerCalc(edgePow, aggPow, corePow, linkPow);
        
        int minNum = (int)Math.ceil(this.getTotalWork()/serverPow.getMaxUsage());
        int maxAvail = (int)(Math.pow(FatTree.getK(), 3)/4);
        int bestNum = -1;
        double bestConsumption = Double.POSITIVE_INFINITY;
        
        outss = new HashMap<>();
        for(int jj=minNum; jj<maxAvail; ++jj) {
            double servEs = srvPow.getC(jj, this.getTotalWork());
            double netEs = netPow.getConsumptionProbabilityBased(jj, this.getTotalWork(), eCpu, 
                    eBw, connectionProbability, islandSize, FatTree.getK());
            double cur = servEs + netEs;
            Output oo = new Output();
            oo.addVal(Output.SERVER, Output.VALUE, servEs);
            oo.addVal(Output.NETWORK, Output.VALUE, netEs);
            oo.addVal(Output.DC, Output.VALUE, netEs+servEs);
            outss.put(jj, oo);
            if(cur < bestConsumption) {
                bestConsumption = cur;
                bestNum = jj;
            }
        }
        
        return bestNum;
    }
    
    public int bestServerNumber(DevicePowerUsageModel serverPow, DevicePowerUsageModel edgePow, 
            DevicePowerUsageModel aggPow, DevicePowerUsageModel corePow, DevicePowerUsageModel linkPow
            ) {
        
        outss = new HashMap<>();
        
        double cpuAvg = 0.0;
        double numC = 0.0;
        for(JobMy j : this.jobs) {
            for(double d : j.getCpu()) {
                cpuAvg += d;
                numC++;
            }
        }
        cpuAvg /= numC;
        
        ServerPowerCalc srvPow = new ServerPowerCalc(serverPow, cpuAvg);
        NetworkPowerCalc netPow = new NetworkPowerCalc(edgePow, aggPow, corePow, linkPow);
        
        double totalWork = this.getTotalWork();
        List<Switch> hostToEdge = this.getHostToEdgeByServer(this.servers, FatTree.getK()/2);
        double eHoToEd = 0.0;
        double num = 0.0;
        for(Switch sw : hostToEdge) {
            for(SwitchTraffic st : sw.getSts()) {
                eHoToEd += st.in;
                eHoToEd += st.out;
                num++;
            }
        }
        eHoToEd = 2*eHoToEd/num;
        List<Switch> edgeToAgg = this.getEdgeToAggByServer(hostToEdge, 1);
        double eEdToAg = 0.0;
        num = 0.0;
        for(Switch sw : edgeToAgg) {
            for(SwitchTraffic st : sw.getSts()) {
                eEdToAg += (2*st.in/FatTree.getK());
                eEdToAg += (2*st.out/FatTree.getK());
                num++;
            }
        }
        eEdToAg = 2*eEdToAg/num;
        List<Switch> pods = this.getInterPodByServer(edgeToAgg, FatTree.getK()/2);
        double eAgToCo = 0.0;
        num = 0.0;
        for(Switch sw : pods) {
            for(SwitchTraffic st : sw.getSts()) {
                eAgToCo += (st.in/FatTree.getK());
                eAgToCo += (st.out/FatTree.getK());
                num++;
            }
        }
        eAgToCo = 2*eAgToCo/num;
//        double prConn = this.getEConnNum();
//        LogUtil.LOGGER.log(Level.INFO, "bw and con: {0} {1} {2}", 
//                new Double[]{eHoToEd, eEdToAg, eAgToCo});
        //
        //configure and run this experiment
        
        int minNum = (int)Math.ceil(totalWork/serverPow.getMaxUsage());
        int maxAvail = (int)(Math.pow(FatTree.getK(), 3)/4);
        int bestNum = -1;
        double bestConsumption = Double.POSITIVE_INFINITY;
        
        for(int jj=minNum; jj<maxAvail; ++jj) {
            double servEs = srvPow.getC(jj, totalWork);
            double netEs = netPow.getConsumption(jj, totalWork, 0, eHoToEd, eEdToAg, eAgToCo, FatTree.getK());
            double cur = servEs+netEs;
//            System.out.print(" jj:" + jj + ":serv:" + cur + ", ");
            Output oo = new Output();
            oo.addVal(Output.SERVER, Output.VALUE, servEs);
            oo.addVal(Output.NETWORK, Output.VALUE, netEs);
            oo.addVal(Output.DC, Output.VALUE, netEs+servEs);
            outss.put(jj, oo);
//            this.createServersWithAllJobs(jj, totalWork);
//            this.vmPlacementServerBasedSenderPriority(totalWork/bestNum, serverPow.getMaxUsage(), My.EXCEED_RETAIN);
//            RunMyAlgorithm rm = new RunMyAlgorithm();
//            rm.setAggPower(aggPow);
//            rm.setCorePowModel(corePow);
//            rm.setEdgePower(edgePow);
//            rm.setServerPower(serverPow);
//            rm.setLinkPower(linkPow);
//            Output o = rm.getConsumptionByServer(this);
//            System.out.println("n:" + (netEs/o.getNetworkConsumption()));
//            System.out.println("s:" + (o.getStat(Output.SERVER, Output.VALUE)-servEs)/servEs);
            if(cur < bestConsumption) {
                bestConsumption = cur;
                bestNum = jj;
            }
        }
//        System.out.println("");
        
        int mode = 1;
        if(mode == 1) {
            return bestNum;
        }else if(mode ==2 )
            return minNum;
        
        return bestNum;
//        NondominatedPopulation result = new Executor()
//                        .withProblemClass(ServerNumProblem.class, 
//                                srvPow, netPow, totalWork, serverPow.getMaxUsage(), eHoToEd, eEdToAg, eAgToCo, 
//                                FatTree.getK(), this.getServerNum())
//                        .withAlgorithm("NSGAII")
//                        .withMaxEvaluations(10000)
//                        .run();
//
//        //display the results
//        //System.out.format("Objective1  Objective2%n");
//        int rret = -1;
//        for (Solution solution : result) {
////                System.out.format("%s,,       %.4f,,       %.4f,,        %.4f,,%n",
////                                solution.getVariable(0).toString(),
////                                solution.getObjective(0),
////                                srvPow.getC(Double.valueOf(solution.getVariable(0).toString()), totalWork),
////                                solution.getObjective(0));
//                rret = Double.valueOf(solution.getVariable(0).toString()).intValue();
////                System.out.println("000000>>>>>GENETIC:: " + rret);
////                LogUtil.LOGGER.log(Level.INFO, "adad: {0}", rret);
//                return rret;
//        }
//        
//        //no solution
//        return rret;
    }
    
    public double getEBw() {
        double sum = 0.0;
        int num = this.servers.size();
        SwitchTraffic st = new SwitchTraffic();
        for(Server server : this.servers) {
            st.addSwTr(server.getInOut());
        }
        return (st.in+st.out)/(num);
    }
    
    public double getEConnNum() {
        int connNum = 0;
        int num = this.servers.size();
        for(Server sv1 : this.servers) {
            for(Server sv2 : this.servers) {
                if(sv1 != sv2 && sv1.isConnected(sv2))
                    connNum++;
            }
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
    
    public void generateJobs(int vmNum, int numOfCenters, double mean) {
        for(int i=0; i<jobNumber; ++i) {
            JobMy j = new JobMy(new VmNumberGenerator(vmNum), new TrafficGenerator(mean, mean/4, numOfCenters), new CpuUsage(), numOfCenters);
            jobs.add(j);
        }
    }
    
    public void createSuperVmsBinaryMerge(double serverCap) {
//        double serverCap = powModel.getMaxUsage();
        for(JobMy j : this.jobs) {
            j.createSuperVm(serverCap, null);
        }
        servers = new ArrayList<>();
        for(JobMy jj : this.jobs) {
            servers.addAll(jj.servers);
        }
//        boolean hadAssign;
//        do{
//            hadAssign = false;
//            for(int i=0; i<servers.size(); ++i) {
//                for(int j=0; j<servers.size(); ++j) {
//                    if(i == j)
//                        continue;
//                    if(servers.get(i).curLoad+servers.get(j).curLoad>serverCap)
//                        continue;
//                    servers.get(i).merge(servers.get(j));
//                    servers.get(i).curLoad += servers.get(j).curLoad;
//                    servers.remove(j);
//                    hadAssign = true;
//                    break;
//                }
//            }
//        }while(hadAssign);
    }
    
    public void createSuperVms(DevicePowerUsageModel powModel) {
        for(JobMy j : this.jobs) {
//            j.createSuperVmMy(powModel.getMaxUsage());
            j.createSuperVmOneAtATime(powModel.getMaxUsage(), powModel);
        }
    }

    public void createSuperVmsElastic(double serverCap) {
//        double serverCap = powModel.getMaxUsage();
        for(JobMy j : this.jobs) {
            j.createSuperElastic(serverCap);
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
//        System.out.println("DDDDDDD" + this.getServerNum());
        
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
    
    public final static int EXCEED = 0;
    public final static int RETAIN = 1;
    public final static int EXCEED_RETAIN = 2;
    
    public void vmPlacementServerBasedSenderPriority(double serverCap, double serverMaxCap,
            int exceedRetain) {
        servers = new ArrayList<>();
        for(JobMy j : this.jobs) {
            j.vmPlacementServerBasedKMeansUsingMatrix(serverCap, serverMaxCap, exceedRetain);
            servers.addAll(j.servers);
//            System.out.println("--");
        }
        for(Server server : servers) {
            server.deleted = false;
        }
        boolean hadAssign;
        for(int i=0; i<servers.size(); ++i) {
            do{
                if(servers.get(i).curLoad >= serverCap) 
                    break;
                hadAssign = false;
                double curBest = Double.POSITIVE_INFINITY;
                int bestIndex = -1;
                for(int j=0; j<servers.size(); ++j) {
                    if(i == j)
                        continue;
                    if(servers.get(i).curLoad+servers.get(j).curLoad > serverCap)
                        continue;
                    bestIndex = j;
                    hadAssign = true;
                    break;
//                    if(servers.get(j).curLoad < curBest){
//                        curBest = servers.get(j).curLoad;
//                        bestIndex = j;
//                    }
                }
                if(hadAssign) {
                    servers.get(i).merge(servers.get(bestIndex));
                    servers.get(i).curLoad += servers.get(bestIndex).curLoad;
                    servers.remove(bestIndex);
                }
//                if(bestIndex != -1) {
//                    servers.get(i).merge(servers.get(bestIndex));
//                    servers.get(i).curLoad += servers.get(bestIndex).curLoad;
//                    servers.remove(bestIndex);
//                }
            }while(hadAssign);
        }
//        boolean hadAssign;
//        do{
//            hadAssign = false;
//            for(int i=0; i<servers.size(); ++i) {
//                for(int j=0; j<servers.size(); ++j) {
//                    if(i == j)
//                        continue;
//                    if(servers.get(i).curLoad+servers.get(j).curLoad>serverCap)
//                        continue;
//                    servers.get(i).merge(servers.get(j));
//                    servers.get(i).curLoad += servers.get(j).curLoad;
//                    servers.remove(j);
//                    hadAssign = true;
//                }
//            }
//        }while(hadAssign);
    }
    
    public void vmPlacementServerBasedKMeans(double serverCap, double serverMaxCap,
            int exceedRetain) {
        servers = new ArrayList<>();
        for(JobMy j : this.jobs) {
            j.vmPlacementServerBasedKMeansUsingMatrix(serverCap, serverMaxCap, exceedRetain);
            servers.addAll(j.servers);
//            System.out.println("--");
        }
        for(Server server : servers) {
            server.deleted = false;
        }
        boolean hadAssign;
        do{
            hadAssign = false;
            for(int i=0; i<servers.size(); ++i) {
                double between = -1;
                for(int j=0; j<servers.size(); ++j) {
                    if(i == j)
                        continue;
                    if(servers.get(i).deleted || servers.get(j).deleted)
                        continue;
                    if(servers.get(i).curLoad+servers.get(j).curLoad>serverCap)
                        continue;
                    
                    servers.get(i).merge(servers.get(j));
                    servers.get(i).curLoad += servers.get(j).curLoad;
                    servers.get(j).deleted = true;
                    hadAssign = true;
                    
                }
            }
        }while(hadAssign);
    }
    
    //place vms into servers
    public void vmPlacementServerBased(double serverCap, int exceedRetain) {
        servers = new ArrayList<>();
        for(JobMy j : this.jobs) {
            j.selected = new ArrayList<>();
        }
        
        Server curServer = new Server(serverCap);
        curServer.canExceed = false;
        
        switch(exceedRetain) {
            case My.EXCEED: curServer.canExceed = true; break;
            case My.RETAIN: curServer.canExceed = false; break;
            case My.EXCEED_RETAIN: curServer.canExceed = false; break;
            default: curServer.canExceed = false;
        }
        
        boolean stillVm;
        do {
            stillVm = false;
            ReturnTrCpu bestCandid = new ReturnTrCpu();
            for(JobMy j : this.jobs) {
                ReturnTrCpu candid = curServer.candidateFrom(j);
                if(candid.index != -1) {
                    if(candid.proper == false && bestCandid.proper == true)
                        continue;
                    if(candid.tr > bestCandid.tr || (candid.tr==bestCandid.tr && candid.cpu<bestCandid.cpu)) {
                        bestCandid = candid;
                    }
                }
                if(j.hasUnassigned())
                    stillVm = j.hasUnassigned();
            }
            if(bestCandid.index!=-1) {
                curServer.addCandidate(bestCandid);
                bestCandid.j.addSelected(bestCandid.index);
            }else if(stillVm) {
                if(curServer.curLoad != 0)
                    servers.add(curServer);
                boolean ce;
                if(exceedRetain == My.EXCEED_RETAIN)
                    ce = !curServer.canExceed;
                else
                    ce = curServer.canExceed;
                curServer = new Server(serverCap);
                curServer.canExceed = ce;
            }
        }while(stillVm);
        if(curServer.curLoad != 0 && servers.contains(curServer)==false) {
            servers.add(curServer);
        }
        
    }
    
    //place vms into servers
    public void createServersWithAllJobs(int serverNum, double total) {
        double serverCap = total / serverNum;
        this.vmPlacementServerBased(serverCap, My.EXCEED);
    }
    
    //sorts servers so servers with more traffic stay with each other
    private void sortServers() {
        List<Server> srvs = new ArrayList<>();
        
    }
    
    public void printSizeOfEachVm() {
        for(JobMy j : this.jobs) {
            j.printSizeOfVms();
            System.out.println("");
        }
    }

    public int getServerNum() {
        return this.servers.size();
//        return n;
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
        if(sw.getSwitchTraffic().size() != 0 && sws.contains(sw)==false)
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

    public double averageServerUtilization() {
        double sum = 0.0;
        double sum2 = 0.0;
        double num = 0;
        for(Server srv : this.servers) {
            sum += srv.curLoad;
            sum2 += Math.pow(srv.curLoad, 2);
            num++;
        }
        return sum/num;
    }
    
    public double stdDevServerUtilization() {
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
    
}
