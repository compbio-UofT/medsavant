/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.db.connection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class ConnectionPool {

    private Vector connections;
    private String url, user, password;
    private final long timeout=60000;
    private ConnectionReaper reaper;
    private final int poolsize=3;
    private final static Object lock = new Object();

    public ConnectionPool(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        connections = new Vector(poolsize);
        reaper = new ConnectionReaper(this);
        reaper.start();
    }

    public void reapConnections() {

        long stale = System.currentTimeMillis() - timeout;

        synchronized(lock){
            Enumeration connlist = connections.elements();
            while((connlist != null) && (connlist.hasMoreElements())) {
                MSConnection conn = (MSConnection)connlist.nextElement();
                if(conn.inUse() && stale >conn.getLastUse() && !conn.validate()) {
                    removeConnection(conn);
                }
            }
        }
    }

    public void closeConnections() {
        synchronized(lock){
            Enumeration connlist = connections.elements();
            while((connlist != null) && (connlist.hasMoreElements())) {
                MSConnection conn = (MSConnection)connlist.nextElement();
                removeConnection(conn);
            }
        }
    }

    private void removeConnection(MSConnection conn) {
        synchronized(lock){
            connections.removeElement(conn);
        }
    }


    public Connection getConnection() throws SQLException {

       MSConnection c;
       while(true){
           
            synchronized(lock){

                //check for existing connection
                for(int i = 0; i < connections.size(); i++) {
                    c = (MSConnection)connections.elementAt(i);
                    if (c.lease()) {
                        //System.out.println("Leasing connection " + i);
                        return c;
                    }
                }

                //create a new connection
                if(connections.size() < poolsize){
                    Connection conn = DriverManager.getConnection(url, user, password);
                    c = new MSConnection(conn, this);
                    //System.out.println("Leasing new connection");
                    c.lease();
                    connections.addElement(c);
                    return c;
                }
            }
           
            //wait for a connection to close
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(ConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
       }

    } 

    public void returnConnection(MSConnection conn) {
        synchronized(lock){
            //System.out.println("Expiring Lease");
            conn.expireLease();
        }
    }
}

class ConnectionReaper extends Thread {

    private ConnectionPool pool;
    private final long delay=30000;

    ConnectionReaper(ConnectionPool pool) {
        this.pool=pool;
    }

    public void run() {
        while(true) {
            try {
                sleep(delay);
            } catch (InterruptedException e) { }
            pool.reapConnections();
        }
    }
}