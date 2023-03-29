package dev.redio.internal.annotation.javac.yield;

import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Name;

public record YieldData(JCVariableDecl result, JCStatement controlFlowStatement) {

}
