# MATSim Assignment: Siouxfalls

This repository contains the MATSim assignment for the Siouxfalls simulation from the Agent Based Simulation intensive Spring 2026 Class offered by Hiroshima University, taught by [Pieter J. Fourie](https://github.com/langkoos). In this course, we are now focusing on how to utilize agentic AI in developing agent-based models. Therefore, this repository also contains several `CLAUDE.md` files that provide instructions on how to use the agentic AI models, with the [CLAUDE.md](CLAUDE.md) file being the main instruction file.

---

## Requirements

- Java 21
- Maven 3.8+

---

## Input files (not tracked in git — obtain separately)

Place these files in the project root before running:

| File | Path |
|---|---|
| OSM road network | `input/siouxfalls.osm` |
| OD matrix | `input/od_matrix.csv` |
| Voronoi zones (shapefile) | `input/siouxfalls-voronoi-zones/voronoi_zones.shp` (+ `.dbf`, `.prj`, `.shx`, `.cpg`) |

---

## Running the simulation

```bash
mvn exec:java
```

This runs the full four-stage pipeline:

1. **Network preprocessing** — converts `input/siouxfalls.osm` to `scenarios/siouxfalls/network.xml.gz`
2. **Population generation** — converts the OD matrix + Voronoi zones to `scenarios/siouxfalls/population.xml.gz`
3. **Config assembly** — writes `scenarios/siouxfalls/config.xml`
4. **Simulation** — runs 50 iterations via the MATSim Controler, writes results to `output/`

Stages 1 and 2 are **skipped automatically** if their output files already exist. To force a full re-run from scratch, delete `scenarios/siouxfalls/network.xml.gz` and `scenarios/siouxfalls/population.xml.gz` before running.

---

## Output files

After a successful run, the following files will be in `output/`:

| File | Description |
|---|---|
| `output_plans.xml.gz` | Final scored plans for all agents |
| `output_network.xml.gz` | Network used by the simulation |
| `output_events.xml.gz` | Complete event stream |
| `scorestats.txt` | Score statistics per iteration |
| `modestats.txt` | Mode share per iteration |

The `output/ITERS/` subdirectory contains per-iteration diagnostics and should be excluded from any submission zip.

---

## Running the tests

```bash
mvn test
```

Three unit tests cover `PopulationGenerator`: agent count, plan structure, and departure time windows.

---

## Report

See [`docs/sioux-falls-report.md`](docs/sioux-falls-report.md) for the full assignment report including background, methodology, results, and discussion.

## Development log

See [`docs/siouxfalls-development-log.md`](docs/siouxfalls-development-log.md) for a complete record of all design decisions, implementation choices, and known limitations.
