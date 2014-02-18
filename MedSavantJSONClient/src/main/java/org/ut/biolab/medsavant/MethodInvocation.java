package org.ut.biolab.medsavant;

import com.google.gson.Gson;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.model.exception.LockException;

/**
 * Used to invoke methods, and stores everything necessary to invoke a method at
 * a later time. Communicates with the BlockingQueueManager to enqueue method
 * requests that are locked out by the MedSavant Server.
 *
 * Only methods that throw LockExceptions are enqueued, as other methods don't
 * use locks and should return promptly.
 *
 */
public class MethodInvocation {

    //Sleep this long after a session has expired before trying a new one;
    private static final int SESSION_EXPIRE_DELAY = 1000;

    //The value to return to indicate that a request has been queued.  All
    //methods that generate LockExceptions return positive integers, so use
    //a negative integer.
    private static final int QUEUED_RETURNVAL = -100;
    private final Object adapter;
    private final Method method;
    private final Object[] args;
    private final Session session;
    private final Gson gson;
    private boolean throwsLockingException;

    public MethodInvocation(Session session, Gson gson, Object adapter, Method method, Object[] args) {
        this.gson = gson;
        this.adapter = adapter;
        this.method = method;
        this.args = args;
        this.session = session;
        this.throwsLockingException = false;

        for (Class t : method.getExceptionTypes()) {
            if (t.getName().endsWith("LockException")) {
                throwsLockingException = true;
            }
        }
    }

    public String getName() {
        return method.getName();
    }

    @Override
    public String toString(){
        String s = adapter.getClass().getName()+"."+method.getName()+"(";
        for(Object o : args){
            s += o.getClass().getName()+",";
        }
        return (args.length > 0 ? s.substring(0, s.length()-2) : "")+")";        
    }
    
    public String invoke() throws IllegalArgumentException, LockException {
        return invoke(false);
    }
    
    String invoke(boolean invokedFromQueue) throws IllegalArgumentException, LockException {
        if (throwsLockingException && !invokedFromQueue) {
            BlockingQueueManager.enqueue(this);
            return gson.toJson(QUEUED_RETURNVAL, Integer.class);
        } else {
            boolean sessionExpired = false;
            do {
                try {
                    Object returnVal = this.method.invoke(adapter, args);
                    if (returnVal != null) {
                        return gson.toJson(returnVal, method.getReturnType());
                    }
                    return null;
                } catch (IllegalAccessException iae) {
                    throw new IllegalArgumentException("Couldn't execute method with given arguments: " + iae.getMessage());
                } catch (InvocationTargetException ite) {
                    if (ite.getCause() instanceof SessionExpiredException) {
                        String sessionId = session.getSessionId(true);
                        args[0] = sessionId;
                        sessionExpired = true;
                        try {
                            Thread.sleep(SESSION_EXPIRE_DELAY);
                        } catch (InterruptedException iex) {
                        }
                    } else if (ite.getCause() instanceof LockException) {
                        throw (LockException) (ite.getCause());
                    } else {
                        throw new IllegalArgumentException("Couldn't execute method with given arguments, "
                                + ite.getCause());
                    }
                }
            } while (sessionExpired);

            return null;
        }

    }
}
