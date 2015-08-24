/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package MyPackage.MyPack;

import greendcn.JobInterface;
import greendcn.generator.CpuUsage;
import greendcn.generator.TrafficGenerator;
import greendcn.generator.VmNumberGenerator;
import greendcn.power.DevicePowerUsageModel;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import utility.FatTree;
import utility.LogUtil;
import utility.Output;
import utility.Switch;
import utility.SwitchTraffic;

/**
 *
 * @author mahdi
 */
public class JobMy extends JobInterface {
   
    private List<List<Double>> superVmTrafficMatrix;
    private List<Double> superVmCpu;
    private Map<Switch,List<Integer>> superToEdgeAssignment;
    private Map<Switch,List<Integer>> superToPodAssignment;
    private List<Integer> assigned;
    private List<List<Integer>> cuts;
    
    // second method
    List<Integer> selected;
    //
    
    public JobMy(VmNumberGenerator vmGen, TrafficGenerator trGen, CpuUsage cpuGen, int centersNum) {
        super(vmGen, trGen, cpuGen, centersNum);
        this.superVmCpu = new ArrayList<>();
        this.superVmTrafficMatrix = new ArrayList<>();
        this.superToEdgeAssignment = new HashMap<>();
        this.superToPodAssignment = new HashMap<>();
        this.assigned = new ArrayList<>();
        //
        this.selected = new ArrayList<>();
    }
     
    //first merge vms one at a time for first time to get estimation
    public void createSuperVmOneAtATime(double serverCap, DevicePowerUsageModel powModel) {
       this.copyToSuper();
       boolean hasAssignment = false;
       do{
           hasAssignment = false;
           int i, dest=-1;
            for(i=0; i<this.superVmCpu.size(); ++i) {
                dest = this.getMyBest(i, serverCap);
                if(dest != -1)
                    break;
            }
            if(dest != -1) {
                hasAssignment = true;
                this.merge(i, dest);
                do{
                    dest = this.getMyBest(i, serverCap);
                    if(dest != -1)
                        this.merge(i, dest);
                }while(dest != -1);
            }
       }while(hasAssignment);
   }
    
    public void createSuperVm(double serverCap, DevicePowerUsageModel powModel) {
       this.copyToSuper();
       this.servers = new ArrayList<>();
       Server cur;
       int numOfVms = this.cpu.size();
       for(int vmsIndex=0; vmsIndex<this.superVmCpu.size(); ++vmsIndex) {
           cur = new Server();
           ReturnTrCpu re = new ReturnTrCpu();
           re.index = vmsIndex;
           re.j = this;
           re.cpu = this.superVmCpu.get(vmsIndex);
           cur.addCandidate(re);
           this.servers.add(cur);
       }
       int source = -1;
        int destination = -1;
        double best = -1;
       do {
            source = -1;
            destination = -1;
            best = -1;
            for(int i=0; i<this.superVmCpu.size(); ++i) {
                int dest = this.getBest(i, serverCap);
                if(dest != -1) {
                    double ret = this.getBw(i, dest);
                    if(ret > best) {
                        source = i;
                        destination = dest;
                        best = ret;
                    }
                }
            }
            if(destination != -1) {
                this.merge(source, destination);
                this.servers.get(source).merge(this.servers.get(destination));
                this.servers.get(source).curLoad += this.servers.get(destination).curLoad;
                this.servers.remove(destination);
            }
       }while(destination != -1);
       int assignedVms = 0;
       for(Server ss : this.servers) {
           assignedVms += ss.hosted.get(this).size();
       }
   }
   
    //yek server ra begir ta anja ke mishavad poresh kon
    public void createSuperVmMyOneAtATimeFillOne(double serverCap) {
        this.copyToSuper();
        int curServer = 0;
        while(curServer < this.superVmCpu.size()) {
            int dest = this.getBest(curServer, serverCap);
            if(dest == -1) {
                curServer++;
            }else{
                this.merge(curServer, dest);
            }
        }
    }
    
  public void createSuperVmMyOneAtATime(double serverCap) {
    this.copyToSuper();
       boolean hasAssignment;
       do{
           hasAssignment = false;
           int i, dest=-1;
            for(i=0; i<this.superVmCpu.size(); ++i) {
                dest = this.getBest(i, serverCap);
                if(dest != -1)
                    break;
            }
            if(dest != -1) {
                hasAssignment = true;
                this.merge(i, dest);
                do{
                    dest = this.getBest(i, serverCap);
                    if(dest != -1)
                        this.merge(i, dest);
                }while(dest != -1);
            }
       }while(hasAssignment);
   }
    
    
    public void createSuperVmMy(double serverCap) {
       this.copyToSuper();
       while(true) {
            int source = -1;
            int destination = -1;
            double best = -1;
            for(int i=0; i<this.superVmCpu.size(); ++i) {
                int dest = this.getBestBetter(i, serverCap);
//                int dest = this.getMyBest(i, serverCap);
                if(dest != -1) {
                    double ret = this.measure(i, dest);
                    if(ret > best) {
                        source = i;
                        destination = dest;
                        best = ret;
                    }
                }
            }
            if(destination != -1) {
                this.merge(source, destination);
            }else{
                return;
            }
       }
   }
    
