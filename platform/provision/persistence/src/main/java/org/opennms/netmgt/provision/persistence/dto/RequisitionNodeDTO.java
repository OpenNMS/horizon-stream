/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ValidationException;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opennms.horizon.db.model.PrimaryType;

@Data
@NoArgsConstructor
public class RequisitionNodeDTO extends CategoriesAndMetadataDTO {

    private Integer id;
    private String foreignId = null;
    private String location = null;
    private String building = null;
    private String city = null;
    private String nodeLabel = null;
    private String parentForeignId;
    private String parentForeignSource;
    protected String parentNodeLabel;

    protected Map<String, RequisitionInterfaceDTO> interfaces = new HashMap<>();

    /**
     * <p>getInterfaceCount</p>
     *
     * @return a int.
     */
    public int getInterfaceCount() {
        return interfaces.size();
    }

    /**
     * <p>getInterfaces</p>
     *
     * @return a {@link List} object.
     */
    public List<RequisitionInterfaceDTO> getInterfacesAsList() {
        return interfaces.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
    }

    //TODO: do we want to allow this?
//    /**
//     * <p>setInterfaces</p>
//     *
//     * @param interfaces a {@link List} object.
//     */
//    public void setInterfaces(Collection<RequisitionInterface> interfaces) {
//        if (interfaces == null) {
//            interfaces = new TreeSet<>();
//        }
//        if (this.interfaces == interfaces) return;
//        this.interfaces.clear();
//        this.interfaces.addAll(interfaces);
//    }

    /**
     * <p>getInterface</p>
     *
     * @param ipAddress a {@link String} object.
     * @return a {@link RequisitionInterfaceDTO} object.
     */
    public RequisitionInterfaceDTO getInterface(String ipAddress) {
        return interfaces.get(ipAddress);
    }

    /**
     * <p>removeInterface</p>
     *
     * @param iface a {@link RequisitionInterfaceDTO} object.
     */
    public boolean deleteInterface(final RequisitionInterfaceDTO iface) {
        return deleteInterface(iface.getIpAddress().toString());
    }

    /**
     * <p>deleteInterface</p>
     *
     * @param ipAddress a {@link String} object.
     */
    public boolean deleteInterface(final String ipAddress) {
        return (interfaces.remove(ipAddress) != null);
    }

    /**
     * <p>putInterface</p>
     *
     * @param iface a {@link RequisitionInterfaceDTO} object.
     */
    public void putInterface(RequisitionInterfaceDTO iface) {
        interfaces.put(iface.getIpAddress().toString(), iface);
    }

    /**
     * <p>Setter for the field <code>parentForeignSource</code>.</p>
     *
     * @param value a {@link String} object.
     */
    public void setParentForeignSource(String value) {
        parentForeignSource = value != null && "".equals(value.trim()) ? null : value;
    }

    /**
     * <p>Setter for the field <code>parentForeignId</code>.</p>
     *
     * @param value a {@link String} object.
     */
    public void setParentForeignId(String value) {
        parentForeignId = value != null && "".equals(value.trim()) ? null : value;
    }

    /**
     * <p>Setter for the field <code>parentNodeLabel</code>.</p>
     *
     * @param value a {@link String} object.
     */
    public void setParentNodeLabel(String value) {
        parentNodeLabel = value != null && "".equals(value.trim()) ? null : value;
    }

    public void validate() throws ValidationException {
        //this.pruneInterfaces();
        if (nodeLabel == null) {
            throw new ValidationException("Requisition node 'node-label' is a required attribute!");
        }
        if (foreignId == null) {
            throw new ValidationException("Requisition node 'foreign-id' is a required attribute!");
        }
        if (foreignId.contains("/")) {
            throw new ValidationException("Node foreign ID (" + foreignId + ") contains invalid characters. ('/' is forbidden.)");
        }
        //TODO: figure out if we need to handle this
//        if (interfaces != null) {
//            interfaces.values().forEach(interfaceDTO -> {
//                try {
//                    interfaceDTO.validate(this);
//                } catch (IPValidationException ive) {
//                    interfaces.remove(interfaceDTO.getIpAddress().toString());
//                }
//            });
//        }
//            Iterator<RequisitionInterfaceDTO> iter = interfaces.iterator();
//            while (iter.hasNext()) {
//                try {
//                    iter.next().validate(this);
//                }
//                catch (IPValidationException ive) {
//                    iter.remove();
//                }
//            }
        // there can be only one primary interface per node
        if (interfaces.values().stream().filter(iface -> PrimaryType.PRIMARY == iface.getSnmpPrimary()).count() > 1) {
            throw new ValidationException("Node foreign ID (" + foreignId + ") contains multiple primary interfaces. Maximum one is allowed.");
        }
        categories.values().forEach(cat -> cat.validate());
    }
}
