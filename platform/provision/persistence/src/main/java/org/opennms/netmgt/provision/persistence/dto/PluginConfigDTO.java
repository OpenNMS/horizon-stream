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

package org.opennms.netmgt.provision.persistence.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.netmgt.provision.core.support.PluginWrapper;

/**
 * A PluginConfig represents a portion of a configuration that defines a reference
 * to a Java class "plugin" along with a set of parameters used to configure the
 * behavior of that plugin.
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name="plugin")
@Data
@NoArgsConstructor
public class PluginConfigDTO implements Comparable<PluginConfigDTO> {

    private static final long serialVersionUID = 4307231598310473690L;
    private String name;
    private String pluginClass;
    private Set<PluginParameterDTO> parameters = new LinkedHashSet<>();
    
    /**
     * Creates a plugin configuration with the given name and class.
     *
     * @param name the human-readable name of the plugin
     * @param clazz the name of the plugin's java class
     */
    public PluginConfigDTO(String name, String clazz) {
        setName(name);
        setPluginClass(clazz);
    }

    public PluginConfigDTO(PluginConfigDTO pluginConfig) {
        setName(pluginConfig.getName());
        setPluginClass(pluginConfig.getPluginClass());
        setParameterMap(pluginConfig.getParameterMap());
    }

  
    public Set<PluginParameterDTO> getParameters() {
        for (PluginParameterDTO p : parameters) {
            p.setParent(this);
        }
        return parameters;
    }
    
    /**
     * <p>setParameters</p>
     *
     * @param list a {@link Set} object.
     */
    public void setParameters(Set<PluginParameterDTO> list) {
        for (PluginParameterDTO p : list) {
            p.setParent(this);
        }
        parameters = list;
    }
    
    /**
     * <p>getParameterMap</p>
     *
     * @return the parameters
     */
    public Map<String,String> getParameterMap() {
        Map<String,String> parms = new LinkedHashMap<String,String>();
        for (PluginParameterDTO p : getParameters()) {
            parms.put(p.getKey(), p.getValue());
        }
        return Collections.unmodifiableMap(parms);
    }
    
    /**
     * <p>setParameterMap</p>
     *
     * @param parameters the parameters to set
     */
    public void setParameterMap(Map<String, String> parameters) {
        this.parameters.clear();
        for (Entry<String,String> set : parameters.entrySet()) {
            this.parameters.add(new PluginParameterDTO(this, set));
        }
    }

    /**
     * <p>getParameter</p>
     *
     * @param key the parameter name
     * @return the parameter value
     */
    public String getParameter(String key) {
        for (PluginParameterDTO p : getParameters()) {
            if (p.getKey().equals(key)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * <p>addParameter</p>
     *
     * @param key the parameter name
     * @param value the parameter value
     */
    public void addParameter(String key, String value) {
        parameters.add(new PluginParameterDTO(this, key, value));
    }

    public void deleteParameters(PluginParameterDTO p) {
        parameters.remove(p);
    }

    /**
     * <p>getAvailableParameterKeys</p>
     *
     * @return a {@link Set} object.
     */
    public Set<String> getAvailableParameterKeys() {
        Set<String> keys = new TreeSet<>();
        try {
            PluginWrapper pw = new PluginWrapper(pluginClass);
            keys = pw.getOptionalKeys();
            for (PluginParameterDTO p : getParameters()) {
                keys.remove(p.getKey());
            }
        } catch (ClassNotFoundException e) {
            // we just let it return the empty set
        }
        return keys;
    }

    private String getParametersAsString() {
        final StringBuilder sb = new StringBuilder();
        for (final PluginParameterDTO p : getParameters()) {
            sb.append(p.getKey()).append('=').append(p.getValue()).append('/');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 107;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((pluginClass == null) ? 0 : pluginClass.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof PluginConfigDTO)) return false;
        final PluginConfigDTO other = (PluginConfigDTO) obj;
        if (name == null) {
            if (other.getName() != null) return false;
        } else if (!name.equals(other.getName())) {
            return false;
        }
        if (pluginClass == null) {
            if (other.getPluginClass() != null) return false;
        } else if (!pluginClass.equals(other.getPluginClass())) {
            return false;
        }
        if (parameters == null) {
            if (other.getParameters() != null) return false;
        } else if (!parameters.equals(other.getParameters())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PluginConfig [name=" + name + ", pluginClass="
                + pluginClass + ", parameters=" + getParametersAsString() + "]";
    }


    @Override
    public int compareTo(final PluginConfigDTO other) {
        return new CompareToBuilder()
            .append(name, other.name)
            .append(pluginClass, other.pluginClass)
            .toComparison();
    }
}
