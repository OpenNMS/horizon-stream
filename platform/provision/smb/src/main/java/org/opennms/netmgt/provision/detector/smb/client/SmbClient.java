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

package org.opennms.netmgt.provision.detector.smb.client;

import java.io.IOException;
import java.net.InetAddress;

import jcifs.CIFSContext;
import jcifs.NameServiceClient;
import jcifs.context.SingletonContext;
import jcifs.netbios.NameServiceClientImpl;
import jcifs.netbios.NbtAddress;

import org.opennms.horizon.core.lib.InetAddressUtils;
import org.opennms.netmgt.provision.core.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse;
import org.opennms.netmgt.provision.support.Client;

/**
 * <p>SmbClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SmbClient implements Client<LineOrientedRequest, NbtAddressResponse> {
    
    private NbtAddress m_nbtAddress;
    private NameServiceClient m_nsc;
    private String m_address;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }

    /** {@inheritDoc} */
    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
       CIFSContext base = SingletonContext.getInstance();
       m_nsc = new NameServiceClientImpl(base);
       m_address = InetAddressUtils.str(address);
       m_nbtAddress = (NbtAddress) m_nsc.getNbtByName(m_address);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public NbtAddressResponse receiveBanner() throws IOException {
        return receiveResponse();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link LineOrientedRequest} object.
     * @return a {@link org.opennms.netmgt.provision.detector.smb.response.NbtAddressResponse} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public NbtAddressResponse sendRequest(LineOrientedRequest request) throws IOException, Exception {
        return receiveResponse();
    }
    
    private NbtAddressResponse receiveResponse() {
        NbtAddressResponse nbtAddrResponse = new NbtAddressResponse();
        nbtAddrResponse.receive(m_address, m_nbtAddress);
        return nbtAddrResponse;
    }

}
