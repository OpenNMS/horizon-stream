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

package org.opennms.netmgt.alarmd.drools;

import java.util.Date;

import javax.transaction.Transactional;

import org.opennms.horizon.alarms.api.AlarmEntityNotifier;
import org.opennms.horizon.db.dao.api.AlarmDao;
import org.opennms.horizon.db.model.OnmsAlarm;
import org.opennms.horizon.events.api.EventBuilder;
import org.opennms.horizon.events.api.EventConstants;
import org.opennms.horizon.events.api.EventForwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultAlarmTicketerService implements AlarmTicketerService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAlarmTicketerService.class);

    private static final boolean ALARM_TROUBLE_TICKET_ENABLED = Boolean.getBoolean("opennms.alarmTroubleTicketEnabled");

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private AlarmEntityNotifier alarmEntityNotifier;

    @Override
    public boolean isTicketingEnabled() {
        return ALARM_TROUBLE_TICKET_ENABLED;
    }

    @Override
    @Transactional
    public void createTicket(OnmsAlarm alarm, Date now) {
        /*
            <action-event name="createTicket" for-each-result="true" >
              <assignment type="field" name="uei" value="uei.opennms.org/troubleTicket/create" />
              <assignment type="parameter" name="alarmUei" value="${_eventuei}" />
              <assignment type="parameter" name="user" value="${_user}" />
              <assignment type="parameter" name="alarmId" value="${_alarmid}" />
            </action-event>
        */

        // Send the create ticket event
        eventForwarder.sendNow(new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, DefaultAlarmTicketerService.class.getSimpleName())
                .addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei())
                .addParam(EventConstants.PARM_USER, DefaultAlarmService.DEFAULT_USER)
                .addParam(EventConstants.PARM_ALARM_ID, alarm.getId())
                .getEvent());

        // Update the lastAutomationTime
        updateLastAutomationTime(alarm, now);
    }

    @Override
    @Transactional
    public void updateTicket(OnmsAlarm alarm, Date now) {
        /*
            <action-event name="updateTicket" for-each-result="true" >
              <assignment type="field" name="uei" value="uei.opennms.org/troubleTicket/update" />
              <assignment type="parameter" name="alarmUei" value="${_eventuei}" />
              <assignment type="parameter" name="user" value="${_user}" />
              <assignment type="parameter" name="alarmId" value="${_alarmid}" />
              <assignment type="parameter" name="troubleTicket" value="${_tticketID}" />
            </action-event>
         */

        // Send the update ticket event
        eventForwarder.sendNow(new EventBuilder(EventConstants.TROUBLETICKET_UPDATE_UEI, DefaultAlarmTicketerService.class.getSimpleName())
                .addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei())
                .addParam(EventConstants.PARM_USER, DefaultAlarmService.DEFAULT_USER)
                .addParam(EventConstants.PARM_ALARM_ID, alarm.getId())
                .addParam(EventConstants.PARM_TROUBLE_TICKET, alarm.getTTicketId())
                .getEvent());

        // Update the lastAutomationTime
        updateLastAutomationTime(alarm, now);
    }

    @Override
    @Transactional
    public void closeTicket(OnmsAlarm alarm, Date now) {
        /*
            <action-event name="closeTicket" for-each-result="true" >
              <assignment type="field" name="uei" value="uei.opennms.org/troubleTicket/close" />
              <assignment type="parameter" name="alarmUei" value="${_eventuei}" />
              <assignment type="parameter" name="user" value="${_user}" />
              <assignment type="parameter" name="alarmId" value="${_alarmid}" />
              <assignment type="parameter" name="troubleTicket" value="${_tticketID}" />
            </action-event>
         */

        // Send the update ticket event
        eventForwarder.sendNow(new EventBuilder(EventConstants.TROUBLETICKET_CLOSE_UEI, DefaultAlarmTicketerService.class.getSimpleName())
                .addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei())
                .addParam(EventConstants.PARM_USER, DefaultAlarmService.DEFAULT_USER)
                .addParam(EventConstants.PARM_ALARM_ID, alarm.getId())
                .addParam(EventConstants.PARM_TROUBLE_TICKET, alarm.getTTicketId())
                .getEvent());

        // Update the lastAutomationTime
        updateLastAutomationTime(alarm, now);
    }

    private void updateLastAutomationTime(OnmsAlarm alarm, Date now) {
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. lastAutomationTime will not be updated.", alarm);
            return;
        }

        // Update the lastAutomationTime
        final Date previousLastAutomationTime = alarmInTrans.getLastAutomationTime();
        alarmInTrans.setLastAutomationTime(now);
        alarmEntityNotifier.didUpdateLastAutomationTime(alarmInTrans, previousLastAutomationTime);
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        this.alarmDao = alarmDao;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    public void setAlarmEntityNotifier(AlarmEntityNotifier alarmEntityNotifier) {
        this.alarmEntityNotifier = alarmEntityNotifier;
    }
}
