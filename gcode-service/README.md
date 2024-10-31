# gcode-service for BierdeckelBot
This service is part of the **BierdeckelBot** project, providing endpoints to create and manage G-code files, along with a frontend for user interaction. For a detailed overview of the **BierdeckelBot** project, including background and architecture, refer to the [main README](/README.md) in the root folder.

### Table of Contents
- [Purpose](#purpose)
- [Folder Structure](#folder-structure)
- [Installation & Running the Service](#installation--running-the-service)
- [Endpoints Overview](#endpoints-overview)
- [High-Level Code Overview](#high-level-code-overview)
- [General Remarks](#general-remarks)
- [Variables in config.json](#variables-in-configjson)
- [Acknowledgment of External Software](#acknowledgment-of-external-software)

## Purpose
The `gcode-service` offers APIs for G-code file creation and a frontend for users to interact with. It’s also integral to the CPEE process, as users initiate printing by selecting files in the frontend. 

Since this service doesn't require hardware connections to other components, it can be hosted on any machine. Since the HTTP has to call the endpoints via HTTP, ensure that this service is exposed to the internet. Also this service can function as a standalone service outside the BierdeckelBot environment: all functionalities for creating and managing G-code files are independent of the CPEE process, requiring the rest of the BierdeckelBot only for printing.

The `gcode-service` uses external programs for conversion tasks, which are later further explained. The code is well-commented, but high-level explanations here will provide a high-level understanding.


## Folder Structure
gcode-service/
- **build/**: Contains the production build of the React frontend.
- **frontend/**: Contains files for frontend development, only relevant if you plan to modify the UI.
- **gcode-files/**: Serves as storage for G-code files and contains two examples.
- **svg2gcode/**: Contains code for SVG conversion and should note be altered.
- **text2gcode/**: Contains code for text conversion and should note be altered.
- **config.json**: Contains essential machine-specific settings (e.g., drawing height). Should be updated if you change any hardware setup.
- **gcode-service.py**: Main Flask service file containing the code discussed here.

In general, you only should / need to interact with `config.json` and `gcode-service.py`.

## Installation & Running the Service

### Step 1: Required Software
1. **Python**: Required for the Flask backend.
2. **Java**: Required for the text-to-Gcode program (`text2gcode/`).
3. **Rust**: Required for the SVG-to-Gcode program(`svg2gcode/`).
4. (**Node.js and npm**: Only required for re-building the React frontend in case you make changes to it.)

Make sure all software is installed and accessible in your environment.

### Step 2: Install Dependencies
1. Clone this repository to your machine.
2. Navigate to the `gcode-service` directory:
```
cd gcode-service
```
3. Install Python dependencies:
```
pip install -r requirements.txt
```
If you don’t intend to modify the frontend, this is sufficient. However, if you need frontend changes, follow these steps:

4. Install Node dependencies for the frontend:
```
cd frontend npm install
```
5. Build the frontend for production:
```
npm run build
```
6. If you follow step 4&5, you also need to move the `build` folder into the `gcode-service` folder (or change the referencing filepath in `gcode-service.py`, lines 12-16)

### Step 3: Running the Service
1. Navigate to the `gcode-service` directory.
2. Start the Flask app to run the service and serve the frontend:
```
flask --app gcode-service.py run
```
3. To specify a different port:
```
flask --app gcode-service.py run --port <desired-port>
```
The service should now be accessible at your chosen port, with the React frontend as the path `/` and API endpoints with their respective paths.
![Screenshot of gcode frontend](/documentation-material/images/gcode-service-frontend.png)

## Endpoints Overview
The `gcode-service` provides endpoints for creating and managing G-code files, as well as communication with the CPEE.

### 1. Creating G-code files
**Endpoint**: `/svg2gcode`
- **Method**: POST
- **Description**: Converts an SVG file into a G-code file, using the CLI tool from `svg2gcode/`.
- **Input**: SVG file via `multipart/form-data`.

**Endpoint**: `/text2gcode`
- **Method**: POST
- **Description**: Converts text into G-code, using the CLI tool from `text2gcode/`.
- **Parameters**:
  - `text`: The text to convert.
  - `fontSize`: Font size to use.
  - `filename`: Name for the resulting G-code file.

### 2. CPEE Connection Management
**Endpoint**: `/connect`
- **Method**: POST
- **Description**: Sets `connected` to `True`, indicating that a CPEE process is active and waiting for printing tasks.
  
**Endpoint**: `/deconnect`
- **Method**: POST
- **Description**: Sets `connected` to `False`. This is not used in the current process, but could be helpful for debugging or further development.

**Endpoint**: `/getConnectionState`
- **Method**: GET
- **Description**: Returns the current connection state, used by the frontend to display status.

### 3. Printing Queue Management
**Endpoint**: `/getPrintingQueue`
- **Method**: GET
- **Description**: Retrieves the `printingQueue` status, showing `empty` or the filename if a print is queued.
  
**Endpoint**: `/setPrintingQueue`
- **Method**: POST
- **Description**: Sets the printing queue with a given filename. Used by the frontend, if the user selects a file for printing, and by the CPEE to empty the queue again.
- **Parameters**:
  - `filename`: File to add to the printing queue.

### 4. File Management
**Endpoint**: `/getFile`
- **Method**: POST
- **Description**: Returns the G-code content of a specified file.
- **Parameters**:
  - `filename`: File to retrieve.

**Endpoint**: `/deleteFile`
- **Method**: POST
- **Description**: Deletes a specified G-code file.
- **Parameters**:
  - `filename`: File to delete.

**Endpoint**: `/gcodeFiles`
- **Method**: GET
- **Description**: Lists all G-code files in the `gcode-files` folder.

## High-Level Code Overview
### Frontend
The frontend is a straight-forward React app using Axios for HTTP requests.
The React app is already built and included as a `build/`-folder.
So if you change anything in `frontend/`, make sure to `npm run build` and move the folder into this directory.

### How SVGs are Converted to G-code
The `svg2gcode` endpoint converts SVG files into G-code with the Rust CLI tool from `svg2gcode/`. Here’s a summary of the process:
1. The SVG file is saved temporarily in `svg2gcode/` as the CLI tool works file-based and requires a filepath.
2. The endpoint loads the configuration settings from `config.json` to scale and position the SVG correctly.
3. A CLI command executes the Rust tool, generating a G-code file.
4. The generated G-code is moved to `gcode-files/`, and the SVG is deleted after processing.

### How Text is Converted to G-code
The `text2gcode` endpoint processes text input to create G-code using the Java CLI tool `text2gode/Romans.java`:

1. Font metrics, like line height and spacing for each fontSize, are loaded from `text2gcode/`.
2. The input text is split by line breaks and preliminary generated as Gcode, centered horizontally on the Bierdeckel.
3. Adjusted G-code for each line is generated based on the Bierdeckel’s mid-point coordinates.
4. Machine-specific configurations are applied in form of G-code commands (e.g. specific start and end parts of the code), and the final G-code is saved to `gcode-files/`.

![Overview of how G-code is generated](/documentation-material/images/gcode-examples.png)

## Variables in config.json
Each variable in `config.json` is in millimeters unless otherwise stated:
- `lower_left_corner_x`/`lower_left_corner_y`: Coordinates for the lower left corner of the drawing board.
- `upper_right_corner_x`/`upper_right_corner_y`: Coordinates for the upper right corner of the drawing board.
- `feedrate`: Speed for drawing movements (`G1` commands), current default is `1500`.
- `z_drawing_height`: Z-coordinate for the drawing position; must be recalibrated if the pen or hardware changes (currently `-64.3`).
- `z_safe_height`: Z-height used for non-drawing movements, usually 1mm above the drawing height (currently `-63`).

## General Remarks
1. Be aware that users could theoretically generate a high number of files, filling up the size of `gcode-files`. If deployed publicly, some access protection would make sense.
2. New files with the same name as existing ones will replace them.
3. The `Romans` font tool generates characters in single strokes, instead of outlining the characters like svg2gcode would do. This is also the reason why this exists as a second way of generating g-code in the first place.

## Acknowledgment of External Software
This service relies on two external tools:

1. `svg2gcode`: A Rust CLI program for SVG-to-Gcode conversion. [GitHub Repo](https://github.com/sameer/svg2gcode?tab=readme-ov-file).
2. `Romans`: A Java CLI program for generating G-code from text input. [GitHub Repo](https://github.com/misan/gcodeFont/blob/master/Romans.java).

These tools have been essential to this project. Many thanks to the contributors!


