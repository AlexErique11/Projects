// chess-analyzer-backend.js
// Node.js backend service that calls the Python chess analyzer

const express = require('express');
const cors = require('cors');
const { spawn } = require('child_process');
const path = require('path');

const app = express();
const PORT = 3001;

app.use(cors());
app.use(express.json());

// Path to the Python script (adjust as needed)
const PYTHON_SCRIPT_PATH = path.join(__dirname, '..', '..', 'chess_analyzer_wrapper.py');

app.post('/api/analyze', (req, res) => {
    const { fen, avgElo = 1500, timeControl = 'blitz' } = req.body;
    
    if (!fen) {
        return res.status(400).json({ success: false, error: 'FEN is required' });
    }

    // Call Python script with parameters
    const pythonProcess = spawn('python', [PYTHON_SCRIPT_PATH, fen, avgElo.toString(), timeControl]);
    
    let outputData = '';
    let errorData = '';

    pythonProcess.stdout.on('data', (data) => {
        outputData += data.toString();
    });

    pythonProcess.stderr.on('data', (data) => {
        errorData += data.toString();
    });

    pythonProcess.on('close', (code) => {
        if (code === 0) {
            try {
                const result = JSON.parse(outputData);
                res.json(result);
            } catch (error) {
                res.status(500).json({
                    success: false,
                    error: 'Failed to parse Python output',
                    pythonOutput: outputData
                });
            }
        } else {
            res.status(500).json({
                success: false,
                error: 'Python script failed',
                details: errorData || 'Unknown error'
            });
        }
    });
});

app.get('/api/health', (req, res) => {
    res.json({
        status: 'healthy',
        service: 'chess-analyzer-backend',
        version: '1.0.0'
    });
});

app.listen(PORT, () => {
    console.log(`Chess Analyzer Backend running on http://localhost:${PORT}`);
});