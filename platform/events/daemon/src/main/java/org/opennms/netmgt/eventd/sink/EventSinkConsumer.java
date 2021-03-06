/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

/**
 * @author ms043660 (Malatesh.Sudarshan@cerner.com)
 */
package org.opennms.netmgt.eventd.sink;

import org.opennms.horizon.core.lib.Logging;
import org.opennms.horizon.events.api.EventForwarder;
import org.opennms.horizon.events.api.EventdConfig;
import org.opennms.horizon.events.xml.Event;
import org.opennms.horizon.events.xml.Log;
import org.opennms.horizon.ipc.sink.api.MessageConsumer;
import org.opennms.horizon.ipc.sink.api.MessageConsumerManager;
import org.opennms.horizon.ipc.sink.api.SinkModule;
import org.opennms.netmgt.eventd.Eventd;

/**
 * @author Malatesh Sudarshan
 */
public class EventSinkConsumer implements MessageConsumer<Event, Log> {

    private EventdConfig m_config;

    public void init() throws Exception {
        messageConsumerManager.registerConsumer(this);
    }

    private MessageConsumerManager messageConsumerManager;

    private EventForwarder eventForwarder;

    @Override
    public SinkModule<Event, Log> getModule() {
        return new EventSinkModule(m_config);
    }

    @Override
    public void handleMessage(Log eventLog) {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Eventd.LOG4J_CATEGORY)) {
            eventForwarder.sendNowSync(eventLog);
        }

    }

    public void setconfig(EventdConfig m_config) {
        this.m_config = m_config;
    }

    public void setMessageConsumerManager(
            MessageConsumerManager messageConsumerManager) {
        this.messageConsumerManager = messageConsumerManager;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }
}
