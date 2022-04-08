/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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

package org.opennms.horizon.server.model.entity;

import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Converter;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "node")
public class Node {
    @Id
    @GeneratedValue(generator = "node_seq")
    @SequenceGenerator(name = "node_seq", sequenceName = "nodenxtid", allocationSize = 1)
    @Column(name = "nodeid")
    private int id;
    @Column(name = "nodecreatetime")
    private Date createTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeparentid")
    private Node parent;
    @Column(name = "nodetype")
    private NodeType type;
    @Column(name = "nodesysoid")
    private String sysOid;
    @Column(name = "nodesysname")
    private String sysName;
    @Column(name = "nodesysdescription")
    private String sysDescription;
    @Column(name = "nodesyslocation")
    private String sysLocation;
    @Column(name = "nodesyscontact")
    private String sysContact;
    @Column(name = "nodelabel")
    private String label;
    @Column(name = "nodelabelsource")
    private NodeLabelSource labelSource;
    @Column(name = "nodenetbiosname")
    private String netBiosName;
    @Column(name = "nodedomainname")
    private String domainName;
    @Column(name = "operatingsystem")
    private String operatingSystem;
    @Column(name = "lastcapsdpoll")
    private Date lastPoll;
    @Column(name = "foreignsource")
    private String foreignSource;
    @Column(name = "foreignid")
    private String foreignId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "location")
    private MonitoringLocation location;
    private Date lastIngressFlow;
    private Date lastEgressFlow;

    public enum NodeType {
        ACTIVE('A'),
        DELETED('D'),
        UNKNOWN(' ');
        private final char value;
        NodeType(char c) {
            value = c;
        }

        public char value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static NodeType fromValueString(String s) {
            if (s == null || s.length() == 0) return null;
            for (NodeType nodeType: NodeType.values()) {
                if (nodeType.value == s.charAt(0))
                    return nodeType;
            }
            return null;
        }
    }

    @Converter(autoApply = true)
    public static class NodeTypeConverter implements AttributeConverter<NodeType, Character> {

        @Override
        public Character convertToDatabaseColumn(NodeType nodeType) {
            return nodeType == null? ' ': nodeType.value();
        }

        @Override
        public NodeType convertToEntityAttribute(Character character) {
            return NodeType.fromValueString(String.valueOf(character));
        }
    }

    public enum NodeLabelSource {
        /**
         * Label source set by user
         */

        USER('U'),

        /**
         * Label source set by netbios
         */

        NETBIOS('N'),

        /**
         * Label source set by hostname
         */

        HOSTNAME('H'),

        /**
         * Label source set by SNMP sysname
         */

        SYSNAME('S'),

        /**
         * Label source set by IP Address
         */

        ADDRESS('A'),

        /**
         * Label source unset/unknown
         */

        UNKNOWN(' ');

        private final char value;

        NodeLabelSource(char c) {
            value = c;
        }

        public char value() {
            return value;
        }

        public static NodeLabelSource fromValueString(String s) {
            if (s == null || s.length() == 0) return null;
            for (NodeLabelSource src : NodeLabelSource.values()) {
                if (src.value == s.charAt(0)) return src;
            }
            return null;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Converter(autoApply = true)
    public static class NodeLabelSourceConverter implements AttributeConverter<NodeLabelSource, Character> {

        @Override
        public Character convertToDatabaseColumn(NodeLabelSource labelSource) {
            return labelSource == null? ' ': labelSource.value();
        }

        @Override
        public NodeLabelSource convertToEntityAttribute(Character character) {
            return NodeLabelSource.fromValueString(String.valueOf(character));
        }
    }

}