   class ValIndex {
       int i;
       double v;
   }
    
   public void createSuperElastic(double serverCap) {
       this.copyToSuper();
       List<ValIndex> vis = new ArrayList<>();
       for(int i=0; i<this.cpu.size(); ++i) {
           ValIndex vi = new ValIndex();
           vi.i = i;
           vi.v = this.cpu.get(i);
           vis.add(vi);
       }
       vis.sort(new Comparator<ValIndex>() {
           @Override
           public int compare(ValIndex o1, ValIndex o2) {
               if(o1.v < o2.v)
                   return 1;
               if(o1.v > o2.v)
                   return -1;
               return 0;
           }
       });
       this.servers = new ArrayList<>();
       Server cur = new Server();
       this.servers.add(cur);
       for(int i=0; i<vis.size(); ++i) {
           if(cur.curLoad+vis.get(i).v > serverCap) {
               cur = new Server();
               this.servers.add(cur);
           }
           ReturnTrCpu r = new ReturnTrCpu();
            r.cpu = vis.get(i).v;
            r.index = vis.get(i).i;
            r.j = this;
            cur.addCandidate(r);
       }
//       System.out.println("==========================");
//       for(Server s1 : servers) {
//           for(Server s2 : servers) {
//               System.out.println("" + s1.getBetweenCpu(s2) + " ,");
//           }
//           System.out.println("");
//       }
//       System.out.println("==========================");
   } 
    
   private int getBest(int source, double serverCap) {
       double mostTraffic = -1;
       int best = -1;
       for(int i=0; i<this.superVmCpu.size(); ++i) {
            if(i != source) {
                double finalServerUsage = this.superVmCpu.get(i)+this.superVmCpu.get(source);
                if(finalServerUsage <= serverCap) {
                    double newTraffic = this.getBw(source, i)+this.getBw(i, source);
                    if(mostTraffic < newTraffic){
                        mostTraffic = newTraffic;
                        best = i;
                    }
                }
            }
       }
       return best;
   }
   
   private int getBestBetter(int source, double serverCap) {
       double mostTraffic = -1;
       int best = -1;
       for(int i=0; i<this.superVmCpu.size(); ++i) {
           if(i != source && 
                   this.superVmCpu.get(i)+this.superVmCpu.get(source) <= serverCap)
                if(mostTraffic < this.superVmTrafficMatrix.get(source).get(i)) {
                    mostTraffic = this.getBw(source, i) + this.getBw(i, source);
                    best = i;
                }
       }
       return best;
   }
   
   private double measure(int source, int destination) {
       double between = 0.0;
       double toOthers = 0.0;
       between += this.superVmTrafficMatrix.get(source).get(destination);
       between += this.superVmTrafficMatrix.get(destination).get(source);
       for(int i=0; i<this.superVmCpu.size(); ++i) {
           if(i == source || i == destination) {
               toOthers += this.superVmTrafficMatrix.get(i).get(source);
               toOthers += this.superVmTrafficMatrix.get(source).get(i);
               toOthers += this.superVmTrafficMatrix.get(i).get(destination);
               toOthers += this.superVmTrafficMatrix.get(destination).get(i);
           }
       }
       return between/toOthers;
   }
   
   private int getMyBest(int source, double serverCap) {
       double bestMeasure = -1;
       int best = -1;
       for(int i=0; i<this.superVmCpu.size(); ++i) {
           if(i != source && this.superVmCpu.get(i)+this.superVmCpu.get(source) <= serverCap) {
                double d = this.measure(source, i);
                if(bestMeasure < d) {
                    bestMeasure = d;
                    best = i;
                }
           }
       }
       return best;
   }
   
   private int getRandomVm(int source, double serverCap) {
       for(int i=this.superVmCpu.size()-1; i>=0; --i) {
           if(i != source && this.superVmCpu.get(i)+this.superVmCpu.get(source) <= serverCap) {
                return i;
           }
       }
       return -1;
   }
   
   //complexity 3 size dimension super traffic 
   public void merge(int s, int d) {
       this.superVmCpu.set(s, this.superVmCpu.get(s) + this.superVmCpu.get(d));
       this.superVmCpu.remove(d);
       for(int i=0; i<this.superVmTrafficMatrix.size(); i++) {
           this.superVmTrafficMatrix.get(s).set(i, this.superVmTrafficMatrix.get(s).get(i)+this.superVmTrafficMatrix.get(d).get(i));
           this.superVmTrafficMatrix.get(i).set(s, this.superVmTrafficMatrix.get(i).get(s)+this.superVmTrafficMatrix.get(i).get(d));
       }
       this.superVmTrafficMatrix.get(s).set(s, 0.0);
       for(int i=0; i<this.superVmTrafficMatrix.size(); i++) {
           this.superVmTrafficMatrix.get(i).remove(d);
       }
       this.superVmTrafficMatrix.remove(d);
   }
   
