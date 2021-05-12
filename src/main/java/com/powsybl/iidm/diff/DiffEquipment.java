/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@acotel-group.com>
 */
public class DiffEquipment {

    private List<DiffEquipmentType> equipmentTypes = Collections.singletonList(DiffEquipmentType.ALL);
    private List<String> voltageLevels = null;
    private List<String> branches = null;

    public List<DiffEquipmentType> getEquipmentTypes() {
        return equipmentTypes;
    }

    public void setEquipmentTypes(List<DiffEquipmentType> equipmentTypes) {
        this.equipmentTypes = Objects.requireNonNull(equipmentTypes);
    }

    public List<String> getVoltageLevels() {
        return voltageLevels;
    }

    public void setVoltageLevels(List<String> voltageLevels) {
        this.voltageLevels = voltageLevels;
    }

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }
}
