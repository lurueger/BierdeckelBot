import React, { useState, useEffect, useRef, useCallback } from "react";
import axios from "axios";
import "./App.css";

function App() {
  // State for execution variables
  const [connected, setConnected] = useState(false);
  const [printingQueue, setPrintingQueue] = useState("none");
  // States for SVG to Gcode
  const [file, setFile] = useState(null);
  const fileInputRef = useRef(null);
  // States for Text to Gcode
  const [text, setText] = useState("");
  const [fontSize, setFontSize] = useState(11);
  const [filename, setFilename] = useState("");
  const [textMessage, setTextMessage] = useState("");
  const [selectedFile, setSelectedFile] = useState(null);
  // State for Gcode Filemanagement
  const [fileList, setFileList] = useState([]);
  // State for popup messages (Success and error messages)
  const [popupMessage, setPopupMessage] = useState(null);
  const [popupType, setPopupType] = useState("success"); // "success" or "error"
  const [showPopup, setShowPopup] = useState(false);

  // Constraints (chars per line & # of lines) per fontSize
  const fontSizeConstraints = {
    20: { charsPerRow: 4, maxRows: 2 },
    19: { charsPerRow: 4, maxRows: 2 },
    18: { charsPerRow: 4, maxRows: 3 },
    17: { charsPerRow: 4, maxRows: 3 },
    16: { charsPerRow: 5, maxRows: 3 },
    15: { charsPerRow: 5, maxRows: 3 },
    14: { charsPerRow: 6, maxRows: 4 },
    13: { charsPerRow: 6, maxRows: 4 },
    12: { charsPerRow: 6, maxRows: 4 },
    11: { charsPerRow: 7, maxRows: 5 },
    10: { charsPerRow: 8, maxRows: 5 },
    9: { charsPerRow: 9, maxRows: 6 },
    8: { charsPerRow: 10, maxRows: 6 },
  };

  // Show popup message
  const showPopupMessage = (message, type = "success") => {
    setPopupMessage(message);
    setPopupType(type);
    setShowPopup(true);
    setTimeout(() => setShowPopup(false), 3000);
  };
  
  // Functions for Uploadingfiles for Svg to Gcode
  const handleFileChange = (event) => {
    const selectedFile = event.target.files[0];
    setFile(selectedFile);
  };
  const handleFileClick = (file) => {
    setSelectedFile(file === selectedFile ? null : file); // Toggle selection
  };
  const handleDropZoneClick = () => {
    fileInputRef.current && fileInputRef.current.click();
  };
  const handleFileDrop = (event) => {
    event.preventDefault();
    const droppedFile = event.dataTransfer.files[0];
    if (droppedFile?.type === "image/svg+xml") {
      setFile(droppedFile);
      showPopupMessage("SVG file selected", "success");
    } else {
      showPopupMessage("Please upload a valid SVG file.", "error");
      setFile(null);
    }
  };

  // Function for generating Gcode from Svg
  const handleSvgToGcode = async () => {
    if (!file) {
      showPopupMessage("Please drop an SVG file.", "error");
      return;
    }
    const formData = new FormData();
    formData.append("file", file);
    try {
      await axios.post(`/svg2gcode`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      showPopupMessage("Gcode file was successfully created.", "success");
      fetchFileList();
      setFile(null); // Reset file state
    } catch (error) {
      showPopupMessage("Error: " + error.message, "error");
    }
  };

  // Function for printing a file (= putting it into the printing queue)
  const handlePrintFile = async () => {
    if (selectedFile && printingQueue === "empty") {
      await axios.post(`/setPrintingQueue`, {
        filename: selectedFile,
      });
      showPopupMessage(`File "${selectedFile}" was put into the printing queue`, "success");
    }
  };

  // Function for deleting a file from the files-folder
  const handleDeleteFile = async (filename) => {
    try {
      await axios.post(`/deleteFile`, { filename });
      showPopupMessage("File was successfully deleted.", "success");
      fetchFileList();
    } catch (error) {
      showPopupMessage("Error deleting file: " + error.message, "error");
    }
  };

  // Function for Text Validation (Text to Gcode part)  
  const validateText = (text, fontSize) => {
    const constraints = fontSizeConstraints[fontSize];
    if (!constraints) return "Invalid font size.";
    const allowedChars = /^[a-zA-Z0-9\-!?*$/\s]*$/;
    const lines = text.split("\n");
    for (let line of lines) {
      if (!allowedChars.test(line)) {
        return "Invalid character(s) detected. Allowed: a-z, A-Z, 0-9, -, !, ?, *, $, /";
      }
      if (line.length > constraints.charsPerRow) {
        return `Each line can have a maximum of ${constraints.charsPerRow} characters for font size ${fontSize}.`;
      }
    }
    if (lines.length > constraints.maxRows) {
      return `Maximum ${constraints.maxRows} lines allowed for font size ${fontSize}.`;
    }
    return null;
  };

  // Function for handling Text Input (Text to Gcode part)
  const handleTextChange = (e) => {
    const newText = e.target.value;
    setTextMessage(validateText(newText, fontSize) || "");
    setText(newText);
  };

  // Function for generating Gcode from Text (Text to Gcode part)
  const handleTextToGcode = async () => {
    if (!text) {
      showPopupMessage("Please enter text.", "error");
      return;
    }
    try {
      await axios.post(`/text2gcode`, {
        text,
        fontSize,
        filename,
      });
      showPopupMessage("Gcode file was successfully created.", "success");
      setText("");
      setFilename("");
      setFontSize(11);
      fetchFileList();
    } catch (error) {
      showPopupMessage("Error: " + error.message, "error");
    }
  };

  const handleFilenameChange = (e) => setFilename(e.target.value);

  // Hook for fetching file list from the server for the Gcode Filemanagement
  const fetchFileList = useCallback(async () => {
    try {
      const response = await axios.get(`/gcodeFiles`);
      if (response.data.status === "success") {
        setFileList(response.data.files);
      } else {
        showPopupMessage("Error fetching file list", "error");
      }
    } catch (error) {
      showPopupMessage("Error: " + error.message, "error");
    }
  }, []);

  // Hook for fetching CPEE connection state & printing queue state
  const fetchExecutionState = useCallback(async () => {
    try {
      const connectionResponse = await axios.get(`/getConnectionState`);
      if (connectionResponse.data.status === "success") {
        setConnected(connectionResponse.data.connected);
      }
      const printingQueueResponse = await axios.get(`/getPrintingQueue`);
      if (printingQueueResponse.data.status === "success") {
        setPrintingQueue(printingQueueResponse.data.printingQueue);
      }
    } catch (error) {
      console.error("Error fetching execution state:", error);
    }
  }, []);

  // Hook for fetching file list and execution state on initial load
  useEffect(() => {
    fetchFileList();
    fetchExecutionState();
    const intervalId = setInterval(fetchExecutionState, 5000);
    return () => clearInterval(intervalId);
  }, [fetchFileList, fetchExecutionState]);

  return (
    <div style={{ padding: "20px" }}>
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", marginBottom: "40px" }}>
        <img src="/TUM_logo.svg" alt="TUM Logo" style={{ width: "100px", marginRight: "20px" }} />
        <h1>BierdeckelBot</h1>
      </div>
  
      {/* Display the status (whether a CPEE is connected and if a file is being printed) and user instructions */}
      <div style={{ marginBottom: "40px", justifyContent: "center" }}>
        <strong>Status:</strong>{" "}
        {connected ? (
          printingQueue === "empty" ? (
            <span>Connected to a CPEE instance that is ready to print. Create G-code files and/or select a file with the "print" button to start the printing process.</span>
          ) : (
            <span>Connected to a CPEE instance that is currently printing the file <strong>{printingQueue}</strong>. You can create G-code files, but you can't print them until the process is finished.</span>
          )
        ) : (
          <span style={{ color: "orange" }}>
            You're not connected to a CPEE instance and can currently only create G-code files, but not print them. To print, please start the CPEE process.
          </span>
        )}
      </div>

      {/* SVG to Gcode Section */}
      <div style={{ marginBottom: "40px", justifyContent: "center" }}>
      <h2>SVG to Gcode</h2>
      <p>Drop any SVG file to convert it into a Gcode file for the BierdeckelBot. Please refer to the Documentation for instructions to generate optimized SVG files using Inkscape.</p>
      <div style={{ display: "flex", alignItems: "center" }}>
        <div onDrop={handleFileDrop} onDragOver={(e) => e.preventDefault()} onClick={handleDropZoneClick} style={{ border: "2px dashed lightgrey", padding: "20px", marginBottom: "20px", cursor: "pointer", textAlign: "center", backgroundColor: "#ffffff", width: "33%" }}>
          {file ? file.name : "Drop your SVG in here or click to select a file"}
        </div>
        <div style={{ width: "66%", paddingLeft: "20px" }}>
          <button onClick={handleSvgToGcode} disabled={!file} style={{ backgroundColor: file ? "#3170b3" : "grey", color: "white", cursor: file ? "pointer" : "not-allowed", padding: "10px 20px", marginBottom: "10px" }}>
            Convert SVG to Gcode
          </button>
        </div>
      </div>
      <input type="file" accept=".svg" ref={fileInputRef} onChange={handleFileChange} style={{ display: "none" }} />
    </div>


      {/* Text to Gcode Section */}
      <h2>Text to Gcode</h2>
      <p>Enter a text, select a font size and set a filename to create a gcode-file that prints this text.</p>
      {textMessage && <p style={{ color: "red" }}>{textMessage}</p>}
      <div style={{ display: "flex", alignItems: "flex-start" }}>
        <div style={{ width: "33%", paddingRight: "20px" }}>
          <textarea value={text} onChange={handleTextChange} placeholder="Enter your text here" style={{ width: "100%", height: "120px", border: "2px dashed lightgrey", padding: "10px", backgroundColor: "#ffffff", resize: "none" }} />
        </div>
        <div style={{ width: "50%", paddingLeft: "20px" }}>
          <label style={{ display: "block", marginTop: "5px" }}>
            Fontsize:
            <select value={fontSize} onChange={(e) => setFontSize(Number(e.target.value))} style={{ marginLeft: "10px", padding: "5px" }}>
              <option value="">Select Font Size</option>
              {Object.keys(fontSizeConstraints).map((size) => (
                <option key={size} value={size}>{size} (Max {fontSizeConstraints[size].charsPerRow} chars, {fontSizeConstraints[size].maxRows} lines)</option>
              ))}
            </select>
          </label>
          <label style={{ display: "block", marginTop: "5px" }}>
            Filename:
            <input type="text" value={filename} onChange={handleFilenameChange} placeholder="Enter a filename" style={{ marginLeft: "10px", padding: "5px" }} />
          </label>
          <button onClick={handleTextToGcode} disabled={!filename || !fontSize || textMessage} style={{ backgroundColor: !filename || !fontSize || textMessage ? "grey" : "#3170b3", color: "white", cursor: !filename || !fontSize || textMessage ? "not-allowed" : "pointer", padding: "10px 20px", marginTop: "20px" }}>
            Convert Text to Gcode
          </button>
        </div>
      </div>

      {/* File Management Section */}
      <h2>G-Code Files</h2>
      <div style={{ width: "50%" }}>
        <p>Select a file, then buttons appear to delete or print it.</p>
        {fileList.length === 0 ? (
          <p style={{ color: "gray", fontStyle: "italic" }}>No files so far... get started!</p>
        ) : (
          <ul style={{ paddingLeft: "0" }}>
            {fileList.map((file) => (
              <li key={file} onClick={() => handleFileClick(file)} style={{ display: "flex", alignItems: "center", padding: "10px", border: file === selectedFile ? "2px solid black" : "1px solid lightgrey", cursor: "pointer", marginBottom: "5px", height: "40px" }}>
                <span>{file}</span>
                {file === selectedFile && (
                  <div style={{ marginLeft: "auto", display: "flex", alignItems: "center" }}>
                    <button onClick={() => handleDeleteFile(file)} style={{ color: "darkred", backgroundColor: "transparent", border: "none", cursor: "pointer", marginTop: "15px", marginRight: "10px", width: "50px", padding: "0", height: "100%" }}>Delete</button>
                    <button onClick={handlePrintFile} disabled={printingQueue !== "empty"} style={{ color: "white", backgroundColor: printingQueue !== "empty" ? "grey" : "#3170b3", border: "none", marginTop: "15px", padding: "5px 10px", cursor: printingQueue !== "empty" ? "not-allowed" : "pointer", width: "100px", height: "100%" }}>Print</button>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* Popup for success & error messages */}
      {showPopup && (
        <div style={{ position: "fixed", bottom: "20px", left: "50%", transform: "translateX(-50%)", backgroundColor: popupType === "success" ? "green" : "red", color: "white", padding: "10px 20px", borderRadius: "5px", boxShadow: "0px 4px 8px rgba(0,0,0,0.2)", zIndex: 1000 }}>
          {popupMessage}
        </div>
      )}
    </div>
  );
}

export default App;
