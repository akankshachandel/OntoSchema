/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontologyschema;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import grph.DirectedGraph;
import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.jgrapht.io.*;

import java.net.*;
import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.alg.scoring.ClosenessCentrality;
import org.jgrapht.alg.scoring.PageRank;

/**
 *
 * @author akanksha
 *
 * http://www.statisticshowto.com/normalized/
 *
 */
public class betweenessCentrality {

    public static Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    public static List<String> list = new ArrayList();

    public static void main(String[] args) throws InterruptedException, SQLException {

        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            if (con == null) {
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ontoschema", "root", "root");
                System.out.println("connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String serviceURI = "http://vulcan.cs.uga.edu/sparql";

        addObjectGraph(con, serviceURI);
        //myBetweenessCentrality(con, serviceURI);     //https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/scoring/BetweennessCentrality.java
        //pageRank(con,serviceURI);
        //vertexDegree(con,serviceURI);
       claculateweight(con, serviceURI);
       // System.out.println(ins_count(con, serviceURI));

    }

    public static void addObjectGraph(Connection con, String ep) throws SQLException {

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

        //creating vertexes
        Statement stmt = null;
        //creating edges, connecting nodes/vertexes of the graph
        String qry = "select class_URI,property_URI,property_triple_type from object_triple_type where endpoint='" + ep + "'";
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(qry);

            while (rs.next()) {

                String class_URI = rs.getString("class_URI");
                String property_URI = rs.getString("property_URI");
                String property_triple_type = rs.getString("property_triple_type");

                if (property_URI.contains("http://www.openlinksw.com/") || property_URI.contains("http://www.w3.org/") || class_URI.contains("http://www.openlinksw.com/") || class_URI.contains("http://www.w3.org/") || property_triple_type.contains("http://www.openlinksw.com/") || property_triple_type.contains("http://www.w3.org/")) {
                    continue;
                } else {

                    if (!graph.containsVertex(class_URI)) {
                        graph.addVertex(class_URI);
                        list.add(class_URI);
                    }
                    if (!graph.containsVertex(property_triple_type)) {
                        graph.addVertex(property_triple_type);
                        list.add(property_triple_type);
                    }
                    graph.addEdge(class_URI, property_triple_type);

                    System.out.println("adding Edge:    " + class_URI + "****************" + property_triple_type);
                }
            }

            stmt.close();

        } catch (SQLException e) {
            System.out.print(e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }

        //graph.display(false);
    }

    public static void myBetweenessCentrality(Connection con, String ep) throws SQLException {
        Statement st = null;
        BetweennessCentrality bcb = new BetweennessCentrality(graph);
        System.out.println("Betweeness Centrality");
        for (String s : list) {
            double rank = bcb.getVertexScore(s);

            st = con.createStatement();
            String updater = "UPDATE class SET bc='" + rank + "' WHERE class_URI='" + s + "' AND endpoint='" + ep + "'";
            st.execute(updater);
            System.out.println("class:  " + s + "  Score:   " + bcb.getVertexScore(s));
        }

        st.close();

    }

    public static void pageRank(Connection con, String ep) throws SQLException {
        Statement st = null;
        PageRank pr = new PageRank(graph);
        System.out.println("PageRank");
        for (String s : list) {
            double rank = pr.getVertexScore(s);
            st = con.createStatement();
            String updater = "UPDATE class SET pr='" + rank + "' WHERE class_URI='" + s + "' AND endpoint='" + ep + "'";
            st.execute(updater);
            System.out.println("class:  " + s + "  Score:   " + rank);
        }
        st.close();
    }
    
     public static void vertexDegree(Connection con, String ep) throws SQLException {
        Statement st = null;
        System.out.println("class:  " );
        for (String s : list) {
             System.out.println("class:  " );
            int rank = graph.degreeOf(s);
            st = con.createStatement();
            String updater = "UPDATE class SET degree='" + rank + "' WHERE class_URI='" + s + "' AND endpoint='" + ep + "'";
            st.execute(updater);
            System.out.println("class:  " + s + "  Score:   " + rank);
        }
        st.close();
    }

    public static long ins_count(Connection con, String ep) {
        long insc = 0;
        String ins = "SELECT( COUNT (?instance ) AS ?in )WHERE { ?instance a ?class }";
        QueryExecution qr = QueryExecutionFactory.sparqlService(ep, ins);
        com.hp.hpl.jena.query.ResultSet r = qr.execSelect();
        //   ResultSetFormatter.out(System.out, r);
        while (r.hasNext()) {
            final QuerySolution sol = r.nextSolution();
            RDFNode inst = sol.get("in");
            String num = inst.toString();
            num = num.substring(0, num.indexOf("^"));
            insc = Integer.parseInt(num);
            System.out.println(inst.toString());

        }
        return insc;
    }

    public static void claculateweight(Connection con, String ep) throws SQLException {
        Statement st = con.createStatement();
        Long insc = ins_count(con, ep);
        float mxpr = 0, mnpr = 0, mxbc = 0, mnbc = 0, mxdp = 0, mndp = 0,mxdg=0,mndg=0, pl = 0, bl = 0, dl = 0,dg=0;
        String mi = "Select max(dpavg) AS mxc,min(dpavg) as minc from class where endpoint='" + ep + "'";
        ResultSet rs = st.executeQuery(mi);
        while (rs.next()) {
            mxdp = rs.getFloat("mxc");
            mndp = rs.getFloat("minc");
            dl = mxdp - mndp;
            System.out.println(mxdp + "  **Data Property Average**  " + mndp + "   ***   " + dl);
        }
        String mp = "Select max(pr) AS mxc,min(pr) as minc from class where endpoint='" + ep + "'";
        rs = st.executeQuery(mp);
        while (rs.next()) {
            mxpr = rs.getFloat("mxc");
            mnpr = rs.getFloat("minc");
            pl = mxpr - mnpr;
            System.out.println(mxpr + "  **page rank**  " + mnpr + "    ****   " + pl);
        }
        String mb = "Select max(bc) AS mxc,min(bc) as minc from class where endpoint='" + ep + "'";
        rs = st.executeQuery(mb);
        while (rs.next()) {
            mxbc = rs.getFloat("mxc");
            mnbc = rs.getFloat("minc");
            bl = mxbc - mnbc;
            System.out.println(mxbc + "  **betweeness Centrality**  " + mnbc + "   ***   " + bl);

        }
 String mdg = "Select max(degree) AS mxc,min(degree) as minc from class where endpoint='" + ep + "'";
        rs = st.executeQuery(mdg);
        while (rs.next()) {
            mxdg = rs.getFloat("mxc");
            mndg = rs.getFloat("minc");
            dg = mxbc - mnbc;
            System.out.println(mxdg + "  **get degree**  " + mndg + "   ***   " + dg);

        }
        //  String all = "Select class_URI,instance_count,pr,bc,dpavg from class where endpoint='" + ep + "'";
        String all = "SELECT class_URI,instance_count,pr,bc,dpavg,degree FROM ontoschema.class where endpoint='" + ep + "'  AND instance_count IS not null AND bc  IS not null AND dpavg  IS not null AND pr  IS not null order by pr desc";
        rs = st.executeQuery(all);
        while (rs.next()) {

            String class_URI = rs.getString("class_URI");
            int inc = rs.getInt("instance_count");
            float pr = rs.getFloat("pr");
            float bc = rs.getFloat("bc");
            float dpavg = rs.getFloat("dpavg"), i, p, b, d,dge;
            int degree =rs.getInt("degree");

            i = ((float) inc) / insc;
            p = ((float) (pr - mnpr)) / pl;
            b = ((float) (bc - mnbc)) / bl;
            d = ((float) (dpavg - mndp)) / dl;
            dge=((float)(degree-mndg))/dg;
            
           float wt=((float)i+p+b+d+dge)/5;
        //   float wt=(float)(i*0.2+p*0.2+b*0.2+d*0.4);
System.out.println(class_URI+ " ***  "+dge);

 st = con.createStatement();
            String updater = "UPDATE class SET dg='" + dge + "'  WHERE class_URI='" + class_URI + "' AND endpoint='" + ep + "'";
            st.execute(updater);

           // System.out.println("For Class" + class_URI + "  Instance Count   " + i + "   pagerank   " + p + "   betweeness Centrality   " + b + "    dpavg   " + d);
        }
        
        st.close();
    }
}

// End HelloJGraphT.java
