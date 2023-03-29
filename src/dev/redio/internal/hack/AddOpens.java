package dev.redio.internal.hack;

import sun.misc.Unsafe;

public final class AddOpens {
    private AddOpens() {
    }

    @SuppressWarnings("deprecation")
    public static void addOpens() {
        Unsafe unsafe = null;
        try {
            var theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new Error("Could not acquire Unsafe", e);
        }
        var jdkCompilerModule = ModuleLayer.boot().findModule("jdk.compiler").get();
        var ownModule = AddOpens.class.getModule();
        String[] allPkgs = {
                "com.sun.tools.javac.code",
                "com.sun.tools.javac.comp",
                "com.sun.tools.javac.file",
                "com.sun.tools.javac.main",
                "com.sun.tools.javac.model",
                "com.sun.tools.javac.parser",
                "com.sun.tools.javac.processing",
                "com.sun.tools.javac.tree",
                "com.sun.tools.javac.util",
                "com.sun.tools.javac.jvm",
        };
        try {

            var m = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);
            var field = DummyMethod.class.getDeclaredField("override");
            var offset = unsafe.objectFieldOffset(field);
            unsafe.putBooleanVolatile(m, offset, true);
            for (String p : allPkgs)
                m.invoke(jdkCompilerModule, p, ownModule);
        } catch (Exception e) {
            throw new Error("Could not add opens for internal packages", e);
        }
    }
}
