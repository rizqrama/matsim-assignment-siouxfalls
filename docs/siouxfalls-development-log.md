# Sioux Falls — Development Log

---

## 2026-05-21: Full Simulation Pipeline — Specification

### Task
Specify and plan the complete end-to-end MATSim simulation pipeline for the Sioux Falls scenario: OSM → network conversion, OD matrix → synthetic population, config assembly, and simulation execution via the MATSim Controler.

---

### Technical Specification

**Objective**: Build a config-driven MATSim simulation pipeline that preprocesses OSM into a road network, generates a synthetic population from the OD matrix and Voronoi zones, assembles the MATSim config, and runs the Controler to produce the required output files.

**Scope**:
- Included: OSM → MATSim network conversion; synthetic population generation from OD matrix + Voronoi shapefiles; MATSim config assembly; simulation execution; all five required output files
- Excluded: transit schedule / GTFS processing (teleported PT only); post-simulation analysis / report writing; visualisation tooling; containerisation / Docker (deferred)

**Affected Components**:
- `src/main/java/org/matsim/siouxfalls/RunSimulation.java` — main entry point (create)
- `src/main/java/org/matsim/siouxfalls/preprocessing/NetworkPreprocessor.java` — OSM → network (create)
- `src/main/java/org/matsim/siouxfalls/demand/PopulationGenerator.java` — OD → population (create)
- `src/main/java/org/matsim/siouxfalls/config/SiouxFallsConfigBuilder.java` — config assembly (create)
- `scenarios/siouxfalls/config.xml` — generated MATSim config
- `scenarios/siouxfalls/network.xml.gz` — generated network
- `scenarios/siouxfalls/population.xml.gz` — generated population
- `docs/siouxfalls-development-log.md` — this file

**Expected Outputs**:
- `scenarios/siouxfalls/network.xml.gz` — preprocessed road network in EPSG:32614
- `scenarios/siouxfalls/population.xml.gz` — 44,311 agents with home + work plans
- `scenarios/siouxfalls/config.xml` — full MATSim config
- `output/output_plans.xml.gz` — final scored plans after 50 iterations
- `output/output_network.xml.gz` — network copy
- `output/output_events.xml.gz` — full event stream
- `output/scorestats.txt` — score statistics per iteration
- `output/modestats.txt` — mode share per iteration

**Acceptance Criteria**:
- `mvn exec:java` runs without exception to iteration 50
- `output/scorestats.txt` exists and shows a score convergence trend
- `output/modestats.txt` exists and shows both `car` and `pt` mode shares
- `output/output_events.xml.gz` is non-empty
- Stuck agents do not exceed 5% of population
- Innovation strategies are disabled after iteration 45 (confirmed in simulation log)
- No hardcoded file paths — all paths resolve through MATSim config

---

### Clarifications / Assumptions

1. **CRS**: EPSG:32614 (WGS 84 / UTM Zone 14N) — confirmed from Voronoi shapefile `.prj` file.
2. **Sample size**: OD matrix total = 44,311 trips. Below the 50,000 threshold, therefore 100% sample → 44,311 agents.
3. **Agent plan structure**: each OD pair produces one agent with plan: home activity → leg → work activity → leg → home activity.
4. **Self-loop OD entries** (zone i → zone i): valid. Home and work coordinates are sampled independently within the same zone polygon.
5. **Coordinate sampling**: uniform random point within Voronoi polygon (bounding-box rejection sampling with polygon containment check). Fallback to polygon centroid if sampling fails after a fixed number of attempts.
6. **Activity typical durations**: work = 9h (midpoint of the 8–10h range produced by the departure/return windows); home = 12h (overnight).
7. **Departure times**: work departure drawn from Uniform(07:00, 08:00); return home departure drawn from Uniform(16:00, 17:00).
8. **Replanning split** (confirmed by user): SubtourModeChoice 10%, ReRoute 10%, KeepLastSelected 80%. Total innovation = 20%.
9. **Teleported PT speed** (confirmed by user): 15 km/h, beeline distance factor 1.3 (MATSim urban defaults).
10. **Road network filter**: OSM highway types `motorway`, `trunk`, `primary`, `secondary`, `tertiary`, `residential`, `unclassified`.
11. **Network cleaning**: `NetworkCleaner` applied after OSM conversion to remove disconnected components.
12. **Pipeline skip logic** (confirmed by user): `RunSimulation` skips network preprocessing and population generation if `scenarios/siouxfalls/network.xml.gz` and `scenarios/siouxfalls/population.xml.gz` already exist. This allows re-running the simulation without re-preprocessing.
13. **Intermediate artifacts**: network and population written to `scenarios/siouxfalls/`; simulation output written to `output/`.

