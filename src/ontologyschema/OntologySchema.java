/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ontologyschema;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author priyadarshi
 */
public class OntologySchema {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException {
        // TODO code application logic here

        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            if (con == null) {
                con = DriverManager.getConnection("jdbc:mysql://localhost:3306/ontoschema", "root", "root");
                System.out.println("connected");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         *
         * @param con
         */
        String serviceURI = "http://vulcan.cs.uga.edu/sparql";
//        Statement stm = con.createStatement();
//        String m = "INSERT INTO endpoint(endpoint) VALUES ('" + serviceURI + "') ";
//        stm.execute(m);
//        stm.close();

        HashMap<String, Integer> hm = new HashMap();

       // classes(con, serviceURI, hm);
        //explicitClasses(con, serviceURI, hm);
        objectProperty(con, serviceURI, hm);
        //dataProperty(con, serviceURI, hm);
        //explicitdataProperty(con, serviceURI, hm);
       // explicitobjProperty(con, serviceURI, hm);
        //subproperty(con);
        //hasdomain(con, serviceURI);
        //prefix(con, hm);
        // explicitDomainRangeObj(con,serviceURI);

        con.close();
    }

    public static void classes(Connection con, String serviceURI, HashMap<String, Integer> hm) {

        String query = "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "select distinct ?class ?label ?comment where{[] a ?class.\n"
                + "OPTIONAL { ?class rdfs:label ?label .\n"
                + " ?class rdfs:comment ?comment.\n"
                + "}  \n"
                + " }";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;
        //  String qry="INSERT INTO class(class_URI) values(?)";

        while (results.hasNext()) {

            final QuerySolution soln = results.nextSolution();
            RDFNode classes = soln.get("class");
            RDFNode label = soln.get("label");
            RDFNode comment = soln.get("comment");

            String pre = classes.toString();
            if (pre.contains("#")) {
                pre = pre.substring(0, pre.lastIndexOf("#") + 1);
            } else {
                pre = pre.substring(0, pre.lastIndexOf("/") + 1);
            }

            if (hm.containsKey(pre)) {
                hm.put(pre, hm.get(pre) + 1);
            } else {
                hm.put(pre, 1);
            }

            try {
                StringBuilder qry = new StringBuilder();
                // System.out.println(classes.toString()+label.toString()!=null?label.toString():"hello1"+comment.toString()!=null?comment.toString():"comment");
                s = con.createStatement();
                qry.append("INSERT INTO class(class_URI");
                if (label != null) {

                    qry.append(", label");
                }

                if (comment != null) {

                    qry.append(", comment");
                }
                qry.append(") values('").append(classes.toString()).append("'");
                if (label != null) {

                    qry.append(",'").append(label.toString()).append("'");
                }

                if (comment != null) {

                    qry.append(",'").append(comment.toString()).append("'");
                }
                qry.append(")");
                s.execute(qry.toString());
                s.close();
                String ins = "SELECT( COUNT (?instance ) AS ?in )WHERE { ?instance a <" + classes.toString() + "> }";
                QueryExecution qr = QueryExecutionFactory.sparqlService(serviceURI, ins);
                ResultSet r = qr.execSelect();
                //   ResultSetFormatter.out(System.out, r);
                while (r.hasNext()) {
                    final QuerySolution sol = r.nextSolution();
                    RDFNode inst = sol.get("in");
                    String num = inst.toString();
                    num = num.substring(0, num.indexOf("^"));
                    System.out.println(inst.toString());
                    Statement st = con.createStatement();
                    String m = "UPDATE class SET instance_count='" + num + "' WHERE class_URI='" + classes.toString() + "'";
                    //x=x+1;
                    //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                    st.execute(m);
                    st.close();
                }
                String superc = "PREFIX     rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                        + "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                        + "SELECT ?superClass WHERE { <" + classes.toString() + "> rdfs:subClassOf+ ?superClass .  }";
                QueryExecution qx = QueryExecutionFactory.sparqlService(serviceURI, superc);
                ResultSet res = qx.execSelect();
                //   ResultSetFormatter.out(System.out, r);
                while (res.hasNext()) {
                    final QuerySolution sol = res.nextSolution();
                    RDFNode sup = sol.get("superClass");
                    System.out.println(sup.toString());
                    Statement stm = con.createStatement();
                    String m = "INSERT INTO superclass (class_URI, superclass_URI) VALUES ('" + classes.toString() + "','" + sup.toString() + "') ";
                    stm.execute(m);
                    stm.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }

    }

    public static void explicitClasses(Connection con, String serviceURI, HashMap<String, Integer> hm) {

        String query = "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT DISTINCT ?class ?label ?comment\n"
                + "                        WHERE {\n"
                + " ?class a rdfs:Class.\n"
                + "OPTIONAL { ?class rdfs:label ?label . }\n"
                + "OPTIONAL {  ?class rdfs:comment ?comment. }  }";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;
        //  String qry="INSERT INTO class(class_URI) values(?)";

        while (results.hasNext()) {

            final QuerySolution soln = results.nextSolution();
            RDFNode classes = soln.get("class");
            RDFNode label = soln.get("label");
            RDFNode comment = soln.get("comment");

            String pre = classes.toString();
            if (pre.contains("#")) {
                pre = pre.substring(0, pre.lastIndexOf("#") + 1);
            } else {
                pre = pre.substring(0, pre.lastIndexOf("/") + 1);
            }

            if (hm.containsKey(pre)) {
                hm.put(pre, hm.get(pre) + 1);
            } else {
                hm.put(pre, 1);
            }

            try {
                StringBuilder qry = new StringBuilder();
                // System.out.println(classes.toString()+label.toString()!=null?label.toString():"hello1"+comment.toString()!=null?comment.toString():"comment");
                s = con.createStatement();
                qry.append("INSERT INTO class(class_URI");
                if (label != null) {

                    qry.append(", label");
                }

                if (comment != null) {

                    qry.append(", comment");
                }
                qry.append(") values('").append(classes.toString()).append("'");
                if (label != null) {

                    qry.append(",'").append(label.toString()).append("'");
                }

                if (comment != null) {

                    qry.append(",'").append(comment.toString()).append("'");
                }
                qry.append(")");
                s.execute(qry.toString());
                s.close();
                String ins = "SELECT( COUNT (?instance ) AS ?in )WHERE { ?instance a <" + classes.toString() + "> }";
                QueryExecution qr = QueryExecutionFactory.sparqlService(serviceURI, ins);
                ResultSet r = qr.execSelect();
                //   ResultSetFormatter.out(System.out, r);
                while (r.hasNext()) {
                    final QuerySolution sol = r.nextSolution();
                    RDFNode inst = sol.get("in");
                    String num = inst.toString();
                    num = num.substring(0, num.indexOf("^"));
                    System.out.println(inst.toString());
                    Statement st = con.createStatement();
                    String m = "UPDATE class SET instance_count='" + num + "' WHERE class_URI='" + classes.toString() + "'";
                    //x=x+1;
                    //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                    st.execute(m);
                    st.close();
                }
                String superc = "PREFIX     rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                        + "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                        + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                        + "SELECT ?superClass WHERE { <" + classes.toString() + "> rdfs:subClassOf+ ?superClass .  }";
                QueryExecution qx = QueryExecutionFactory.sparqlService(serviceURI, superc);
                ResultSet res = qx.execSelect();
                //   ResultSetFormatter.out(System.out, r);
                while (res.hasNext()) {
                    final QuerySolution sol = res.nextSolution();
                    RDFNode sup = sol.get("superClass");
                    System.out.println(sup.toString());
                    Statement stm = con.createStatement();
                    String m = "INSERT INTO superclass (class_URI, superclass_URI) VALUES ('" + classes.toString() + "','" + sup.toString() + "') ";
                    stm.execute(m);
                    stm.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }

    }

    public static void hasdomain(Connection con, String serviceURI) {

        String query = "SELECT DISTINCT ?class ?property\n"
                + "                        WHERE { ?subject ?property [] .\n"
                + "   ?subject a ?class }\n"
                + "                        ORDER BY ?class ?property";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;

        while (results.hasNext()) {

            final QuerySolution soln = results.nextSolution();
            RDFNode classes = soln.get("class");
            RDFNode prop = soln.get("property");

            try {
                StringBuilder qry = new StringBuilder();

                s = con.createStatement();
                qry.append("INSERT INTO hasDomain(class_URI, property_URI) values('").append(classes.toString()).append("','").append(prop.toString()).append("'").append(")");
                s.execute(qry.toString());
                s.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }
    }

    public static void explicitdataProperty(Connection con, String serviceURI, HashMap<String, Integer> hm) {
        String query = "PREFIX     rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT DISTINCT ?property ?label ?comment\n"
                + "                        WHERE {\n"
                + " ?property a rdf:Property.\n"
                + "[] ?property ?object.\n"
                + "  filter(isLiteral(?object))\n"
                + "                 OPTIONAL { ?property rdfs:label ?label . }\n"
                + "OPTIONAL {  ?property rdfs:comment ?comment. }      }";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;

        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            RDFNode prop = soln.get("property");
            RDFNode label = soln.get("label");
            RDFNode comment = soln.get("comment");

            String pre = prop.toString();
            if (pre.contains("#")) {
                pre = pre.substring(0, pre.lastIndexOf("#") + 1);
            } else {
                pre = pre.substring(0, pre.lastIndexOf("/") + 1);
            }

            if (hm.containsKey(pre)) {
                hm.put(pre, hm.get(pre) + 1);
            } else {
                hm.put(pre, 1);
            }

            try {
                StringBuilder qry = new StringBuilder();
                s = con.createStatement();
                qry.append("INSERT INTO property (property_URI");
                if (label != null) {
                    qry.append(", label");
                }

                if (comment != null) {
                    qry.append(", comment ");
                }

                qry.append(",property_type) values('").append(prop.toString()).append("'");

                if (label != null) {
                    qry.append(",'").append(label.toString()).append("'");
                }

                if (comment != null) {
                    qry.append(",'").append(comment.toString()).append("'");
                }
                qry.append(",'data')");
                System.out.println(qry.toString());
                s.execute(qry.toString());
                s.close();

                String ins = "SELECT DISTINCT (count(*)AS ?in)\n"
                        + "                        WHERE { [] <" + prop.toString() + "> [] . }";
                QueryExecution qr = QueryExecutionFactory.sparqlService(serviceURI, ins);
                ResultSet r = qr.execSelect();
                while (r.hasNext()) {
                    final QuerySolution sol = r.nextSolution();
                    RDFNode inst = sol.get("in");
                    String num = inst.toString();
                    num = num.substring(0, num.indexOf("^"));
                    Statement st = con.createStatement();
                    String m = "UPDATE property SET instance_count='" + num + "' WHERE property_URI='" + prop.toString() + "'";
                    st.execute(m);
                    st.close();
                }
                String dr = "SELECT DISTINCT ?domain (datatype(?object) as ?range) \n"
                        + "                        WHERE { \n"
                        + "   ?subject <" + prop.toString() + "> ?object .\n"
                        + "   ?subject a ?domain .\n"
                        + "     Filter isLiteral(?object) \n"
                        + " }";

                QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, dr);
                ResultSet resultset = qe.execSelect();

//System.out.println(resultset.hasNext());
                //ResultSetFormatter.out(System.out, resultset);
                while (resultset.hasNext()) {

                    final QuerySolution solns = resultset.nextSolution();
                    RDFNode domain = solns.get("domain");
                    RDFNode range = solns.get("range");
                    try {
                        StringBuilder qrey = new StringBuilder();

                        Statement stm = con.createStatement();

                        qrey.append("INSERT INTO triple_type(class_URI,property_URI,property_triple_type,triple_type) values(");

                                         

                        if (domain != null && domain.toString() != null) {
                            qrey.append("'").append(domain.toString()).append("',");
                        }
                        else
                               qrey.append("'http://localhost/blank',");
                        
                        qrey.append("'").append(prop.toString()).append("'");

                        if (range != null && range.toString() != null) {
                            qrey.append(",'").append(range.toString()).append("'");
                        }
                        
                        else
                               qrey.append("'http://localhost/blank',");
                        
                        qrey.append(",'data')");
                        System.out.println(qrey.toString());
                        stm.execute(qrey.toString());
                        stm.close();
                        StringBuilder inst = new StringBuilder();
                        inst.append("SELECT (COUNT(*) as ?in) WHERE { ?s <").append(prop.toString()).append("> ?o .");
                        if (domain != null && domain.toString() != null) {
                            inst.append(" ?s a <").append(domain.toString()).append(">.\n");
                        }
                        if (range != null && range.toString() != null) {
                            inst.append(" ?o a <").append(range.toString()).append(">.\n");
                        }
                        inst.append("}");

                        QueryExecution qre = QueryExecutionFactory.sparqlService(serviceURI, inst.toString());
                        ResultSet rst = qre.execSelect();
                        //   ResultSetFormatter.out(System.out, r);
                        while (rst.hasNext()) {
                            final QuerySolution sol = rst.nextSolution();
                            RDFNode instn = sol.get("in");
                            if (instn != null) {
                                String num = instn.toString();
                                num = num.substring(0, num.indexOf("^"));
                                System.out.println(instn.toString());
                                Statement stmt = con.createStatement();
                                StringBuilder m = new StringBuilder("UPDATE triple_type SET instance_count='");
                                m.append(num).append("' WHERE");
                                if (domain != null && domain.toString() != null) {
                                    m.append(" class_URI='").append(domain.toString()).append("' AND");
                                }

                                m.append(" property_URI='").append(prop.toString()).append("'");

                                if (range != null && range.toString() != null) {
                                    m.append("AND property_triple_type='").append(range.toString()).append("'");
                                }
                                //x=x+1;
                                //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                                stmt.execute(m.toString());
                                stmt.close();

                            }
                        }
                        if (range != null && range.toString() != null) {

                            String pr = range.toString();
                            if (pr.contains("#")) {
                                pr = pr.substring(0, pr.lastIndexOf("#") + 1);
                            } else {
                                pr = pr.substring(0, pr.lastIndexOf("/") + 1);
                            }

                            if (hm.containsKey(pr)) {
                                hm.put(pr, hm.get(pr) + 1);
                            } else {
                                hm.put(pr, 1);
                            }
                            String xsd = "INSERT INTO XSD_type(XSD_URI,data_prop) values('" + range.toString() + "','" + prop.toString() + "')";
                            Statement st = con.createStatement();
                            st.execute(xsd);
                            st.close();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf(e.getMessage());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }
    }

    public static void explicitobjProperty(Connection con, String serviceURI, HashMap<String, Integer> hm) {
        String query = "PREFIX     rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT DISTINCT ?property ?label ?comment\n"
                + "                        WHERE {\n"
                + " ?property a rdf:Property.\n"
                + "[] ?property ?object.\n"
                + " filter(isIRI(?object) ||  isBlank(?object))\n"
                + "                 OPTIONAL { ?property rdfs:label ?label . }\n"
                + "OPTIONAL {  ?property rdfs:comment ?comment. }      }";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;

        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            RDFNode prop = soln.get("property");
            RDFNode label = soln.get("label");
            RDFNode comment = soln.get("comment");

            String pre = prop.toString();
            if (pre.contains("#")) {
                pre = pre.substring(0, pre.lastIndexOf("#") + 1);
            } else {
                pre = pre.substring(0, pre.lastIndexOf("/") + 1);
            }

            if (hm.containsKey(pre)) {
                hm.put(pre, hm.get(pre) + 1);
            } else {
                hm.put(pre, 1);
            }

            try {
                StringBuilder qry = new StringBuilder();
                s = con.createStatement();
                qry.append("INSERT INTO property (property_URI");
                if (label != null) {
                    qry.append(", label");
                }

                if (comment != null) {
                    qry.append(", comment ");
                }

                qry.append(",property_type) values('").append(prop.toString()).append("'");

                if (label != null) {
                    qry.append(",'").append(label.toString()).append("'");
                }

                if (comment != null) {
                    qry.append(",'").append(comment.toString()).append("'");
                }
                qry.append(",'object')");
                System.out.println(qry.toString());
                s.execute(qry.toString());
                s.close();

                String ins = "SELECT DISTINCT (count(*)AS ?in)\n"
                        + "                        WHERE { [] <" + prop.toString() + "> [] . }";
                QueryExecution qr = QueryExecutionFactory.sparqlService(serviceURI, ins);
                ResultSet r = qr.execSelect();
                while (r.hasNext()) {
                    final QuerySolution sol = r.nextSolution();
                    RDFNode inst = sol.get("in");
                    String num = inst.toString();
                    num = num.substring(0, num.indexOf("^"));
                    Statement st = con.createStatement();
                    String m = "UPDATE property SET instance_count='" + num + "' WHERE property_URI='" + prop.toString() + "'";
                    st.execute(m);
                    st.close();
                }

                String dr = "SELECT DISTINCT ?domain ?range \n"
                        + "                        WHERE { \n"
                        + "   ?subject <" + prop.toString() + "> ?object .\n"
                        + " ?object a ?range.\n"
                        + " filter(isIRI(?object) ||  isBlank(?object))   "
                        + " }";

                QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, dr);
                ResultSet resultset = qe.execSelect();

                //  Statement stm = null;
                //  String qry="INSERT INTO class(class_URI) values(?)";
                System.out.println(resultset.hasNext());
                //ResultSetFormatter.out(System.out, resultset);

                while (resultset.hasNext()) {

                    final QuerySolution solns = resultset.nextSolution();
                    RDFNode domain = solns.get("domain");
                    // RDFNode property = soln.get("property");
                    RDFNode range = solns.get("range");
                    try {
                        StringBuilder qrey = new StringBuilder();
                        // System.out.println(classes.toString()+label.toString()!=null?label.toString():"hello1"+comment.toString()!=null?comment.toString():"comment");
                        Statement stm = con.createStatement();

                        qrey.append("INSERT INTO triple_type(class_URI,property_URI,property_triple_type,triple_type) values(");

                                         

                        if (domain != null && domain.toString() != null) {
                            qrey.append("'").append(domain.toString()).append("',");
                        }
                        else
                               qrey.append("'http://localhost/blank',");
                        
                        qrey.append("'").append(prop.toString()).append("'");

                        if (range != null && range.toString() != null) {
                            qrey.append(",'").append(range.toString()).append("'");
                        }
                        
                        else
                               qrey.append("'http://localhost/blank',");

                        qrey.append(",'object')");
                        System.out.println(qrey.toString());
                        stm.execute(qrey.toString());
                        stm.close();
                        StringBuilder inst = new StringBuilder();
                        inst.append("SELECT (COUNT(*) as ?in) WHERE { ?s <").append(prop.toString()).append("> ?o .");
                        if (domain != null && domain.toString() != null) {
                            inst.append(" ?s a <").append(domain.toString()).append(">.\n");
                        }
                        if (range != null && range.toString() != null) {
                            inst.append(" ?o a <").append(range.toString()).append(">.\n");
                        }
                        inst.append("}");

                        QueryExecution qre = QueryExecutionFactory.sparqlService(serviceURI, inst.toString());
                        ResultSet rst = qre.execSelect();
                        //   ResultSetFormatter.out(System.out, r);
                        while (rst.hasNext()) {
                            final QuerySolution sol = rst.nextSolution();
                            RDFNode instn = sol.get("in");
                            if (instn != null) {
                                String num = instn.toString();
                                num = num.substring(0, num.indexOf("^"));
                                System.out.println(instn.toString());
                                try (Statement stmt = con.createStatement()) {
                                    StringBuilder m = new StringBuilder("UPDATE triple_type SET instance_count='");
                                    m.append(num).append("' WHERE");
                                    if (domain != null && domain.toString() != null) {
                                        m.append(" class_URI='").append(domain.toString()).append("' AND");
                                    }

                                    m.append(" property_URI='").append(prop.toString()).append("'");

                                    if (range != null && range.toString() != null) {
                                        m.append("AND property_triple_type='").append(range.toString()).append("'");
                                    }
                                    //x=x+1;
                                    //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                                    stmt.execute(m.toString());
                                    stmt.close();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf(e.getMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }
    }

    public static void objectProperty(Connection con, String serviceURI, HashMap<String, Integer> hm) {
        String query = "  PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT DISTINCT ?property ?label ?comment\n"
                + "                        WHERE { [] ?property ?object\n"
                + "       filter(isIRI(?object) ||  isBlank(?object))\n"
                + "OPTIONAL { ?property rdfs:label ?label . }\n"
                + "OPTIONAL {  ?property rdfs:comment ?comment. }\n"
                + "}";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;

        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            RDFNode prop = soln.get("property");
            RDFNode label = soln.get("label");
            RDFNode comment = soln.get("comment");

            String pre = prop.toString();
            if (pre.contains("#")) {
                pre = pre.substring(0, pre.lastIndexOf("#") + 1);
            } else {
                pre = pre.substring(0, pre.lastIndexOf("/") + 1);
            }

            if (hm.containsKey(pre)) {
                hm.put(pre, hm.get(pre) + 1);
            } else {
                hm.put(pre, 1);
            }

            try {
                StringBuilder qry = new StringBuilder();
                s = con.createStatement();
                qry.append("INSERT INTO property (property_URI");
                if (label != null) {
                    qry.append(", label");
                }

                if (comment != null) {
                    qry.append(", comment ");
                }

                qry.append(",property_type) values('").append(prop.toString()).append("'");

                if (label != null) {
                    qry.append(",'").append(label.toString()).append("'");
                }

                if (comment != null) {
                    qry.append(",'").append(comment.toString()).append("'");
                }
                qry.append(",'object')");
                System.out.println(qry.toString());
                s.execute(qry.toString());
                s.close();

                String ins = "SELECT DISTINCT (count(*)AS ?in)\n"
                        + "                        WHERE { [] <" + prop.toString() + "> [] . }";
                QueryExecution qr = QueryExecutionFactory.sparqlService(serviceURI, ins);
                ResultSet r = qr.execSelect();
                while (r.hasNext()) {
                    final QuerySolution sol = r.nextSolution();
                    RDFNode inst = sol.get("in");
                    String num = inst.toString();
                    num = num.substring(0, num.indexOf("^"));
                    Statement st = con.createStatement();
                    String m = "UPDATE property SET instance_count='" + num + "' WHERE property_URI='" + prop.toString() + "'";
                    st.execute(m);
                    st.close();
                }

                String dr = "SELECT DISTINCT ?domain ?range \n"
                        + "                        WHERE { \n"
                        + "   ?subject <" + prop.toString() + "> ?object .\n"
                        + " ?object a ?range.\n"
                        + " filter(isIRI(?object) ||  isBlank(?object))   "
                        + " }";

                QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, dr);
                ResultSet resultset = qe.execSelect();

                //  Statement stm = null;
                //  String qry="INSERT INTO class(class_URI) values(?)";
                System.out.println(resultset.hasNext());
                //ResultSetFormatter.out(System.out, resultset);

                while (resultset.hasNext()) {

                    final QuerySolution solns = resultset.nextSolution();
                    RDFNode domain = solns.get("domain");
                    // RDFNode property = soln.get("property");
                    RDFNode range = solns.get("range");
                    try {
                        StringBuilder qrey = new StringBuilder();
                        // System.out.println(classes.toString()+label.toString()!=null?label.toString():"hello1"+comment.toString()!=null?comment.toString():"comment");
                        Statement stm = con.createStatement();

                         qrey.append("INSERT INTO triple_type(class_URI,property_URI,property_triple_type,triple_type) values(");

                                         

                        if (domain != null && domain.toString() != null) {
                            qrey.append("'").append(domain.toString()).append("',");
                        }
                        else
                               qrey.append("'http://localhost/blank',");
                        
                        qrey.append("'").append(prop.toString()).append("'");

                        if (range != null && range.toString() != null) {
                            qrey.append(",'").append(range.toString()).append("'");
                        }
                        
                        else
                               qrey.append("'http://localhost/blank',");

                        qrey.append(",'object')");
                        System.out.println(qrey.toString());
                        stm.execute(qrey.toString());
                        stm.close();
                        StringBuilder inst = new StringBuilder();
                        inst.append("SELECT (COUNT(*) as ?in) WHERE { ?s <").append(prop.toString()).append("> ?o .");
                        if (domain != null && domain.toString() != null) {
                            inst.append(" ?s a <").append(domain.toString()).append(">.\n");
                        }
                        if (range != null && range.toString() != null) {
                            inst.append(" ?o a <").append(range.toString()).append(">.\n");
                        }
                        inst.append("}");

                        QueryExecution qre = QueryExecutionFactory.sparqlService(serviceURI, inst.toString());
                        ResultSet rst = qre.execSelect();
                        //   ResultSetFormatter.out(System.out, r);
                        while (rst.hasNext()) {
                            final QuerySolution sol = rst.nextSolution();
                            RDFNode instn = sol.get("in");
                            if (instn != null) {
                                String num = instn.toString();
                                num = num.substring(0, num.indexOf("^"));
                                System.out.println(instn.toString());
                                try (Statement stmt = con.createStatement()) {
                                    StringBuilder m = new StringBuilder("UPDATE triple_type SET instance_count='");
                                    m.append(num).append("' WHERE");
                                    if (domain != null && domain.toString() != null) {
                                        m.append(" class_URI='").append(domain.toString()).append("' AND");
                                    }

                                    m.append(" property_URI='").append(prop.toString()).append("'");

                                    if (range != null && range.toString() != null) {
                                        m.append("AND property_triple_type='").append(range.toString()).append("'");
                                    }
                                    //x=x+1;
                                    //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                                    stmt.execute(m.toString());
                                    stmt.close();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf(e.getMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }
    }

    public static void dataProperty(Connection con, String serviceURI, HashMap<String, Integer> hm) {
        String query = "PREFIX    rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "SELECT DISTINCT ?property ?label ?comment\n"
                + "                        WHERE { [] ?property ?object\n"
                + "       filter(isLiteral(?object))\n"
                + "OPTIONAL { ?property rdfs:label ?label . }\n"
                + "OPTIONAL {  ?property rdfs:comment ?comment. }\n"
                + "}";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;

        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            RDFNode prop = soln.get("property");
            RDFNode label = soln.get("label");
            RDFNode comment = soln.get("comment");

            String pre = prop.toString();
            if (pre.contains("#")) {
                pre = pre.substring(0, pre.lastIndexOf("#") + 1);
            } else {
                pre = pre.substring(0, pre.lastIndexOf("/") + 1);
            }

            if (hm.containsKey(pre)) {
                hm.put(pre, hm.get(pre) + 1);
            } else {
                hm.put(pre, 1);
            }

            try {
                StringBuilder qry = new StringBuilder();
                s = con.createStatement();
                qry.append("INSERT INTO property (property_URI");
                if (label != null) {
                    qry.append(", label");
                }

                if (comment != null) {
                    qry.append(", comment ");
                }

                qry.append(",property_type) values('").append(prop.toString()).append("'");

                if (label != null) {
                    qry.append(",'").append(label.toString()).append("'");
                }

                if (comment != null) {
                    qry.append(",'").append(comment.toString()).append("'");
                }
                qry.append(",'data')");
                System.out.println(qry.toString());
                s.execute(qry.toString());
                s.close();

                String ins = "SELECT DISTINCT (count(*)AS ?in)\n"
                        + "                        WHERE { [] <" + prop.toString() + "> [] . }";
                QueryExecution qr = QueryExecutionFactory.sparqlService(serviceURI, ins);
                ResultSet r = qr.execSelect();
                while (r.hasNext()) {
                    final QuerySolution sol = r.nextSolution();
                    RDFNode inst = sol.get("in");
                    String num = inst.toString();
                    num = num.substring(0, num.indexOf("^"));
                    Statement st = con.createStatement();
                    String m = "UPDATE property SET instance_count='" + num + "' WHERE property_URI='" + prop.toString() + "'";
                    st.execute(m);
                    st.close();
                }

                String dr = "SELECT DISTINCT ?domain (datatype(?object) as ?range) \n"
                        + "                        WHERE { \n"
                        + "   ?subject <" + prop.toString() + "> ?object .\n"
                        + "   ?subject a ?domain .\n"
                        + "     Filter isLiteral(?object) \n"
                        + " }";

                QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, dr);
                ResultSet resultset = qe.execSelect();

//System.out.println(resultset.hasNext());
                //ResultSetFormatter.out(System.out, resultset);
                while (resultset.hasNext()) {

                    final QuerySolution solns = resultset.nextSolution();
                    RDFNode domain = solns.get("domain");
                    RDFNode range = solns.get("range");
                    try {
                        StringBuilder qrey = new StringBuilder();

                        Statement stm = con.createStatement();

                         qrey.append("INSERT INTO triple_type(class_URI,property_URI,property_triple_type,triple_type) values(");

                                         

                        if (domain != null && domain.toString() != null) {
                            qrey.append("'").append(domain.toString()).append("',");
                        }
                        else
                               qrey.append("'http://localhost/blank',");
                        
                        qrey.append("'").append(prop.toString()).append("'");

                        if (range != null && range.toString() != null) {
                            qrey.append(",'").append(range.toString()).append("'");
                        }
                        
                        else
                               qrey.append("'http://localhost/blank',");

                        qrey.append(",'data')");
                        System.out.println(qrey.toString());
                        stm.execute(qrey.toString());
                        stm.close();
                        StringBuilder inst = new StringBuilder();
                        inst.append("SELECT (COUNT(*) as ?in) WHERE { ?s <").append(prop.toString()).append("> ?o .");
                        if (domain != null && domain.toString() != null) {
                            inst.append(" ?s a <").append(domain.toString()).append(">.\n");
                        }
                        if (range != null && range.toString() != null) {
                            inst.append(" ?o a <").append(range.toString()).append(">.\n");
                        }
                        inst.append("}");

                        QueryExecution qre = QueryExecutionFactory.sparqlService(serviceURI, inst.toString());
                        ResultSet rst = qre.execSelect();
                        //   ResultSetFormatter.out(System.out, r);
                        while (rst.hasNext()) {
                            final QuerySolution sol = rst.nextSolution();
                            RDFNode instn = sol.get("in");
                            if (instn != null) {
                                String num = instn.toString();
                                num = num.substring(0, num.indexOf("^"));
                                System.out.println(instn.toString());
                                Statement stmt = con.createStatement();
                                StringBuilder m = new StringBuilder("UPDATE triple_type SET instance_count='");
                                m.append(num).append("' WHERE");
                                if (domain != null && domain.toString() != null) {
                                    m.append(" class_URI='").append(domain.toString()).append("' AND");
                                }

                                m.append(" property_URI='").append(prop.toString()).append("'");

                                if (range != null && range.toString() != null) {
                                    m.append("AND property_triple_type='").append(range.toString()).append("'");
                                }
                                //x=x+1;
                                //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                                stmt.execute(m.toString());
                                stmt.close();
                            }
                        }
                        if (range != null && range.toString() != null) {
                            String pr = range.toString();
                            if (pr.contains("#")) {
                                pr = pr.substring(0, pr.lastIndexOf("#") + 1);
                            } else {
                                pr = pr.substring(0, pr.lastIndexOf("/") + 1);
                            }

                            if (hm.containsKey(pr)) {
                                hm.put(pr, hm.get(pr) + 1);
                            } else {
                                hm.put(pr, 1);
                            }

                            String xsd = "INSERT INTO XSD_type(XSD_URI,data_prop) values('" + range.toString() + "','" + prop.toString() + "')";
                            Statement st = con.createStatement();
                            st.execute(xsd);
                            st.close();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf(e.getMessage());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }

    }

    public static void subproperty(Connection con) {

        String serviceURI = "http://vulcan.cs.uga.edu/sparql";
        String query = " prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> \n SELECT DISTINCT ?property1 ?property2 \n"
                + "WHERE { ?property1 rdfs:subPropertyOf ?property2 .}";

        QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
        ResultSet results = q.execSelect();

        Statement s = null;

        while (results.hasNext()) {

            final QuerySolution soln = results.nextSolution();
            RDFNode prop1 = soln.get("property1");
            RDFNode prop2 = soln.get("property2");

            try {
                StringBuilder qry = new StringBuilder();

                s = con.createStatement();
                qry.append("INSERT INTO subproperty(property_URI,subproperty_URI) values('").append(prop2.toString()).append("','").append(prop1.toString()).append("')");
                System.out.println(qry.toString());
                s.execute(qry.toString());
                s.close();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.printf(e.getMessage());
            }
        }
    }
    
    public static void explicitDomainRangeObj(Connection con, String serviceURI) {

        String dr = "prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
"prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#>\n" +
"\n" +
"select ?d ?p ?r where {\n" +
"  ?p a rdf:Property.\n" +
"  ?p rdfs:domain ?d.\n" +
"  ?p rdfs:range ?r.\n" +
"  filter(isIRI(?r) ||  isBlank(?r))\n" +
"}";

                QueryExecution qe = QueryExecutionFactory.sparqlService(serviceURI, dr);
                ResultSet resultset = qe.execSelect();

              //  Statement stm = null;
                //  String qry="INSERT INTO class(class_URI) values(?)";
                System.out.println(resultset.hasNext());
 //ResultSetFormatter.out(System.out, resultset);

                while (resultset.hasNext()) {

                    final QuerySolution solns = resultset.nextSolution();
                    RDFNode domain = solns.get("d");
                    RDFNode property = solns.get("p");
                    RDFNode range = solns.get("r");
                    try {
                        StringBuilder qrey = new StringBuilder();
                        // System.out.println(classes.toString()+label.toString()!=null?label.toString():"hello1"+comment.toString()!=null?comment.toString():"comment");
                        Statement stm = con.createStatement();

                        qrey.append("INSERT INTO triple_type(");

                        if (domain != null && domain.toString() != null) {
                            qrey.append("class_URI,");
                        }

                        qrey.append("property_URI");

                        if (range != null && range.toString() != null) {
                            qrey.append(",property_triple_type");
                        }

                        qrey.append(",triple_type) values(");

                        if (domain != null && domain.toString() != null) {
                            qrey.append("'").append(domain.toString()).append("',");
                        }

                        qrey.append("'").append(property.toString()).append("'");

                        if (range != null && range.toString() != null) {
                            qrey.append(",'").append(range.toString()).append("'");
                        }

                        qrey.append(",'object')");
                        System.out.println(qrey.toString());
                        stm.execute(qrey.toString());
                        stm.close();
                        StringBuilder inst = new StringBuilder();
                        inst.append("SELECT (COUNT(*) as ?in) WHERE { ?s <").append(property.toString()).append("> ?o .");
                        if (domain != null && domain.toString() != null) {
                            inst.append(" ?s a <").append(domain.toString()).append(">.\n");
                        }
                        if (range != null && range.toString() != null) {
                            inst.append(" ?o a <").append(range.toString()).append(">.\n");
                        }
                        inst.append("}");

                        QueryExecution qre = QueryExecutionFactory.sparqlService(serviceURI, inst.toString());
                        ResultSet rst = qre.execSelect();
                        //   ResultSetFormatter.out(System.out, r);
                        while (rst.hasNext()) {
                            final QuerySolution sol = rst.nextSolution();
                            RDFNode instn = sol.get("in");
                            if (instn != null) {
                                String num = instn.toString();
                                num = num.substring(0, num.indexOf("^"));
                                System.out.println(instn.toString());
                                try (Statement stmt = con.createStatement()) {
                                    StringBuilder m = new StringBuilder("UPDATE triple_type SET instance_count='");
                                    m.append(num).append("' WHERE");
                                    if (domain != null && domain.toString() != null) {
                                        m.append(" class_URI='").append(domain.toString()).append("' AND");
                                    }

                                    m.append(" property_URI='").append(property.toString()).append("'");

                                    if (range != null && range.toString() != null) {
                                        m.append("AND property_triple_type='").append(range.toString()).append("'");
                                    }
                                //x=x+1;
                                    //String m = "UPDATE class SET instance_count='"+x+"' WHERE class_URI='" + classes.toString() + "'";
                                    stmt.execute(m.toString());
                                    stmt.close();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf(e.getMessage());
                    }

    }
 }

    public static void prefix(Connection con, HashMap<String, Integer> hm) throws SQLException {
        for (Map.Entry<String, Integer> entry : hm.entrySet()) {

            Statement st = con.createStatement();
            String key = entry.getKey();
            int value = entry.getValue();
            String qry = "INSERT INTO prefix (prefix,count) values('" + key + "','" + value + "')";
            System.out.println("Prefix:" + key + "   Count: " + value + qry);
            st.execute(qry);
            st.close();
        }
    }

}
 