   private double getBw(int s, int d) {
        return this.superVmTrafficMatrix.get(s).get(d);
   }
   
   private void copyToSuper() {
       this.superVmCpu = new ArrayList<>();
       this.superVmTrafficMatrix = new ArrayList<>();
       for(int i=0; i<this.cpu.size(); ++i) {
           this.superVmCpu.add(this.cpu.get(i));
           this.superVmTrafficMatrix.add(new ArrayList<>());
           for(int j=0; j<this.cpu.size(); ++j) {
               this.superVmTrafficMatrix.get(i).add(this.trafficMatrix.get(i).get(j));
           }
       }
   }
   
   public void print() {
       //System.out.println(this.superVmCpu);
       for(int i=0; i<this.superVmTrafficMatrix.size(); ++i) {
           //System.out.println(this.superVmTrafficMatrix.get(i));
       }
   }
   
   public List<List<Double>> getSuperTrafficMatrix() {
       return this.superVmTrafficMatrix;
   }
   
   public List<Double> getSuperVmCpu() {
       return this.superVmCpu;
   }
   
   public double getSumBWSuper() {
       double sum = 0.0;
       for(List<Double> l : this.superVmTrafficMatrix) {
           for(Double d : l) { 
               sum += d;
           }
       }
       return sum;
   }
   
   public int getSumConnNumber() {
       int num = 0;
       for(List<Double> l : this.superVmTrafficMatrix) {
           for(Double d : l) {
               if(d > 0)
                num++;
           }
       }
       return num;
   }
   
   public int getNumSuper() {
       return this.superVmTrafficMatrix.size();
   }
   
   public double getTotalWork() {
       double sum = 0.0;
       for(double d : this.cpu)
           sum += d;
       return sum;
   }

   public List<Integer> getAssigned() {
        return assigned;
    }

    public void setAssigned(List<Integer> assigned) {
        this.assigned = assigned;
    }
   
   //hesab mikone az machine majazi shomare index che traffici kharej va dakhel mishavad
   public SwitchTraffic getSwitchFor(int index, Switch sw) {
       SwitchTraffic st = new SwitchTraffic();
       st.j = this;
       st.superVm = index;
       st.in = 0.0;
       st.out = 0.0;
       for(int i=0; i<this.superVmTrafficMatrix.size(); ++i) {
           if(i != index) {
                st.in += this.superVmTrafficMatrix.get(i).get(index);
                st.out += this.superVmTrafficMatrix.get(index).get(i);
                if(st.in < 0 || st.out < 0) {
                    System.err.println("" + st.in + " " + st.out + " " +
                            this.superVmTrafficMatrix.get(i).get(index) + " " + 
                            this.superVmTrafficMatrix.get(index).get(i));
                }
           }
       }
       
       //biroon add shode!
//       sw.addServer(this.servers.get(index));
       
       if(this.superToEdgeAssignment.containsKey(sw)) {
           this.superToEdgeAssignment.get(sw).add(index);
       }else{
           List<Integer> l = new ArrayList<>();
           l.add(index);
           this.superToEdgeAssignment.put(sw, l);
       }
       
       return st;
   }
   
   //ye switchi ra be map-e core assign mikonad
   public void getSwitchForCore(int index, Switch sw) {
       if(this.superToPodAssignment.containsKey(sw)) {
           this.superToPodAssignment.get(sw).add(index);
       }else{
           List<Integer> l = new ArrayList<>();
           l.add(index);
           this.superToPodAssignment.put(sw, l);
       }
   }
   
   //ye switch migirad ke aggrigate ast va
   //voroodi va khoroji in switch ra kesab mokonad
   public double[] getSwitchTrafficAgg(Switch sw, Switch EdToAg) {
       double[] res = new double[2];
       if(this.superToEdgeAssignment.containsKey(sw) == false)
           return null;
       for(int i : this.superToEdgeAssignment.get(sw)) {
           EdToAg.addServer(this.servers.get(i));
           for(int j=0; j<this.superVmTrafficMatrix.size(); j++) {
               if(this.superToEdgeAssignment.get(sw).contains(j) == false) {
                   res[0] += this.superVmTrafficMatrix.get(j).get(i);
                   res[1] += this.superVmTrafficMatrix.get(i).get(j);
               }
           }
       }
       return res;
   }
   
   //ye switch core migirad va voroodi va khorooji ra hesab mikonad
   public double[] getSwitchTrafficCore(Switch sw) {
       double[] res = new double[2];
       if(this.superToPodAssignment.containsKey(sw) == false)
           return null;
       for(int i : this.superToPodAssignment.get(sw)) {
           sw.addServer(this.servers.get(i));
           for(int j=0; j<this.superVmTrafficMatrix.size(); j++) {
               if(this.superToPodAssignment.get(sw).contains(j) == false) {
                   res[0] += this.superVmTrafficMatrix.get(j).get(i);
                   res[1] += this.superVmTrafficMatrix.get(i).get(j);
               }
           }
       }
       return res;
   }
   
