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

package org.opennms.horizon.server.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.ToString;

@Data
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
    @OneToOne
    @JoinColumn(name = "nodeparentid")
    private Node parent;
    @Column(name = "nodetype")
    private String type;
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
    private String labelSource;
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
    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "location")
    private MonitoringLocation location;
    private Date lastIngressFlow;
    private Date lastEgressFlow;
}
