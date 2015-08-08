package Sedgewick;

import GomoryHuP.RowProxy;
import java.util.List;

/*************************************************************************
 *  Compilation:  javac FlowNetwork.java
 *  Execution:    java FlowNetwork V E
 *  Dependencies: Bag.java FlowEdge.java
 *
 *  A capacitated flow network, implemented using adjacency lists.
 *
 *************************************************************************/

public class FlowNetwork {
    private final int V;
    private int E;
    private Bag<FlowEdge>[] adj;
    
    // empty graph with V vertices
    public FlowNetwork(int V) {
        this.V = V;
        this.E = 0;
        adj = (Bag<FlowEdge>[]) new Bag[V];
        for (int v = 0; v < V; v++)
            adj[v] = new Bag<FlowEdge>();
    }
    
    public FlowNetwork(List<RowProxy> trMx) {
        this.V = trMx.size();
        this.E = 0;
        adj = (Bag<FlowEdge>[]) new Bag[V];
        for(int i=0; i<this.V; ++i) {
            adj[i] = new Bag<FlowEdge>();
        }
        for(int i=0; i<this.V; ++i) {
            for(int j=0; j<this.V; ++j) {
                if(trMx.get(i).row.get(j) != 0.0) {
                    addEdge(new FlowEdge(i, j, trMx.get(i).row.get(j)));
                } 
            }
        }
        /*
        for(int i=0; i<this.adj.length; ++i) {
            for(FlowEdge f : this.adj(i))
                System.out.print(""+f);
            System.out.println("");
        }
        */
    }

    // number of vertices and edges
    public int V() { return V; }
    public int E() { return E; }

    // add edge e in both v's and w's adjacency lists
    public void addEdge(FlowEdge e) {
        E++;
        int v = e.from();
        int w = e.to();
        adj[v].add(e);
        adj[w].add(e);
    }

    // return list of edges incident to  v
    public Iterable<FlowEdge> adj(int v) {
        return adj[v];
    }

    // return list of all edges - excludes self loops
    public Iterable<FlowEdge> edges() {
        Bag<FlowEdge> list = new Bag<FlowEdge>();
        for (int v = 0; v < V; v++)
            for (FlowEdge e : adj(v)) {
                if (e.to() != v)
                    list.add(e);
            }
        return list;
    }


    // string representation of Graph (excludes self loops) - takes quadratic time
    public String toString() {
        String NEWLINE = System.getProperty("line.separator");
        StringBuilder s = new StringBuilder();
        s.append(V + " " + E + NEWLINE);
        for (int v = 0; v < V; v++) {
            s.append(v + ":  ");
            for (FlowEdge e : adj[v]) {
                if (e.to() != v) s.append(e + "  ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }

    // test client
    public static void main(String[] args) {
        FlowNetwork G = new FlowNetwork(7);
        System.out.println(G);
    }

}
