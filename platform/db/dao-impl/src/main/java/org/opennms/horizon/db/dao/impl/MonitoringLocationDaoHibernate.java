/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.horizon.db.dao.impl;

import org.opennms.horizon.db.dao.api.EntityManagerHolder;
import org.opennms.horizon.db.dao.api.MonitoringLocationDao;
import org.opennms.horizon.db.dao.util.AbstractDaoHibernate;
import org.opennms.horizon.db.model.OnmsMonitoringLocation;

public class MonitoringLocationDaoHibernate extends AbstractDaoHibernate<OnmsMonitoringLocation, String> implements MonitoringLocationDao {

    public MonitoringLocationDaoHibernate(EntityManagerHolder entityManagerHolder) {
        super(entityManagerHolder, OnmsMonitoringLocation.class);
    }

    public OnmsMonitoringLocation getDefaultLocation() {
        return get(DEFAULT_MONITORING_LOCATION_ID);
    }
}