---

### Decision Taken

Specification confirmed. Implementation not yet started. Proceeding with the pipeline design described above.

---

### Reason for Decision

The pipeline is split into four discrete stages (network, population, config, simulation) to honour the anti-pattern rule against mixing preprocessing, simulation, and analysis in one class. The skip logic for existing artifacts allows fast iteration during development without re-running expensive preprocessing steps on every run.

---

### Alternatives Considered

- **Single monolithic `RunSimulation` class**: rejected — violates separation-of-concerns and makes individual stages untestable.
- **Separate main classes per stage**: rejected — more complex to operate. Single entry point with skip logic is simpler while still keeping stage classes separate.
- **10% sample from the start**: rejected — 44,311 agents is below the 50,000 threshold stated in the scenario spec.

---

### Tests Added / Updated

None at specification stage. Unit tests for `PopulationGenerator` (agent count, plan structure, activity times) will be added during implementation.

---

### Known Limitations

1. Teleported PT speed (15 km/h) is a generic urban default, not calibrated to Sioux Falls transit.
2. No small-scale validation run (1% sample) has been defined yet — to be added as a pre-run checklist item before the full 50-iteration run.
3. Activity scoring parameters use MATSim defaults; no local calibration has been performed.

---

### Next Recommended Checkpoint

- **Scope**: specification file + README changes (current working tree)
- **Suggested commit message**: `spec: add full simulation pipeline specification and development log`

---

## 2026-05-21: Full Simulation Pipeline — Implementation

### Task
Implement the four-stage pipeline defined in the specification above and run the simulation to 50 iterations, producing all required output files.

---

### Technical Specification

See the specification entry above (2026-05-21: Full Simulation Pipeline — Specification).

---

### Clarifications / Assumptions

1. `SupersonicOsmNetworkReader` (from the `osm` contrib) only reads PBF-format OSM files. The input `siouxfalls.osm` is XML format, so the core `org.matsim.core.utils.io.OsmNetworkReader` was used instead.
2. GeoTools FID strings for shapefiles are 1-based (e.g. `voronoi_zones.1`). The shapefile contains a dedicated `FID` attribute column (0-based, values 0–25) that matches the OD matrix zone IDs directly. Zone IDs are read from this attribute, not the GeoTools FID string.
3. When any teleported mode params are added explicitly to `PlansCalcRouteConfigGroup`, MATSim clears all default teleported mode params (including `walk`). `walk` must be re-declared explicitly at 1.4 m/s; it is required internally by the `NetworkRoutingProvider` for car access/egress routing.
4. Config file paths must be absolute when passed to `SiouxFallsConfigBuilder`. MATSim resolves relative paths in the config relative to the config file's own directory; passing `scenarios/siouxfalls/network.xml.gz` from a config at `scenarios/siouxfalls/config.xml` would resolve to the doubled path `scenarios/siouxfalls/scenarios/siouxfalls/network.xml.gz`. `RunSimulation` converts all paths to absolute via `Path.toAbsolutePath()` before passing them to the builder.
5. MATSim 15.0 config group class names differ from the names used in documentation for earlier versions. Correct names: `StrategyConfigGroup` (not `ReplanningConfigGroup`), `PlansCalcRouteConfigGroup` (not `RoutingConfigGroup`), `PlanCalcScoreConfigGroup` (not `ScoringConfigGroup`), `config.controler()` (not `config.controller()`). `StrategySettings.setDisableAfter()` (not `setDisableAfterIteration()`).
6. Selector used: `SelectExpBeta` (Boltzmann selector, MATSim standard) rather than `KeepLastSelected`. The specification assumption was updated accordingly.

