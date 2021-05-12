/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiffResults {
    final String networkId1;
    final String networkId2;

    final List<DiffResult> vlDiffs;
    final List<DiffResult> branchDiffs;

    public NetworkDiffResults(String networkId1, String networkId2, List<DiffResult> vlDiffs, List<DiffResult> branchDiffs) {
        this.networkId1 = Objects.requireNonNull(networkId1);
        this.networkId2 = Objects.requireNonNull(networkId2);
        this.vlDiffs = Objects.requireNonNull(vlDiffs);
        this.branchDiffs = Objects.requireNonNull(branchDiffs);
    }

    public boolean isDifferent() {
        return vlDiffs.stream().anyMatch(DiffResult::isDifferent)
                || branchDiffs.stream().anyMatch(DiffResult::isDifferent);
    }

    void writeJson(JsonGenerator generator) {
        Objects.requireNonNull(generator);
        try {
            generator.writeStartObject();
            generator.writeStringField("network1", networkId1);
            generator.writeStringField("network2", networkId2);
            generator.writeFieldName("diff.VoltageLevels");
            NetworkDiff.writeJson(generator, vlDiffs);
            generator.writeFieldName("diff.Branches");
            NetworkDiff.writeJson(generator, branchDiffs);
            generator.writeEndObject();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
