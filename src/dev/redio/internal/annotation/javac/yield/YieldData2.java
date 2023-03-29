package dev.redio.internal.annotation.javac.yield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Messager;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.Name;

public class YieldData2 {

    private final Map<JCStatement, JCVariableDecl> targetVarMap;
    private final Map<JCYield, JCStatement> enclosingSourceStatementMap;
    private final Map<JCStatement, JCVariableDecl> tempTargetVarMap;
    private final TreeMaker maker;
    private final Name.Table names;

    public YieldData2(JCClassDecl classDecl, TreeMaker maker, Name.Table names, Messager messager) {
        targetVarMap = createTargetMap(classDecl, messager);
        enclosingSourceStatementMap = createEnclosingStatementMap(classDecl);
        tempTargetVarMap = new HashMap<>();
        this.maker = maker;
        this.names = names;
    }

    public JCVariableDecl getTarget(JCStatement sourceStatement) {
        return targetVarMap.get(sourceStatement);
    }

    public JCVariableDecl getOrCreateTempTarget(JCStatement sourceStatement) {
        return tempTargetVarMap.computeIfAbsent(sourceStatement, this::createTempVar);

    }

    public JCStatement getEnclosingSourceStatement(JCYield yieldStatement) {
        return enclosingSourceStatementMap.get(yieldStatement);
    }

    private JCVariableDecl createTempVar(JCStatement sourceStatement) {
        var target = targetVarMap.get(sourceStatement);
        return maker.VarDef(maker.Modifiers(Flags.FINAL), names.fromString("__" + target.name), target.vartype, null);
    }

    private static Map<JCStatement, JCVariableDecl> createTargetMap(JCClassDecl classDecl, Messager messager) {
        var result = new HashMap<JCStatement, JCVariableDecl>();
        for (var element : classDecl.defs) {
            if (!(element instanceof JCMethodDecl method))
                continue;
            JCStatement previousStatement = null;
            for (var statement : method.body.stats) {

                switch (statement.getKind()) {
                    case FOR_LOOP:
                    case ENHANCED_FOR_LOOP:
                    case WHILE_LOOP:
                    case DO_WHILE_LOOP:
                    case TRY:
                        if (previousStatement instanceof JCVariableDecl varDecl && checkYieldStatement(statement))
                            result.put(statement, varDecl);
                        else
                            messager.printNote(
                                    "previousStatement:" + previousStatement + "Class:" + previousStatement.getClass());
                        // result.put(statement, invoke.args.head);
                        break;
                    default:

                }
                previousStatement = statement;
            }
        }
        return result;
    }

    private static Map<JCYield, JCStatement> createEnclosingStatementMap(JCClassDecl classDecl) {
        var result = new HashMap<JCYield, JCStatement>();
        var statementFinder = new StatementFinder();
        classDecl.accept(statementFinder);
        for (var statement : statementFinder.statements) {
            var yieldFinder = new YieldFinder();
            statement.accept(yieldFinder);
            for (var yieldStatement : yieldFinder.yields) {
                result.put(yieldStatement, statement);
            }
        }
        return result;
    }

    private static class StatementFinder extends TreeTranslator {
        java.util.List<JCStatement> statements = new ArrayList<>();

        @Override
        public void visitDoLoop(JCDoWhileLoop that) {
            statements.add(that);
            super.visitDoLoop(that);
        }

        @Override
        public void visitForLoop(JCForLoop that) {
            statements.add(that);
            super.visitForLoop(that);
        }

        @Override
        public void visitForeachLoop(JCEnhancedForLoop that) {
            statements.add(that);
            super.visitForeachLoop(that);
        }

        @Override
        public void visitTry(JCTry that) {
            statements.add(that);
            super.visitTry(that);
        }

    }

    private static boolean checkYieldStatement(JCStatement enclosingStatement) {
        var checker = new YieldFinder();
        enclosingStatement.accept(checker);
        return !checker.yields.isEmpty();
    }

    private static class YieldFinder extends TreeTranslator {
        java.util.List<JCYield> yields = new ArrayList<>();

        @Override
        public void visitYield(JCYield that) {
            yields.add(that);
            super.visitYield(that);
        }
    }

    // only if allowed in expression
    // private static boolean checkYieldStatementRecursive(boolean wasFound, JCBlock
    // block) {
    // if (wasFound)
    // return true;
    // for (var statement : block.stats) {
    // if (statement instanceof JCYield)
    // return true;
    // if (!(statement instanceof JCIf _if))
    // continue;
    // //check for other things then blocks <<if (true) yield 5>>
    // if (_if.thenpart instanceof JCBlock thenBlock &&
    // checkYieldStatementRecursive(wasFound, thenBlock))
    // return true;
    // if (_if.elsepart instanceof JCBlock elseBlock &&
    // checkYieldStatementRecursive(wasFound, elseBlock))
    // return true;

    // }
    // return false;
    // }
}
