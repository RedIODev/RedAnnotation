package dev.redio.internal.annotation.javac;

import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

import com.sun.source.util.Trees;

import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import dev.redio.internal.annotation.AnnoProc;
import dev.redio.internal.annotation.javac.yield.YieldResolver;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;

public class JavacProc implements AnnoProc {

    private final Trees trees;
    private final TreeMaker maker;
    private final Name.Table names;
    private final Messager messager;

    public JavacProc(JavacProcessingEnvironment env) {
        trees = Trees.instance(env);
        Context context = (env).getContext();
        maker = TreeMaker.instance(context);
        names = Names.instance(context).table;
        this.messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())
            return false;

        var elements = roundEnv.getRootElements();
        for (var element : elements) {
            if (!(trees.getTree(element) instanceof JCTree tree))
                throw new Error("Illegal source Tree");
            if (!(tree instanceof JCClassDecl classDecl))
                continue;
            messager.printNote("JavacProc:" + tree);
            tree.accept(new YieldResolver(classDecl, maker, names, messager));
            //tree.accept(new Test());

        }
        return false;
    }

    // private class Test extends TreeTranslator {

    //     @Override
    //     public void visitYield(JCYield tree) {
    //         messager.printNote("YieldFound");
    //         super.visitYield(tree);
    //     }
    // }
}
