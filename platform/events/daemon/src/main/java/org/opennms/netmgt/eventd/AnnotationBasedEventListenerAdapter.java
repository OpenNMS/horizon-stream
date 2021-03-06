/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.eventd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.horizon.core.lib.Logging;
import org.opennms.horizon.events.annotations.EventExceptionHandler;
import org.opennms.horizon.events.annotations.EventHandler;
import org.opennms.horizon.events.annotations.EventListener;
import org.opennms.horizon.events.annotations.EventPostProcessor;
import org.opennms.horizon.events.annotations.EventPreProcessor;
import org.opennms.horizon.events.api.AnnotationBasedEventListenerFactory;
import org.opennms.horizon.events.api.EventSubscriptionService;
import org.opennms.horizon.events.api.ThreadAwareEventListener;
import org.opennms.horizon.events.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * AnnotationBasedEventListenerAdapter
 *
 * @author brozow
 * @version $Id: $
 */
public class AnnotationBasedEventListenerAdapter implements org.opennms.horizon.events.api.EventListener, ThreadAwareEventListener, AnnotationBasedEventListenerFactory.AnnotationBasedEventListener {
    
	
	private static final Logger LOG = LoggerFactory.getLogger(AnnotationBasedEventListenerAdapter.class);

    private volatile String m_name = null;
    private volatile Object m_annotatedListener;
    private volatile String m_logPrefix = null;
    private volatile int m_threads = 1;
    private final EventSubscriptionService m_subscriptionService;

    private final Map<String, Method> m_ueiToHandlerMap = new HashMap<>();
    private final List<Method> m_eventPreProcessors = new LinkedList<>();
    private final List<Method> m_eventPostProcessors = new LinkedList<>();
    private final SortedSet<Method> m_exceptionHandlers = new TreeSet<>(createExceptionHandlerComparator());

    public AnnotationBasedEventListenerAdapter(Object annotatedListener, EventSubscriptionService subscriptionService) {
        m_annotatedListener = annotatedListener;
        m_subscriptionService = subscriptionService;
    }

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getLogPrefix() {
        return m_logPrefix;
    }

    public void setLogPrefix(String logPrefix) {
        m_logPrefix = logPrefix;
    }

