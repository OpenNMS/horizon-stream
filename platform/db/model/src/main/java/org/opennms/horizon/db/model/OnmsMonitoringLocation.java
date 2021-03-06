/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.horizon.db.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * This element contains the name of the location, the name of the
 * monitoring area (used to aggregate locations, example: Area San Francisco,
 * location name "SFO" which becomes SFO-1 or SFO-BuildingA, etc.)
 * Additionally, a geolocation can be provided (an address or other
 * identifying location that can be looked up with a geolocation
 *  API), as well as coordinates (latitude,longitude). Finally, a
 * priority can be assigned to the location, for purposes of sorting
 * (1 = highest, 100 = lowest).
 * </p>
 * <p>
 * The polling package name is used to associate with a polling
 * configuration found in the polling-configuration.xml file. 
 * </p>
 * <p>
 * The collection package name is used to associate with a collection
 * configuration found in the collectd-configuration.xml file.
 */
@Entity
@Table(name="monitoringLocations")
@XmlRootElement(name="location")
@XmlAccessorType(XmlAccessType.NONE)
public class OnmsMonitoringLocation implements Serializable {
    private static final long serialVersionUID = -7651610012389148818L;

    /**
     * The name of the location.  This must be a unique identifier.
     */
    private String m_locationName;

    /**
     * The name of the monitoring area.  This field is used to group
     * multiple locations together, ie, a region, or abstract category.
     */
    private String m_monitoringArea;

    /**
     * The geolocation (address) of this monitoring location.
     */
    private String m_geolocation;

    /**
     * The latitude of this monitoring location.
     */
    private Float m_longitude;

    /**
     * The latitude of this monitoring location.
     */
    private Float m_latitude;

    /**
     * The priority of the location. (1=highest)
     */
    private Long m_priority;

    private List<String> m_tags;

    public OnmsMonitoringLocation() {
        super();
    }

    /**
     * This constructor is only used during unit testing.
     * 
     * @param locationName
     * @param monitoringArea
     */
    public OnmsMonitoringLocation(final String locationName, final String monitoringArea) {
        this(locationName, monitoringArea, null, null, null, null, null, null);
    }

    public OnmsMonitoringLocation(final String locationName, final String monitoringArea, final String geolocation, final Float latitude, final Float longitude, final Long priority, final String... tags) {
        m_locationName = locationName;
        m_monitoringArea = monitoringArea;
        m_geolocation = geolocation;
        m_latitude = latitude;
        m_longitude = longitude;
        m_priority = priority;
        // Because tags is a vararg, if you have no arguments for it, it comes in as String[0]
        m_tags = ((tags == null || tags.length == 0) ? Collections.emptyList() : Arrays.asList(tags));
    }

    @XmlID
    @XmlAttribute(name="location-name")
    @Id
    @Column(name="id", nullable=false)
    public String getLocationName() {
        return m_locationName;
    }

    public void setLocationName(final String locationName) {
        m_locationName = locationName;
    }

    @XmlAttribute(name="monitoring-area")
    @Column(name="monitoringArea", nullable=false)
    public String getMonitoringArea() {
        return m_monitoringArea;
    }

    public void setMonitoringArea(final String monitoringArea) {
        m_monitoringArea = monitoringArea;
    }

    @XmlAttribute(name="geolocation")
    @Column(name="geolocation")
    public String getGeolocation() {
        return m_geolocation;
    }

    public void setGeolocation(final String geolocation) {
        m_geolocation = geolocation;
    }

    /**
     * The longitude coordinate of this node.
     * @return
     */
    @XmlAttribute(name="longitude")
    @Column(name="longitude")
    public Float getLongitude() {
        return m_longitude;
    }

    public void setLongitude(final Float longitude) {
        m_longitude = longitude;
    }

    /**
     * The latitude coordinate of this node.
     * @return
     */
    @XmlAttribute(name="latitude")
    @Column(name="latitude")
    public Float getLatitude() {
        return m_latitude;
    }

    public void setLatitude(final Float latitude) {
        m_latitude = latitude;
    }

    @XmlAttribute(name="priority")
    @Column(name="priority")
    public Long getPriority() {
        return m_priority == null ? 100L : m_priority;
    }

    public void setPriority(final Long priority) {
        m_priority = priority;
    }

    @XmlElementWrapper(name="tags")
    @XmlElement(name="tag")
    @ElementCollection
    @JoinTable(name="monitoringLocationsTags", joinColumns = @JoinColumn(name="monitoringLocationId"))
    @Column(name="tag")
    public List<String> getTags() {
        if (m_tags == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_tags);
        }
    }

    public void setTags(final List<String> tags) {
        if (tags == null || tags.size() == 0) {
            m_tags = Collections.emptyList();
        } else {
            m_tags = new ArrayList<String>(tags);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnmsMonitoringLocation that = (OnmsMonitoringLocation) o;
        return Objects.equals(m_locationName, that.m_locationName) && Objects.equals(m_monitoringArea, that.m_monitoringArea) && Objects.equals(m_geolocation, that.m_geolocation) && Objects.equals(m_longitude, that.m_longitude) && Objects.equals(m_latitude, that.m_latitude) && Objects.equals(m_priority, that.m_priority) && Objects.equals(m_tags, that.m_tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_locationName, m_monitoringArea, m_geolocation, m_longitude, m_latitude, m_priority, m_tags);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OnmsMonitoringLocation.class.getSimpleName() + "[", "]")
                .add("m_locationName='" + m_locationName + "'")
                .add("m_monitoringArea='" + m_monitoringArea + "'")
                .add("m_geolocation='" + m_geolocation + "'")
                .add("m_longitude=" + m_longitude)
                .add("m_latitude=" + m_latitude)
                .add("m_priority=" + m_priority)
                .add("m_tags=" + m_tags)
                .toString();
    }
}
