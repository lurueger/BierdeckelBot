from flask import Flask, jsonify, request
import tempfile
import subprocess
import os
import threading
import time

app = Flask(__name__)

# Port Configuration: These variables are used to connect to the CNC machine.
# Please change this accordingly after connecting the CNC machine via USB to your machine
PORT = "cu.usbserial-110"  # Replace with the port name of the USB slot, which the CNC machine is connected to
BAUD_RATE = "115200" # Replace if necessary, however most systems should be using 115200 as their default value
# This state is used to log whether a G-code execution is currently running (False by default)
execution_state = {"running": False}

# grbl-service uses the headless version of UniversalGcodeSender to send gcode-files to the GRBL of the CNC machine
# This functionality is extracted in this function, which expects a path to a gcode file and then sends that file to the GRBL 
# The method returns a dictionary containing the status and message
def send_gcode_file_to_grbl(gcode_file): #formerly send_gcode_file
    try:
        # Command to send G-code to the CNC machine
        command = [
            "java", "-cp", "UniversalGcodeSender.jar",
            "com.willwinder.ugs.cli.TerminalClient",
            "--controller", "GRBL",
            "--port", PORT, # You can change this variable in line 12
            "--baud", BAUD_RATE, # You can change this variable in line 13
            "--print-stream",
            "--file", gcode_file
        ]

        # Run the command and capture output
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode == 0:
            return {"status": "success", "message": f"G-code executed successfully from {gcode_file}."}
        else:
            return {"status": "error", "message": result.stderr}

    except Exception as e:
        return {"status": "error", "message": str(e)} 

# Executing regular gcode files should happen asynchronously, as they can take a long time to execute.
# This function is used for making a temporary gcode-file (since the UniversalGcodeSender requires a file) sending it to the GRBL in a new thread
def execute_gcode_async(gcode_text):
    global execution_state
    execution_state["running"] = True
    response = {}
    try:
        # Write G-code content to a temporary file in the main service directory
        with tempfile.NamedTemporaryFile(delete=False, suffix=".gcode", dir=".") as temp_file:
            temp_file.write(gcode_text.encode('utf-8'))
            temp_file_name = temp_file.name
        # Execute the G-code file
        response = send_gcode_file_to_grbl(temp_file_name)
    except Exception as e:
        response = {"status": "error", "message": str(e)}
    finally:
        # Ensure the temporary file is deleted after use, regardless of success or failure
        try:
            if os.path.exists(temp_file_name):
                os.remove(temp_file_name)
                response["message"] = response.get("message", "") + " Temporary file deleted after use."
        except Exception as delete_error:
            response["status"] = "error"
            response["message"] += f" Failed to delete temporary file: {str(delete_error)}"
        execution_state["running"] = False
    return response

# Endpoint to check the current status of the G-code execution, to prevent multiple running requests.
@app.route('/status', methods=['GET'])
def status():
    if execution_state["running"]:
        return jsonify({"status": "running"}), 200
    else:
        return jsonify({"status": "available"}), 200

# This endpoint executes Gcode that was passed from the request body, if no other execution is currently running.
# Expected arguments: "gcode", which is the full text of the gcode
# It accepts both application/x-www-form-urlencoded and application/json content types. (Former is used by CPEE)
@app.route('/executeGcode', methods=['POST'])
def execute_gcode():
    global execution_state
    # Reject request if another execution is already running
    if execution_state["running"]:
        return jsonify({"status": "error", "message": "Another G-code execution is already in progress."}), 400
    # Set the state to running right at the start
    execution_state["running"] = True
    # Handle form data (application/x-www-form-urlencoded)
    if request.content_type == 'application/x-www-form-urlencoded':
        gcode_text = request.form.get('gcode')
    # Handle JSON data (application/json)
    elif request.content_type == 'application/json':
        data = request.get_json()
        gcode_text = data.get('gcode')
    else:
        # Reset the state if there's an error and return
        execution_state["running"] = False
        return jsonify({"status": "error", "message": "Unsupported Content-Type."}), 415
    if not gcode_text:
        # Reset the state if the gcode is empty and return
        execution_state["running"] = False
        return jsonify({"status": "error", "message": "G-code text is required."}), 400
    # Start a new thread for G-code execution
    threading.Thread(target=execute_gcode_async, args=(gcode_text,)).start()
    return jsonify({"status": "success", "message": "G-code execution started asynchronously."})

# Endpoint to home the CNC machine (remains synchronous, as the time is always short and can be waited for)
@app.route('/home', methods=['POST'])
def home():
    global execution_state
    if execution_state["running"]:
        return jsonify({"status": "error", "message": "Another operation is in progress."}), 400
    execution_state["running"] = True
    try:
        # Command to send homing command to the CNC machine
        command = [
            "java", "-cp", "UniversalGcodeSender.jar",
            "com.willwinder.ugs.cli.TerminalClient",
            "--controller", "GRBL",
            "--port", PORT, # You can change this variable in line 12
            "--baud", BAUD_RATE, # You can change this variable in line 13
            "--home"  # This triggers the homing command
        ]
        # Run the command and capture output
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode == 0:
            return jsonify({"status": "success", "output": result.stdout})
        else:
            return jsonify({"status": "error", "output": result.stderr}), 500

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500
    finally:
        execution_state["running"] = False  # Reset state after execution completes

# This endpoint moves the y-axis of the CNC-machine to a position that provides best access for robot movements
# The gcode for this movement is in the file "robot-position.gcode", which is sent to the GRBL
# This endpoint is synchronous, as the movement is short and can be waited for
@app.route('/robot', methods=['POST'])
def robot():
    global execution_state
    if execution_state["running"]:
        return jsonify({"status": "error", "message": "Another operation is in progress."}), 400

    execution_state["running"] = True  # Set state to running
    try:
        gcode_file = "robot-position.gcode"
        response = send_gcode_file_to_grbl(gcode_file)
        return jsonify(response)
    finally:
        execution_state["running"] = False  # Reset state after execution

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0")