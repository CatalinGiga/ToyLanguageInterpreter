# Toy Language Interpreter

A Java-based toy language interpreter project that provides a simple programming language implementation with a graphical user interface built using JavaFX.

## Project Overview

This project implements a toy programming language interpreter with the following features:
- Custom language syntax parsing and execution
- Graphical User Interface (GUI) built with JavaFX
- Maven-based project structure
- Java 17+ compatibility

## Screenshots

### Main Interface
![Main Interface](screenshots/Screenshot%202025-04-29%20110526.png)

### Program Execution
![Program Execution](screenshots/Screenshot%202025-04-29%20110603.png)

### Program Output
![Program Output](screenshots/Screenshot%202025-04-29%20110629.png)

## Prerequisites

- Java 17 or higher
- Maven 3.8.0 or higher
- JavaFX 23.0.1

## Project Structure

```
src/
├── main/
│   ├── java/          # Java source files
│   └── resources/     # Resource files
target/                # Compiled output
pom.xml               # Maven project configuration
```

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd ToyLanguageInterpreter
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

## Dependencies

The project uses the following main dependencies:
- JavaFX Controls 23.0.1
- JavaFX FXML 23.0.1
- Maven Compiler Plugin 3.8.0
- JavaFX Maven Plugin 0.0.6

## Building and Running

### Building
```bash
mvn clean install
```

### Running
```bash
mvn javafx:run
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For any questions or suggestions, please open an issue in the repository. 