/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jim
 */
public class MedSavantServlet extends HttpServlet {
    public void doPost(HttpServletRequest request, 
                       HttpServletResponse response)
                       throws IOException, ServletException {
        
      
      doGet(request, response);
    } 
}
