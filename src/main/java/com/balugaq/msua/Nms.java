package com.balugaq.msua;

public class Nms {
    private static Object server = null;

    static {
        try {
            server = ReflectionUtil.invokeStaticMethod(Class.forName("net.minecraft.server.MinecraftServer"), "getServer");
        } catch (ClassNotFoundException e) {
            MSUA.console(e);
        }
    }

    public static Object getMinecraftServer() {
        return server;
    }

    public static Object getRecipeManager() {
        return ReflectionUtil.invokeMethod(server, "getRecipeManager");
    }
}
