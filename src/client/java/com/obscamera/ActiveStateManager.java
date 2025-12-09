package com.obscamera;

public class ActiveStateManager {
    private static boolean isActive = false;
    private static String currentActivePlayer = null;

    public static void setActive() {
        if (!isActive) {
            OBSCameraSwitchingMod.LOGGER.info("カメラがアクティブになりました");
        }
        isActive = true;
    }

    public static void setInactive() {
        if (isActive) {
            OBSCameraSwitchingMod.LOGGER.info("カメラが非アクティブになりました");
        }
        isActive = false;
    }

    public static boolean isActive() {
        return isActive;
    }

    public static void setCurrentActivePlayer(String playerName) {
        currentActivePlayer = playerName;
    }

    public static String getCurrentActivePlayer() {
        return currentActivePlayer;
    }
}