    @Override
    public void onEvent(final IEvent event) {
        if (event.getUei() == null) {
            return;
        }

        Method m = m_ueiToHandlerMap.get(event.getUei());
        if (m == null) {
            // Try to get a catch-all event handler
            m = m_ueiToHandlerMap.get(EventHandler.ALL_UEIS);
            if (m == null) {
                throw new IllegalArgumentException("Received an event for which we have no handler!");
            }
        }
        
        final Method method = m;
        Logging.withPrefix(m_logPrefix, new Runnable() {

            @Override
            public void run() {
                try {
                    preprocessEvent(event);
                    
                    processEvent(event, method);
                    
                    postprocessEvent(event);
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (IllegalAccessException e) {
                    throw new UndeclaredThrowableException(e);
                } catch (InvocationTargetException e) {
                    handleException(event, e.getCause());
                }
            }
            
        });
    }

    protected void postprocessEvent(IEvent event) throws IllegalAccessException,
            InvocationTargetException {
        for(Method m : m_eventPostProcessors) {
            processEvent(event, m);
        }
    }

    protected void processEvent(IEvent event, Method method)
            throws IllegalAccessException, InvocationTargetException {
        method.invoke(m_annotatedListener, event);
    }

    protected void preprocessEvent(IEvent event) throws IllegalAccessException,
            InvocationTargetException {
        for(Method m : m_eventPreProcessors) {
            processEvent(event, m);
        }
    }

    protected void handleException(IEvent event, Throwable cause) {
        for(Method method : m_exceptionHandlers) {
            if (ClassUtils.isAssignableValue(method.getParameterTypes()[1], cause)) {
                try {
                    method.invoke(m_annotatedListener, event, cause);
                    
                    // we found the correct handler to we are done
                    return;
                } catch (Throwable e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        }
        LOG.warn("Caught an unhandled exception while processing event {}, for listener {}. Add EventExceptionHandler annotation to the listener", event.getUei(), m_annotatedListener, cause);
    }

    public void setAnnotatedListener(Object annotatedListener) {
        m_annotatedListener = annotatedListener;
    }

    @Override
    public void start() {
        EventListener listenerInfo = findEventListenerAnnotation(m_annotatedListener);
        if (listenerInfo == null) {
            throw new IllegalArgumentException("value of annotatedListener property of class " + m_annotatedListener.getClass() + " must be annotated as " + EventListener.class.getName());
        }

        if (m_name == null) {
            m_name = listenerInfo.name();
        }
        
        if (m_logPrefix == null) {
            if (listenerInfo.logPrefix() != null && !"".equals(listenerInfo.logPrefix())) {
                m_logPrefix = listenerInfo.logPrefix();
            } else {
                m_logPrefix = m_name;
            }
        }

        m_threads = listenerInfo.threads();

        populatePreProcessorList();
        
        populateUeiToHandlerMap();
        
        populatePostProcessorList();
        
        populateExceptionHandlersSet();

        // If we only have one EventHandler that is intended to be used as a handler for any UEI, then
        // register this class as an EventListener for all UEIs
        if (m_ueiToHandlerMap.size() == 1 && EventHandler.ALL_UEIS.equals(m_ueiToHandlerMap.keySet().toArray()[0])) {
            m_subscriptionService.addEventListener(this);
        } else {
            m_subscriptionService.addEventListener(this, new HashSet<String>(m_ueiToHandlerMap.keySet()));
        }
    }

    private static EventListener findEventListenerAnnotation(Object annotatedListener) {
        return annotatedListener.getClass().getAnnotation(EventListener.class);
    }

    private void populateExceptionHandlersSet() {
        Method[] methods = m_annotatedListener.getClass().getMethods();
        for(Method method : methods) {
            if (method.isAnnotationPresent(EventExceptionHandler.class)) {
                validateMethodAsEventExceptionHandler(method);
                m_exceptionHandlers.add(method);
            }
        }
    }
    
    private static void validateMethodAsEventExceptionHandler(Method method) {
        Assert.state(method.getParameterTypes().length == 2, "Invalid number of parameters. EventExceptionHandler methods must take 2 arguments with types (Event, ? extends Throwable)");
        Assert.state(ClassUtils.isAssignable(IEvent.class, method.getParameterTypes()[0]), "First parameter of incorrect type. EventExceptionHandler first paramenter must be of type Event");
        Assert.state(ClassUtils.isAssignable(Throwable.class, method.getParameterTypes()[1]), "Second parameter of incorrect type. EventExceptionHandler second paramenter must be of type ? extends Throwable");
    }

    @Override
    public int getNumThreads() {
        // If the listener is thread aware, then use the value provided by
        // the thread aware interface, otherwise fall back to the value
        // provided in the annotation
        if (m_annotatedListener instanceof ThreadAwareEventListener) {
            return ((ThreadAwareEventListener) m_annotatedListener).getNumThreads();
        }
        return m_threads;
    }

    private static class ClassComparator<T> implements Comparator<Class<? extends T>> {
        @Override
        public int compare(Class<? extends T> lhsType, Class<? extends T> rhsType) {
            return ClassUtils.isAssignable(lhsType, rhsType) ? 1 : -1;
        }
    }

    private Comparator<Method> createExceptionHandlerComparator() {
        final ClassComparator<Throwable> classComparator = new ClassComparator<>();
        
        Comparator<Method> comparator = new Comparator<Method>() {

            @Override
            public int compare(Method left, Method right) {
                Class<? extends Throwable> lhsType = left.getParameterTypes()[1].asSubclass(Throwable.class);
                Class<? extends Throwable> rhsType = right.getParameterTypes()[1].asSubclass(Throwable.class);
                
                EventExceptionHandler leftHandlerInfo = AnnotationUtils.findAnnotation(left, EventExceptionHandler.class);
                EventExceptionHandler rightHandlerInfo = AnnotationUtils.findAnnotation(right, EventExceptionHandler.class);
                
                if (leftHandlerInfo.order() == rightHandlerInfo.order()) {
                    return classComparator.compare(lhsType, rhsType);
                } else {
                    return leftHandlerInfo.order() - rightHandlerInfo.order();
                }
            }
            
        };
        
        return comparator;

    }

    private void populatePostProcessorList() {
        Method[] methods = m_annotatedListener.getClass().getMethods();
        for(Method method : methods) {
            if (method.isAnnotationPresent(EventPostProcessor.class)) {
                validateMethodAsEventHandler(method);
                m_eventPostProcessors.add(method);
            }
        }
    }

    private void populatePreProcessorList() {
        Method[] methods = m_annotatedListener.getClass().getMethods();
        for(Method method : methods) {
            EventPreProcessor ann = AnnotationUtils.findAnnotation(method, EventPreProcessor.class);
            if (ann != null) {
                validateMethodAsEventHandler(method);
                m_eventPreProcessors.add(method);
            }
        }
    }

    private void populateUeiToHandlerMap() {
        Method[] methods = m_annotatedListener.getClass().getMethods();
        
        for(Method method : methods) {
            EventHandler handlerInfo = AnnotationUtils.findAnnotation(method, EventHandler.class);
            if (handlerInfo != null) {
                String singleUei = handlerInfo.uei();
                if (singleUei != null && !"".equals(singleUei)) {
                    validateMethodAsEventHandler(method);
                    Assert.state(!m_ueiToHandlerMap.containsKey(singleUei), "Cannot define method "+method+" as a handler for event "+singleUei+" since "+m_ueiToHandlerMap.get(singleUei)+" is already defined as a handler");
                    m_ueiToHandlerMap.put(singleUei, method);
                }

                String[] ueis = handlerInfo.ueis();
                if (ueis != null && ueis.length > 0) {
                    validateMethodAsEventHandler(method);
                    for (String uei : ueis) {
                        Assert.state(!m_ueiToHandlerMap.containsKey(uei), "Cannot define method "+method+" as a handler for event "+uei+" since "+m_ueiToHandlerMap.get(uei)+" is already defined as a handler");
                        m_ueiToHandlerMap.put(uei, method);
                    }
                }
            }
        }

        Assert.state(!m_ueiToHandlerMap.isEmpty(), "annotatedListener must have public EventHandler annotated methods");

    }

    private static void validateMethodAsEventHandler(Method method) {
        Assert.state(method.getParameterTypes().length == 1, "Invalid number of paremeters for method "+method+". EventHandler methods must take a single event argument");
        Assert.state(method.getParameterTypes()[0].isAssignableFrom(IEvent.class), "Parameter of incorrent type for method "+method+". EventHandler methods must take a single event argument");
    }

    @Override
    public void stop() {
        m_subscriptionService.removeEventListener(this);
    }

}
