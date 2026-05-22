# Sioux Falls MATSim Simulation — Assignment Report

**Course**: Agent Based Simulation Intensive, Spring 2026  
**Institution**: Hiroshima University  
**Instructor**: Pieter J. Fourie  
**Date**: 2026-05-21  

---

## 1. Background

Sioux Falls, South Dakota is one of the most widely used benchmark networks in transport modelling. Its road network is small enough to allow rapid experimentation, yet large enough to exhibit meaningful congestion and mode-choice dynamics. In the academic literature it appears frequently as a test case for traffic assignment, network design, and equilibrium computation.

This assignment uses MATSim (Multi-Agent Transport Simulation), a Java-based open-source framework for agent-based transport modelling. In MATSim, individual travellers — called agents — are simulated one by one. Each agent has a daily plan (a sequence of activities and trips), and over many simulation iterations, agents learn better plans through a co-evolutionary process: they try new routes and modes, score the outcomes, and gradually keep the plans that work best.

The key insight of agent-based simulation is that traffic congestion and mode choice are **emergent phenomena**. They are not prescribed by the modeller; they arise from the collective behaviour of thousands of individual agents each pursuing their own goals on a shared network.

---

## 2. Objective

The objective of this assignment is to build and run a complete MATSim simulation of peak-hour travel demand in Sioux Falls using:

- A road network derived from OpenStreetMap
- A synthetic population generated from a zone-to-zone origin–destination (OD) matrix
- Car routing on the physical network
- Public transport (PT) modelled as a teleported mode (speed-based, no transit schedule)

The simulation runs for 50 iterations and produces the standard MATSim output files required for analysis.

---

## 3. Input Data

| Input | Format | Source | Description |
|---|---|---|---|
| Road network | OSM XML | `input/siouxfalls.osm` | Raw OpenStreetMap extract for Sioux Falls, SD |
| OD matrix | CSV | `input/od_matrix.csv` | Zone-to-zone peak-hour trip counts, 26 zones |
| Traffic zones | Shapefile | `input/siouxfalls-voronoi-zones/` | Voronoi polygon zones in EPSG:32614 (UTM Zone 14N) |

### OD Matrix Summary

The OD matrix covers 26 traffic analysis zones (numbered 0–25) and contains 614 zone pairs with non-zero demand. The total peak-hour trip volume is **44,311 trips**. This is below the 50,000-agent threshold defined in the scenario specification, so a **100% sample** is used — every trip in the OD matrix corresponds to exactly one agent in the simulation.

| Metric | Value |
|---|---|
| Number of zones | 26 |
| Non-zero OD pairs | 614 |
| Total peak-hour trips | 44,311 |
| Sample factor | 100% |
| Agents in simulation | 44,311 |

### Coordinate Reference System

All spatial data uses **EPSG:32614** (WGS 84 / UTM Zone 14N), confirmed from the shapefile `.prj` file. The OSM network is reprojected from WGS84 (EPSG:4326) to EPSG:32614 during preprocessing.

---

## 4. Workflow and Method

The simulation is implemented as a four-stage pipeline in Java (MATSim 15.0, Java 21), driven by a single entry point (`RunSimulation`). Stages 1 and 2 are skipped on subsequent runs if their output files already exist, allowing fast re-experimentation without re-preprocessing.

```
Stage 1: NetworkPreprocessor
  input/siouxfalls.osm  →  scenarios/siouxfalls/network.xml.gz

Stage 2: PopulationGenerator
  input/od_matrix.csv
  input/siouxfalls-voronoi-zones/voronoi_zones.shp  →  scenarios/siouxfalls/population.xml.gz

Stage 3: SiouxFallsConfigBuilder
  (assembles config programmatically)  →  scenarios/siouxfalls/config.xml

Stage 4: Controler
  scenarios/siouxfalls/config.xml  →  output/
```

### Stage 1 — Network Preprocessing

The raw OSM file is read using `org.matsim.core.utils.io.OsmNetworkReader`. Only road types relevant to urban car routing are retained: `motorway`, `trunk`, `primary`, `secondary`, `tertiary`, `residential`, and `unclassified`. After conversion, `NetworkCleaner` removes all links not connected to the largest weakly connected component, ensuring every agent can reach every destination.

**Result**: 10,352 nodes, 26,645 links. After QSim assignment across 4 parallel threads: approximately 13,330 links are active in the mobility simulation.

