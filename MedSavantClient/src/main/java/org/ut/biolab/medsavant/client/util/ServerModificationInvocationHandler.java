/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.util.Modifier;

public class ServerModificationInvocationHandler<T> implements InvocationHandler {

    private final T proxiedInterface;

    public ServerModificationInvocationHandler(T proxiedInterface) {
        this.proxiedInterface = proxiedInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            Object m = method.invoke(proxiedInterface, args);
            if (method.isAnnotationPresent(Modifier.class)) {
                Modifier modifier = method.getAnnotation(Modifier.class);
                CacheController.getInstance().expire(modifier.type(), Thread.currentThread().getId());
            }

            return m;

        // handle session expiration
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getCause();
            }

            if (e instanceof SessionExpiredException) {
                SessionExpiredException see = (SessionExpiredException) e;
                MedSavantExceptionHandler.handleSessionExpiredException(see);
                return null;
            } else {
                throw e;
            }
        }

    }

}
