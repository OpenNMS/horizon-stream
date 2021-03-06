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

package org.opennms.netmgt.provision.detector.jdbc;

import org.opennms.netmgt.provision.core.support.BasicDetector;
import org.opennms.netmgt.provision.detector.jdbc.client.JDBCClient;
import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>Abstract AbstractJdbcDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractJdbcDetector extends BasicDetector<JDBCRequest, JDBCResponse> {

    public static final String DEFAULT_DB_DRIVER = "org.postgresql.Driver";
    public static final String DEFAULT_DB_URL = "jdbc:postgresql://:" + JDBCClient.OPENNMS_JDBC_HOSTNAME_MARKER + "/opennms";
    public static final String DEFAULT_DB_USER = "postgres";
    public static final String DEFAULT_DB_PASSWORD = "";

    /** Constant <code>DEFAULT_PORT=3306</code> */
    protected static int DEFAULT_PORT = 3306;
    
    private String m_dbDriver = DEFAULT_DB_DRIVER;
    private String m_user = DEFAULT_DB_USER;
    private String m_password = DEFAULT_DB_PASSWORD;
    private String m_url = DEFAULT_DB_URL;
    
    /**
     * <p>Constructor for AbstractJdbcDetector.</p>
     *
     * @param serviceName a {@link String} object.
     * @param port a int.
     */
    protected AbstractJdbcDetector(String serviceName, int port) {
        super(serviceName, port);
        
    }
    
    /**
     * <p>resultSetNotNull</p>
     *
     * @return a {@link ResponseValidator} object.
     */
    protected static ResponseValidator<JDBCResponse> resultSetNotNull(){
        return new ResponseValidator<JDBCResponse>() {

            @Override
            public boolean validate(JDBCResponse response) {
                return response.resultSetNotNull();
            }
        };
    }
    
    /** {@inheritDoc} */
    @Override
    protected Client<JDBCRequest, JDBCResponse> getClient() {
        JDBCClient client = new JDBCClient();
        client.setDbDriver(getDbDriver());
        client.setUser(getUser());
        client.setPassword(getPassword());
        client.setUrl(getUrl());
        return client;
    }
    
    /**
     * <p>setDbDriver</p>
     *
     * @param dbDriver a {@link String} object.
     */
    public void setDbDriver(String dbDriver) {
        m_dbDriver = dbDriver;
    }

    /**
     * <p>getDbDriver</p>
     *
     * @return a {@link String} object.
     */
    public String getDbDriver() {
        return m_dbDriver;
    }

    /**
     * <p>setUser</p>
     *
     * @param username a {@link String} object.
     */
    public void setUser(String username) {
        m_user = username;
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link String} object.
     */
    public String getUser() {
        return m_user;
    }

    /**
     * <p>setPassword</p>
     *
     * @param password a {@link String} object.
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * <p>getPassword</p>
     *
     * @return a {@link String} object.
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * <p>setUrl</p>
     *
     * @param url a {@link String} object.
     */
    public void setUrl(String url) {
        m_url = url;
    }

    /**
     * <p>getUrl</p>
     *
     * @return a {@link String} object.
     */
    public String getUrl() {
        return m_url;
    }

}
