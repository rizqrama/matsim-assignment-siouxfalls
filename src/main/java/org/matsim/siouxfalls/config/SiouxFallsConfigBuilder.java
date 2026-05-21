package org.matsim.siouxfalls.config;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.SubtourModeChoiceConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Assembles the MATSim config for the Sioux Falls scenario and writes it to disk.
 * All parameter values are derived from the scenario specification — do not edit
 * these programmatically; update the spec and rebuild.
 */
public class SiouxFallsConfigBuilder {

    private static final int LAST_ITERATION = 50;
    private static final int INNOVATION_CUTOFF = 45;
    private static final double PT_SPEED_MS = 15.0 / 3.6;   // 15 km/h → m/s
    private static final double PT_BEELINE_FACTOR = 1.3;
    private static final double TYPICAL_DURATION_WORK = 9 * 3600.0;
    private static final double TYPICAL_DURATION_HOME = 12 * 3600.0;

    public static void build(String networkPath, String populationPath,
                             String outputDir, String configOutputPath) throws Exception {

        Files.createDirectories(Path.of(configOutputPath).getParent());

        Config config = ConfigUtils.createConfig();

        // ── Global ──────────────────────────────────────────────────────────
        config.global().setCoordinateSystem("EPSG:32614");
        config.global().setNumberOfThreads(Runtime.getRuntime().availableProcessors());

        // ── Network ─────────────────────────────────────────────────────────
        config.network().setInputFile(networkPath);

        // ── Plans ───────────────────────────────────────────────────────────
        config.plans().setInputFile(populationPath);

        // ── Controller ──────────────────────────────────────────────────────
        config.controler().setOutputDirectory(outputDir);
        config.controler().setLastIteration(LAST_ITERATION);
        config.controler().setWriteEventsInterval(LAST_ITERATION);
        config.controler().setWritePlansInterval(LAST_ITERATION);
        config.controler().setOverwriteFileSetting(
                OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        // ── QSim ────────────────────────────────────────────────────────────
        config.qsim().setStartTime(0);
        config.qsim().setEndTime(30 * 3600.0);
        config.qsim().setMainModes(List.of("car"));
        config.qsim().setNumberOfThreads(Runtime.getRuntime().availableProcessors());

        // ── Routing ─────────────────────────────────────────────────────────
        PlansCalcRouteConfigGroup routing = config.plansCalcRoute();
        routing.setNetworkModes(List.of("car"));

        // MATSim clears default teleported params when the first explicit entry is added,
        // so walk must be re-declared (used for car access/egress routing internally).
        PlansCalcRouteConfigGroup.TeleportedModeParams walkRouting =
                new PlansCalcRouteConfigGroup.TeleportedModeParams("walk");
        walkRouting.setTeleportedModeSpeed(1.4);   // ~5 km/h
        walkRouting.setBeelineDistanceFactor(1.3);
        routing.addTeleportedModeParams(walkRouting);

        PlansCalcRouteConfigGroup.TeleportedModeParams ptRouting =
                new PlansCalcRouteConfigGroup.TeleportedModeParams("pt");
        ptRouting.setTeleportedModeSpeed(PT_SPEED_MS);
        ptRouting.setBeelineDistanceFactor(PT_BEELINE_FACTOR);
        routing.addTeleportedModeParams(ptRouting);

        // ── Scoring ─────────────────────────────────────────────────────────
        PlanCalcScoreConfigGroup scoring = config.planCalcScore();

        PlanCalcScoreConfigGroup.ActivityParams homeParams =
                new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeParams.setTypicalDuration(TYPICAL_DURATION_HOME);
        scoring.addActivityParams(homeParams);

        PlanCalcScoreConfigGroup.ActivityParams workParams =
                new PlanCalcScoreConfigGroup.ActivityParams("work");
        workParams.setTypicalDuration(TYPICAL_DURATION_WORK);
        scoring.addActivityParams(workParams);

        // ── Replanning ──────────────────────────────────────────────────────
        StrategyConfigGroup strategy = config.strategy();
        strategy.setMaxAgentPlanMemorySize(5);

        // 80 % — selector (no innovation)
        StrategyConfigGroup.StrategySettings selector = new StrategyConfigGroup.StrategySettings();
        selector.setStrategyName("SelectExpBeta");
        selector.setWeight(0.8);
        strategy.addStrategySettings(selector);

        // 10 % — mode choice (disabled after iteration 45)
        StrategyConfigGroup.StrategySettings modeChoice = new StrategyConfigGroup.StrategySettings();
        modeChoice.setStrategyName("SubtourModeChoice");
        modeChoice.setWeight(0.1);
        modeChoice.setDisableAfter(INNOVATION_CUTOFF);
        strategy.addStrategySettings(modeChoice);

        // 10 % — re-route (disabled after iteration 45)
        StrategyConfigGroup.StrategySettings reRoute = new StrategyConfigGroup.StrategySettings();
        reRoute.setStrategyName("ReRoute");
        reRoute.setWeight(0.1);
        reRoute.setDisableAfter(INNOVATION_CUTOFF);
        strategy.addStrategySettings(reRoute);

        // ── SubtourModeChoice ────────────────────────────────────────────────
        SubtourModeChoiceConfigGroup smc =
                ConfigUtils.addOrGetModule(config, SubtourModeChoiceConfigGroup.class);
        smc.setModes(new String[]{"car", "pt"});
        smc.setChainBasedModes(new String[]{"car"});

        // ── Write config ─────────────────────────────────────────────────────
        new ConfigWriter(config).write(configOutputPath);
        System.out.println("Config written to " + configOutputPath);
    }
}
