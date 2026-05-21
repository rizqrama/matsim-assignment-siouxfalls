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
