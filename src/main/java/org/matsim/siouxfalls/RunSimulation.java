package org.matsim.siouxfalls;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.siouxfalls.config.SiouxFallsConfigBuilder;
import org.matsim.siouxfalls.demand.PopulationGenerator;
import org.matsim.siouxfalls.preprocessing.NetworkPreprocessor;

import java.io.File;
import java.nio.file.Path;

/**
 * Entry point for the Sioux Falls simulation pipeline.
 *
 * Pipeline:
 *   1. NetworkPreprocessor  — OSM → scenarios/siouxfalls/network.xml.gz  (skipped if exists)
 *   2. PopulationGenerator  — OD matrix + zones → population.xml.gz      (skipped if exists)
 *   3. SiouxFallsConfigBuilder — assembles scenarios/siouxfalls/config.xml (always rebuilt)
 *   4. Controler            — runs 50 iterations, writes to output/
 */
public class RunSimulation {

    private static final String OSM_PATH          = "input/siouxfalls.osm";
    private static final String OD_MATRIX_PATH    = "input/od_matrix.csv";
    private static final String ZONES_PATH        = "input/siouxfalls-voronoi-zones/voronoi_zones.shp";
    private static final String NETWORK_PATH      = "scenarios/siouxfalls/network.xml.gz";
    private static final String POPULATION_PATH   = "scenarios/siouxfalls/population.xml.gz";
    private static final String CONFIG_PATH       = "scenarios/siouxfalls/config.xml";
    private static final String OUTPUT_DIR        = "output";

    public static void main(String[] args) throws Exception {

        // Stage 1: Network preprocessing
        if (new File(NETWORK_PATH).exists()) {
            System.out.println("[Stage 1] Network file exists — skipping preprocessing.");
        } else {
            System.out.println("[Stage 1] Running network preprocessing...");
            new NetworkPreprocessor(OSM_PATH, NETWORK_PATH).run();
        }

        // Stage 2: Population generation
        if (new File(POPULATION_PATH).exists()) {
            System.out.println("[Stage 2] Population file exists — skipping generation.");
        } else {
            System.out.println("[Stage 2] Running population generation...");
            new PopulationGenerator(OD_MATRIX_PATH, ZONES_PATH, POPULATION_PATH).run();
        }

        // Stage 3: Build config (always rebuilt so parameter changes take effect)
        // Paths are converted to absolute so MATSim doesn't double-resolve them
        // relative to the config file's directory.
        System.out.println("[Stage 3] Building config...");
        SiouxFallsConfigBuilder.build(
                Path.of(NETWORK_PATH).toAbsolutePath().toString(),
                Path.of(POPULATION_PATH).toAbsolutePath().toString(),
                Path.of(OUTPUT_DIR).toAbsolutePath().toString(),
                CONFIG_PATH);

        // Stage 4: Run simulation
        System.out.println("[Stage 4] Starting simulation...");
        Config config = ConfigUtils.loadConfig(CONFIG_PATH);
        Controler controler = new Controler(config);
        controler.run();
    }
}
