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

package org.opennms.horizon;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class IcmpMinionProcessingTestSteps {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpMinionProcessingTestSteps.class);
    private static final String PING_COMMAND = "ping -l cloud 127.0.0.1";
    private String username;
    private String password;
    private String host;
    private int port;
    private Session session;
    private ChannelExec channel;
    private String response;
    @Given("SSH username {string} and password {string}")
    public void setUsernameAndPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }
    @Given("host {string} and port in system property {string}")
    public void setHost(String host, String portKey) {
        this.host = host;
        this.port = Integer.parseInt(System.getProperty(portKey));
    }
    @Then("ping the mock minion ICMP server via ssh connection")
    public void pingMockMinion() throws Exception {
        connectAndRunCommand();
    }
    @Then("verify the response matches {string}")
    public void verifyResponse(String expectedTemplate) throws UnknownHostException {
        assertEquals(1, regexMatches(expectedTemplate));
    }

    private void connectAndRunCommand() throws Exception{
        try {
            session = new JSch().getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(PING_COMMAND);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            channel.setErrStream(errorStream);
            channel.connect();
            while (channel.isConnected()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            String error = errorStream.toString();
            if (!error.isEmpty()) {
                LOG.error("error happened during execute command {} against host {} on port {}: {}", PING_COMMAND, host, port, error);
                throw new Exception(error);
            }
            response = responseStream.toString();
            LOG.info("ping response from {}:{}: {}", host, port, response);
        } finally {
            closeSshConnection();
        }
    }

    private int regexMatches(String regTemplate) throws UnknownHostException {
        String ip = InetAddress.getByName(host).getHostAddress();
        String expectedRegex = String.format(regTemplate, ip);
        Pattern pattern = Pattern.compile(expectedRegex);
        Matcher matcher = pattern.matcher(response);
        int matches = 0;
        while (matcher.find()) {
            matches++;
        }
        return matches;
    }

    private void closeSshConnection() {
        if(channel!= null) {
            channel.disconnect();
        }
        if(session != null) {
            session.disconnect();
        }
    }
}
