/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.soa;

import java.util.Map;

/**
 * Registration
 *
 * @author brozow
 * @version $Id: $
 */
public interface Registration {
    
    /**
     * <p>getRegistry</p>
     *
     * @return a {@link org.opennms.core.soa.ServiceRegistry} object.
     */
    public ServiceRegistry getRegistry();
    
    /**
     * <p>getProvidedInterfaces</p>
     *
     * @return an array of {@link Class} objects.
     */
    public Class<?>[] getProvidedInterfaces();
    
    /**
     * <p>getProvider</p>
     *
     * @param service a {@link Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T getProvider(Class<T> service);
    
    public Object getProvider();
    
    /**
     * <p>getProperties</p>
     *
     * @return a {@link Map} object.
     */
    public Map<String, String> getProperties();
    
    /**
     * <p>isUnregistered</p>
     *
     * @return a boolean.
     */
    public boolean isUnregistered();
    
    /**
     * <p>unregister</p>
     */
    public void unregister();
    
}
