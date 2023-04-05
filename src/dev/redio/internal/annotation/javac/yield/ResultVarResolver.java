package dev.redio.internal.annotation.javac.yield;

import java.util.Optional;

import javax.annotation.processing.Messager;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public class ResultVarResolver {

    private final YieldData2 data;
    private final TreeMaker maker;
    private final Name.Table names;
    private final Messager messager;

    public ResultVarResolver(YieldData2 data, TreeMaker maker, Name.Table names, Messager messager) {
        this.maker = maker;
        this.names = names;
        this.messager = messager;
        this.data = data;

    }

    public void visit(JCClassDecl classDecl) {
        classDecl.accept(new ForLoopVisitor());
        classDecl.accept(new ForEachVisitor());
        classDecl.accept(new WhileVisitor());
        classDecl.accept(new DoWhileVisitor());
        classDecl.accept(new TryVisitor());
    }

    private class ForLoopVisitor extends TreeTranslator {
        @Override
        public void visitForLoop(JCForLoop tree) {
            result = visitEnclosingStatement(tree);
            if (result == null)
                super.visitForLoop(tree);
        }
    }

    private class ForEachVisitor extends TreeTranslator {
        @Override
        public void visitForeachLoop(JCEnhancedForLoop tree) {
            result = visitEnclosingStatement(tree);
            if (result == null)
                super.visitForeachLoop(tree);
        }
    }

    private class WhileVisitor extends TreeTranslator {
        @Override
        public void visitWhileLoop(JCWhileLoop tree) {
            result = visitEnclosingStatement(tree);
            if (result == null)
                super.visitWhileLoop(tree);
        }
    }

    private class DoWhileVisitor extends TreeTranslator {
        @Override
        public void visitDoLoop(JCDoWhileLoop tree) {
            result = visitEnclosingStatement(tree);
            if (result == null)
                super.visitDoLoop(tree);
        }
    }

    private class TryVisitor extends TreeTranslator {
        @Override
        public void visitTry(JCTry tree) {
            var catchParam = maker.ReceiverVarDef(maker.Modifiers(0), maker.Ident(names.fromString("ignored")),
                    maker.Ident(names.fromString("dev.redio.internal.BreakException")));
            var catcher = maker.Catch(catchParam, maker.Block(0, List.nil()));
            var catcherArray = tree.catchers.toArray(JCCatch[]::new);
            var newCatcherArray = new JCCatch[catcherArray.length + 1];
            newCatcherArray[0] = catcher;
            System.arraycopy(catcherArray, 0, newCatcherArray, 1, catcherArray.length);
            tree.catchers = List.from(newCatcherArray);
            result = visitEnclosingStatement(tree);
            if (result == null)
                super.visitTry(tree);
            messager.printWarning("AfterTryVisit:" + tree);
        }
    }

    private JCTree visitEnclosingStatement(JCStatement tree) {
        var target = getTarget(tree);
        if (target.isEmpty())
            return null;
        return addResultVar(target.get(), tree);
    }

    private Optional<JCVariableDecl> getTarget(JCStatement controlFlowStatement) {
        return Optional.ofNullable(data.getTarget(controlFlowStatement));
    }

    private JCTree addResultVar(JCVariableDecl target, JCStatement controlFlowStatement) {
        var tempVar = data.getOrCreateTempTarget(controlFlowStatement);
        // messager.printWarning("TempVar" + tempVar.toString());
        return maker.Block(0, List.of(
                tempVar,
                controlFlowStatement,
                maker.Exec(maker.Assign(maker.Ident(target.name), maker.Ident(tempVar.name)))));
    }
}
