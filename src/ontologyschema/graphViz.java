package Summary;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import grph.Grph;
import grph.in_memory.InMemoryGrph;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

/**
 *
 * @author achandel
 */
public class graphViz {
    
    public static HashMap<String, Integer> hm = new HashMap();

     public static  Grph g = new InMemoryGrph();
     
     public static void visualize(Connection con, String ep) throws SQLException
     {
         createObjectGraph(con, ep);
      createDataGraph(con, ep);
      g.display();
     }


    public static void createObjectGraph(Connection con, String ep) throws SQLException {

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
       

        //creating vertexes
      Statement stmt = null;
//        String query = "select class_URI from class where endpoint='" + ep + "'";
//        try {
//            stmt = con.createStatement();
//            ResultSet rs = stmt.executeQuery(query);
//
//            while (rs.next()) {
//                String class_URI = rs.getString("class_URI");
//                String pre = class_URI;
//                if (pre.contains("#")) {
//                    pre = pre.substring(pre.lastIndexOf("#") + 1);
//                } else {
//                    pre = pre.substring(pre.lastIndexOf("/") + 1);
//                }
//                //System.out.println(pre);
//                int v = g.addVertex();
//                g.getVertexLabelProperty().setValue(v, pre);
//                hm.put(class_URI, v);
//
//            }
//
//            stmt.close();
//        } 
//        catch (SQLException e) {
//            System.out.print(e);
//        } 
//        finally {
//            if (stmt != null) {
//                stmt.close();
//            }
//        }

        //creating edges, connecting nodes/vertexes of the graph
        String qry = "select class_URI,property_URI,property_triple_type from object_triple_type where endpoint='" + ep + "'";
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(qry);

            while (rs.next()) {
               
                String class_URI = rs.getString("class_URI");
                String property_URI = rs.getString("property_URI");
                String property_triple_type = rs.getString("property_triple_type");

                String pre = property_URI;
                if (pre.contains("#")) {
                    pre = pre.substring(pre.lastIndexOf("#") + 1);
                } else {
                    pre = pre.substring(pre.lastIndexOf("/") + 1);
                }

                int v1, v2;
               
                if (class_URI == null || class_URI.isEmpty()) //if domain/subject is null or empty in a triple
                {
                    v1 = hm.get("http://localhost/blank");        //represented by a blank node
                }
                
                else                                              //if domain/subject has a value
                {
                    if (hm.containsKey(class_URI)) //check if hashmap already has saved a vertex for class_URI or if that vertex already exists
                    {
                        v1 = hm.get(class_URI);        //use the id for that vertex
                    }
                    else                              //otherwise create a new vertex and add it to hashmap and in the map
                    {
                        String pc = class_URI;                 //adding instances as vertexes
                        if (pc.contains("#")) {
                            pc = pc.substring(pc.lastIndexOf("#") + 1);
                        } 
                        else 
                        {
                            pc = pc.substring(pc.lastIndexOf("/") + 1);
                        }
                        //System.out.println(pc);

                        int v = g.addVertex();

                        g.getVertexLabelProperty().setValue(v, pc);

                        hm.put(class_URI, v);

                        v1 = hm.get(class_URI);
                    }

                }

                if (property_triple_type == null || property_triple_type.isEmpty()) //if range/object in a triple is empty or null
                {
                    v2 = hm.get("http://localhost/blank");
                }
                else                //if range /object is not null
                {

                    if (hm.containsKey(property_triple_type)) //if range/object exists in hashmap for all object
                    {
                      v2 = hm.get(property_triple_type);
                     // System.out.println(v2+" "+property_triple_type);
                    } 
                    else                                //creating a new vertex and adding it to hashmap
                    {
                        String p = property_triple_type;                 //adding instances as vertexes
                        if (p.contains("#")) {
                            p = p.substring(p.lastIndexOf("#") + 1);
                        } 
                        else
                        {
                            p = p.substring(p.lastIndexOf("/") + 1);
                        }
                        
                        //System.out.println(p);
                        int v = g.addVertex();
                        g.getVertexLabelProperty().setValue(v, p);
                        hm.put(property_triple_type, v);

                        v2 = hm.get(property_triple_type);
                    }

                }

                int e1 = g.addDirectedSimpleEdge(v1, v2);       //creating an edge here
                
                g.getEdgeLabelProperty().setValue(e1, pre);      //renaming/labeling the edge in the graph
                
                System.out.println("new edge created with node v1 "+v1+" label: "+ pre +" edge number "+e1+"   node v2  "+v2);
            }

            stmt.close();
        } 
        catch (SQLException e) {
            System.out.print(e);
        } 
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }

    //  g.display();

    }

    public static void createDataGraph(Connection con, String ep) throws SQLException {

        ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);
       

        //creating vertexes
      Statement stmt = null;
