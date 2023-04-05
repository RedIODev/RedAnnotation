module red.annotation {
    exports dev.redio.annotation;
    exports dev.redio.internal;

    requires jdk.compiler;
    requires java.compiler;
    requires jdk.unsupported;
	requires org.eclipse.jdt.compiler.apt;
}