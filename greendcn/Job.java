///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package greendcn;
//
//import greendcn.generator.CpuUsage;
//import greendcn.generator.TrafficGenerator;
//import greendcn.generator.VmNumberGenerator;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// *
// * @author mahdi
// */
//
//public class Job extends JobInterface {
//   private List<List<Double>> superVmTrafficMatrix;
//   private List<Double> superVmCpu;
//
//   public Job(VmNumberGenerator vmGen, TrafficGenerator trGen, CpuUsage cpuGen) {
//       super(vmGen, trGen, cpuGen);
//   }
//
//   public void createSuperVm(double serverCap) {
//       this.copyToSuper();
//       while(true) {
//            int source = -1;
//            int destination = -1;
//            double best = -1;
//            for(int i=0; i<this.superVmCpu.size(); ++i) {
//                int dest = this.getBest(i, serverCap);
//                if(dest != -1) {
//                    double ret = this.getBw(i, dest);
//                    if(ret > best) {
//                        source = i;
//                        destination = dest;
//                        best = ret;
//                    }
//                }
//            }
//            if(destination != -1) {
//                this.merge(source, destination);
//            }else{
//                return;
//            }
//       }
//   }
//
//   private int getBest(int source, double serverCap) {
//       double mostTraffic = -1;
//       int best = -1;
//       for(int i=0; i<this.superVmCpu.size(); ++i) {
//           if(i != source && this.superVmCpu.get(i)+this.superVmCpu.get(source) <= serverCap)
//                if(mostTraffic < this.superVmTrafficMatrix.get(source).get(i)) {
//                    mostTraffic = this.getBw(source, i);
//                    best = i;
//                }
//       }
//       return best;
//   }
//
//   public void merge(int s, int d) {
//       this.superVmCpu.set(s, this.superVmCpu.get(s) + this.superVmCpu.get(d));
//       this.superVmCpu.remove(d);
//       for(int i=0; i<this.superVmTrafficMatrix.size(); i++) {
//           this.superVmTrafficMatrix.get(s).set(i, this.superVmTrafficMatrix.get(s).get(i)+this.superVmTrafficMatrix.get(d).get(i));
//           this.superVmTrafficMatrix.get(i).set(s, this.superVmTrafficMatrix.get(i).get(s)+this.superVmTrafficMatrix.get(i).get(d));
//       }
//       this.superVmTrafficMatrix.get(s).set(s, 0.0);
//       for(int i=0; i<this.superVmTrafficMatrix.size(); i++) {
//           this.superVmTrafficMatrix.get(i).remove(d);
//       }
//       this.superVmTrafficMatrix.remove(d);
//   }
//
//   private double getBw(int s, int d) {
//        return this.superVmTrafficMatrix.get(s).get(d);
//   }
//
//   private void copyToSuper() {
//       this.superVmCpu = new ArrayList<>();
//       this.superVmTrafficMatrix = new ArrayList<>();
//       for(int i=0; i<this.cpu.size(); ++i) {
//           this.superVmCpu.add(this.cpu.get(i));
//           this.superVmTrafficMatrix.add(new ArrayList<>());
//           for(int j=0; j<this.cpu.size(); ++j) {
//               this.superVmTrafficMatrix.get(i).add(this.trafficMatrix.get(i).get(j));
//           }
//       }
//   }
//
//   public void print() {
////       System.out.println(this.superVmCpu);
////       for(int i=0; i<this.superVmTrafficMatrix.size(); ++i) {
////           System.out.println(this.superVmTrafficMatrix.get(i));
////       }
//   }
//
//   public List<List<Double>> getSuperTrafficMatrix() {
//       return this.superVmTrafficMatrix;
//   }
//
//   public List<Double> getSuperVmCpu() {
//       return this.superVmCpu;
//   }
//
//   public double getSumBWSuper() {
//       double sum = 0.0;
//       for(List<Double> l : this.superVmTrafficMatrix) {
//           for(Double d : l) {
//               sum += d;
//           }
//       }
//       return sum;
//   }
//
//    public int getSumConnNumber() {
//        int num = 0;
//        for (List<Double> l : this.superVmTrafficMatrix) {
//            for (Double d : l) {
//                if (d > 0) {
//                    num++;
//                }
//            }
//        }
//        return num;
//    }
//
//   public int getNumSuper() {
//       return this.superVmTrafficMatrix.size();
//   }
//
//}