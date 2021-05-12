/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@acotel-group.com>
 */
public class BrancDiffTest {

    private FileSystem fileSystem;
    private DiffConfig config;
    private Line line1;
    private Line line2;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        InMemoryPlatformConfig platformConfig = new InMemoryPlatformConfig(fileSystem);
        config = DiffConfig.load(platformConfig);
        Network network1 = NetworkDiffTestUtils.createNetwork1();
        Network network2 = NetworkDiffTestUtils.createNetwork1();
        line1 = network1.getLine("NHV1_NHV2_1");
        line2 = network2.getLine("NHV1_NHV2_1");
    }

    @After
    public void tearDown() throws IOException {
        fileSystem.close();
    }

    @Test
    public void testNoDifferences() {
        BranchDiffProc branchDiffProc = new BranchDiffProc(config);
        DiffResult diffResult = branchDiffProc.diff(line1, line2);
        assertFalse(diffResult.isDifferent());
        assertNotNull(JsonUtil.toJson(diffResult::writeJson));
    }

    @Test
    public void testOneLineDisconnected() {
        line2.getTerminal1().disconnect();
        BranchDiffProc branchDiffProc = new BranchDiffProc(config);
        DiffResult diffResult = branchDiffProc.diff(line1, line2);
        assertTrue(diffResult.isDifferent());
        assertNotNull(JsonUtil.toJson(diffResult::writeJson));
    }

    @Test
    public void testDifferentFlows() {
        line2.getTerminal2().setP(-302.8).setQ(-15.3);
        BranchDiffProc branchDiffProc = new BranchDiffProc(config);
        DiffResult diffResult = branchDiffProc.diff(line1, line2);
        assertTrue(diffResult.isDifferent());
        assertNotNull(JsonUtil.toJson(diffResult::writeJson));
    }

    @Test
    public void testChangeThreshold() {
        line2.getTerminal2().setP(-302.8).setQ(-135.3);
        config.setGenericTreshold(100.0);
        BranchDiffProc branchDiffProc = new BranchDiffProc(config);
        DiffResult diffResult = branchDiffProc.diff(line1, line2);
        assertFalse(diffResult.isDifferent());
        assertNotNull(JsonUtil.toJson(diffResult::writeJson));
    }
}