   //aya hame super vm ha assign shode and
  public boolean allAssigned() {
       if(this.assigned.size() >= this.superVmTrafficMatrix.size())
           return true;
       return false;
   }

   //ta anjaee ke mishavad be switch sw va pod super vm assign mikonad
   public SwitchTraffic newSwitchAdd(Switch sw, Switch pod) {
       if(sw.hasFree() == false)
           return null;
       int i=0;
       int j;
       for(i=0; i<this.superVmTrafficMatrix.size(); ++i){
            do{
                j = (int)Math.floor(Math.random()*(this.superVmTrafficMatrix.size()+1));
            }while(this.assigned.contains(j) && this.assigned.size()<this.superVmTrafficMatrix.size());
            if(this.assigned.size() == this.superVmTrafficMatrix.size())
                return null;
            if(sw.hasFree() == false)
                return null;
            this.assigned.add(i);
            this.getSwitchForCore(i, pod);
            return this.getSwitchFor(i, sw);
       }
       
       for(i=0; i<this.superVmTrafficMatrix.size(); ++i){
           if(sw.hasFree() && this.assigned.contains(i) == false) {
               this.assigned.add(i);
               this.getSwitchForCore(i, pod);
               return this.getSwitchFor(i, sw);               
           }else if(sw.hasFree() == false) {
               return null;
           } else if(this.assigned.size() == this.superVmTrafficMatrix.size()) {
               return null;
           }
       }
       
       return null;
   }

   //copy az in job
    public JobMy copy() {
        JobMy j = new JobMy(null, null, null, 0);
        for(List<Double> l : this.trafficMatrix) {
            List<Double> t = new ArrayList<>();
            for(Double d : l)
                t.add(d);
            j.trafficMatrix.add(t);
        }
        for(Double d : this.cpu) {
            j.cpu.add(d);
        }
        j.superVmCpu = new ArrayList<>();
        j.superVmTrafficMatrix = new ArrayList<>();
        for(List<Double> l : this.superVmTrafficMatrix) {
            List<Double> t = new ArrayList<>();
            for(Double d : l)
                t.add(d);
            j.superVmTrafficMatrix.add(t);
        }
        for(Double d : this.superVmCpu) {
            j.superVmCpu.add(d);
        }
        for(List<Double> l : this.trafficMatrix) {
            for(double d : l) {
                if(d < 0) {
//                    System.out.println("end of copy");
                    break;
                }
            }
        }
        return j;
    }

    //tedade super vm haye assign nashode
    public int unassignedSupers() {
        return this.superVmCpu.size() - this.assigned.size();
    }

    public void calculateCuts() {
        this.cuts = KCutSaran.KCut.getKCut(FatTree.getK()/2, this.superVmTrafficMatrix);
    }
    
    //be hadafe inke tor kam shavad super vm ha ra pakhash kon
    public void assignSuperToSwitchTorMin(List<Switch> edges, Switch pod, double maxLoad) {
        if(this.superToPodAssignment.get(pod) == null) {
            List<Integer> lis = new ArrayList<>();
            this.superToPodAssignment.put(pod, lis);
        }
        for(Switch sw : edges) {
            for(List<Integer> aCut : this.cuts) {
//                if(sw.hasFree() == false)
//                    break;
                for(int i : aCut) {
                    if(this.assigned.contains(i))
                        continue;
                    if(sw.hasFree(this.servers.get(i).curLoad, maxLoad)) {
                        SwitchTraffic nst = this.getSwitchFor(i, sw);
                        boolean wasDedicated = sw.hasDedicatePlace();
                        sw.add(nst, this.servers.get(i),maxLoad);
                        this.assigned.add(i);
                        if(wasDedicated == false) {
//                            this.servers.remove(i);
                            this.servers.get(i).deleted = true;
                        }else{
                            pod.addServer(this.servers.get(i));
                            this.superToPodAssignment.get(pod).add(i);
                        }
//                        break;
                    }else{
                        break;
                    }
                }
            }
        }
    }
    
    //bar asase min cut nesbat bede be switch vali minimum ra dar yek switch negah dar
    //ne be hadafe kaheshe tor switch pakhsh kon
    public void assignSuperToSwitchImproved(List<Switch> edges, Switch pod) {
        int switchNum = 0;
        for(List<Integer> l : this.cuts) {
            for(int i : l) {
                if(this.assigned.contains(i))
                    continue;
                Switch sw = edges.get(switchNum);
                if(sw.hasFree() == false) {
                    switchNum++;
                    if(switchNum == edges.size())
                        return;
                    sw = edges.get(switchNum);
                }
                sw.add(this.getSwitchFor(i, sw));
                if(this.superToPodAssignment.get(pod) == null) {
                    List<Integer> lis = new ArrayList<>();
                    lis.add(i);
                    this.superToPodAssignment.put(pod, lis);
                }else{
                    this.superToPodAssignment.get(pod).add(i);
                }
                this.assigned.add(i);            
            }
        }
    }
    
