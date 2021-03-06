/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.provision.detector.jdbc.request.JDBCRequest;
import org.opennms.netmgt.provision.detector.jdbc.response.JDBCResponse;
import org.opennms.netmgt.provision.support.RequestBuilder;
import org.opennms.netmgt.provision.support.ResponseValidator;

/**
 * <p>JdbcQueryDetector class.</p>
 *
 * @author agalue
 * @version $Id: $
 */

public class JdbcQueryDetector extends AbstractJdbcDetector {

    private String m_sqlQuery;

    /**
     * <p>Constructor for JdbcQueryDetector.</p>
     */
    public JdbcQueryDetector(){
        super("JdbcQueryDetector", 3306);
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit(){
        expectBanner(resultSetNotNull());
        send(sqlQuery(getSqlQuery()), isValidQuery());
    }

    private static ResponseValidator<JDBCResponse> isValidQuery() {
        return new ResponseValidator<JDBCResponse>(){
            @Override
            public boolean validate(JDBCResponse response) {
                return response.isValidQuery();
            }
        };
    }

    private static RequestBuilder<JDBCRequest> sqlQuery(final String sqlQuery) {
        return new RequestBuilder<JDBCRequest>(){
            @Override
            public JDBCRequest getRequest() {
                JDBCRequest request = new JDBCRequest();
                request.setSqyQuery(sqlQuery);
                return request;
            }
        };
    }

    /**
     * <p>setSqlQuery</p>
     *
     * @param sqlQuery a {@link String} object.
     */
    public void setSqlQuery(String sqlQuery) {
        m_sqlQuery = sqlQuery;
    }

    /**
     * <p>getSqlQuery</p>
     *
     * @return a {@link String} object.
     */
    public String getSqlQuery() {
        return m_sqlQuery;
    }

}
