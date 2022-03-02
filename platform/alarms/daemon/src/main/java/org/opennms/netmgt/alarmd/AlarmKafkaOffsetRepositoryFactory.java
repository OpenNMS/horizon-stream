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

package org.opennms.netmgt.alarmd;

import org.apache.camel.impl.engine.FileStateRepository;
import org.apache.camel.spi.StateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AlarmKafkaOffsetRepositoryFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AlarmKafkaOffsetRepositoryFactory.class);

    private final String defaultFilePath;

    public AlarmKafkaOffsetRepositoryFactory(String defaultFilePath) {
        this.defaultFilePath = defaultFilePath;
    }

    public StateRepository<String, String> get(String filePath) {
        // starting with $ indicates an unset environment variable was passed-in from Blueprint
        if (filePath == null || filePath.isEmpty() || filePath.startsWith("$")) {
            filePath = defaultFilePath;
        }

        LOG.debug("Persisting Alarmd Kafka offset at file path {}", filePath);

        File file = new File(filePath);
        return FileStateRepository.fileStateRepository(file);
    }
}
