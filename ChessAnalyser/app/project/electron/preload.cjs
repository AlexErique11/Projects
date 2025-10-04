// Electron preload script
// Runs in isolated context; expose safe APIs only
const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('electron', {
  // Example: no-op ping for wiring test
  ping: () => 'pong',
});
