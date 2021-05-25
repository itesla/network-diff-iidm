/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import java.util.Objects;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public class DiffConfig {

    public static final double EPSILON_DEFAULT = 0.0;
    public static final boolean FILTER_DIFF_DEFAULT = true;

    private double genericThreshold;

    private double voltageThreshold;

    private boolean filterDifferent;

    public static DiffConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static DiffConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        double epsilon = EPSILON_DEFAULT;
        double voltageEpsilon = EPSILON_DEFAULT;
        boolean filterDiff = FILTER_DIFF_DEFAULT;
        if (platformConfig.moduleExists("networks-diff")) {
            ModuleConfig config = platformConfig.getModuleConfig("networks-diff");
            epsilon = config.getDoubleProperty("generic-threshold", EPSILON_DEFAULT);
            voltageEpsilon = config.getDoubleProperty("voltage-threshold", EPSILON_DEFAULT);
            filterDiff = config.getBooleanProperty("filter-diff", FILTER_DIFF_DEFAULT);
        }
        return new DiffConfig(epsilon, voltageEpsilon, filterDiff);
    }

    public DiffConfig(double genericThreshold, boolean filterDifferent) {
        this(genericThreshold, genericThreshold, filterDifferent);
    }

    public DiffConfig(double genericThreshold, double voltageThreshold, boolean filterDifferent) {
        if (genericThreshold < 0 || voltageThreshold < 0) {
            throw new IllegalArgumentException("Negative values for threshold not permitted");
        }
        this.genericThreshold = genericThreshold;
        this.voltageThreshold = voltageThreshold;
        this.filterDifferent = filterDifferent;
    }

    public double getGenericThreshold() {
        return genericThreshold;
    }

    public void setGenericThreshold(double genericThreshold) {
        this.genericThreshold = genericThreshold;
    }

    public double getVoltageThreshold() {
        return voltageThreshold;
    }

    public void setVoltageThreshold(double voltageThreshold) {
        this.voltageThreshold = voltageThreshold;
    }

    public boolean isFilterDifferent() {
        return filterDifferent;
    }

    public void setFilterDifferent(boolean filterDifferent) {
        this.filterDifferent = filterDifferent;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "filterDifferent=" + filterDifferent +
                ", genericThreshold=" + genericThreshold +
                ", voltageThreshold=" + voltageThreshold +
                "]";
    }

}
