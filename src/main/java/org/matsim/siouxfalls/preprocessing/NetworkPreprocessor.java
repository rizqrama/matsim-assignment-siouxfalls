package org.matsim.siouxfalls.preprocessing;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Converts the raw XML OSM file to a cleaned MATSim network in EPSG:32614.
 * Only highway types relevant to urban car routing are included.
 * NetworkCleaner removes disconnected components after conversion.
 */
public class NetworkPreprocessor {

    private final String osmInputPath;
    private final String networkOutputPath;

    public NetworkPreprocessor(String osmInputPath, String networkOutputPath) {
        this.osmInputPath = osmInputPath;
        this.networkOutputPath = networkOutputPath;
    }

    public void run() throws Exception {
        Files.createDirectories(Path.of(networkOutputPath).getParent());

        Network network = NetworkUtils.createNetwork();
        var ct = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, "EPSG:32614");

        OsmNetworkReader reader = new OsmNetworkReader(network, ct);

        // Hierarchy levels and road type defaults (lanes, freespeed m/s, factor, capacity/lane/h)
        reader.setHighwayDefaults(1, "motorway",     2, 33.33, 1.0, 2000, true);
        reader.setHighwayDefaults(2, "trunk",        1, 27.78, 1.0, 2000, false);
        reader.setHighwayDefaults(3, "primary",      1, 22.22, 1.0, 1500, false);
        reader.setHighwayDefaults(4, "secondary",    1, 16.67, 1.0, 1000, false);
        reader.setHighwayDefaults(5, "tertiary",     1, 11.11, 1.0,  600, false);
        reader.setHighwayDefaults(6, "residential",  1,  8.33, 1.0,  600, false);
        reader.setHighwayDefaults(6, "unclassified", 1,  8.33, 1.0,  600, false);

        reader.parse(osmInputPath);

        new NetworkCleaner().run(network);

        new NetworkWriter(network).write(networkOutputPath);

        System.out.printf("Network written to %s — %d nodes, %d links%n",
                networkOutputPath,
                network.getNodes().size(),
                network.getLinks().size());
    }
}
