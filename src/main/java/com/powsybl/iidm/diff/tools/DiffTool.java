/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.diff.tools;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.auto.service.AutoService;
import com.powsybl.iidm.diff.DiffConfig;
import com.powsybl.iidm.diff.DiffEquipment;
import com.powsybl.iidm.diff.DiffEquipmentType;
import com.powsybl.iidm.diff.NetworkDiff;
import com.powsybl.iidm.diff.NetworkDiffResults;
import com.powsybl.iidm.import_.ImportConfig;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolRunningContext;

/**
 *
 * @author Christian Biasuzzi <christian.biasuzzi@techrain.eu>
 */
@AutoService(Tool.class)
public class DiffTool implements Tool {

    private static final String INPUT_FILE1 = "input-file1";
    private static final String INPUT_FILE2 = "input-file2";
    private static final String OUTPUT_FILE = "output-file";
    private static final String EQUIPMENT_TYPES = "equipment-types";
    private static final String VL_IDS = "vl-ids";
    private static final String BRANCH_IDS = "branch-ids";

    @Override
    public Command getCommand() {
        return new Command() {

            @Override
            public String getName() {
                return "compare-network";
            }

            @Override
            public String getDescription() {
                return "Compare two networks";
            }

            @Override
            public String getTheme() {
                return "Computation";
            }

            @Override
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt(INPUT_FILE1)
                        .desc("the input file1")
                        .hasArg()
                        .argName("INPUT_FILE1")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(INPUT_FILE2)
                        .desc("the input file2")
                        .hasArg()
                        .argName("INPUT_FILE2")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(OUTPUT_FILE)
                        .desc("the output file")
                        .hasArg()
                        .argName("OUTPUT_FILE")
                        .required()
                        .build());
                options.addOption(Option.builder().longOpt(EQUIPMENT_TYPES)
                        .desc("equipment types " + Arrays.toString(DiffEquipmentType.values()) + " to compare, all of them if the option if not specified")
                        .hasArg()
                        .argName("EQUIPMENT_TYPES")
                        .numberOfArgs(Option.UNLIMITED_VALUES)
                        .valueSeparator(',')
                        .build());
                options.addOption(Option.builder().longOpt(VL_IDS)
                        .desc("voltage level ids to compare, all of them if the option if not specified")
                        .hasArg()
                        .argName("VL_IDS")
                        .numberOfArgs(Option.UNLIMITED_VALUES)
                        .valueSeparator(',')
                        .build());
                options.addOption(Option.builder().longOpt(BRANCH_IDS)
                        .desc("branch ids to compare, all of them if the option if not specified")
                        .hasArg()
                        .argName("BRANCH_IDS")
                        .numberOfArgs(Option.UNLIMITED_VALUES)
                        .valueSeparator(',')
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return null;
            }
        };
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        String inputFile1 = line.getOptionValue(INPUT_FILE1);
        String inputFile2 = line.getOptionValue(INPUT_FILE2);
        String outputFile = line.getOptionValue(OUTPUT_FILE);
        List<DiffEquipmentType> equipmentTypes = Arrays.asList(DiffEquipmentType.values());
        if (line.hasOption(EQUIPMENT_TYPES)) {
            equipmentTypes = Arrays.stream(line.getOptionValues(EQUIPMENT_TYPES))
                                   .map(DiffEquipmentType::valueOf)
                                   .collect(Collectors.toList());
        }
        String[] vlIds = line.getOptionValues(VL_IDS);
        String[] branchIds = line.getOptionValues(BRANCH_IDS);

        DiffConfig config = DiffConfig.load();

        ImportConfig importConfig = new ImportConfig();

        Network network1 = Importers.loadNetwork(context.getFileSystem().getPath(inputFile1), context.getShortTimeExecutionComputationManager(), importConfig, null);
        Network network2 = Importers.loadNetwork(context.getFileSystem().getPath(inputFile2), context.getShortTimeExecutionComputationManager(), importConfig, null);
        DiffEquipment diffEquipment = new DiffEquipment();
        diffEquipment.setEquipmentTypes(equipmentTypes);
        if (vlIds != null) {
            diffEquipment.setVoltageLevels(Arrays.asList(vlIds));
        }
        if (branchIds != null) {
            diffEquipment.setBranches(Arrays.asList(branchIds));
        }
        NetworkDiffResults ndifr = new NetworkDiff(config).diff(network1, network2, diffEquipment);
        NetworkDiff.writeJson(context.getFileSystem().getPath(outputFile), ndifr);
    }
}
