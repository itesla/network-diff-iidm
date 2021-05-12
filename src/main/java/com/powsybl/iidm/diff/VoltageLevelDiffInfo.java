/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import java.util.Map;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class VoltageLevelDiffInfo {
    private final String vlId;
    private final long noBus;
    private final double minV;
    private final double maxV;
    private final Map<String, Boolean> switchesStatus;

    public VoltageLevelDiffInfo(String vlId, long noBus, double minV, double maxV, Map<String, Boolean> switchesStatus) {
        this.vlId = vlId;
        this.noBus = noBus;
        this.minV = minV;
        this.maxV = maxV;
        this.switchesStatus = switchesStatus;
    }

    public String getVlId() {
        return vlId;
    }

    public long getNoBus() {
        return noBus;
    }

    public double getMinV() {
        return minV;
    }

    public double getMaxV() {
        return maxV;
    }

    public Map<String, Boolean> getSwitchesStatus() {
        return switchesStatus;
    }
}