    //bar asase minimum k-cut ta anjaee ke mishavad be in switch ha assign kon
    public void assignSuperToSwitches(List<Switch> edges, Switch pod) {
        int switchNum = 0;        
        for(List<Integer> l : this.cuts) {
            Switch sw = edges.get(switchNum);
            for(int i : l) {
                if(this.assigned.contains(i))
                    continue;
                //if designated switch has capacity insert into it
                //else insert into some switch
                if(sw.hasFree()) {
                    sw.add(this.getSwitchFor(i, sw));
                    if(this.superToPodAssignment.get(pod) == null) {
                        List<Integer> lis = new ArrayList<>();
                        lis.add(i);
                        this.superToPodAssignment.put(pod, lis);
                    }else{
                        this.superToPodAssignment.get(pod).add(i);
                    }
                    this.assigned.add(i);
                }else{
                    //find a switch for current super vm and assign and break
                    for(Switch s : edges) {
                        if(s.hasFree()) {
                            s.add(this.getSwitchFor(i, s));
                            if(this.superToPodAssignment.get(pod) == null) {
                                List<Integer> lis = new ArrayList<>();
                                lis.add(i);
                                this.superToPodAssignment.put(pod, lis);
                            }else{
                                this.superToPodAssignment.get(pod).add(i);
                            }
                            this.assigned.add(i);
                            break;
                        }
                    }
                }
            }
            switchNum++;
        }
    }
    
    public double sumSuperTrafficMatrix() {
        double sum = 0;
        for(List<Double> l : this.superVmTrafficMatrix) {
            for(Double d : l) {
                sum += d;
            }
        }
        return sum;
    }

    //baraye inke serverha tooye yek pod nazdik ham bashan!!
    void sortTrafficMatrix() {
        int i, j;
        int serversInPod = FatTree.getK()*FatTree.getK()/4;
        List<Integer> collected = new ArrayList<>();
        for(i=0; i<this.superVmTrafficMatrix.size(); ++i) {
            if(collected.contains(i) == false) {
                collected.add(i);
                for(j=0; j<serversInPod-1; ++j) {
                    int alike = this.sortTrafficMatrixAux(i, serversInPod, collected);
                    if(alike == -1)
                        return;
                    else{
                        collected.add(alike);
                    }
                }
            }
        }
        this.reCreateTrAndCpur(collected);
    }
    
    void reCreateTrAndCpur(List<Integer> collected) {
        List<List<Double>> str = new ArrayList<>();
        List<Double> scpu = new ArrayList<>();
        for(Integer i : collected) {
            List<Double> l = new ArrayList<>();
            for(Integer j : collected) {
                l.add(this.superVmTrafficMatrix.get(i).get(j));
            }
            str.add(l);
        }
        this.superVmTrafficMatrix = str;
        for(Integer i : collected) {
            scpu.add(this.superVmCpu.get(i));
        }
        this.superVmCpu = scpu;
    }
    
    int sortTrafficMatrixAux(int source, int num, List<Integer> collected) {
       double mostTraffic = -1;
       int best = -1;
       for(int i=0; i<this.superVmTrafficMatrix.size(); ++i) {
           if(collected.contains(i) == false && i != source) {
               double cur = this.superVmTrafficMatrix.get(i).get(source);
               cur += this.superVmTrafficMatrix.get(source).get(i);
               if(cur > mostTraffic) {
                   mostTraffic = cur;
                   best = i;
               }
           }
       }
       return best;
    }

    void reset() {
        this.assigned = new ArrayList<>();
        this.superToEdgeAssignment = new HashMap<>();
        this.superToPodAssignment = new HashMap<>();
        this.superVmCpu = new ArrayList<>();
        this.superVmTrafficMatrix = new ArrayList<>();
    }

    void printSizeOfVms() {
        for(Double d : this.superVmCpu) {
            System.out.print("" + d + " ");
        }
    }

    //when creating servers globally, outer tells a vm is selected to be placed on a server and 
    //by calling this removes from later placement process
    void addSelected(int i) {
        if(this.selected.contains(i))
            throw new NullPointerException();
        this.selected.add(i);
    }

    //choose best with respect to a group of other vms
    ReturnTrCpu getBestFor(Set<Integer> curHosting, double capacity, double maxCapacity, boolean soso) {
        double bestTr = -1;
        double bestCpu = -1;
        double dist = Double.MAX_VALUE;
        int index = -1;
        boolean hasProper = false;
        for(int i=0; i<this.cpu.size(); ++i) {
            if(this.selected.contains(i))
                continue;
            if(this.cpu.get(i) > maxCapacity)
                continue;
            double sumTraffic = 0;
            for(int other : curHosting) {
                sumTraffic = this.trafficMatrix.get(i).get(other)+this.trafficMatrix.get(other).get(i);
                double sumIn = 0.0;
                for(int j=0; j<this.cpu.size(); ++j) {
                    if(j == i)
                        continue;
                    if(curHosting.contains(j))
                        continue;
                    sumIn += this.trafficMatrix.get(i).get(j)+this.trafficMatrix.get(j).get(i);
                }
                sumTraffic -= (sumIn);
            }
            if(this.cpu.get(i)<=capacity) {
                if(sumTraffic > bestTr || (sumTraffic==bestTr && this.cpu.get(i)<bestCpu)) {
                    bestCpu = this.cpu.get(i);
                    bestTr = sumTraffic;
                    index = i;
                    hasProper = true;
                }
            }else if(soso && hasProper==false) {
                if(this.cpu.get(i)-capacity < dist) {
                    bestCpu = this.cpu.get(i);
                    bestTr = sumTraffic;
                    index = i;
                    dist = this.cpu.get(i)-capacity;
                }
            }
        }
        ReturnTrCpu ret = new ReturnTrCpu();
        ret.cpu = bestCpu;
        ret.tr = bestTr;
        ret.index = index;
        ret.j = this;
        ret.proper = hasProper;
        return ret;
    }

