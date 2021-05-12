/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.math.DoubleMath;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Terminal;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@acotel-group.com>
 */
public class BranchDiffProc implements DiffProc<Branch> {

    private final DiffConfig config;

    public BranchDiffProc(DiffConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    class BranchDiffResult implements DiffResult {

        final BranchDiffInfo branchInfo1;
        final BranchDiffInfo branchInfo2;
        final Map<Side, Boolean> sideDifferent;
        final boolean isDifferent;

        public BranchDiffResult(BranchDiffInfo branchInfo1, BranchDiffInfo branchInfo2, Map<Side, Boolean> sideDifferent) {
            this.branchInfo1 = branchInfo1;
            this.branchInfo2 = branchInfo2;
            this.sideDifferent = sideDifferent;
            this.isDifferent = sideDifferent.get(Side.ONE) || sideDifferent.get(Side.TWO);
        }

        @Override
        public boolean isDifferent() {
            return config.isFilterDifferent() && isDifferent;
        }

        @Override
        public void writeJson(JsonGenerator generator) {
            Objects.requireNonNull(generator);
            try {
                generator.writeStartObject();
                generator.writeStringField("branch.branchId1", branchInfo1.getBranchId());
                generator.writeStringField("branch.branchId2", branchInfo2.getBranchId());
                writeJson(generator, "terminal1", branchInfo1.getTerminalData(Side.ONE), branchInfo2.getTerminalData(Side.ONE));
                writeJson(generator, "terminal2", branchInfo1.getTerminalData(Side.TWO), branchInfo2.getTerminalData(Side.TWO));
                generator.writeFieldName("branch.connectionStatus-delta");
                new ObjectMapper().writeValue(generator, getConnectionStatusDelta());
                generator.writeFieldName("branch.terminalStatus-delta");
                new ObjectMapper().writeValue(generator, getTerminalStatusDelta());
                generator.writeBooleanField("branch.isDifferent", isDifferent);
                generator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private void writeJson(JsonGenerator generator, String terminal, BranchDiffInfo.TerminalData terminalData1,
                               BranchDiffInfo.TerminalData terminalData2) throws IOException {
            generator.writeBooleanField("branch." + terminal + ".isConnected1", terminalData1.isConnected());
            generator.writeBooleanField("branch." + terminal + ".isConnected2", terminalData2.isConnected());
            generator.writeNumberField("branch." + terminal + ".p1", terminalData1.getP());
            generator.writeNumberField("branch." + terminal + ".p2", terminalData2.getP());
            generator.writeNumberField("branch." + terminal + ".p-delta", Math.abs(terminalData1.getP() - terminalData2.getP()));
            generator.writeNumberField("branch." + terminal + ".p-delta-percent", Math.abs(terminalData1.getP() - terminalData2.getP()) / Math.abs(terminalData1.getP()) * 100);
            generator.writeNumberField("branch." + terminal + ".q1", terminalData1.getQ());
            generator.writeNumberField("branch." + terminal + ".q2", terminalData2.getQ());
            generator.writeNumberField("branch." + terminal + ".q-delta", Math.abs(terminalData1.getQ() - terminalData2.getQ()));
            generator.writeNumberField("branch." + terminal + ".q-delta-percent", Math.abs(terminalData1.getQ() - terminalData2.getQ()) / Math.abs(terminalData1.getQ()) * 100);
            generator.writeNumberField("branch." + terminal + ".i1", terminalData1.getI());
            generator.writeNumberField("branch." + terminal + ".i2", terminalData2.getI());
            generator.writeNumberField("branch." + terminal + ".i-delta", Math.abs(terminalData1.getI() - terminalData2.getI()));
            generator.writeNumberField("branch." + terminal + ".i-delta-percent", Math.abs(terminalData1.getI() - terminalData2.getI()) / Math.abs(terminalData1.getI()) * 100);
        }

        private Set<String> getConnectionStatusDelta() {
            Set<String> connectionStatusDelta = new HashSet<>();
            if (branchInfo1.getTerminalData(Side.ONE).isConnected() != branchInfo2.getTerminalData(Side.ONE).isConnected()) {
                connectionStatusDelta.add(branchInfo1.getBranchId() + "_" + Side.ONE);
            }
            if (branchInfo1.getTerminalData(Side.TWO).isConnected() != branchInfo2.getTerminalData(Side.TWO).isConnected()) {
                connectionStatusDelta.add(branchInfo1.getBranchId() + "_" + Side.TWO);
            }
            return connectionStatusDelta;
        }

        private Set<String> getTerminalStatusDelta() {
            Set<String> terminalStatusDelta = new HashSet<>();
            if (sideDifferent.get(Side.ONE)) {
                terminalStatusDelta.add(branchInfo1.getBranchId() + "_" + Side.ONE);
            }
            if (sideDifferent.get(Side.TWO)) {
                terminalStatusDelta.add(branchInfo1.getBranchId() + "_" + Side.TWO);
            }
            return terminalStatusDelta;
        }
    }

    @Override
    public DiffResult diff(Branch branch1, Branch branch2) {
        Objects.requireNonNull(branch1);
        Objects.requireNonNull(branch2);
        BranchDiffInfo branchInfo1 = new BranchDiffInfo(branch1.getId(), new HashMap<Branch.Side, BranchDiffInfo.TerminalData>());
        branchInfo1.setTerminalData(Side.ONE, getTerminalData(branchInfo1, branch1.getTerminal(Side.ONE)));
        branchInfo1.setTerminalData(Side.TWO, getTerminalData(branchInfo1, branch1.getTerminal(Side.TWO)));
        BranchDiffInfo branchInfo2 = new BranchDiffInfo(branch2.getId(), new HashMap<Branch.Side, BranchDiffInfo.TerminalData>());
        branchInfo2.setTerminalData(Side.ONE, getTerminalData(branchInfo2, branch2.getTerminal(Side.ONE)));
        branchInfo2.setTerminalData(Side.TWO, getTerminalData(branchInfo2, branch2.getTerminal(Side.TWO)));
        Map<Side, Boolean> sideDifferent = new HashMap<Side, Boolean>();
        sideDifferent.put(Side.ONE, !areEquals(branchInfo1.getTerminalData(Side.ONE), branchInfo2.getTerminalData(Side.ONE)));
        sideDifferent.put(Side.TWO, !areEquals(branchInfo1.getTerminalData(Side.TWO), branchInfo2.getTerminalData(Side.TWO)));
        return new BranchDiffResult(branchInfo1, branchInfo2, sideDifferent);
    }

    private BranchDiffInfo.TerminalData getTerminalData(BranchDiffInfo branchDiffInfo, Terminal terminal) {
        return branchDiffInfo.new TerminalData(terminal.isConnected(),
                                               terminal.getP(),
                                               terminal.getQ(),
                                               terminal.getI());
    }

    private boolean areEquals(BranchDiffInfo.TerminalData terminalData1, BranchDiffInfo.TerminalData terminalData2) {
        return terminalData1.isConnected() == terminalData2.isConnected()
               && DoubleMath.fuzzyEquals(terminalData1.getP(), terminalData2.getP(), config.getGenericTreshold())
               && DoubleMath.fuzzyEquals(terminalData1.getQ(), terminalData2.getQ(), config.getGenericTreshold())
               && DoubleMath.fuzzyEquals(terminalData1.getI(), terminalData2.getI(), config.getGenericTreshold());
    }
}
