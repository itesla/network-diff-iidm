/**
 * Copyright (c) 2020-2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.NetworkTest1Factory;

/**
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
public final class NetworkDiffTestUtils {

    private NetworkDiffTestUtils() {
    }

    public static Network createNetwork1() {
        Network network = EurostagTutorialExample1Factory.create();
        network.getBusView().getBus("VLGEN_0").setV(24.5).setAngle(2.33);
        network.getBusView().getBus("VLHV1_0").setV(402.14).setAngle(0);
        network.getBusView().getBus("VLHV2_0").setV(389.95).setAngle(-3.5);
        network.getBusView().getBus("VLLOAD_0").setV(147.58).setAngle(9.61);
        network.getLine("NHV1_NHV2_1").getTerminal1().setP(302.4).setQ(98.7);
        network.getLine("NHV1_NHV2_1").getTerminal2().setP(-300.4).setQ(-137.1);
        network.getLine("NHV1_NHV2_1").newCurrentLimits1().setPermanentLimit(600).add();
        network.getLine("NHV1_NHV2_1").newCurrentLimits2().setPermanentLimit(600).add();
        network.getLine("NHV1_NHV2_2").getTerminal1().setP(302.4).setQ(98.7);
        network.getLine("NHV1_NHV2_2").getTerminal2().setP(-300.4).setQ(-137.1);
        network.getLine("NHV1_NHV2_2").newCurrentLimits1().setPermanentLimit(600).add();
        network.getLine("NHV1_NHV2_2").newCurrentLimits2().setPermanentLimit(600).add();
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().setP(607).setQ(225.4);
        network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().setP(-606.3).setQ(-197.4);
        network.getTwoWindingsTransformer("NGEN_NHV1").newCurrentLimits1().setPermanentLimit(18000).add();
        network.getTwoWindingsTransformer("NGEN_NHV1").newCurrentLimits2().setPermanentLimit(1200).add();
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal1().setP(600).setQ(274.3);
        network.getTwoWindingsTransformer("NHV2_NLOAD").getTerminal2().setP(-600).setQ(-200);
        network.getTwoWindingsTransformer("NHV2_NLOAD").newCurrentLimits1().setPermanentLimit(1500).add();
        network.getTwoWindingsTransformer("NHV2_NLOAD").newCurrentLimits2().setPermanentLimit(2800).add();
        network.getGenerator("GEN").getTerminal().setP(607).setQ(225.4);
        network.getLoad("LOAD").getTerminal().setP(600).setQ(200);
        return network;
    }

    public static Network createNetwork2() {
        Network network = createNetwork1();
        network.getBranches().iterator().next().remove();
        network.getBusView().getBus("VLHV2_0").setV(350);
        return network;
    }

    public static Network createNetwork3() {
        Network network = NetworkTest1Factory.create();
        return network;
    }

    public static Network createNetwork4() {
        Network network = NetworkDiffTestUtils.createNetwork3();
        network.getSwitch("voltageLevel1Breaker1").setOpen(true);
        return network;
    }

    public static Network createNetwork5() {
        Network network = NetworkTest1Factory.create();
        return network;
    }

    public static Network createNetwork6() {
        Network network = NetworkDiffTestUtils.createNetwork5();
        network.getSwitch("voltageLevel1Breaker1").setOpen(true);
        network.getSwitch("load1Disconnector1").setOpen(true);
        return network;
    }

    public static Network createNetwork7() {
        Network network = NetworkDiffTestUtils.createNetwork2();
        network.getBusView().getBus("VLGEN_0").setV(Double.NaN);
        return network;
    }

    public static Network createNetwork8() {
        Network network = NetworkDiffTestUtils.createNetwork2();
        network.getBusView().getBus("VLHV1_0").setV(Double.NaN);
        return network;
    }

}
