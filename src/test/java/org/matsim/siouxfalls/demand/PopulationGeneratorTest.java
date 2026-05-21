package org.matsim.siouxfalls.demand;

import org.junit.jupiter.api.Test;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PopulationGeneratorTest {

    private static final String OD_MATRIX = "input/od_matrix.csv";
    private static final String ZONES     = "input/siouxfalls-voronoi-zones/voronoi_zones.shp";

    @Test
    void agentCountMatchesOdMatrixTotal() throws Exception {
        Path outputPath = Files.createTempFile("population_test_", ".xml.gz");
        outputPath.toFile().deleteOnExit();

        new PopulationGenerator(OD_MATRIX, ZONES, outputPath.toString()).run();

        var scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(outputPath.toString());
        Population population = scenario.getPopulation();

        // 44,311 total OD trips at 100% sample
        assertEquals(44311, population.getPersons().size());
    }

    @Test
    void eachAgentHasHomeWorkHomePlan() throws Exception {
        Path outputPath = Files.createTempFile("population_plan_test_", ".xml.gz");
        outputPath.toFile().deleteOnExit();

        new PopulationGenerator(OD_MATRIX, ZONES, outputPath.toString()).run();

        var scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(outputPath.toString());

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            assertNotNull(plan, "Every person must have a selected plan");

            List<PlanElement> elements = plan.getPlanElements();
            // Structure: home, leg, work, leg, home → 5 elements
            assertEquals(5, elements.size(), "Plan must have 5 elements");

            Activity home1 = (Activity) elements.get(0);
            Leg toWork     = (Leg)      elements.get(1);
            Activity work  = (Activity) elements.get(2);
            Leg toHome     = (Leg)      elements.get(3);
            Activity home2 = (Activity) elements.get(4);

            assertEquals("home", home1.getType());
            assertEquals("work", work.getType());
            assertEquals("home", home2.getType());
            assertEquals("car", toWork.getMode());
            assertEquals("car", toHome.getMode());
        }
    }

    @Test
    void departureTimesAreWithinSpecifiedWindows() throws Exception {
        Path outputPath = Files.createTempFile("population_times_test_", ".xml.gz");
        outputPath.toFile().deleteOnExit();

        new PopulationGenerator(OD_MATRIX, ZONES, outputPath.toString()).run();

        var scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        new PopulationReader(scenario).readFile(outputPath.toString());

        for (Person person : scenario.getPopulation().getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            Activity home1 = (Activity) plan.getPlanElements().get(0);
            Activity work  = (Activity) plan.getPlanElements().get(2);

            double workDeparture = home1.getEndTime().seconds();
            double homeDeparture = work.getEndTime().seconds();

            assertTrue(workDeparture >= 7 * 3600.0 && workDeparture < 8 * 3600.0,
                    "Work departure must be within 07:00–08:00");
            assertTrue(homeDeparture >= 16 * 3600.0 && homeDeparture < 17 * 3600.0,
                    "Home departure must be within 16:00–17:00");
        }
    }
}
