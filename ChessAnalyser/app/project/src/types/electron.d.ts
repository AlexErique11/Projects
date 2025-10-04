// Ambient declaration for the preload-exposed API
export {}; // ensure this is a module

declare global {
  interface Window {
    electron: {
      ping: () => string;
    };
  }
}
