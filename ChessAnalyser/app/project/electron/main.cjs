// Electron main process
// CommonJS to align with package.json "main": "electron/main.cjs"
const { app, BrowserWindow, shell } = require('electron');
const path = require('path');
const fs = require('fs');

const isDev = process.env.ELECTRON_START_URL || process.env.VITE_DEV_SERVER_URL || process.env.NODE_ENV === 'development';

/**
 * Create the main application window
 */
function createWindow() {
  const win = new BrowserWindow({
    width: 1200,
    height: 800,
    show: false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: false,
    },
  });

  win.once('ready-to-show', () => win.show());

  if (isDev) {
    const devServerURL = process.env.ELECTRON_START_URL || process.env.VITE_DEV_SERVER_URL || 'http://localhost:5173';
    win.loadURL(devServerURL);
    win.webContents.openDevTools({ mode: 'detach' });
  } else {
    const indexPath = path.join(__dirname, '..', 'dist', 'index.html');
    if (fs.existsSync(indexPath)) {
      win.loadFile(indexPath);
    } else {
      // Fallback if path differs
      win.loadURL(`file://${path.resolve(process.cwd(), 'dist', 'index.html')}`);
    }
  }

  // Open URLs in user's browser rather than in-app
  win.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: 'deny' };
  });
}

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

app.whenReady().then(() => {
  createWindow();

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});
