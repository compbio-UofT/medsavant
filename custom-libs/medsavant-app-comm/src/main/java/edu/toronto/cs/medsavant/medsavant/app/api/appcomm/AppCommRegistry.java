/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
