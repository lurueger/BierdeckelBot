from flask import Flask, jsonify, request
from flask_cors import CORS
import subprocess
import os
import json

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# ----------------- Global Variables -----------------
# These execution state variables are used to keep track of whether a CPEE process is connected and if there's a file in the printing queue
execution_state = {
    "connected": False,  # True if connected to a CPEE process, False otherwise
    "printingQueue": "empty"  # 'empty' for an empty queue, otherwise the filename of the file in the queue
}
# Important machine-specific data (e.g. drawing height, feedrate, x-y boundaries, ...) are saved in the config.json and imported here
config_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'config.json')
with open(config_path, 'r') as f:
    config = json.load(f)
# Relevant font size data is stored in the text2gcode-folder and imported here
font_sizes_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'text2gcode', 'fontSizes.json')
with open(font_sizes_path, 'r') as f:
    font_data = json.load(f)
# Path to relevant folders is defined here, in case the folder structure changes in the future
SVG2GCODE_FOLDER = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'svg2gcode')
GCODE_FILES_FOLDER = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'gcode-files')
os.makedirs(GCODE_FILES_FOLDER, exist_ok=True)

# ----------------- Endpoints for G-code generation -----------------
# This endpoint takes a svg file and turns it into gcode, using the svg2gcode-tool from the svg2gcode-folder
@app.route('/svg2gcode', methods=['POST'])
def svg_to_gcode():
    # Ensure that a file was sent in the request
    if 'file' not in request.files:
        return jsonify({"status": "error", "message": "No file part in the request"}), 400
    file = request.files['file']
    if file.filename == '':
        return jsonify({"status": "error", "message": "No selected file"}), 400

    # Save the file temporarily in the svg2gcode folder, because the svg2gcode tool requires a file path
    svg_file_path = os.path.join(SVG2GCODE_FOLDER, file.filename)
    file.save(svg_file_path)

    # Define the output G-code filename, which is the same as the input SVG filename but with a .gcode extension
    output_gcode_file = svg_file_path.replace('.svg', '.gcode')

    # Calculate origin (lower-left-corner of Bierdeckel) and dimensions (x- and y-boundaries) based on configuration
    origin_x = config['lower_left_corner_x'] + 5
    origin_y = config['lower_left_corner_y'] + 5
    origin = f"{origin_x},{origin_y}"
    width = config['upper_right_corner_x'] - config['lower_left_corner_x'] - 10
    height = config['upper_right_corner_y'] - config['lower_left_corner_y'] - 10
    dimensions = f"{width}mm,{height}mm"

    # Build the command for the svg2gcode tool
    command = [
        "cargo", "run", "--release",
        "--", svg_file_path,
        "--on", f"G0 Z{config['z_drawing_height']}",
        "--off", f"G0 Z{config['z_safe_height']}",
        f"--origin={origin}",
        f"--dimensions={dimensions}",
        "-o", output_gcode_file,
        f"--feedrate={config['feedrate']}",
        "--end", "G0 Z-30 \n M2"
    ]

    try:
        # Execute the command inside the svg2gcode directory
        result = subprocess.run(command, capture_output=True, text=True, cwd=SVG2GCODE_FOLDER)

        if result.returncode == 0:
            # Move the generated G-code file to gcode-files
            final_gcode_path = os.path.join("gcode-files", os.path.basename(output_gcode_file))
            os.rename(output_gcode_file, final_gcode_path)
            # Delete the SVG file after moving the G-code
            os.remove(svg_file_path)
            return jsonify({"status": "success", "message": f"G-code generated at {final_gcode_path}"})
        else:
            return jsonify({"status": "error", "message": result.stderr}), 500

    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# This endpoint takes a text and turns it into gcode, using the Romans tool from the text2gcode-folder
