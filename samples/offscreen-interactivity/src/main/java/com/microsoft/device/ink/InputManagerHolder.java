package com.microsoft.device.ink;

public class InputManagerHolder {
    private static InputManager inputManager;

    public static void setInputManager(InputManager manager) {
        inputManager = manager;
    }

    public static InputManager getInputManager() {
        return inputManager;
    }

    public static void enableRawDrawing() {
        if (inputManager != null && inputManager.getTouchHelper() != null) {
            inputManager.getTouchHelper().setRawDrawingEnabled(true);
        }
    }

    public static void disableRawDrawing() {
        if (inputManager != null && inputManager.getTouchHelper() != null) {
            inputManager.getTouchHelper().setRawDrawingEnabled(false);
        }
    }
}