    boolean hasUnassigned() {
//        System.out.println("" + this.selected.toString());
        return this.selected.size()<this.cpu.size();
    }

    SwitchTraffic getInOut(Set<Integer> get) {
        SwitchTraffic st = new SwitchTraffic();
        st.in = 0;
        st.out = 0;
        for(int i=0; i<this.cpu.size(); ++i) {
            if(get.contains(i) == false) {
                for(int j : get) {
                    st.in += this.trafficMatrix.get(i).get(j);
                    st.out += this.trafficMatrix.get(j).get(i);
                }
            }
        }
        return st;
    }
    
    SwitchTraffic getInOutSuper(Set<Integer> get) {
        SwitchTraffic st = new SwitchTraffic();
        st.in = 0;
        st.out = 0;
        for(int i=0; i<this.superVmTrafficMatrix.size(); ++i) {
            if(get.contains(i) == false) {
                for(int j : get) {
                    st.in += this.superVmTrafficMatrix.get(i).get(j);
                    st.out += this.superVmTrafficMatrix.get(j).get(i);
                }
            }
        }
        return st;
    }
    
    public List<Server> servers;
    
    class TrafficIndex {
        double traffic;
        int index;
    }
    
    //in bishtarin ferestande ra bar midarad va por mikonad
    public void vmPlacementServerBasedKmeansReally(double serverCap, 
            double serverMaxCap, int exceedRetain) {
        this.copyToSuper();
        
        boolean canExceed;
        switch(exceedRetain) {
            case My.EXCEED: canExceed = true; break;
            case My.RETAIN: canExceed = false; break;
            case My.EXCEED_RETAIN: canExceed = false; break;
            default: canExceed = false;
        }
        
        double bwAvg = 0.0;
        double cpuAvg = 0.0;
        //two vectors hols all trans and recieve of one super
        List<Double> trans = new ArrayList<>();
        List<Double> reciev = new ArrayList<>();
        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
            trans.add(0.0);
            reciev.add(0.0);
            cpuAvg += this.getSuperVmCpu().get(i);
        }
        servers = new ArrayList<>();
        Server curServer;
        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
            curServer = new Server(serverCap);
            servers.add(curServer);
            ReturnTrCpu rr = new ReturnTrCpu();
            rr.cpu = this.getSuperVmCpu().get(i);
            rr.j = this;
            rr.index = i;
            curServer.addCandidate(rr);
        }
        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
            for(int j=0; j<this.getSuperTrafficMatrix().size(); ++j){
                trans.set(i, this.getSuperTrafficMatrix().get(i).get(j)+trans.get(i));
                reciev.set(i, this.getSuperTrafficMatrix().get(j).get(i)+reciev.get(i));
                bwAvg += this.getSuperTrafficMatrix().get(i).get(j);
                bwAvg += this.getSuperTrafficMatrix().get(j).get(i);
            }
        }
        bwAvg /= (this.getSuperVmCpu().size()*this.getSuperVmCpu().size());
        cpuAvg /= this.getSuperVmCpu().size();
