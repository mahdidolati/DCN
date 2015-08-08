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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utility.FatTree;
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
    
    public JobMy(VmNumberGenerator vmGen, TrafficGenerator trGen, CpuUsage cpuGen) {
        super(vmGen, trGen, cpuGen);
        this.superVmCpu = new ArrayList<>();
        this.superVmTrafficMatrix = new ArrayList<>();
        this.superToEdgeAssignment = new HashMap<>();
        this.superToPodAssignment = new HashMap<>();
        this.assigned = new ArrayList<>();
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
       while(true) {
            int source = -1;
            int destination = -1;
            double best = -1;
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
            }else{
                return;
            }
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
    
   public void createSuperElastic(double serverCap) {
       this.copyToSuper();
       while(true) {
            int source = -1;
            int destination = -1;
            for(int i=0; i<this.superVmCpu.size(); ++i) {
                source = i;
                destination = this.getRandomVm(i, serverCap);
                if(destination != -1)
                    break;
            }
            if(destination != -1) {
                this.merge(source, destination);
            }else{
                return;
            }
       }
   } 
    
   private int getBest(int source, double serverCap) {
       double mostTraffic = -1;
       int best = -1;
       for(int i=0; i<this.superVmCpu.size(); ++i) {
           if(i != source && 
                   this.superVmCpu.get(i)+this.superVmCpu.get(source) <= serverCap)
                if(mostTraffic < this.getBw(source, i)+this.getBw(i, source)){
//                        this.superVmTrafficMatrix.get(source).get(i)) {
                    mostTraffic = this.getBw(source, i)+this.getBw(i, source);
                    best = i;
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
   public double[] getSwitchTrafficAgg(Switch sw) {
       double[] res = new double[2];
       if(this.superToEdgeAssignment.containsKey(sw) == false)
           return null;
       for(int i : this.superToEdgeAssignment.get(sw)) {
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
        JobMy j = new JobMy(null, null, null);
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
                    System.out.println("end of copy");
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
    public void assignSuperToSwitchTorMin(List<Switch> edges, Switch pod) {
        if(this.superToPodAssignment.get(pod) == null) {
            List<Integer> lis = new ArrayList<>();
            this.superToPodAssignment.put(pod, lis);
        }
        for(Switch sw : edges) {
            for(List<Integer> aCut : this.cuts) {
                if(sw.hasFree() == false)
                    break;
                for(int i : aCut) {
                    if(this.assigned.contains(i))
                        continue;
                    if(sw.hasFree()) {
                        sw.add(this.getSwitchFor(i, sw));
                        this.superToPodAssignment.get(pod).add(i);
                        this.assigned.add(i);
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
}
