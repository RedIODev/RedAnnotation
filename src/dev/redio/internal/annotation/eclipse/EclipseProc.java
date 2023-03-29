package dev.redio.internal.annotation.eclipse;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;

import dev.redio.internal.annotation.AnnoProc;

public class EclipseProc implements AnnoProc {

    public EclipseProc(BaseProcessingEnvImpl env) {

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'process'");
    }

}