# Next to the text, it expects a font size and a filename for the gcode-generation
@app.route('/text2gcode', methods=['POST'])
def text_to_gcode():
    data = request.get_json()
    text = data.get('text')
    fontSize = data.get('fontSize')
    filename = data.get('filename')

    if not filename:
        return jsonify({"status": "error", "message": "Filename is required."}), 400

    # Get the font settings based on the provided fontSize
    if str(fontSize) in font_data:
        font_settings = font_data[str(fontSize)]
        lineHeight = font_settings["lineHeight"]
        lineSpacing = font_settings["lineSpacing"]
        lineScale = font_settings["lineScale"]
    else:
        return jsonify({"status": "error", "message": f"Font size {fontSize} not found"}), 400

    # Relevant settings from the config-file are loaded and used for the G-code generation
    lower_left_corner_x = config['lower_left_corner_x']
    lower_left_corner_y = config['lower_left_corner_y']
    upper_right_corner_x = config['upper_right_corner_x']
    upper_right_corner_y = config['upper_right_corner_y']
    z_drawing_height = config['z_drawing_height']
    z_safe_height = config['z_safe_height']
    feedrate = config['feedrate']

    # Function to call the Java command and get the G-code for a single line
    def generate_gcode_for_line(lineText, offsetX, offsetY, scale, angle=0):
        command = ["java", "Romans", lineText, str(offsetX), str(offsetY), str(scale), str(angle)]
        text2gcode_folder = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'text2gcode')
        result = subprocess.run(command, capture_output=True, text=True, cwd=text2gcode_folder)
        if result.returncode == 0:
            return result.stdout
        else:
            raise Exception(f"Error generating G-code: {result.stderr}")
        
    # Helper function to determine the maximum X value (width) of a line (later used for centering the text)
    def get_maximum_x_for_line(lineText):
        gcode = generate_gcode_for_line(lineText, 0, 0, lineScale)
        max_x = float('-inf')
        for line in gcode.splitlines():
            if "X" in line:
                parts = line.split()
                for part in parts:
                    if part.startswith("X"):
                        x = float(part[1:])
                        max_x = max(max_x, x)
        return max_x
        
    # Step 1: Split the text into lines and calculate the maximum Y value, to later generate y-offsets for each line
    lines = text.split("\n")
    gcode_output = ""
    total_text_height = (len(lines) * lineHeight) + ((len(lines) - 1) * lineSpacing)
    mid_y = (lower_left_corner_y + upper_right_corner_y) / 2
    maximumY = total_text_height / 2 + mid_y

    # Step 2: Iterate through each line and generate the G-code, using the maximum X value for centering the text horizontally
    for i, line in enumerate(lines):
        try:
            offsetY = maximumY - ((i + 1) * lineHeight) - (i * lineSpacing)
            maximumX = get_maximum_x_for_line(line)
            mid_x = (lower_left_corner_x + upper_right_corner_x) / 2
            offsetX = mid_x - (maximumX / 2)
            gcode_for_line = generate_gcode_for_line(line, offsetX, offsetY, lineScale)
            gcode_output += gcode_for_line + "\n"
        except Exception as e:
            return jsonify({"status": "error", "message": f"Failed to generate G-code for line {i+1}: {e}"}), 500

    # Step 3: Process the G-code and perform replacements, so the G-code is compatible with the BierdeckelBot
    processed_gcode = []
    for line in gcode_output.splitlines():
        if "G21" in line:
            continue
        line = line.replace('Z-72.685', f'Z{z_drawing_height}')
        line = line.replace('Z-70', f'Z{z_safe_height}')
        if "G1" in line:
            line += f" F{feedrate}"
        processed_gcode.append(line)
    processed_gcode.insert(0, "G90")
    processed_gcode.insert(1, "G21")
    processed_gcode.append("G0 Z-30")
    processed_gcode.append("M2")

    # Save the final processed G-code to the specified file in gcode-files folder
    output_gcode_file = os.path.join("gcode-files", f"{filename}.gcode")
    with open(output_gcode_file, "w") as f:
        f.write("\n".join(processed_gcode))

    return jsonify({"status": "success", "message": f"G-code generated at {output_gcode_file}"})

