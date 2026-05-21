# ENVIRONMENT.md

## Development Environment

| Item | Value |
|------|-------|
| OS | macOS (Apple Silicon, aarch64) |
| Java | OpenJDK 21 (Homebrew) |
| Maven | 3.9.x |
| MATSim | 15.0 |
| IDE | Zed or VS Code |
| Shell | zsh |

## Project Layout

```shell
matsim-assignment-siouxfalls/
 |-pom.xml
 |-CLAUDE.md
 |-ENVIRONMENT.md
 |-src/main/java/org/matsim/siouxfalls/
 |-src/test/java/org/matsim/siouxfalls/
 |-input/
  |-siouxfalls.osm
  |-od_matrix.csv
  |-siouxfalls-voronoi-zones/
 |-output/
 |-docs/
 |-scenarios/
  |-siouxfalls/
 |-.claude/commands/
```

## Maven Commands

```bash
# Compile
mvn compile

# Run simulation
mvn exec:java -Dexec.mainClass="org.matsim.siouxfalls.RunSimulation"

# Run tests
mvn test
```