//        String query = "select class_URI from class where endpoint='" + ep + "'";
//        try {
//            stmt = con.createStatement();
//            ResultSet rs = stmt.executeQuery(query);
//
//            while (rs.next()) {
//                String class_URI = rs.getString("class_URI");
//                String pre = class_URI;
//                if (pre.contains("#")) {
//                    pre = pre.substring(pre.lastIndexOf("#") + 1);
//                } else {
//                    pre = pre.substring(pre.lastIndexOf("/") + 1);
//                }
//                //System.out.println(pre);
//                int v = g.addVertex();
//                g.getVertexLabelProperty().setValue(v, pre);
//                hm.put(class_URI, v);
//
//            }
//
//            stmt.close();
//        } 
//        catch (SQLException e) {
//            System.out.print(e);
//        } 
//        finally {
//            if (stmt != null) {
//                stmt.close();
//            }
//        }

        //creating edges, connecting nodes/vertexes of the graph
        String qry = "select class_URI,property_URI,property_triple_type from data_triple_type where endpoint='" + ep + "'";
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(qry);

            while (rs.next()) {
               
                String class_URI = rs.getString("class_URI");
                String property_URI = rs.getString("property_URI");
                String property_triple_type = rs.getString("property_triple_type");

                String pre = property_URI;
                if (pre.contains("#")) {
                    pre = pre.substring(pre.lastIndexOf("#") + 1);
                } else {
                    pre = pre.substring(pre.lastIndexOf("/") + 1);
                }

                int v1, v2;
               
                if (class_URI == null || class_URI.isEmpty()) //if domain/subject is null or empty in a triple
                {
                    v1 = hm.get("http://localhost/blank");        //represented by a blank node
                }
                
                else                                              //if domain/subject has a value
                {
                    if (hm.containsKey(class_URI)) //check if hashmap already has saved a vertex for class_URI or if that vertex already exists
                    {
                        v1 = hm.get(class_URI);        //use the id for that vertex
                    }
                    else                              //otherwise create a new vertex and add it to hashmap and in the map
                    {
                        String pc = class_URI;                 //adding instances as vertexes
                        if (pc.contains("#")) {
                            pc = pc.substring(pc.lastIndexOf("#") + 1);
                        } 
                        else 
                        {
                            pc = pc.substring(pc.lastIndexOf("/") + 1);
                        }
                        //System.out.println(pc);

                        int v = g.addVertex();

                        g.getVertexLabelProperty().setValue(v, pc);

                        hm.put(class_URI, v);

                        v1 = hm.get(class_URI);
                    }

                }

                if (property_triple_type == null || property_triple_type.isEmpty()) //if range/object in a triple is empty or null
                {
                    v2 = hm.get("http://localhost/blank");
                }
                else                //if range /object is not null
                {

                    if (hm.containsKey(property_triple_type)) //if range/object exists in hashmap for all object
                    {
                      v2 = hm.get(property_triple_type);
                     // System.out.println(v2+" "+property_triple_type);
                    } 
                    else                                //creating a new vertex and adding it to hashmap
                    {
                        String p = property_triple_type;                 //adding instances as vertexes
                        if (p.contains("#")) {
                            p = p.substring(p.lastIndexOf("#") + 1);
                        } 
                        else
                        {
                            p = p.substring(p.lastIndexOf("/") + 1);
                        }
                        
                        //System.out.println(p);
                        int v = g.addVertex();
                        g.getVertexLabelProperty().setValue(v, p);
                        hm.put(property_triple_type, v);

                        v2 = hm.get(property_triple_type);
                    }

                }

                int e1 = g.addDirectedSimpleEdge(v1, v2);       //creating an edge here
                
                g.getEdgeLabelProperty().setValue(e1, pre);      //renaming/labeling the edge in the graph
                
                System.out.println("new edge created with node v1 "+v1+" label: "+ pre +" edge number "+e1+"   node v2  "+v2);
            }

            stmt.close();
        } 
        catch (SQLException e) {
            System.out.print(e);
        } 
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }

    //  g.display();

    }

}
