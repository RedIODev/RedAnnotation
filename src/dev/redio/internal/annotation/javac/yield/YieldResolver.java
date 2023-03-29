package dev.redio.internal.annotation.javac.yield;

import java.security.KeyRep.Type;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.processing.Messager;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

//TODO: 1 TreeTranslator per visitX
//TODO: non modifying visits need to call super
public class YieldResolver extends TreeTranslator {

    private final YieldData2 data;
    private final TreeMaker maker;
    private final Name.Table names;
    private final Messager messager;

    public YieldResolver(JCClassDecl classDecl, TreeMaker maker, Name.Table names, Messager messager) {
        this.maker = maker;
        this.names = names;
        this.messager = messager;

        data = new YieldData2(classDecl, maker, names, messager);
        messager.printNote("YieldResolver init done.");
    }

    @Override
    public void visitYield(JCYield tree) {
        messager.printNote("VisitYield");
        var enclosingStatement = data.getEnclosingSourceStatement(tree);
        messager.printNote("Visited yield statement:" + enclosingStatement);
        if (enclosingStatement == null)
            return;
        result = switch (enclosingStatement.getKind()) {
            case FOR_LOOP:
            case ENHANCED_FOR_LOOP:
            case WHILE_LOOP:
            case DO_WHILE_LOOP:
                messager.printNote("LoopYield Visited");
                yield loopExpression(tree, data.getOrCreateTempTarget(enclosingStatement));
            case TRY:
                yield tryExpression(tree, data.getOrCreateTempTarget(enclosingStatement));
            default:
                yield tree;

        };
        // result = switch (enclosingStatement) {
        // case JCForLoop loop -> forExpression(tree,
        // data.getOrCreateTempTarget(enclosingStatement), loop);
        // case JCEnhancedForLoop loop ->
        // forEachExpression(tree, data.getOrCreateTempTarget(enclosingStatement),
        // loop);
        // case JCWhileLoop loop -> whileExpression(tree,
        // data.getOrCreateTempTarget(enclosingStatement), loop);
        // case JCDoWhileLoop loop -> doWhileExpression(tree,
        // data.getOrCreateTempTarget(enclosingStatement), loop);
        // case JCTry tryExpr -> tryExpression(tree,
        // data.getOrCreateTempTarget(enclosingStatement), tryExpr);
        // default -> result;
        // };
    }

    @Override
    public void visitForLoop(JCForLoop tree) {
        visitEnclosingStatement(tree);
    }

    @Override
    public void visitForeachLoop(JCEnhancedForLoop tree) {
        visitEnclosingStatement(tree);
    }

    @Override
    public void visitWhileLoop(JCWhileLoop tree) {
        visitEnclosingStatement(tree);
    }

    @Override
    public void visitDoLoop(JCDoWhileLoop tree) {
        visitEnclosingStatement(tree);
    }

    @Override
    public void visitTry(JCTry tree) {
        var catchParam = maker.ReceiverVarDef(null, maker.Ident(names.fromString("ignored")),
                maker.Ident(names.fromString("BreakException")));
        var catcher = maker.Catch(catchParam, maker.Block(0, List.nil()));
        tree.catchers.add(0, catcher);
        visitEnclosingStatement(tree);
    }

    // private JCTree forExpression(JCYield yieldStatement, JCVariableDecl result) {
    // return maker.Block(0, List.of(
    // maker.Exec(maker.Assign(maker.Ident(result), yieldStatement.value)),
    // maker.Break(null)
    // ));
    // }

    // private JCTree forEachExpression(JCYield yieldStatement, JCVariableDecl
    // result) {
    // return maker.Block(0, List.of(
    // maker.Exec(maker.Assign(maker.Ident(result), yieldStatement.value)),
    // maker.Break(null)
    // ));
    // }

    // private JCTree whileExpression(JCYield yieldStatement, JCVariableDecl result)
    // {
    // return maker.Block(0, List.of(
    // maker.Exec(maker.Assign(maker.Ident(result), yieldStatement.value)),
    // maker.Break(null)
    // ));
    // }

    // private JCTree doWhileExpression(JCYield yieldStatement, JCVariableDecl
    // result) {
    // return maker.Block(0, List.of(
    // maker.Exec(maker.Assign(maker.Ident(result), yieldStatement.value)),
    // maker.Break(null)
    // ));
    // }

    private JCTree loopExpression(JCYield yieldStatement, JCVariableDecl result) {
        messager.printNote("Loop expr triggered");
        return maker.Block(0, List.of(
                maker.Exec(maker.Assign(maker.Ident(result), yieldStatement.value)),
                maker.Break(null)));
    }

    private JCTree tryExpression(JCYield yieldStatement, JCVariableDecl result) {
        return maker.Block(0, List.of(
                maker.Exec(maker.Assign(maker.Ident(result), yieldStatement.value)),
                maker.Throw(maker.NewClass(null, null, maker.Ident(names.fromString("BreakError")), null, null))));
    }

    private Optional<JCVariableDecl> getTarget(JCStatement controlFlowStatement) {
        return Optional.ofNullable(data.getTarget(controlFlowStatement));
    }

    private JCTree addResultVar(JCVariableDecl target, JCStatement controlFlowStatement) {
        return maker.Block(0, List.of(
                data.getOrCreateTempTarget(controlFlowStatement),
                controlFlowStatement));
    }

    private void visitEnclosingStatement(JCStatement tree) {
        var target = getTarget(tree);
        if (target.isEmpty())
            return;
        result = addResultVar(target.get(), tree);
    }
}
