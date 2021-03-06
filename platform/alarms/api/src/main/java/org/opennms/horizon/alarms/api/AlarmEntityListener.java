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

package org.opennms.horizon.alarms.api;

import java.util.Date;
import java.util.Set;

import org.opennms.horizon.db.model.OnmsAlarm;
import org.opennms.horizon.db.model.OnmsMemo;
import org.opennms.horizon.db.model.OnmsReductionKeyMemo;
import org.opennms.horizon.db.model.OnmsSeverity;
import org.opennms.horizon.db.model.TroubleTicketState;

/**
 * Used to get callbacks when alarm entities are created, updated and/or deleted.
 *
 * @author jwhite
 */
public interface AlarmEntityListener {

    void onAlarmCreated(OnmsAlarm alarm);

    void onAlarmUpdatedWithReducedEvent(OnmsAlarm alarm);

    void onAlarmAcknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime);

    void onAlarmUnacknowledged(OnmsAlarm alarm, String previousAckUser, Date previousAckTime);

    void onAlarmSeverityUpdated(OnmsAlarm alarm, OnmsSeverity previousSeverity);

    void onAlarmArchived(OnmsAlarm alarm, String previousReductionKey);

    void onAlarmDeleted(OnmsAlarm alarm);

    void onStickyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated);

    void onReductionKeyMemoUpdated(OnmsAlarm alarm, String previousBody, String previousAuthor, Date previousUpdated);

    void onStickyMemoDeleted(OnmsAlarm alarm, OnmsMemo memo);

    void onReductionKeyMemoDeleted(OnmsAlarm alarm, OnmsReductionKeyMemo memo);

    void onLastAutomationTimeUpdated(OnmsAlarm alarm, Date previousLastAutomationTime);

    void onRelatedAlarmsUpdated(OnmsAlarm alarm, Set<OnmsAlarm> previousRelatedAlarms);

    void onTicketStateChanged(OnmsAlarm alarm, TroubleTicketState previousState);

}
