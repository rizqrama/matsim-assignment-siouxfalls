# CLAUDE.md — Sioux Falls Scenario Context

## Assignment Specification

**Scenario**: Sioux Falls, South Dakota
**Purpose**: MATSim assignment submission

## Input Files

| File | Path | Format |
|------|------|--------|
| Road network | [project_root]/input/siouxfalls.osm | Raw OSM |
| OD matrix | [project_root]/input/od_matrix.csv | CSV, zone-to-zone peak hour trips |
| Traffic zones | [project_root]/input/siouxfalls-voronoi-zones/ | Shapefile (Voronoi polygons) |

## Simulation Requirements

- **Modes**: car + teleported pt (no transit schedule)
- **Teleported pt config**:
  - `useTransit = false`
  - `usingTransitInMobsim = true`
- **Agent activity pattern**:
  - Depart for work: 7:00–8:00am (uniform random within window)
  - Return home: 4:00–5:00pm (uniform random within window)
- **Iterations**: 50
- **Replanning rate**: 20% total
- **Strategies**:
  - SubtourModeChoice
  - ReRoute
  - Innovation off at iteration 45
- **Sample size**: 100% of OD trips unless population exceeds
  50,000 agents, in which case use 10% sample

## Output Requirements

- Output directory: [project_root]/output/
- Exclude ITERS/ from final zip
- Required output files:
  - output_plans.xml.gz
  - output_network.xml.gz
  - output_events.xml.gz
  - scorestats.txt
  - modestats.txt

## Report

- Location: [project_root]/docs/sioux-falls-report.md
- Sections: Background, Objective, Input Data, Workflow/Method,
  Output Results, Discussion, Conclusions
