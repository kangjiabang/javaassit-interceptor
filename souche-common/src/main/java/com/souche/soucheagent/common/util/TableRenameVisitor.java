package com.souche.soucheagent.common.util;


import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Find all used tables within an select statement.
 */
public class TableRenameVisitor implements SelectVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor, SelectItemVisitor, StatementVisitor
{

    private TableRenameUtil.TableRenamer tableRenamer;


    /**
     * There are special names, that are not table names but are parsed as
     * tables. These names are collected here and are not included in the tables
     * - names anymore.
     */
    private List<String> otherItemNames;





    public TableRenameVisitor(TableRenameUtil.TableRenamer tableRenamer)
    {
        this.tableRenamer = tableRenamer;
        this.otherItemNames=new ArrayList<String>();
    }



    @Override
    public void visit(Select select)
    {
        if (select.getWithItemsList() != null)
        {
            for (WithItem withItem : select.getWithItemsList())
            {
                withItem.accept(this);
            }
        }
        select.getSelectBody().accept(this);
    }



    @Override
    public void visit(WithItem withItem)
    {
        otherItemNames.add(withItem.getName().toLowerCase());
        withItem.getSelectBody().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect)
    {
        if (plainSelect.getSelectItems() != null)
        {
            for (SelectItem item : plainSelect.getSelectItems())
            {
                item.accept(this);
            }
        }

        plainSelect.getFromItem().accept(this);

        if (plainSelect.getJoins() != null)
        {
            for (Join join : plainSelect.getJoins())
            {
                join.getRightItem().accept(this);
            }
        }
        if (plainSelect.getWhere() != null)
        {
            plainSelect.getWhere().accept(this);
        }
        if (plainSelect.getOracleHierarchical() != null)
        {
            plainSelect.getOracleHierarchical().accept(this);
        }
    }

    @Override
    public void visit(Table table)
    {
        String fullyQualifiedName = table.getFullyQualifiedName();
        if (!otherItemNames.contains(fullyQualifiedName.toLowerCase()))
        {
            String oldTableName=table.getName();
            String newTableName=this.tableRenamer.rename(oldTableName);
            table.setName(newTableName);
        }
    }

    @Override
    public void visit(SubSelect subSelect)
    {
        subSelect.getSelectBody().accept(this);
    }

