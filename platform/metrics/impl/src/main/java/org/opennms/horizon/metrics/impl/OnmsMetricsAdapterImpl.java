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

package org.opennms.horizon.metrics.impl;

import io.prometheus.client.Collector;
import io.prometheus.client.exporter.PushGateway;
import org.opennms.horizon.metrics.api.OnmsMetricsAdapter;

import java.io.IOException;

public class OnmsMetricsAdapterImpl implements OnmsMetricsAdapter {

    private final PushGateway pushGateway;
    private final String DEFAULT_JOB_PUSHGATEWAY = "horizon-core";

    public OnmsMetricsAdapterImpl(String pushGatewayUrl) {
        this.pushGateway = new PushGateway(pushGatewayUrl);
    }


    @Override
    public void push(Collector collector) throws IOException {
        pushGateway.push(collector, DEFAULT_JOB_PUSHGATEWAY);
    }
}