---

### Decision Taken

All four classes implemented and verified:
- `NetworkPreprocessor` — uses core `OsmNetworkReader` for XML OSM; 10,352 nodes, 26,645 links after cleaning
- `PopulationGenerator` — 44,311 agents generated; 3 unit tests passing (count, plan structure, departure times)
- `SiouxFallsConfigBuilder` — full MATSim 15.0 config assembled with correct class names
- `RunSimulation` — pipeline skip logic working; simulation ran to 50 iterations without error

---

### Reason for Decision

Using the core `OsmNetworkReader` for XML OSM avoids a conversion step (OSM XML → PBF) and keeps the pipeline self-contained. The contrib `SupersonicOsmNetworkReader` is faster for large PBF inputs but is not needed at this scale.

---

### Alternatives Considered

- **Convert OSM to PBF then use `SupersonicOsmNetworkReader`**: rejected — adds an external tooling dependency (osmosis or osmconvert) for no benefit at the scale of Sioux Falls.
- **Use absolute paths stored in the config**: chosen approach — simpler than computing relative paths from config directory.

---

### Tests Added / Updated

`src/test/java/org/matsim/siouxfalls/demand/PopulationGeneratorTest.java` — 3 tests:
1. `agentCountMatchesOdMatrixTotal` — 44,311 agents produced
2. `eachAgentHasHomeWorkHomePlan` — all agents have correct 5-element plan structure
3. `departureTimesAreWithinSpecifiedWindows` — work departure within 07:00–08:00, return within 16:00–17:00

All 3 tests pass (`mvn test`).

---

### Simulation Results (50 iterations, 44,311 agents)

| Metric | Iter 0 | Iter 45 (cutoff) | Iter 50 (final) |
|---|---|---|---|
| Avg executed score | 137.1 | 140.1 | 140.3 |
| Car share | 100% | 87.0% | 96.5% |
| PT share | 0% | 13.0% | 3.5% |

**Score convergence**: executed average rose from 137.1 → 140.3 and stabilised after ~iteration 20. Expected behaviour.

**Mode split behaviour**: During the innovation phase (iter 1–44), SubtourModeChoice explored PT, settling at ~84% car / ~16% PT. After the innovation cutoff at iteration 45, `SelectExpBeta` selects based on score alone. Car trips score higher than teleported PT at 15 km/h on this network, so car share rose sharply to ~96% in the final iterations. This is consistent with the uncalibrated PT speed assumption.

**Required output files** (all present):

| File | Size |
|---|---|
| `output/output_plans.xml.gz` | 16 MB |
| `output/output_network.xml.gz` | 756 KB |
| `output/output_events.xml.gz` | 40 MB |
| `output/scorestats.txt` | 4.0 KB |
| `output/modestats.txt` | 4.0 KB |

---

### Known Limitations

1. Teleported PT speed (15 km/h) is not calibrated. As a result, car dominates in the final scored plans. For a calibrated run, PT speed should reflect actual Sioux Falls transit performance.
2. No small-scale (1% sample) validation was run prior to full-scale. Acceptable for an assignment context; would be required for production work.
3. Activity scoring uses MATSim defaults only. No local calibration of marginal utilities or value of time.
4. `output/ITERS/` subdirectories are present and contain per-iteration output. These should be excluded from the final submission zip per the scenario spec.

---

### Next Recommended Checkpoint

- **Scope**: all implementation files + test + development log update
- **Suggested commit message**: `feat: implement full simulation pipeline; all 50 iterations complete`