//        LogUtil.LOGGER.log(Level.INFO, "{0} {1} {2}", 
//                new Integer[] {
//                    this.superVmCpu.size(), 
//                    this.superVmTrafficMatrix.size(),
//                    this.servers.size()
//                });
        //
        while(true) {
            int indexMaxSender = -1;
            double maxSend = -1;
            for(int i=0; i<servers.size(); ++i) {
                if(servers.get(i).hasSpace == false)
                    continue;
                if(trans.get(i) > maxSend) {
                    maxSend = trans.get(i);
                    indexMaxSender = i;
                }
            }
            if(indexMaxSender == -1)
                break;
            while(true) {
                int indexBestToMerge = -1;
                double maxMeasure = Double.NEGATIVE_INFINITY;
                for(int i=0; i<servers.size(); ++i) {
                    if(i == indexMaxSender)
                        continue;
                    if(servers.get(i).hasSpace == false)
                        continue;
                    if(this.getSuperVmCpu().get(i)+this.getSuperVmCpu().get(indexMaxSender) > serverMaxCap)
                        continue;
                    if(canExceed == false)
                        if(this.getSuperVmCpu().get(i)+this.getSuperVmCpu().get(indexMaxSender) > serverCap)
                            continue;
                    double rem = serverCap - this.getSuperVmCpu().get(i)-this.getSuperVmCpu().get(indexMaxSender);
                    double reduce = ((rem/cpuAvg)*bwAvg);
                    double bet = this.getSuperTrafficMatrix().get(i).get(indexMaxSender) +
                                    this.getSuperTrafficMatrix().get(indexMaxSender).get(i);
                    double out = trans.get(i) + trans.get(indexMaxSender) 
                            + reciev.get(i) + reciev.get(indexMaxSender)
                            - this.getSuperTrafficMatrix().get(i).get(indexMaxSender)
                            - this.getSuperTrafficMatrix().get(indexMaxSender).get(i);
                    bet = (bet/(out-reduce));
                    if(bet > maxMeasure) {
                        maxMeasure = bet;
                        indexBestToMerge = i;
                    }
                }
                if(indexBestToMerge == -1)
                    break;
                int tempIndex;
                if(indexMaxSender > indexBestToMerge) {
                    tempIndex = indexMaxSender;
                    indexMaxSender = indexBestToMerge;
                    indexBestToMerge = tempIndex;
                }
                trans.set(indexMaxSender, 
                        trans.get(indexMaxSender)-this.getSuperTrafficMatrix().get(indexMaxSender).get(indexBestToMerge));
                reciev.set(indexMaxSender, 
                        reciev.get(indexMaxSender)-this.getSuperTrafficMatrix().get(indexBestToMerge).get(indexMaxSender));
                trans.remove(indexBestToMerge);
                reciev.remove(indexBestToMerge);
                this.merge(indexMaxSender, indexBestToMerge);
                servers.get(indexMaxSender).merge(servers.get(indexBestToMerge));
                servers.get(indexMaxSender).curLoad += servers.get(indexBestToMerge).curLoad;
                servers.remove(indexBestToMerge);
                if(My.EXCEED_RETAIN == exceedRetain)
                    canExceed = !canExceed;
//                LogUtil.LOGGER.log(Level.INFO, "{0} {1} {2}", 
//                new Integer[] {
//                    this.superVmCpu.size(), 
//                    this.superVmTrafficMatrix.size(),
//                    this.servers.size()
//                });
            }
//            LogUtil.LOGGER.log(Level.INFO, "{0} {1} {2}",
            servers.get(indexMaxSender).hasSpace = false;
        }
    }
    
    public void vmPlacementBruteForce(double serverCap, 
            double serverMaxCap, int exceedRetain) {
        
    }
    
    //in k-means nist ke!!!! do ta do ta merge ast
    public void vmPlacementServerBasedKMeansUsingMatrix(double serverCap, 
            double serverMaxCap, int exceedRetain) {
        this.copyToSuper();
        double bwAvg = 0.0;
        double cpuAvg = 0.0;
        //two vectors hols all trans and recieve of one super
        List<Double> trans = new ArrayList<>();
        List<Double> reciev = new ArrayList<>();
        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
            trans.add(0.0);
            reciev.add(0.0);
//            System.out.print("" + this.getSuperVmCpu().get(i) + ", ");
            cpuAvg += this.getSuperVmCpu().get(i);
        }
//        System.out.println("");
        servers = new ArrayList<>();
        Server curServer;
        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
            curServer = new Server(serverCap);
            servers.add(curServer);
            ReturnTrCpu rr = new ReturnTrCpu();
            rr.cpu = this.getSuperVmCpu().get(i);
            rr.j = this;
            rr.index = i;
            curServer.addCandidate(rr);
        }
        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
            for(int j=0; j<this.getSuperTrafficMatrix().size(); ++j){
                trans.set(i, this.getSuperTrafficMatrix().get(i).get(j)+trans.get(i));
                reciev.set(i, this.getSuperTrafficMatrix().get(j).get(i)+reciev.get(i));
                bwAvg += this.getSuperTrafficMatrix().get(i).get(j);
                bwAvg += this.getSuperTrafficMatrix().get(j).get(i);
//                System.out.print("" + this.getSuperTrafficMatrix().get(i).get(j) + ", ");
            }