# ----------------- Endpoints for communication with the CPEE -----------------
# Endpoint for the CPEE to communicate that it is running and ready to receive a printing job
@app.route('/connect', methods=['POST'])
def connect():
    global execution_state
    execution_state["connected"] = True
    return jsonify({"status": "success", "message": "Connected to the process engine."})

# Endpoint for the CPEE to communicate that it is turning off (not necessary in practice, but included for completeness)
@app.route('/deconnect', methods=['POST'])
def deconnect():
    global execution_state
    execution_state["connected"] = False
    return jsonify({"status": "success", "message": "Disconnected from the process engine."})

# Endpoint to get back the current connection state of the CPEE (used by Frontend to display the connection status)
@app.route('/getConnectionState', methods=['GET'])
def get_comnnection_state():
    global execution_state
    return jsonify({"status": "success", "connected": execution_state["connected"]})


# Endpoint to get the current printing queue, used by both Frontend and CPEE
@app.route('/getPrintingQueue', methods=['GET'])
def get_printing_queue():
    global execution_state
    return jsonify({"status": "success", "printingQueue": execution_state["printingQueue"]})

# Endpoint to set the printingQueue, used by Frontend to communicate printing jobs and by CPEE to communicate that it's finished
# Expected JSON data: { "filename": "example.gcode" }
@app.route('/setPrintingQueue', methods=['POST'])
def set_printing_queue():
    global execution_state
    # Handle form data (application/x-www-form-urlencoded) -> CPEE
    if request.content_type == 'application/x-www-form-urlencoded':
        filename = request.form.get('filename')
    # Handle JSON data (application/json) -> Frontend
    elif request.content_type == 'application/json':
        data = request.get_json()
        filename = data.get('filename')
    else:
        return jsonify({"status": "error", "message": "Unsupported Content-Type."}), 415
    if not filename:
        return jsonify({"status": "error", "message": "Filename is required to set the printing queue."}), 400
    execution_state["printingQueue"] = filename
    return jsonify({"status": "success", "message": f"Printing queue set to {filename}."})


# ----------------- Endpoints for managing the files in gcode-files/ -----------------
#Get back the gcode of a specific file (Attention: This is a POST request)
@app.route('/getFile', methods=['POST'])
def get_file():
    # Handle form data (application/x-www-form-urlencoded) from CPEE
    if request.content_type == 'application/x-www-form-urlencoded':
        filename = request.form.get('filename')
    # Handle JSON data (application/json) from the frontend
    elif request.content_type == 'application/json':
        data = request.get_json()
        filename = data.get('filename')
    if not filename:
        return jsonify({"status": "error", "message": "Filename is required."}), 400

    gcode_file_path = os.path.join("gcode-files", filename)
    if not os.path.exists(gcode_file_path):
        return jsonify({"status": "error", "message": f"File {filename} not found."}), 404
    
    try:
        with open(gcode_file_path, 'r') as file:
            gcode = file.read()
        return jsonify({"status": "success", "content": gcode})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# Endpoint to delete a G-code file from the folder of G-code files
# Expected JSON data: { "filename": "example.gcode" }
@app.route('/deleteFile', methods=['POST'])
def delete_file():
    data = request.get_json()
    filename = data.get('filename')

    if not filename:
        return jsonify({"status": "error", "message": "Filename is required."}), 400

    gcode_file_path = os.path.join(GCODE_FILES_FOLDER, filename)
    if not os.path.exists(gcode_file_path):
        return jsonify({"status": "error", "message": f"File {filename} not found."}), 404

    try:
        os.remove(gcode_file_path)
        return jsonify({"status": "success", "message": f"File {filename} deleted."})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# Endpoint to list all G-code files, used by the frontend to display the available files
@app.route('/gcodeFiles', methods=['GET'])
def get_gcode_files():
    try:
        files = os.listdir(GCODE_FILES_FOLDER)
        gcode_files = sorted([f for f in files if f.endswith('.gcode')])
        return jsonify({"status": "success", "files": gcode_files})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host="0.0.0.0", port=5001)
