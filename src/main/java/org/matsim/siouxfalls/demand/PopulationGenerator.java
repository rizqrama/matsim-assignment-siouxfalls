package org.matsim.siouxfalls.demand;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generates a synthetic population from a zone-to-zone OD matrix and Voronoi
 * zone polygons. Each OD pair produces N agents with a home→work→home plan.
 * Departure times are drawn uniformly from the specified windows.
 * Sample rate is 100% (total trips < 50,000 threshold).
 */
public class PopulationGenerator {

    private static final int SAMPLE_THRESHOLD = 50_000;
    private static final double WORK_DEPART_START = 7 * 3600.0;
    private static final double WORK_DEPART_WINDOW = 3600.0;
    private static final double HOME_DEPART_START = 16 * 3600.0;
    private static final double HOME_DEPART_WINDOW = 3600.0;
    private static final int MAX_SAMPLE_ATTEMPTS = 100;
    private static final long RANDOM_SEED = 42L;

    private final String odMatrixPath;
    private final String zonesShapePath;
    private final String populationOutputPath;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public PopulationGenerator(String odMatrixPath, String zonesShapePath,
                               String populationOutputPath) {
        this.odMatrixPath = odMatrixPath;
        this.zonesShapePath = zonesShapePath;
        this.populationOutputPath = populationOutputPath;
    }

    public void run() throws Exception {
        Files.createDirectories(Path.of(populationOutputPath).getParent());

        Map<Integer, Geometry> zones = readZones();
        List<int[]> odPairs = readOdMatrix();

        int totalTrips = odPairs.stream().mapToInt(p -> p[2]).sum();
        double sampleFactor = totalTrips > SAMPLE_THRESHOLD ? 0.1 : 1.0;
        System.out.printf("Total OD trips: %d — sample factor: %.0f%%%n",
                totalTrips, sampleFactor * 100);

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        Population population = scenario.getPopulation();
        PopulationFactory factory = population.getFactory();
        Random random = new Random(RANDOM_SEED);
        int agentId = 0;

        for (int[] pair : odPairs) {
            int fromZone = pair[0];
            int toZone = pair[1];
            int count = (int) Math.round(pair[2] * sampleFactor);
            if (count == 0) continue;

            Geometry homeZone = zones.get(fromZone);
            Geometry workZone = zones.get(toZone);

            for (int i = 0; i < count; i++) {
                Coord homeCoord = sampleCoordInZone(homeZone, random);
                Coord workCoord = sampleCoordInZone(workZone, random);
                double workDeparture = WORK_DEPART_START + random.nextDouble() * WORK_DEPART_WINDOW;
                double homeDeparture = HOME_DEPART_START + random.nextDouble() * HOME_DEPART_WINDOW;

                Person person = factory.createPerson(Id.createPersonId(agentId++));
                Plan plan = factory.createPlan();

                Activity home1 = factory.createActivityFromCoord("home", homeCoord);
                home1.setEndTime(workDeparture);
                plan.addActivity(home1);

                Leg toWork = factory.createLeg("car");
                plan.addLeg(toWork);

                Activity work = factory.createActivityFromCoord("work", workCoord);
                work.setEndTime(homeDeparture);
                plan.addActivity(work);

                Leg toHome = factory.createLeg("car");
                plan.addLeg(toHome);

                Activity home2 = factory.createActivityFromCoord("home", homeCoord);
                plan.addActivity(home2);

                person.addPlan(plan);
                person.setSelectedPlan(plan);
                population.addPerson(person);
            }
        }

        new PopulationWriter(population).write(populationOutputPath);
        System.out.printf("Population written to %s — %d agents%n",
                populationOutputPath, population.getPersons().size());
    }

    private Map<Integer, Geometry> readZones() throws Exception {
        Map<Integer, Geometry> zones = new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("url", Path.of(zonesShapePath).toUri().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(params);

        SimpleFeatureSource source = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
        SimpleFeatureCollection collection = source.getFeatures();

        try (SimpleFeatureIterator iterator = collection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                // The DBF has a "FID" attribute column with values 0–25 matching the OD matrix
                int zoneId = ((Number) feature.getAttribute("FID")).intValue();
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                zones.put(zoneId, geometry);
            }
        }

        dataStore.dispose();
        System.out.printf("Loaded %d zone geometries%n", zones.size());
        return zones;
    }

    private List<int[]> readOdMatrix() throws Exception {
        List<int[]> pairs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(odMatrixPath))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int from = Integer.parseInt(parts[0].trim());
                int to = Integer.parseInt(parts[1].trim());
                int value = Integer.parseInt(parts[2].trim());
                if (value > 0) {
                    pairs.add(new int[]{from, to, value});
                }
            }
        }
        return pairs;
    }

    private Coord sampleCoordInZone(Geometry polygon, Random random) {
        Envelope env = polygon.getEnvelopeInternal();
        double dx = env.getMaxX() - env.getMinX();
        double dy = env.getMaxY() - env.getMinY();

        for (int attempt = 0; attempt < MAX_SAMPLE_ATTEMPTS; attempt++) {
            double x = env.getMinX() + random.nextDouble() * dx;
            double y = env.getMinY() + random.nextDouble() * dy;
            Point candidate = geometryFactory.createPoint(new Coordinate(x, y));
            if (polygon.contains(candidate)) {
                return new Coord(x, y);
            }
        }

        // Fallback: centroid (always within convex zone; acceptable for Voronoi)
        Point centroid = polygon.getCentroid();
        return new Coord(centroid.getX(), centroid.getY());
    }
}
