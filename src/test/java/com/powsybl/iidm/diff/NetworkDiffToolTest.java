/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.powsybl.iidm.diff.tools.DiffTool;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.tools.AbstractToolTest;
import com.powsybl.tools.Command;
import com.powsybl.tools.CommandLineTools;
import com.powsybl.tools.Tool;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiffToolTest extends AbstractToolTest {

    @Override
    protected Iterable<Tool> getTools() {
        return Collections.singleton(new DiffTool());
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Network network1 = NetworkDiffTestUtils.createNetwork1();
        Network network2 = NetworkDiffTestUtils.createNetwork2();
        NetworkXml.write(network1, fileSystem.getPath("/network1.xiidm"));
        NetworkXml.write(network2, fileSystem.getPath("/network2.xiidm"));
    }

    @Override
    public void assertCommand() {
        DiffTool tool = new DiffTool();
        Command cmd = tool.getCommand();
        assertEquals("Computation", cmd.getTheme());
        assertEquals("Compare two networks", cmd.getDescription());
        assertCommand(cmd, "compare-network", 6, 3);
        assertOption(cmd.getOptions(), "input-file1", true, true);
        assertOption(cmd.getOptions(), "input-file2", true, true);
        assertOption(cmd.getOptions(), "output-file", true, true);
    }

    @Test
    public void testDiff() throws IOException {
        String[] commandLine = new String[]{
            "compare-network",
            "--input-file1", "/network1.xiidm",
            "--input-file2", "/network2.xiidm",
            "--output-file", "/output.txt"
        };
        assertCommand(commandLine, CommandLineTools.COMMAND_OK_STATUS, "", "");
    }

    @Test
    public void testDiff1() throws IOException {
        String[] commandLine = new String[]{
            "compare-network",
            "--input-file1", "/network1.xiidm",
            "--output-file", "/output.txt"
        };
        assertCommand(commandLine, CommandLineTools.INVALID_COMMAND_STATUS, "", "error: Missing required option: input-file2");
    }
}
