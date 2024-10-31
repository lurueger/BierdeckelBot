# BierdeckelBot
The **BierdeckelBot** is a process automation for drawing custom content (SVGs or text) onto coasters made of cardboard (known as "Bierdeckel" in Germany, commonly used to place beers or other drinks on).
It connects a process engine (**CPEE**), a robot arm, a CNC machine customized with 3D-printed parts, and a frontend that users can interact with to generate their custom Bierdeckel. The BierdeckelBot can draw any SVG or text provided by users.

The BierdeckelBot was created by me, **Lukas Rüger**, as part of the Practical Course "Sustainable Process Automation" at **TUM**. The goal of this repository is to document the course results and provide instructions to re-create and use the BierdeckelBot.

# Demo Video & Bierdeckel Examples
This video shows the full process:
https://github.com/user-attachments/assets/cfdea8d6-ebd7-4296-8547-ba164cf937f5

Here are some examples for finished Bierdeckel:
![Image of the pikachu Bierdeckel](/documentation-material/images/bierdeckel-pikachu-example.jpeg)
![Images of printed Bierdeckel](/documentation-material/images/bierdeckel-examples.jpeg)

# Contents of this documentation
This README provides general documentation for the BierdeckelBot, focusing on the overall process and components.
Please note that the software components have their own READMEs, where technical details (setup, endpoints, etc.) are explained in detail. ([gcode-service](/gcode-service/README.md) and [grbl-service](/grbl-service/README.md)) 
- [Demo Video & Bierdeckel Examples](#demo-video--bierdeckel-examples)
- [Architecture and Components](#architecture-and-components)
  - [Overview of Components](#overview-of-components)
  - [Hardware Components](#hardware-components)
  - [Software Components](#software-components)
- [Process](#process)
  - [Step 0: Starting the process](#step-0-starting-the-process)
  - [Step 1: User creates a gcode](#step-1-user-creates-a-gcode)
  - [Step 2: Robot puts the Bierdeckel into CNC](#step-2-robot-puts-the-bierdeckel-into-cnc)
  - [Step 3: CNC executes the Gcode, drawing on the Bierdeckel](#step-3-cnc-executes-the-gcode-drawing-on-the-bierdeckel)
  - [Step 4: Robot takes the Bierdeckel out](#step-4-robot-takes-the-bierdeckel-out)
- [General Remarks & Tips](#general-remarks--tips)
- [(First) Setup of the CNC Machine](#first-setup-of-the-cnc-machine)
  - [Install the 3d-Parts](#install-the-3d-parts)
  - [Calibrate the z-axis in the config file](#calibrate-the-z-axis-in-the-config-file)
- [Contact](#contact)

# Architecture and Components
## Architecture
![Image of Architecture](/documentation-material/images/architecture.png)
The architecture is intended to be distributed into multiple components, which can be re-used independently. Especially the `grbl-service` can be used to remote-control the CNC machine and include it in other similar projects. All components are connected via the `CPEE`, which handles process execution and coordination between the different parts.

## Hardware Components
### CNC Machine & 3D-Printed Parts
The CNC machine used for the BierdeckelBot is **Genmitsu 3020-PRO MAX**, available on [Amazon](https://www.amazon.de/dp/B0DF2NPKJH), which is modified custom 3D-printed parts.
![Image of the CNC machine](/documentation-material/images/cnc-machine.jpg)

### 3D-Printed Parts
![Models of 3d printed parts](/documentation-material/images/3d-printed-parts.png)
- **Pen-Holder**: This part is inserted instead of the CNC's motor and holds an Edding sharpie, which is used for drawing.
- **Drawing-Bed**: Bierdeckel are placed on this drawing bed for the drawing process.
- **Bierdeckel-Holder**: This stores a stack of Bierdeckel and allows for retrieving one Bierdeckel at a time by pushing & pulling the stack on the tray.
- **Weight-Box**: This box contains metal hinges and is placed on top of the bierdeckel-holder to apply the pressure required for the retrieving mechanism. (not shown on above image)

### Robot
The robot arm was already set up in the university laboratory. It has three movement sequences for moving the Bierdeckel and provides endpoints for triggering these movements.

### Other
- **Bierdeckel**: Standardized, square cardboard coasters (93x93mm), available on [Amazon](https://www.amazon.de/dp/B0BQJX9ZZK).
- **Edding**: An Edding sharpie is used for the actual drawing, as the pen holder is customized for the shape of the Edding. The best option is a thin sharpie, such as the Edding 404. Of course other sharpies can be used aswell, but no pens or pencils should be used, as they require movement and/or more pressure for drawing.
- Installing the hardware components requires several M4 and M5 screws, as can be seen later in the Setup part.

## Software Components

### CPEE
The **CPEE** is a process engine that coordinates the process and steers all BierdeckelBot components via HTTP. Read more about the CPEE [here](https://cpee.org/).
This specific CPEE process is available [here](https://cpee.org/flow/edit.html?monitor=https://cpee.org/flow/engine/27320/).

### gcode-service
The **gcode-service** consists of two parts: the backend (Flask service) provides endpoints for G-code creation and file management, while the frontend (React) provides a UI for users to create the G-code and interact with the process. This service can be hosted anywhere, as it interacts solely via HTTP. Further details are specified in the subfolder's [README](/gcode-service/README.md).
![Screenshot of the gcode frontend](/documentation-material/images/gcode-service-frontend.png)

### grbl-service
The **grbl-service** provides endpoints to control the CNC machine, which normally receives commands from a connected computer. The **grbl-service** (Flask service) needs to be hosted on a computer connected to the CNC machine via USB. Further details are specified in the subfolder's [README](/grbl-service/README.md).

**Note**: GRBL is the software on the CNC machine that actually controls its movements. Hence, everything related to CNC steps is named accordingly (grbl-service, grbl endpoints, etc.). In this documentation, GRBL and CNC can be treated as synonyms.

# Process
Here is the full **CPEE** process:
![CPEE process with marked steps](/documentation-material/images/cpee-full-process.png)
We'll now go through the process steps and explain what is happening in each step.

### Step 0: Starting the Process
Before starting the process, all components need to be started first.
- For the software components, please refer to the individual documentation for instructions.
- Regarding the hardware, the `CNC` needs to be switched on (at the back of the machine), and the robot must be enabled in `Remote Mode`. 
- If the hardware setup is changed in any way (e.g., different sharpie, drawing bed in a different location), a config file in the `gcode-service` needs to be updated accordingly. Refer to the [chapter on calibrating the axes](#calibrate-the-z-axis-in-the-config-file) in this case.
- Also, depending on where you host `gcode-service` and `grbl-service`, you need to change the placeholder-adresses in the `CPEE` endpoints!

Then, the **CPEE process** can be started and executes the first instructions:
- The `CPEE` "connects" to the `gcode-service`, setting an execution state in the service that enables the printing buttons in the frontend. 
- After that, the `CPEE` enters an infinite loop, so it can draw multiple different Bierdeckel without requiring new process instances. The idea behind this is that the **BierdeckelBot** can be set up e.g. at a convention where users can visit the frontend with their device, have their custom ideas drawn sequentially. 
- The first part of the loop is a **homing process** of the CNC, which is executed before every drawing to ensure that all axes are calibrated correctly.

### Step 1: User Creates a G-Code *(Video 0:00-0:13)*
The `CPEE` now waits until it receives a printing order from the `gcode-service`. For this, it checks the `printingQueue` of the `gcode-service` every 5 seconds and only proceeds if it contains a filename.

The user visits the frontend in their browser and can create a **G-code** file in two ways:
1. **Text to G-Code**: The user selects a `fontSize` (equivalent to the height of the letters), a `filename`, and writes text to be drawn on the Bierdeckel (e.g., "I like to drink beer!"). The text is centered both vertically and horizontally.
2. **SVG to G-Code**: The user uploads any SVG, which is then converted into **G-code** by the **gcode-service**. It scales and arranges the design to fill the maximum area of the Bierdeckel.

![Examples of Gcodes](/documentation-material/images/gcode-examples.png)
In both cases, the **G-code** file gets stored in the `gcode-service`. The user can see a list of files and select one to delete or print. If the user chooses to print, the filename gets stored in the `printingQueue` execution state.
This is recognized by the `CPEE` which then proceeds by getting the **G-code** from the respective file.

### Step 2: Robot Puts the Bierdeckel into the CNC *(Video 0:14-0:42)*
- Now, the robot arm moves from its home position to the default position, where its head is in front of the CNC machine.
- The **CNC** machine now moves its bed forward to facilitate robot access.
- The robot pushes the Bierdeckel stack to the back, pushing the lowest Bierdeckel out of the holder's front side.
- The robot then pulls the stack back to the front, retrieves a Bierdeckel, and places it on the Drawing Bed.

### Step 3: CNC Executes the G-Code, Drawing on the Bierdeckel (Video 0:43-2:00)
With the Bierdeckel in place, the actual drawing begins. 
- The `CPEE` sends the **G-code**, which was retrieved in Step 1, to the `grbl-service` and triggers its execution. 
- Since drawing can take up to a few minutes and `CPEE` timeouts should be avoided, the drawing process is implemented asynchronously. This means the starting command receives a `200 success` message immediately. 
- The `grbl-service` provides an endpoint to check its status (`running` or `available`), which is checked by the `CPEE` every 5 seconds.

### Step 4: Robot Takes the Bierdeckel Out (Video 2:01-2:23)
- Once the drawing is finished, the `CPEE` breaks the waiting loop and instructs the CNC machine to move its bed forward again for robot access.
- The robot takes the Bierdeckel off the bed and places it in a designated area, where users can retrieve it or where other processes may use it.
- Finally, all `CPEE` variables and the `printingQueue` variable of the `gcode-service` are reset, allowing the loop of the process to start over again.

# General Remarks & Tips

## Physical Constraints
The actual drawing process is highly sensitive to changes in the z-axis. Deviations of ±0.4mm can lead to:
- The sharpie being pressed harder against the cardboard, resulting in a thicker stroke.
- The sharpie not touching the cardboard at all, resulting in no stroke.

For this reason, please be aware of the following:
- The Bierdeckel need to be perfectly flat. If a Bierdeckel is slightly bent, this can lead to the issues mentioned above.
- If you change the sharpie or its tip (e.g., due to wear), recalibrate the z-axis.

## Limitations Regarding SVGs
Please be aware of the following limitations with SVGs:
1. The **BierdeckelBot** only draws the outlines of SVGs and cannot "fill" any areas. Also obviously everything is drawn in the same color.
2. The sharpie strokes are between 1-2 mm thick. SVGs with too many fine lines close together may result in overlapping strokes, so avoid printing overly detailed SVGs.
3. In file conversion, each stroke of the SVG converts to a **G-code** stroke. So if your SVG has e.g. some paths "hidden" for end users behind an object, these unwanted paths will be drawn aswell. So if you create SVGs, don't forget to e.g. union the objects to delete paths within an object.

## How to Create SVGs from Pictures in Inkscape
A good, easy, and free way to create SVGs is the open-source software **Inkscape**.
You can use it to create or edit SVGs and find many tutorials online. For example, the TUM Pikachu was created by pasting a Pikachu SVG and adding the TUM sign as paths.

To export your creation as an SVG, follow these steps:
1. Select all objects, then click **"Edit > Resize Page to Selection"**. This removes empty space around your objects, which could otherwise interfere with the conversion process.
2. Select all objects and click **"Path > Object to Path"**. This transforms, for example, text into SVG paths.
3. Export the file as an SVG.

The **BierdeckelBot** can only print SVGs and not image formats like jpg or png. However, you can use **Inkscape** to convert images into SVGs. For example, this was done to create a drawing of a Loriot comic.
1. Create a blank file and click **"File > Import"**.
2. Choose your image to import and accept the default settings.
3. On the right side, you can adjust the **Threshold** for brightness cutoff (or select other methods like **Autotrace**). Find a setting that reduces the image to as few objects as possible.
4. Follow the export steps from above to save it as an SVG.
Example for such a process:
![Example for Inkscape creation](/documentation-material/images/inkscape-import-example.png)

# (First) Setup of the CNC Machine
This chapter explains how to transform the CNC machine into the BierdeckelBot, using the 3d-printed parts.

## Install the 3D-Printed Parts
1. Print all 3D models with a high-quality 3D printer.
2. Remove the motor and motor holder from the CNC machine.
3. Insert the Edding sharpie into the **Pen-Holder** until it fully touches the hole on top.
4. Insert the pen holder where the motor was. A high-quality printed pen holder should fit perfectly without screws. If it wobbles, use the screws from the CNC motor to secure it on the left and/or right side.
![Image for step 4](/documentation-material/images/install_step-3-4.jpeg)
5. Place the Drawing Board on the left side of the CNC board and secure it with four M5 screws. Tighten all corners equally to ensure a level, even drawing bed.
6. Place the Tray of the Bierdeckel-Holder on the right side of the drawing bed. **Important**: Only secure one side (e.g., the right side) with two M5 screws. Do not secure all corners, as this may slightly bend it and prevent a smooth sliding of the Bierdeckel-stack.
![Image for step 5&6](/documentation-material/images/install_step-5-6.jpeg)
7. Place the Body of the Bierdeckel-Holder on top so it interlocks with the front part of the Plate. Fill it with Bierdeckel and put the Weight-Box on top.
![Image for step 7](/documentation-material/images/install_step-7_box.jpeg)
![Image for step 7x](/documentation-material/images/install_step-7_weight.jpeg)

After these steps, the hardware setup is completed.
It’s crucial that the drawing bed and Bierdeckel holder are screwed into the correct holes to ensure correct x- and y-calibration of the process.

## Calibrate the Z-Axis in the Config File
As stated earlier, calibrating the z-axis is crucial. This means that the drawing height (z-coordinate where the sharpie touches the Bierdeckel) and safety height (z-coordinate where the sharpie can move in x- and y-directions without touching anything) must be exact.

To calibrate, please install a GRBL controller software like [Universal Gcode Sender](https://winder.github.io/ugs_website/) (UGS) and follow these steps:
1. Connect to the CNC machine via USB and establish a connection in UGS.
2. Home the machine and insert a Bierdeckel into the Drawing Bed.
3. Move the sharpie tip above the Bierdeckel and slowly lower it using manual commands.
4. Lower the sharpie tip until it slightly touches the Bierdeckel. Move the x- or y-axis at this height to check the stroke thickness.
5. Repeat Step 4 until you are satisfied with the stroke thickness. This z-coordinate is your drawing height.
6. Update the drawing height in the **config.json** file in **gcode-service**.

# Contact
This project was a lot of fun, and I learned many things—especially since it was my first development project involving so many hardware components.

If you have any questions or inquiries regarding this project, feel free to contact me via e-mail (lukas.rueger(at)tum.de).
