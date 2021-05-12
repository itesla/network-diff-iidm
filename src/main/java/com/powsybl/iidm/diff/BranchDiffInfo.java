/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import java.util.Map;
import java.util.Objects;

import com.powsybl.iidm.network.Branch.Side;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@acotel-group.com>
 */
public class BranchDiffInfo {

    private final String branchId;
    private final Map<Side, TerminalData> terminalData;

    public BranchDiffInfo(String branchId, Map<Side, TerminalData> terminalData) {
        this.branchId = Objects.requireNonNull(branchId);
        this.terminalData = Objects.requireNonNull(terminalData);
    }

    class TerminalData {
        private final boolean connected;
        private final double p;
        private final double q;
        private final double i;

        TerminalData(boolean connected, double p, double q, double i) {
            this.connected = connected;
            this.p = p;
            this.q = q;
            this.i = i;
        }

        public boolean isConnected() {
            return connected;
        }

        public double getP() {
            return p;
        }

        public double getQ() {
            return q;
        }

        public double getI() {
            return i;
        }
    }

    public String getBranchId() {
        return branchId;
    }

    public Map<Side, TerminalData> getTerminalData() {
        return terminalData;
    }

    public TerminalData getTerminalData(Side side) {
        Objects.requireNonNull(side);
        return terminalData.get(side);
    }

    public void setTerminalData(Side side, TerminalData terminalData) {
        Objects.requireNonNull(side);
        Objects.requireNonNull(terminalData);
        this.terminalData.put(side, terminalData);
    }
}
