/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.VoltageLevel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
class VoltageLevelDiffProc implements DiffProc<VoltageLevel> {

    private final DiffConfig config;

    public VoltageLevelDiffProc(DiffConfig config) {
        this.config = config;
    }

    class VoltageLevelDiffResult implements DiffResult {
        final VoltageLevelDiffInfo vlInfo1;
        final VoltageLevelDiffInfo vlInfo2;
        final boolean isDifferent;

        public VoltageLevelDiffResult(VoltageLevelDiffInfo vlInfo1, VoltageLevelDiffInfo vlInfo2, boolean isDifferent) {
            this.vlInfo1 = vlInfo1;
            this.vlInfo2 = vlInfo2;
            this.isDifferent = isDifferent;
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
                generator.writeStringField("vl.vlId1", vlInfo1.getVlId());
                generator.writeStringField("vl.vlId2", vlInfo2.getVlId());
                generator.writeNumberField("vl.noBus1", vlInfo1.getNoBus());
                generator.writeNumberField("vl.noBus2", vlInfo2.getNoBus());
                generator.writeNumberField("vl.minV1", vlInfo1.getMinV());
                generator.writeNumberField("vl.minV2", vlInfo2.getMinV());
                generator.writeNumberField("vl.minV-delta", Math.abs(vlInfo1.getMinV() - vlInfo2.getMinV()));
                generator.writeNumberField("vl.minV-delta-percent", Math.abs(vlInfo1.getMinV() - vlInfo2.getMinV()) / Math.abs(vlInfo1.getLowVoltageLimit()) * 100);
                generator.writeNumberField("vl.maxV1", vlInfo1.getMaxV());
                generator.writeNumberField("vl.maxV2", vlInfo2.getMaxV());
                generator.writeNumberField("vl.maxV-delta", Math.abs(vlInfo1.getMaxV() - vlInfo2.getMaxV()));
                generator.writeNumberField("vl.maxV-delta-percent", Math.abs(vlInfo1.getMaxV() - vlInfo2.getMaxV()) / Math.abs(vlInfo1.getHighVoltageLimit()) * 100);
                writeSwitchesStatusJson(generator, "vl.switchesStatusV1", vlInfo1);
                writeSwitchesStatusJson(generator, "vl.switchesStatusV2", vlInfo2);
                writeSwitchesDeltaJson(generator, "vl.switchesStatus-delta", vlInfo1, vlInfo2);
                generator.writeBooleanField("vl.isDifferent", isDifferent);
                generator.writeEndObject();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private void writeSwitchesStatusJson(JsonGenerator generator, String name, VoltageLevelDiffInfo vlInfo) throws IOException {
            generator.writeFieldName(name);
            new ObjectMapper().writeValue(generator, vlInfo.getSwitchesStatus());
        }

        private void writeSwitchesDeltaJson(JsonGenerator generator, String name, VoltageLevelDiffInfo vlInfo1, VoltageLevelDiffInfo vlInfo2) throws IOException {
            generator.writeFieldName(name);
            new ObjectMapper().writeValue(generator, Maps.difference(vlInfo1.getSwitchesStatus(), vlInfo2.getSwitchesStatus()).entriesDiffering().keySet());
        }
    }

    @Override
    public DiffResult diff(VoltageLevel vl1, VoltageLevel vl2) {
        double maxV1 = vl1.getBusView().getBusStream().mapToDouble(Bus::getV).filter(v -> !Double.isNaN(v)).max().orElse(0);
        double minV1 = vl1.getBusView().getBusStream().mapToDouble(Bus::getV).filter(v -> !Double.isNaN(v)).min().orElse(0);
        double maxV2 = vl2.getBusView().getBusStream().mapToDouble(Bus::getV).filter(v -> !Double.isNaN(v)).max().orElse(0);
        double minV2 = vl2.getBusView().getBusStream().mapToDouble(Bus::getV).filter(v -> !Double.isNaN(v)).min().orElse(0);

        long noBusesVl1 = vl1.getBusView().getBusStream().count();
        long noBusesVl2 = vl2.getBusView().getBusStream().count();

        Map<String, Boolean> switchesStatusVl1 = StreamSupport.stream(vl1.getSwitches().spliterator(), false).collect(Collectors.toMap(Switch::getId, Switch::isOpen));
        Map<String, Boolean> switchesStatusVl2 = StreamSupport.stream(vl2.getSwitches().spliterator(), false).collect(Collectors.toMap(Switch::getId, Switch::isOpen));
        MapDifference<String, Boolean> switchesDiff = Maps.difference(switchesStatusVl1, switchesStatusVl2);

        boolean isEqual = DoubleMath.fuzzyEquals(maxV1, maxV2, config.getGenericTreshold())
                && DoubleMath.fuzzyEquals(minV1, minV2, config.getGenericTreshold())
                && (noBusesVl1 == noBusesVl2)
                && (switchesDiff.areEqual());
        return new VoltageLevelDiffResult(new VoltageLevelDiffInfo(vl1.getId(), noBusesVl1, minV1, maxV1, switchesStatusVl1, vl1.getLowVoltageLimit(), vl1.getHighVoltageLimit()),
                                          new VoltageLevelDiffInfo(vl2.getId(), noBusesVl2, minV2, maxV2, switchesStatusVl2, vl2.getLowVoltageLimit(), vl2.getHighVoltageLimit()),
                                          !isEqual);
    }
}
