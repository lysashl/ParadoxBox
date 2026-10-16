# ParadoxBox

ParadoxBox is a JavaFX puzzle game inspired by Sokoban, featuring a recursive game mode inspired by Parabox, where players can manipulate nested worlds.

The project uses Maven and is organized into several components, including the graphical interface, game logic, persistence, path generation, and automatic puzzle solving.

## Features

### Classic Mode
- Move the player around the board.
- Push boxes across the grid.
- Place all boxes on their target positions.

### Recursive Mode (Parabox-inspired)
- Explore nested worlds.
- Interact with boxes containing other worlds.
- Navigate between nested levels.

### Level Editors
- Classic level editor.
- Recursive level editor.
- Editing tools: walls, empty spaces, players, boxes, and targets.

### Additional Features
- Save and load levels.
- Automatic puzzle solver.

## Technologies

- **Language:** Java
- **GUI Framework:** JavaFX
- **Build Tool:** Maven

## Requirements

- Java JDK 21
- Maven 3.x
- A graphical environment compatible with JavaFX

Verify your installation:

```bash
java -version
mvn -version
```

## Installation

Clone the repository:

```bash
git clone https://github.com/lysashl/ParadoxBox.git
cd ParadoxBox
```

## Running the Application

### Option 1: Download the Executable

A packaged Windows version is available [here](https://drive.google.com/file/d/1iNBWc7a3Yv1S2FDXJcRRww5buOv1AVY7/view?usp=drive_link).

1. Download and extract the archive.
2. Open the `ParadoxBox` folder.
3. Double-click `ParadoxBox.exe`.

### Option 2: Using the Provided Scripts

**Windows**

```bat
run.bat
```

**Linux / macOS**

```bash
./run.sh
```

### Option 3: Run with Maven

Compile the project:

```bash
mvn clean compile
```

Run the application:

```bash
mvn javafx:run
```

### Option 4: Run the JAR

Package the application:

```bash
mvn clean package
```

If the generated JAR is configured with the required JavaFX dependencies and entry point, run it with:

```bash
java -jar target/paradoxbox-1.0-SNAPSHOT.jar
```

## Project Structure

```text
ParadoxBox/
├── pom.xml
├── README.md
├── run.sh
├── run.bat
└── src/
    └── main/
        ├── java/
        │   ├── ig/                      # Graphical interface
        │   ├── modele/                  # Game logic
        │   ├── plateau/                 # Level editors
        │   ├── persistance/             # Save and load functionality
        │   └── resolutionautomatique/   # Automatic solver
        └── resources/
            ├── ressources/              # Images, sounds, and levels
            ├── plateau/
            └── modele/
```

## Main Class

`ig.game`

## Build and Deployment

Maven is used to manage dependencies, compile the project, and run the application. The repository includes scripts for Windows and Linux/macOS.

## Academic Project

Developed as part of a university team project at Université Sorbonne Paris Nord.