//            System.out.println("");
        }
        bwAvg /= (this.getSuperVmCpu().size()*this.getSuperVmCpu().size());
        cpuAvg /= this.getSuperVmCpu().size();
        Random rand = new Random(new Date().getTime());
        while(true) {
            int b1 = -1;
            int b2 = -1;
            double measure = Double.NEGATIVE_INFINITY;
            boolean exceeded = true;
            double diff = Double.POSITIVE_INFINITY;
            for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
                for(int j=0; j<this.getSuperTrafficMatrix().size(); ++j){
                    if(i == j)
                        continue;
                    if(this.getSuperVmCpu().get(i)+this.getSuperVmCpu().get(j) > serverMaxCap)
                        continue;
                    boolean ex = false;
                    double curDiff = -1;
                    if(this.getSuperVmCpu().get(i) > serverCap)
                        continue;
                    if(this.getSuperVmCpu().get(j) > serverCap)
                        continue;
                    if(this.getSuperVmCpu().get(i)+this.getSuperVmCpu().get(j) > serverCap) {
                        curDiff = this.getSuperVmCpu().get(i) + this.getSuperVmCpu().get(j) - serverCap;
                        ex = true;
                        continue;
                    }
                    double rem = serverCap - this.getSuperVmCpu().get(i)-this.getSuperVmCpu().get(j);
                    double bet = this.getSuperTrafficMatrix().get(i).get(j) +
                                    this.getSuperTrafficMatrix().get(j).get(i);
                    double out = trans.get(i) + trans.get(j) + reciev.get(i) + reciev.get(j);
                    out -= ((rem/cpuAvg)*bwAvg);
                    out -= this.getSuperTrafficMatrix().get(i).get(j);
                    out -= this.getSuperTrafficMatrix().get(j).get(i);
                    double newMeasure = bet/out;
                    if(exceeded) {
                        exceeded = ex;
                        if(ex) {
                            if(curDiff < diff) {
//                                measure = bet-out;
                                diff = curDiff;
                                b1 = i;
                                b2 = j;
                            }
                        } else {
                            if(newMeasure > measure) {
                                measure = newMeasure;
                                b1 = i;
                                b2 = j;
                            }
                        }
                    }else{
                        if(ex == false) {
                            if(newMeasure > measure) {
                                measure = newMeasure;
                                b1 = i;
                                b2 = j;
                            }
                        }
                    }
                    
                }
            }
            if(b1 != -1) {
//                System.out.println("merging: " + b1 + " " + b2);
                trans.set(b1, trans.get(b1)-this.getSuperTrafficMatrix().get(b1).get(b2));
                reciev.set(b1, trans.get(b1)-this.getSuperTrafficMatrix().get(b2).get(b1));
                this.merge(b1, b2);
                servers.get(b1).curLoad += servers.get(b2).curLoad;
                servers.get(b1).merge(servers.get(b2));
                servers.remove(b2);
            }else
                break;
        }
//        System.out.println("--------");
//        for(int i=0; i<this.getSuperTrafficMatrix().size(); ++i) {
//            for(int j=0; j<this.getSuperTrafficMatrix().size(); ++j){
//                System.out.print("" + this.getSuperTrafficMatrix().get(i).get(j) + ", ");
//            }
//            System.out.println("");
//        }
    }
    
    public void vmPlacementServerBasedKMeans(double serverCap, double serverMaxCap,
            int exceedRetain) {
        
        double avgCpu = 0.0;
        int cpuNum = 0;
        servers = new ArrayList<>();
        Server curServer;
        
        this.selected = new ArrayList<>();
        int i = 0;
        for(double d : this.getCpu()) {
            curServer = new Server(serverCap);
            servers.add(curServer);
            ReturnTrCpu rr = new ReturnTrCpu();
            rr.cpu = d;
            rr.j = this;
            rr.index = i;
            i++;
            curServer.addCandidate(rr);
            avgCpu += d;
            cpuNum++;
        }
        
//        System.out.println("started" + servers.size());
        avgCpu = avgCpu / cpuNum;
        boolean hadAssign;
        boolean bestExceeded = true;
        do{
            hadAssign = false;    
            int best = -1;
            int best2 = -1;
             boolean exceeded = true;
            double bestMeasure = Double.NEGATIVE_INFINITY;
            for(i=0; i<servers.size(); ++i) {
//                System.out.println("--");
                for(int j=0; j<servers.size(); ++j) {
                    if(i==j)
                        continue;
                    if(servers.get(i).deleted || servers.get(j).deleted)
                        continue;
                    if(servers.get(i).curLoad + servers.get(j).curLoad > serverMaxCap)
                        continue;
                    if(servers.get(i).curLoad + servers.get(j).curLoad > serverCap)
                        exceeded = true;
                    else
                        exceeded = false;
                    double bet = servers.get(i).getBetweenCpu(servers.get(i));
                    Server tempServer = new Server();
                    tempServer.merge(servers.get(i));
                    tempServer.merge(servers.get(j));
                    servers.get(j).deleted = true;
                    servers.get(j).deleted = true;
//                    System.out.println("bef between all");
                    double outIfMerge = tempServer.getBetween(servers);
//                    System.out.println("af bet all");
                    bet -= (outIfMerge);
                    if(bestExceeded) {
                        if(bet > bestMeasure) {
                            bestExceeded = exceeded;
                            bestMeasure = bet;
                            best = i;
                            best2 = j;
                        }
                    }else{
                        if(exceeded == false) {
                            if(bet > bestMeasure) {
                                bestMeasure = bet;
                                best = i;
                                best2 = j;
                            }
                        }
                    }
                    servers.get(j).deleted = false;
                    servers.get(j).deleted = false;
                }
            }
            if(best != -1) {
                servers.get(best).merge(servers.get(best2));
                servers.get(best).curLoad += servers.get(best2).curLoad;
                servers.get(best2).deleted = true;
//                System.out.println("merg: " + best + " " + best2);
                hadAssign = true;
                exceeded = !exceeded;
            }
        } while(hadAssign);
        
    }
    
}