    @Override
    public void visit(Addition addition)
    {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(AndExpression andExpression)
    {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(Between between)
    {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(Column tableColumn)
    {
    }

    @Override
    public void visit(Division division)
    {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(DoubleValue doubleValue)
    {
    }

    @Override
    public void visit(EqualsTo equalsTo)
    {
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(Function function)
    {
    }

    @Override
    public void visit(GreaterThan greaterThan)
    {
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals)
    {
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression)
    {
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightItemsList().accept(this);
    }

    @Override
    public void visit(SignedExpression signedExpression)
    {
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression)
    {
    }

    @Override
    public void visit(JdbcParameter jdbcParameter)
    {
    }

    @Override
    public void visit(LikeExpression likeExpression)
    {
        visitBinaryExpression(likeExpression);
    }

    @Override
    public void visit(ExistsExpression existsExpression)
    {
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue)
    {
    }

    @Override
    public void visit(MinorThan minorThan)
    {
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals)
    {
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(Multiplication multiplication)
    {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo)
    {
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(NullValue nullValue)
    {
    }

    @Override
    public void visit(OrExpression orExpression)
    {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(Parenthesis parenthesis)
    {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue)
    {
    }

    @Override
    public void visit(Subtraction subtraction)
    {
        visitBinaryExpression(subtraction);
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression)
    {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(ExpressionList expressionList)
    {
        for (Expression expression : expressionList.getExpressions())
        {
            expression.accept(this);
        }

    }

    @Override
    public void visit(DateValue dateValue)
    {
    }

    @Override
    public void visit(TimestampValue timestampValue)
    {
    }

    @Override
    public void visit(TimeValue timeValue)
    {
    }


    @Override
    public void visit(CaseExpression caseExpression)
    {
    }


    @Override
    public void visit(WhenClause whenClause)
    {
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression)
    {
        allComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression)
    {
        anyComparisonExpression.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(SubJoin subjoin)
    {
        subjoin.getLeft().accept(this);
        if (subjoin.getJoinList() != null) {
            for (Join join : subjoin.getJoinList()) {

                join.getRightItem().accept(this);
            }
        }
    }

    @Override
    public void visit(Concat concat)
    {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches)
    {
        visitBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd)
    {
        visitBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr)
    {
        visitBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor)
    {
        visitBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast)
    {
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo)
    {
        visitBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression analytic)
    {
    }

    @Override
    public void visit(SetOperationList list)
    {
        for (SelectBody plainSelect : list.getSelects())
        {
            plainSelect.accept(this);
        }
    }

    @Override
    public void visit(ExtractExpression eexpr)
    {
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect)
    {
        lateralSubSelect.getSubSelect().getSelectBody().accept(this);
    }

    @Override
    public void visit(MultiExpressionList multiExprList)
    {
        for (ExpressionList exprList : multiExprList.getExprList())
        {
            exprList.accept(this);
        }
    }

    @Override
    public void visit(ValuesList valuesList)
    {
    }



    @Override
    public void visit(IntervalExpression iexpr)
    {
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter)
    {
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr)
    {
        if (oexpr.getStartExpression() != null)
        {
            oexpr.getStartExpression().accept(this);
        }

        if (oexpr.getConnectExpression() != null)
        {
            oexpr.getConnectExpression().accept(this);
        }
    }

    @Override
    public void visit(RegExpMatchOperator rexpr)
    {
        visitBinaryExpression(rexpr);
    }

    @Override
    public void visit(RegExpMySQLOperator rexpr)
    {
        visitBinaryExpression(rexpr);
    }

    @Override
    public void visit(JsonExpression jsonExpr)
    {
    }

    @Override
    public void visit(AllColumns allColumns)
    {
    }

    @Override
    public void visit(AllTableColumns allTableColumns)
    {
    }

    @Override
    public void visit(SelectExpressionItem item)
    {
        item.getExpression().accept(this);
    }


    @Override
    public void visit(UserVariable var)
    {
    }

    @Override
    public void visit(NumericBind bind)
    {
    }

    @Override
    public void visit(KeepExpression aexpr)
    {
    }

    @Override
    public void visit(MySQLGroupConcat groupConcat)
    {
    }

    @Override
    public void visit(Delete delete)
    {
        delete.getTable().accept(this);
        if (delete.getWhere() != null)
        {
            delete.getWhere().accept(this);
        }
    }

    @Override
    public void visit(Update update)
    {
        for (Table table : update.getTables())
        {
            table.accept(this);
        }
        if (update.getExpressions() != null)
        {
            for (Expression expression : update.getExpressions())
            {
                expression.accept(this);
            }
        }

        if (update.getFromItem() != null)
        {
            update.getFromItem().accept(this);
        }

        if (update.getJoins() != null)
        {
            for (Join join : update.getJoins())
            {
                join.getRightItem().accept(this);
            }
        }

        if (update.getWhere() != null)
        {
            update.getWhere().accept(this);
        }
    }

    @Override
    public void visit(Insert insert)
    {
        insert.getTable().accept(this);

        if (insert.getItemsList() != null)
        {
            insert.getItemsList().accept(this);
        }
        if (insert.getSelect() != null)
        {
            visit(insert.getSelect());
        }
    }

    @Override
    public void visit(Replace replace)
    {
        replace.getTable().accept(this);

        if (replace.getExpressions() != null)
        {
            for (Expression expression : replace.getExpressions())
            {
                expression.accept(this);
            }
        }
        if (replace.getItemsList() != null)
        {
            replace.getItemsList().accept(this);
        }
    }

    @Override
    public void visit(Drop drop)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Truncate truncate)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(CreateIndex createIndex)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(CreateTable create)
    {
        create.getTable().accept(this);

        if (create.getSelect() != null)
        {
            create.getSelect().accept(this);
        }
    }

    @Override
    public void visit(CreateView createView)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Alter alter)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Statements stmts)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(Execute execute)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(SetStatement set)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visit(RowConstructor rowConstructor)
    {
        for (Expression expr : rowConstructor.getExprList().getExpressions())
        {
            expr.accept(this);
        }
    }

    @Override
    public void visit(HexValue hexValue)
    {

    }

    @Override
    public void visit(BitwiseRightShift bitwiseRightShift) {

    }

    @Override
    public void visit(BitwiseLeftShift bitwiseLeftShift) {

    }

    @Override
    public void visit(JsonOperator jsonOperator) {

    }

    @Override
    public void visit(ValueListExpression valueListExpression) {

    }

    @Override
    public void visit(OracleHint oracleHint) {

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression dateTimeLiteralExpression) {

    }

    @Override
    public void visit(NotExpression notExpression) {

    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {

    }

    @Override
    public void visit(Comment comment) {

    }

    @Override
    public void visit(Commit commit) {

    }

    @Override
    public void visit(AlterView alterView) {

    }

    @Override
    public void visit(ShowStatement showStatement) {

    }

    @Override
    public void visit(Merge merge) {

    }

    @Override
    public void visit(Upsert upsert) {

    }

    @Override
    public void visit(UseStatement useStatement) {

    }

    @Override
    public void visit(Block block) {

    }

    @Override
    public void visit(TableFunction tableFunction) {

    }

    @Override
    public void visit(ParenthesisFromItem parenthesisFromItem) {

    }

    @Override
    public void visit(ValuesStatement valuesStatement) {

    }
}