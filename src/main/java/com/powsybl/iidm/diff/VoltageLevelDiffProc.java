/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Equivalence;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
class VoltageLevelDiffProc implements DiffProc<VoltageLevel> {

    private final DiffConfig config;
    private final Equivalence<Double> diffEq;

    public VoltageLevelDiffProc(DiffConfig config) {
        this.config = config;
        diffEq = new Equivalence<Double>() {

            @Override
            protected boolean doEquivalent(Double val1, Double val2) {
                Double value1 = Double.isNaN(val1) ? 0 : val1;
                Double value2 = Double.isNaN(val2) ? 0 : val2;
                return DoubleMath.fuzzyEquals(value1, value2, config.getVoltageThreshold());
            }

            @Override
            protected int doHash(Double val) {
                return val.hashCode();
            }

        };
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
                generator.writeNumberField("vl.minV-delta", vlInfo2.getMinV() - vlInfo1.getMinV());
                generator.writeNumberField("vl.minV-delta-percent", (vlInfo2.getMinV() - vlInfo1.getMinV()) / Math.abs(vlInfo1.getLowVoltageLimit()) * 100);
                generator.writeNumberField("vl.maxV1", vlInfo1.getMaxV());
                generator.writeNumberField("vl.maxV2", vlInfo2.getMaxV());
                generator.writeNumberField("vl.maxV-delta", vlInfo2.getMaxV() - vlInfo1.getMaxV());
                generator.writeNumberField("vl.maxV-delta-percent", (vlInfo2.getMaxV() - vlInfo1.getMaxV()) / Math.abs(vlInfo1.getHighVoltageLimit()) * 100);
                writeSwitchesStatusJson(generator, "vl.switchesStatusV1", vlInfo1);
                writeSwitchesStatusJson(generator, "vl.switchesStatusV2", vlInfo2);
                writeSwitchesDeltaJson(generator, "vl.switchesStatus-delta", vlInfo1, vlInfo2);
                if (!vlInfo1.getBusbarsVoltage().isEmpty()) {
                    writeBusbarsVoltageJson(generator, "vl.busbarsVoltage1", vlInfo1);
                }
                if (!vlInfo2.getBusbarsVoltage().isEmpty()) {
                    writeBusbarsVoltageJson(generator, "vl.busbarsVoltage2", vlInfo2);
                }
                if (!vlInfo1.getBusbarsVoltage().isEmpty() && !vlInfo2.getBusbarsVoltage().isEmpty()) {
                    writeBusbarsVoltageDeltaJson(generator, "vl.busbarsVoltage-delta", vlInfo1, vlInfo2);
                    writeBusbarsVoltageDeltaPercentJson(generator, "vl.busbarsVoltage-delta-percent", vlInfo1, vlInfo2);
                }
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

        private void writeBusbarsVoltageJson(JsonGenerator generator, String name, VoltageLevelDiffInfo vlInfo) throws IOException {
            generator.writeFieldName(name);
            new ObjectMapper().writeValue(generator, vlInfo.getBusbarsVoltage());
        }

        private void writeBusbarsVoltageDeltaJson(JsonGenerator generator, String name, VoltageLevelDiffInfo vlInfo1, VoltageLevelDiffInfo vlInfo2) throws IOException {
            generator.writeFieldName(name);
            new ObjectMapper().writeValue(generator, Maps.difference(vlInfo1.getBusbarsVoltage(), vlInfo2.getBusbarsVoltage(), diffEq)
                                                         .entriesDiffering()
                                                         .entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                                                             double v1 = Double.isNaN(e.getValue().leftValue()) ? 0 : e.getValue().leftValue();
                                                             double v2 = Double.isNaN(e.getValue().rightValue()) ? 0 : e.getValue().rightValue();
                                                             return v2 - v1;
                                                         })));
        }

        private void writeBusbarsVoltageDeltaPercentJson(JsonGenerator generator, String name, VoltageLevelDiffInfo vlInfo1, VoltageLevelDiffInfo vlInfo2) throws IOException {
            generator.writeFieldName(name);
            new ObjectMapper().writeValue(generator, Maps.difference(vlInfo1.getBusbarsVoltage(), vlInfo2.getBusbarsVoltage(), diffEq)
                                                         .entriesDiffering()
                                                         .entrySet()
                                                         .stream()
                                                         .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                                                             double v1 = Double.isNaN(e.getValue().leftValue()) ? 0 : e.getValue().leftValue();
                                                             double v2 = Double.isNaN(e.getValue().rightValue()) ? 0 : e.getValue().rightValue();
                                                             double percent = (v2 - v1) / Math.abs(vlInfo1.getNominalVoltage()) * 100;
                                                             if (percent > 100) {
                                                                 percent = 100;
                                                             } else if (percent < -100) {
                                                                 percent = -100;
                                                             }
                                                             return percent;
                                                         })));
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

        Map<String, Double> busbarsVoltageVl1 = TopologyKind.NODE_BREAKER.equals(vl1.getTopologyKind())
                                                ? vl1.getNodeBreakerView().getBusbarSectionStream().collect(Collectors.toMap(BusbarSection::getId, BusbarSection::getV))
                                                : Collections.emptyMap();
        Map<String, Double> busbarsVoltageVl2 = TopologyKind.NODE_BREAKER.equals(vl2.getTopologyKind())
                                                ? vl2.getNodeBreakerView().getBusbarSectionStream().collect(Collectors.toMap(BusbarSection::getId, BusbarSection::getV))
                                                : Collections.emptyMap();
        MapDifference<String, Double> busbarsDiff = Maps.difference(busbarsVoltageVl1, busbarsVoltageVl2, diffEq);

        boolean isEqual = DoubleMath.fuzzyEquals(maxV1, maxV2, config.getVoltageThreshold())
                && DoubleMath.fuzzyEquals(minV1, minV2, config.getVoltageThreshold())
                && (noBusesVl1 == noBusesVl2)
                && (switchesDiff.areEqual())
                && (busbarsDiff.areEqual());
        return new VoltageLevelDiffResult(new VoltageLevelDiffInfo(vl1.getId(), noBusesVl1, minV1, maxV1, switchesStatusVl1, vl1.getLowVoltageLimit(), vl1.getHighVoltageLimit(),
                                                                   busbarsVoltageVl1, vl1.getNominalV()),
                                          new VoltageLevelDiffInfo(vl2.getId(), noBusesVl2, minV2, maxV2, switchesStatusVl2, vl2.getLowVoltageLimit(), vl2.getHighVoltageLimit(),
                                                                   busbarsVoltageVl2, vl2.getNominalV()),
                                          !isEqual);
    }
}
