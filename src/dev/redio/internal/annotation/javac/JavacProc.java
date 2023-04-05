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
import dev.redio.internal.annotation.javac.yield.ResultVarResolver;
import dev.redio.internal.annotation.javac.yield.YieldData2;
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
            var data = new YieldData2(classDecl, maker, names, messager);
            new ResultVarResolver(data, maker, names, messager).visit(classDecl);
            tree.accept(new YieldResolver(data, maker, names, messager));
            // messager.printNote("ClassDeclAfter:" + tree);

        }
        return false;
    }

    // private class Test extends TreeTranslator {

    // @Override
    // public void visitYield(JCYield tree) {
    // messager.printNote("YieldFound");
    // super.visitYield(tree);
    // }
    // }
}
