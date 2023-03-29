package dev.redio.internal.annotation;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

import dev.redio.internal.annotation.eclipse.EclipseProc;
import dev.redio.internal.annotation.javac.JavacProc;

@SupportedAnnotationTypes("*")
public final class Processor extends AbstractProcessor {

    private AnnoProc processor;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        if (processingEnv instanceof JavacProcessingEnvironment jEnv)
            processor = new JavacProc(jEnv);
        else if (processingEnv instanceof BaseProcessingEnvImpl eEnv)
            processor = new EclipseProc(eEnv);
        else
            throw new Error("Environment not supported");
        // processor = switch (processingEnv) {
        // case JavacProcessingEnvironment jEnv -> new JavacProc(jEnv);
        // case BaseProcessingEnvImpl eEnv -> new EclipseProc(eEnv);
        // default -> throw new Error("Environment not supported");
        // };
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return processor.process(annotations, roundEnv);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
