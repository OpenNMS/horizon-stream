/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.core.request;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * <p>LineOrientedRequest class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class LineOrientedRequest {
    
    /** Constant <code>Null</code> */
    public static final LineOrientedRequest Null = new LineOrientedRequest(null) {
        
    };
    
    private String m_command;
    
    /**
     * <p>Constructor for LineOrientedRequest.</p>
     *
     * @param command a {@link String} object.
     */
    public LineOrientedRequest(final String command) {
        m_command = command;
    }

    /**
     * <p>send</p>
     *
     * @throws IOException if any.
     * @param out a {@link OutputStream} object.
     */
    public void send(final OutputStream out) throws IOException {
        out.write(String.format("%s\r\n", m_command).getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * <p>getRequest</p>
     *
     * @return a {@link String} object.
     */
    public String getRequest() {
        return String.format("%s\r\n", m_command);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String toString() {
        return String.format("Request: %s", m_command);
    }

}
