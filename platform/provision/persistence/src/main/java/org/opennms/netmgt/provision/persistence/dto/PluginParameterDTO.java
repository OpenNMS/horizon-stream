/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import java.util.Map.Entry;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.CompareToBuilder;

//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "", propOrder = { "m_key", "m_value" })
@Data
@NoArgsConstructor
public class PluginParameterDTO implements Comparable<PluginParameterDTO> {

    private static final long serialVersionUID = -6314596729655404812L;

    private String key = null;
    private String value = null;

    //TODO: anti-pattern, fix this!
    private PluginConfigDTO parent = null;

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param key a {@link String} object.
     * @param value a {@link String} object.
     */
    public PluginParameterDTO(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param e a {@link Entry} object.
     */
    public PluginParameterDTO(Entry<String, String> e) {
        key = e.getKey();
        value = e.getValue();
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param key a {@link String} object.
     * @param value a {@link String} object.
     */
    public PluginParameterDTO(PluginConfigDTO pluginConfig, String key, String value) {
        this(key, value);
        parent = pluginConfig;
    }

    /**
     * <p>Constructor for PluginParameter.</p>
     *
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param set a {@link Entry} object.
     */
    public PluginParameterDTO(PluginConfigDTO pluginConfig, Entry<String, String> set) {
        this(set);
        parent = pluginConfig;
    }

    /**
     * <p>getAvailableParameterKeys</p>
     *
     * @return a {@link Set} object.
     */
//    public Set<String> getAvailableParameterKeys() {
//        Set<String> keys = new TreeSet<>();
//        if (m_parent != null) {
//            try {
//                PluginWrapper pw = new PluginWrapper(m_parent.getPluginClass());
//                keys = pw.getOptionalKeys();
//                for (PluginParameter p : m_parent.getParameters()) {
//                    if (getKey() == null) {
//                        if (p.getKey() != null) {
//                            keys.remove(p.getKey());
//                        }
//                    } else if (!getKey().equals(p.getKey())) {
//                        keys.remove(p.getKey());
//                    }
//                }
//            } catch (ClassNotFoundException e) {
//                // we just let it return the empty set
//            }
//        }
//        return keys;
//    }

    @Override
    public int compareTo(final PluginParameterDTO other) {
        return new CompareToBuilder()
                .append(key, other.key)
                .append(value, other.value)
                .toComparison();
    }
}
