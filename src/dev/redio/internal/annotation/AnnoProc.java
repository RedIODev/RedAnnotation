package dev.redio.internal.annotation;

import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

public interface AnnoProc {

    boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env);
}