### Stage 2 — Population Generation

For each non-zero OD pair, agents are generated with the following plan structure:

```
home activity (end time = work departure)
  → car leg
work activity (end time = return departure)
  → car leg
home activity (open-ended)
```

- **Home coordinate**: sampled uniformly at random within the origin zone's Voronoi polygon (bounding-box rejection sampling; fallback to centroid after 100 failed attempts).
- **Work coordinate**: sampled uniformly at random within the destination zone's Voronoi polygon.
- **Work departure time**: drawn from Uniform(07:00, 08:00).
- **Return home departure time**: drawn from Uniform(16:00, 17:00).
- **Initial leg mode**: `car` for all agents (SubtourModeChoice explores PT during simulation).

Self-loop OD pairs (origin zone = destination zone) are valid: home and work coordinates are sampled independently within the same polygon.

### Stage 3 — Configuration

The MATSim config is assembled programmatically by `SiouxFallsConfigBuilder` and written to `scenarios/siouxfalls/config.xml`. Key parameters:

| Parameter | Value |
|---|---|
| Coordinate system | EPSG:32614 |
| Iterations | 50 |
| Modes in mobility simulation | `car` only |
| PT mode | Teleported, 15 km/h, beeline factor 1.3 |
| Walk mode | Teleported, 1.4 m/s (5 km/h), beeline factor 1.3 |
| Home activity typical duration | 12 hours |
| Work activity typical duration | 9 hours |
| Selector strategy (80%) | SelectExpBeta |
| SubtourModeChoice (10%) | Disabled after iteration 45 |
| ReRoute (10%) | Disabled after iteration 45 |
| Max plans per agent | 5 |

The **SelectExpBeta** selector uses a Boltzmann probability distribution over an agent's stored plans, proportional to their scores. This is the standard MATSim learning mechanism, equivalent to a logit model over experienced utilities.

**SubtourModeChoice** randomly switches all legs within a subtour (a round-trip chain starting and ending at the same activity) to a single mode drawn from `{car, pt}`. Car is designated as chain-based — an agent can only drive away if the car is available at the origin.

**ReRoute** recalculates the shortest-time path for all car legs using current network travel times from the previous iteration.

Innovation strategies (SubtourModeChoice and ReRoute) are **disabled after iteration 45**. From iteration 46 onwards only SelectExpBeta runs, allowing the system to converge to a stable scored equilibrium.

### Stage 4 — Simulation Execution

The MATSim `Controler` runs 50 iterations of the co-evolutionary loop:

1. **Replanning**: 20% of agents receive a new plan (10% SubtourModeChoice, 10% ReRoute)
2. **Mobility simulation (QSim)**: car agents travel the physical network, subject to congestion; PT agents are teleported
3. **Scoring**: each executed plan receives a utility score based on activity duration and travel time
4. **Selection**: agents with multiple stored plans probabilistically choose which plan to execute next iteration

Total wall-clock runtime: **4 minutes 57 seconds** on 8 CPU cores (Apple Silicon).

---

## 5. Output Results

All required output files were produced:

| File | Size | Description |
|---|---|---|
| `output/output_plans.xml.gz` | 16 MB | Final scored plans for all 44,311 agents |
| `output/output_network.xml.gz` | 756 KB | Network used by the simulation |
| `output/output_events.xml.gz` | 40 MB | Complete event stream (every agent action) |
| `output/scorestats.txt` | 4.0 KB | Score statistics per iteration |
| `output/modestats.txt` | 4.0 KB | Mode share per iteration |

### Score Convergence

The executed average score rises steeply in the first 2–3 iterations as agents find substantially better routes, then flattens after approximately iteration 10, indicating the system has reached a near-equilibrium state. The small continuing rise through iterations 10–45 reflects incremental improvements as agents explore alternatives.

| Iteration | Avg Executed Score | Avg Best Score |
|---|---|---|
| 0 | 137.13 | 137.13 |
| 5 | 140.01 | 140.32 |
| 10 | 140.02 | 140.37 |
| 20 | 140.04 | 140.41 |
| 30 | 140.09 | 140.43 |
| 45 (cutoff) | 140.12 | 140.43 |
| 50 (final) | 140.33 | 140.41 |

The gap between avg executed and avg best score reflects plan memory: agents occasionally execute sub-optimal remembered plans during the Boltzmann selection process. This gap narrows after the innovation cutoff as the plan pool stabilises.

