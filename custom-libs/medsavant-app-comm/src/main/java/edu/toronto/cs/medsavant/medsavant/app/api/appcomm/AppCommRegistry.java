/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.toronto.cs.medsavant.medsavant.app.api.appcomm;

import edu.toronto.cs.medsavant.medsavant.app.api.appcomm.AppCommHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mfiume
 */
public class AppCommRegistry {
    
    private static AppCommRegistry instance;
    
    public static AppCommRegistry getInstance() {
        if (instance == null) {
            instance = new AppCommRegistry();
        }
        return instance;
    }
    
    private final HashMap<Class, Set<AppCommHandler>> classToHandlerMap;
    
    private AppCommRegistry() {
        classToHandlerMap = new HashMap<Class,Set<AppCommHandler>>();
    }
    
    public void registerHandler(AppCommHandler handler, Class eventClass) {
        System.out.println("Registering handler for " + eventClass);
        Set<AppCommHandler> handlers = classToHandlerMap.get(eventClass);
        if (handlers == null) {
            handlers = new HashSet<AppCommHandler>();
        }
        handlers.add(handler);
        classToHandlerMap.put(eventClass, handlers);
    }
    
    public Set<AppCommHandler> getHandlersForEvent(Class eventClass) {
        System.out.println("Getting handlers of type " + eventClass);
        Set<AppCommHandler> handlers = classToHandlerMap.get(eventClass);
        if (handlers == null) {
            System.out.println("No handlers of type " + eventClass);
            return new HashSet<AppCommHandler>();
        }
        System.out.println(handlers.size() + " handlers of type " + eventClass);
        return handlers;
    }
    
    
}
