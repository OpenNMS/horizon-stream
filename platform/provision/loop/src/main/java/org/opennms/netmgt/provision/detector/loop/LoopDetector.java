/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.loop;

import org.opennms.netmgt.provision.core.request.LineOrientedRequest;
import org.opennms.netmgt.provision.core.support.BasicDetector;
import org.opennms.netmgt.provision.detector.loop.client.LoopClient;
import org.opennms.netmgt.provision.detector.loop.response.LoopResponse;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>LoopDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class LoopDetector extends BasicDetector<LineOrientedRequest, LoopResponse> {
    private static final String DEFAULT_SERVICE_NAME = "LOOP";
    private static final int DEFAULT_PORT = 0;
    
    private String m_ipMatch;
    private boolean m_isSupported = true;
    
    /**
     * <p>Constructor for LoopDetector.</p>
     */
    public LoopDetector() {
        super(DEFAULT_SERVICE_NAME, DEFAULT_PORT);
    }

    /** {@inheritDoc} */
    @Override
    protected Client<LineOrientedRequest, LoopResponse> getClient() {
        LoopClient loopClient = new LoopClient();
        loopClient.setSupported(isSupported());
        return loopClient;
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        expectBanner(ipMatches(getIpMatch()));
    }

    private static ResponseValidator<LoopResponse> ipMatches(final String ipAddr) {
        
        return new ResponseValidator<LoopResponse>(){

            @Override
            public boolean validate(LoopResponse response) {
                return response.validateIPMatch(ipAddr);
            }
            
        };
    }

    /**
     * <p>setIpMatch</p>
     *
     * @param ipMatch a {@link String} object.
     */
    public void setIpMatch(String ipMatch) {
        m_ipMatch = ipMatch;
    }

    /**
     * <p>getIpMatch</p>
     *
     * @return a {@link String} object.
     */
    public String getIpMatch() {
        return m_ipMatch;
    }

    /**
     * <p>setSupported</p>
     *
     * @param isSupported a boolean.
     */
    public void setSupported(boolean isSupported) {
        m_isSupported = isSupported;
    }

    /**
     * <p>isSupported</p>
     *
     * @return a boolean.
     */
    public boolean isSupported() {
        return m_isSupported;
    }

}
