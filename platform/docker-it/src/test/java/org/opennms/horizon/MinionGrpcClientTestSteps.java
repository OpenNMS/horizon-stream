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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_CLIENT_PID;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_HOST;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_PORT;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.opennms.core.ipc.grpc.client.MinionGrpcClient;
import org.opennms.horizon.core.identity.Identity;
import org.opennms.horizon.core.identity.IdentityImpl;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.grpc.ConnectivityState;


public class MinionGrpcClientTestSteps {
    private int clientReadyRetry = 3;
    private Identity identity;
    private String grpcServer;
    private int grpcPort;
    private MinionGrpcClient client;

    @Given("identity id {string} location {string} type {string}")
    public void setIdentity(String id, String location, String type) {
        identity = new IdentityImpl(id, location, type);
    }

    @Given("server host {string} port {int}")
    public void setServerHost(String host, Integer port) {
        grpcServer = host;
        grpcPort = port;
    }

    @Given("client retry times {int}")
    public void setClientRetry(Integer retry) {
        // Write code here that turns the phrase above into concrete actions
        clientReadyRetry = retry;
    }

    @Then("initial MinionGrpc client")
    public void initialAndStartMinionGrpcClient() throws IOException, InterruptedException {
        Dictionary properties = new Properties();
        properties.put(GRPC_HOST, grpcServer);
        properties.put(GRPC_PORT, grpcPort);
        Configuration config = mock(Configuration.class);
        when(config.getProperties()).thenReturn(properties);
        ConfigurationAdmin configAdmin = mock(ConfigurationAdmin.class);
        when(configAdmin.getConfiguration(GRPC_CLIENT_PID)).thenReturn(config);
        client = new MinionGrpcClient(identity, configAdmin);
        client.start();
    }
    @Then("check minion grpc client is connected")
    public void checkGrpcClientConnected() throws InterruptedException {
        try {
            ConnectivityState state = client.getChannelState();
            int counter = 0;
            while (counter < clientReadyRetry && !ConnectivityState.READY.equals(state)) {
                TimeUnit.SECONDS.sleep(1);
                state = client.getChannelState();
            }
            assertEquals("Grpc client should connect to the server", ConnectivityState.READY, state);
        } finally {
            if(client != null) {
                client.shutdown();
            }
        }
    }
}