### Mode Split Evolution

| Iteration | Car share | PT share |
|---|---|---|
| 0 | 100.0% | 0.0% |
| 1 | 89.9% | 10.1% |
| 5 | 83.6% | 16.4% |
| 10 | 82.2% | 17.8% |
| 20 | 82.0% | 18.0% |
| 30 | 84.5% | 15.5% |
| 45 (cutoff) | 87.0% | 13.0% |
| 50 (final) | 96.5% | 3.5% |

### Travel Distance

Average trip distance was stable throughout the simulation at approximately **3,060–3,115 metres**, consistent with the Sioux Falls spatial scale (urban area roughly 8 × 11 km across). The slight increase after iteration 45 reflects that agents converging to car-only plans travel slightly longer distances than the mixed-mode population in earlier iterations.

---

## 6. Discussion

### Score convergence

The rapid score improvement in iterations 0–3 is expected: agents begin with plans routed on an empty network and quickly adapt to realistic travel times under congestion. The plateau after iteration 10 indicates that the population has largely found its equilibrium — further improvement comes only from the tails of the plan distribution. The final-phase score jump from iteration 45 to 50 (140.12 → 140.33) is caused by the innovation cutoff: once SubtourModeChoice is disabled, agents who had been experimenting with PT switch back to their highest-scored car plans, raising the population average.

### Mode split dynamics

The mode split trajectory reveals an interesting pattern. SubtourModeChoice pushes PT adoption in the first few iterations (reaching ~18% PT by iteration 10), but car share gradually increases again through iterations 10–45. This is because car trips score higher on this network: teleported PT at 15 km/h with a beeline factor of 1.3 results in longer effective PT travel times than car routing on Sioux Falls' mostly uncongested road network. Agents that try PT and score lower eventually return to car through the Boltzmann selection mechanism.

After the innovation cutoff at iteration 45, the population rapidly consolidates on its highest-scored plans. Since car plans dominate the stored plan memories by this point, car share jumps sharply from 87% to 96.5% in a single iteration. The residual 3.5% PT share represents agents whose sampled home–work geometry happened to make PT competitive (e.g., short beeline distances that minimise the 1.3× penalty).

This behaviour is **consistent with an uncalibrated model**. In reality, Sioux Falls would have a different observed mode split driven by socioeconomic factors, transit accessibility, and actual PT service speeds — none of which are captured here. The model correctly demonstrates the MATSim learning dynamics, but its mode split results should not be interpreted as a prediction of real-world behaviour.

### Network quality

`NetworkCleaner` successfully removed disconnected subgraphs, producing a fully connected car network. No agents were reported stuck or forced to abort their trips across all 50 iterations, confirming the network is topologically sound for the given demand.

### Computational performance

50 iterations for 44,311 agents completed in under 5 minutes on a single workstation. Each iteration's QSim phase (the mobility simulation) ran in approximately 3–5 seconds, confirming that Sioux Falls is well within MATSim's small-scenario range. This run could be scaled to a 10% sample with minor code changes (lowering the threshold in `PopulationGenerator`), or run at full scale with additional memory if needed.

---

## 7. Conclusions

A complete end-to-end MATSim simulation pipeline for Sioux Falls was implemented and successfully executed. The following conclusions are drawn:

1. **All required outputs were produced.** The five required files (`output_plans.xml.gz`, `output_network.xml.gz`, `output_events.xml.gz`, `scorestats.txt`, `modestats.txt`) are present and non-empty.

2. **The simulation converged.** Executed average scores stabilised after approximately 10 iterations, indicating that the agent population reached a near-equilibrium state consistent with Wardrop's principle at the individual agent level.

3. **The learning mechanism functions correctly.** SubtourModeChoice introduced meaningful PT exploration, and the innovation cutoff at iteration 45 produced the expected consolidation into highest-scored plans. Zero agents were stuck or aborted.

4. **Teleported PT is not competitive with car on this uncalibrated network.** The 15 km/h default PT speed produces longer effective travel times than car routing in the low-congestion Sioux Falls network. Calibrating PT speed to observed transit performance would be necessary for any policy application.

5. **The pipeline design supports reproducibility.** All parameters are driven by the programmatic config builder. Re-running the simulation (with network and population already generated) requires only `mvn exec:java` and completes in under 5 minutes.
