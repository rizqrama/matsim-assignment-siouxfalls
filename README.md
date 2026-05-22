# MATSim Assignment: Siouxfalls

This repository contains the MATSim assignment for the Siouxfalls simulation from the Agent Based Simulation intensive Spring 2026 Class offered by Hiroshima University, taught by [Pieter J. Fourie](https://github.com/langkoos). In this course, we are now focusing on how to utilize agentic AI in developing agent-based models. Therefore, this repository also contains several `CLAUDE.md` files that provide instructions on how to use the agentic AI models, with the [CLAUDE.md](CLAUDE.md) file being the main instruction file.

---

## Table of Contents

1. [Requirements](#requirements)
2. [Repository Structure](#repository-structure)
3. [Input Files](#input-files-not-tracked-in-git--obtain-separately)
4. [General Workflow](#general-workflow)
5. [Cloning This Repository](#cloning-this-repository)
6. [Running the Simulation](#running-the-simulation)
   - [Using Zed or VS Code](#using-zed-or-vs-code)
   - [Using IntelliJ IDEA](#using-intellij-idea)
7. [Output Files](#output-files)
8. [Report](#report)
9. [Development Log](#development-log)

---

## Requirements

- Java 21
- Maven 3.8+

---

## Repository Structure

```
matsim-assignment-siouxfalls/
├── input/                          # Input data (not tracked — obtain separately)
│   ├── siouxfalls.osm
│   ├── od_matrix.csv
│   └── siouxfalls-voronoi-zones/
├── scenarios/siouxfalls/           # Generated scenario files (network, population, config)
├── src/
│   ├── main/java/org/matsim/siouxfalls/
│   │   ├── RunSimulation.java              # Main entry point
│   │   ├── preprocessing/NetworkPreprocessor.java
│   │   ├── demand/PopulationGenerator.java
│   │   └── config/SiouxFallsConfigBuilder.java
│   └── test/java/org/matsim/siouxfalls/
│       └── demand/PopulationGeneratorTest.java
├── output/                         # Simulation outputs (generated at runtime)
├── docs/
│   ├── sioux-falls-report.md       # Assignment report
│   └── siouxfalls-development-log.md
├── CLAUDE.md                       # AI assistant instructions
└── pom.xml
```

---

## Input Files (not tracked in git — obtain separately)

Place these files in the project root before running:

| File | Path |
|---|---|
| OSM road network | `input/siouxfalls.osm` |
| OD matrix | `input/od_matrix.csv` |
| Voronoi zones (shapefile) | `input/siouxfalls-voronoi-zones/voronoi_zones.shp` (+ `.dbf`, `.prj`, `.shx`, `.cpg`) |

---

## General Workflow

The simulation runs as a four-stage pipeline, all triggered by a single command:

**Stage 1 — [`NetworkPreprocessor.java`](src/main/java/org/matsim/siouxfalls/preprocessing/NetworkPreprocessor.java)**
- INPUT: `input/siouxfalls.osm`
- OUTPUT: `scenarios/siouxfalls/network.xml.gz`

**Stage 2 — [`PopulationGenerator.java`](src/main/java/org/matsim/siouxfalls/demand/PopulationGenerator.java)**
- INPUT: `input/od_matrix.csv`, `input/siouxfalls-voronoi-zones/voronoi_zones.shp`
- OUTPUT: `scenarios/siouxfalls/population.xml.gz`

**Stage 3 — [`SiouxFallsConfigBuilder.java`](src/main/java/org/matsim/siouxfalls/config/SiouxFallsConfigBuilder.java)**
- INPUT: `scenarios/siouxfalls/network.xml.gz`, `scenarios/siouxfalls/population.xml.gz`
- OUTPUT: `scenarios/siouxfalls/config.xml`

**Stage 4 — MATSim Controler** (invoked from [`RunSimulation.java`](src/main/java/org/matsim/siouxfalls/RunSimulation.java))
- INPUT: `scenarios/siouxfalls/config.xml`
- OUTPUT: `output/output_plans.xml.gz`, `output/output_network.xml.gz`, `output/output_events.xml.gz`, `output/scorestats.txt`, `output/modestats.txt`

Stages 1 and 2 are **skipped automatically** if their output files already exist. To force a full re-run from scratch, delete `scenarios/siouxfalls/network.xml.gz` and `scenarios/siouxfalls/population.xml.gz` before running.

---

## Cloning This Repository

```bash
git clone git@github.com:rizqrama/matsim-assignment-siouxfalls.git
cd matsim-assignment-siouxfalls
```

Then place the required input files listed above into the `input/` directory before proceeding.

---

## Running the Simulation

### Using Zed or VS Code

**Step 1 — Open the project**

Open the `matsim-assignment-siouxfalls/` folder in Zed or VS Code (File → Open Folder).

**Step 2 — Verify input files**

Open an integrated terminal (`Ctrl+`` ` in VS Code; `Ctrl+`` ` in Zed) and confirm the required input files are present:

```bash
ls input/siouxfalls.osm
ls input/od_matrix.csv
ls input/siouxfalls-voronoi-zones/voronoi_zones.shp
```

All three must exist before continuing. If any are missing, place them in the correct path first (see [Input Files](#input-files-not-tracked-in-git--obtain-separately)).

**Step 3 — Install dependencies**

Download all Maven dependencies (only needed once):

```bash
mvn dependency:resolve
```

**Step 4 — Run the full pipeline**

```bash
mvn exec:java
```

This triggers all four stages in sequence. You will see log lines like:

```
[Stage 1] Running network preprocessing...
[Stage 2] Running population generation...
[Stage 3] Building config...
[Stage 4] Starting simulation...
```

Stages 1 and 2 print a "skipping" message on subsequent runs if their output files already exist.

**Step 5 — Monitor progress**

MATSim prints one line per iteration during Stage 4, e.g.:
```
... Iteration 1 (10)
... Iteration 2 (10)
```
The full run of 50 iterations takes approximately 5 minutes on a modern laptop.

**Step 6 — Check the output**

When the run finishes, verify the output files exist:

```bash
ls output/output_plans.xml.gz
ls output/output_network.xml.gz
ls output/output_events.xml.gz
ls output/scorestats.txt
ls output/modestats.txt
```

**Step 7 — Run the tests (optional)**

```bash
mvn test
```

Three unit tests cover `PopulationGenerator`. All should pass (`BUILD SUCCESS`).

### Using IntelliJ IDEA

**Step 1 — Open the project**

- Launch IntelliJ IDEA.
- Click **File → Open** and select the `matsim-assignment-siouxfalls/` folder.
- IntelliJ detects the `pom.xml` and imports the Maven project automatically. Wait for indexing to finish (progress bar at the bottom).

**Step 2 — Set the Java SDK**

- Go to **File → Project Structure → Project**.
- Under **SDK**, select **Java 21**. If it is not listed, click **Add SDK → JDK** and point IntelliJ to your Java 21 installation directory.
- Set **Language level** to `21 – ...`.
- Click **OK**.

**Step 3 — Verify input files**

Open a terminal inside IntelliJ (**View → Tool Windows → Terminal**) and confirm the required input files are in place:

```bash
ls input/siouxfalls.osm
ls input/od_matrix.csv
ls input/siouxfalls-voronoi-zones/voronoi_zones.shp
```

All three must exist. If any are missing, place them in the correct path first (see [Input Files](#input-files-not-tracked-in-git--obtain-separately)).

**Step 4 — Load Maven dependencies**

In the **Maven** panel (right side of IntelliJ), click the **Reload All Maven Projects** button (circular arrows icon). This downloads all dependencies declared in `pom.xml`. Wait until the progress bar disappears.

**Step 5 — Run the full pipeline**

Option A — via the source file:
- In the **Project** panel (left side), navigate to:
  `src/main/java/org/matsim/siouxfalls/RunSimulation.java`
- Right-click the file and choose **Run 'RunSimulation.main()'**.
- Alternatively, open the file and click the green **Run** button (▶) in the gutter next to `public static void main`.

Option B — via the Maven panel:
- Open the **Maven** panel → expand **matsim-assignment-siouxfalls → Plugins → exec**.
- Double-click **exec:java**.

Output appears in the **Run** panel at the bottom. You will see:

```
[Stage 1] Running network preprocessing...
[Stage 2] Running population generation...
[Stage 3] Building config...
[Stage 4] Starting simulation...
```

**Step 6 — Monitor progress**

MATSim prints one line per iteration during Stage 4. The full 50-iteration run takes approximately 5 minutes on a modern laptop.

**Step 7 — Check the output**

When the run finishes, open the `output/` folder in the Project panel and confirm these files are present:

```
output/output_plans.xml.gz
output/output_network.xml.gz
output/output_events.xml.gz
output/scorestats.txt
output/modestats.txt
```

**Step 8 — Run the tests (optional)**

- Right-click `src/test/java/org/matsim/siouxfalls/demand/PopulationGeneratorTest.java` and choose **Run 'PopulationGeneratorTest'**.
- All three tests should show a green tick. Alternatively, run all tests via the Maven panel: **Lifecycle → test**.

---

## Output Files

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

## Report

See [`docs/sioux-falls-report.md`](docs/sioux-falls-report.md) for the full assignment report including background, methodology, results, and discussion.

> **Academic integrity reminder**: This report was generated as part of a course exercise in agentic AI-assisted simulation development. If you are a student in the same or a similar course, please do not copy this report. Use it as a reference for understanding the methodology, then write your own analysis based on your own simulation run and results.

---

## Development Log

See [`docs/siouxfalls-development-log.md`](docs/siouxfalls-development-log.md) for a complete record of all design decisions, implementation choices, and known limitations.
