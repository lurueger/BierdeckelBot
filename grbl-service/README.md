# grbl-service for BierdeckelBot

This service is part of the **BierdeckelBot** project and is designed to steer the CNC machine through its GRBL controller. The service runs on the computer that is directly connected to the CNC machine via a USB cable and is also intended to run on a lab server to provide CNC control endpoints remotely.

To read more about the overall **BierdeckelBot** project, including its background and full system architecture, please refer to the general README in the root folder `bierdeckelbot`.

## Purpose

This service provides an API for controlling the CNC machine by sending G-code instructions to its GRBL controller. It uses a headless version of the Universal Gcode Sender (UGS) to handle the connection and execution of G-code files.

## Installation

### Step 1: CNC Machine Setup
1. Connect the CNC machine to the computer via a USB cable.
2. Ensure that you know the USB port (e.g., `/dev/cu.usbserial-110` on macOS) and the BAUD rate (typically `115200`).

### Step 2: Install Dependencies
1. Install the required Python packages using:
   ```bash
   pip install -r requirements.txt
   ```
2. Set the correct port and BAUD rate for your CNC machine in the `grbl-service.py` file. Update the variables `PORT` and `BAUD_RATE` at the top of the file accordingly.

### Step 3: Run the Service
1. Run the Flask app to expose the endpoints:
   ```bash
   flask run
   ```

## Endpoints Overview

This service offers several endpoints for controlling the CNC machine:

### 1. Home the CNC Machine
- **Endpoint**: `/home`
- **Method**: POST
- **Description**: Moves the CNC machine to its home position.

### 2. Move to Robot Access Position
- **Endpoint**: `/robot`
- **Method**: POST
- **Description**: Moves the CNC machine to a predefined position (for easier robot access).

### 3. Execute G-code from a File
- **Endpoint**: `/executeFile`
- **Method**: POST
- **Description**: Executes a G-code file from the `gcode-files` folder.
- **Body Parameters**:
  - `filename`: The name of the G-code file to execute.

### 4. Execute G-code from Text
- **Endpoint**: `/executeGcode`
- **Method**: POST
- **Description**: Accepts G-code as a text string, creates a temporary file, executes it, and deletes the file afterward.
- **Body Parameters**:
  - `gcode`: The G-code to execute.

### 5. Save G-code as a File
- **Endpoint**: `/saveFile`
- **Method**: POST
- **Description**: Saves G-code text to a file in the `gcode-files` folder.
- **Body Parameters**:
  - `filename`: The name of the file to save.
  - `gcode`: The G-code text to save.

### 6. Delete a G-code File
- **Endpoint**: `/deleteFile`
- **Method**: POST
- **Description**: Deletes a G-code file from the `gcode-files` folder.
- **Body Parameters**:
  - `filename`: The name of the file to delete.

### 7. List Available G-code Files
- **Endpoint**: `/gcodeFiles`
- **Method**: GET
- **Description**: Returns a list of all G-code files available in the `gcode-files` folder.

## Universal Gcode Sender

This service uses a headless CLI version of the Universal Gcode Sender (UGS) to interact with the CNC machine. The UGS is developed by contributors from the open-source community. You can learn more about it [here](https://github.com/winder/Universal-G-Code-Sender/tree/master/ugs-cli).

Universal Gcode Sender is licensed under the GNU General Public License (GPL). A big thank you to its contributors for their work on this valuable tool.
