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

package org.opennms.horizon.it.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SshClient {
    private static final Logger LOG = LoggerFactory.getLogger(SshClient.class);
    {
        JSch.setLogger(JschLogger.getInstance());
    }

    public static final int DEFAULT_TIMEOUT_MS = 5*1000;

    private final JSch jsch = new JSch();
    private Session session;
    private Channel channel;
    private PrintStream stdin;
    private InputStream stdout;
    private InputStream stderr;

    private final StringBuffer stdoutBuff = new StringBuffer();
    private final StringBuffer stderrBuff = new StringBuffer();

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private int timeout = DEFAULT_TIMEOUT_MS;

    public SshClient(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public PrintStream openShell() throws Exception {
        // We only support one shell at a time
        close();

        session = jsch.getSession(username, host, port);
        session.setPassword(password.getBytes());
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channel = session.openChannel("shell");
        ((ChannelShell)channel).setPtySize(500, 100, 1920, 1080);
        stdout = channel.getInputStream();
        stderr = channel.getExtInputStream();
        channel.connect(timeout);

        OutputStream ops = channel.getOutputStream();
        stdin = new PrintStream(ops, true);
        return stdin;
    }

    public String getStdout() throws Exception {
        // Prepend the contents of the buffer, which may be have populated by isShellClosed()
        final String stdoutContents = stdoutBuff.append(readAvailableBytes(stdout)).toString();
        stdoutBuff.setLength(0);
        return stripAnsiCodes(stdoutContents);
    }

    public String getStderr() throws Exception {
        // Prepend the contents of the buffer, which may be have populated by isShellClosed()
        final String stderrContents = stderrBuff.append(readAvailableBytes(stderr)).toString();
        stderrBuff.setLength(0);
        return stripAnsiCodes(stderrContents);
    }

    private String readAvailableBytes(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final int BUF_LEN = 1024;
        final byte[] buffer = new byte[BUF_LEN];
        int avail = 0;
        while ((avail = is.available()) > 0) {
            int length = is.read(buffer, 0, Math.min(BUF_LEN, avail));
            baos.write(buffer, 0, length);
        }
        return baos.toString("UTF-8");
    }

    public void close() throws Exception {
        if (channel != null) {
            channel.disconnect();
            channel = null;
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }

    public boolean isClearAndClose() {
        if (channel == null) {
            return true;
        }
        // In certain cases the shell won't close unless we read the available
        // bytes from the stdout and stderr streams.
        try {
            stdoutBuff.append(getStdout());
        } catch (Exception e) {
            // pass
        }

        try {
            stderrBuff.append(getStderr());
        } catch (Exception e) {
            // pass
        }

        return channel.isClosed();
    }

    public Callable<Boolean> isShellClosedCallable() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isClearAndClose();
            }
        };
    }

    public String stripAnsiCodes(String input) throws Exception {
        String retval = input;
        // Erase ANSI bracket codes from the output
        retval = retval.replaceAll("\u001B\\[[\\?\\d;]*[hlm]", "");
        // Erase ANSI keypad codes from the output
        return retval.replaceAll("\u001B[=>]", "");
    }
}
