/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class NetworkDiff {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkDiff.class);

    private final DiffProc<VoltageLevel> voltagediff;
    private final DiffProc<Branch> branchDiff;

    public NetworkDiff(DiffConfig config) {
        Objects.requireNonNull(config);
        this.voltagediff = new VoltageLevelDiffProc(config);
        this.branchDiff = new BranchDiffProc(config);
    }

    static void writeJson(JsonGenerator generator, List<? extends DiffResult> diffResults) {
        Objects.requireNonNull(generator);
        Objects.requireNonNull(diffResults);
        try {
            generator.writeStartArray();
            for (DiffResult diffResult : diffResults) {
                diffResult.writeJson(generator);
            }
            generator.writeEndArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void writeJson(Writer writer, List<? extends DiffResult> diffResults) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(diffResults);
        JsonUtil.writeJson(writer, generator -> writeJson(generator, diffResults));
    }

    static void writeJson(Path file, List<? extends DiffResult> diffResults) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(diffResults);
        JsonUtil.writeJson(file, generator -> writeJson(generator, diffResults));
    }

    static String toJson(List<? extends DiffResult> diffResults) {
        Objects.requireNonNull(diffResults);
        return JsonUtil.toJson(generator -> writeJson(generator, diffResults));
    }

    public static String writeJson(NetworkDiffResults ndifr) {
        Objects.requireNonNull(ndifr);
        return JsonUtil.toJson(ndifr::writeJson);
    }

    public static void writeJson(Writer writer, NetworkDiffResults ndifr) {
        Objects.requireNonNull(writer);
        Objects.requireNonNull(ndifr);
        JsonUtil.writeJson(writer, ndifr::writeJson);
    }

    public static void writeJson(Path file, NetworkDiffResults ndifr) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(ndifr);
        JsonUtil.writeJson(file, ndifr::writeJson);
    }

    public NetworkDiffResults diff(Network network1, Network network2) {
        return diff(network1, network2, new DiffEquipment());
    }

    public NetworkDiffResults diff(Network network1, Network network2, DiffEquipment diffEquipment) {
        Objects.requireNonNull(network1);
        Objects.requireNonNull(network2);
        Objects.requireNonNull(diffEquipment);
        long start = System.currentTimeMillis();

        List<DiffResult> vlDiffs = getVoltageLevelDiffs(network1, network2, diffEquipment);
        List<DiffResult> branchDiffs = getBranchDiffs(network1, network2, diffEquipment);
        NetworkDiffResults ndifr = new NetworkDiffResults(network1.getId(), network2.getId(), vlDiffs, branchDiffs);

        LOGGER.debug("diff generated in {} ms", System.currentTimeMillis() - start);
        return ndifr;
    }

    private List<DiffResult> getVoltageLevelDiffs(Network network1, Network network2, DiffEquipment diffEquipment) {
        if (diffEquipment.getEquipmentTypes().contains(DiffEquipmentType.VOLTAGE_LEVELS)
            || diffEquipment.getEquipmentTypes().contains(DiffEquipmentType.ALL)) {
            Set<String> vlIds = getVoltageLevelIds(network1, network2, diffEquipment.getVoltageLevels());
            return vlIds.stream()
                        .sorted()
                        .map(vlId -> voltagediff.diff(network1.getVoltageLevel(vlId), network2.getVoltageLevel(vlId)))
                        .filter(DiffResult::isDifferent)
                        .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Set<String> getVoltageLevelIds(Network network1, Network network2, List<String> vls) {
        if (vls == null) {
            return network1.getVoltageLevelStream()
                           .map(VoltageLevel::getId)
                           .filter(vlId -> network2.getVoltageLevel(vlId) != null)
                           .collect(Collectors.toSet());
        }
        return vls.stream()
                  .filter(vlId -> network1.getVoltageLevel(vlId) != null && network2.getVoltageLevel(vlId) != null)
                  .collect(Collectors.toSet());
    }

    private List<DiffResult> getBranchDiffs(Network network1, Network network2, DiffEquipment diffEquipment) {
        if (diffEquipment.getEquipmentTypes().contains(DiffEquipmentType.BRANCHES)
            || diffEquipment.getEquipmentTypes().contains(DiffEquipmentType.ALL)) {
            Set<String> branchIds = getBranchIds(network1, network2, diffEquipment.getBranches());
            return branchIds.stream()
                            .sorted()
                            .map(branchId -> branchDiff.diff(network1.getBranch(branchId), network2.getBranch(branchId)))
                            .filter(DiffResult::isDifferent)
                            .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Set<String> getBranchIds(Network network1, Network network2, List<String> branches) {
        if (branches == null) {
            return network1.getBranchStream()
                           .map(Branch::getId)
                           .filter(branchId -> network2.getBranch(branchId) != null)
                           .collect(Collectors.toSet());
        }
        return branches.stream()
                       .filter(branchId -> network1.getBranch(branchId) != null && network2.getBranch(branchId) != null)
                       .collect(Collectors.toSet());
    }
}
