/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.sql.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.prestosql.sql.tree.AddColumn;
import io.prestosql.sql.tree.AliasedRelation;
import io.prestosql.sql.tree.AllColumns;
import io.prestosql.sql.tree.Analyze;
import io.prestosql.sql.tree.ArithmeticBinaryExpression;
import io.prestosql.sql.tree.ArrayConstructor;
import io.prestosql.sql.tree.AtTimeZone;
import io.prestosql.sql.tree.BetweenPredicate;
import io.prestosql.sql.tree.BinaryLiteral;
import io.prestosql.sql.tree.BooleanLiteral;
import io.prestosql.sql.tree.Call;
import io.prestosql.sql.tree.CallArgument;
import io.prestosql.sql.tree.Cast;
import io.prestosql.sql.tree.CharLiteral;
import io.prestosql.sql.tree.CoalesceExpression;
import io.prestosql.sql.tree.ColumnDefinition;
import io.prestosql.sql.tree.Comment;
import io.prestosql.sql.tree.Commit;
import io.prestosql.sql.tree.ComparisonExpression;
import io.prestosql.sql.tree.CreateRole;
import io.prestosql.sql.tree.CreateSchema;
import io.prestosql.sql.tree.CreateTable;
import io.prestosql.sql.tree.CreateTableAsSelect;
import io.prestosql.sql.tree.CreateView;
import io.prestosql.sql.tree.Cube;
import io.prestosql.sql.tree.CurrentTime;
import io.prestosql.sql.tree.Deallocate;
import io.prestosql.sql.tree.DecimalLiteral;
import io.prestosql.sql.tree.Delete;
import io.prestosql.sql.tree.DereferenceExpression;
import io.prestosql.sql.tree.DescribeInput;
import io.prestosql.sql.tree.DescribeOutput;
import io.prestosql.sql.tree.DoubleLiteral;
import io.prestosql.sql.tree.DropColumn;
import io.prestosql.sql.tree.DropRole;
import io.prestosql.sql.tree.DropSchema;
import io.prestosql.sql.tree.DropTable;
import io.prestosql.sql.tree.DropView;
import io.prestosql.sql.tree.Execute;
import io.prestosql.sql.tree.ExistsPredicate;
import io.prestosql.sql.tree.Explain;
import io.prestosql.sql.tree.ExplainFormat;
import io.prestosql.sql.tree.ExplainType;
import io.prestosql.sql.tree.Expression;
import io.prestosql.sql.tree.FetchFirst;
import io.prestosql.sql.tree.Format;
import io.prestosql.sql.tree.FunctionCall;
import io.prestosql.sql.tree.GenericLiteral;
import io.prestosql.sql.tree.Grant;
import io.prestosql.sql.tree.GrantRoles;
import io.prestosql.sql.tree.GrantorSpecification;
import io.prestosql.sql.tree.GroupBy;
import io.prestosql.sql.tree.GroupingOperation;
import io.prestosql.sql.tree.GroupingSets;
import io.prestosql.sql.tree.Identifier;
import io.prestosql.sql.tree.IfExpression;
import io.prestosql.sql.tree.Insert;
import io.prestosql.sql.tree.Intersect;
import io.prestosql.sql.tree.IntervalLiteral;
import io.prestosql.sql.tree.IntervalLiteral.IntervalField;
import io.prestosql.sql.tree.IntervalLiteral.Sign;
import io.prestosql.sql.tree.Isolation;
import io.prestosql.sql.tree.Join;
import io.prestosql.sql.tree.JoinOn;
import io.prestosql.sql.tree.LambdaArgumentDeclaration;
import io.prestosql.sql.tree.LambdaExpression;
import io.prestosql.sql.tree.Lateral;
import io.prestosql.sql.tree.LikeClause;
import io.prestosql.sql.tree.Limit;
import io.prestosql.sql.tree.LogicalBinaryExpression;
import io.prestosql.sql.tree.LongLiteral;
import io.prestosql.sql.tree.NaturalJoin;
import io.prestosql.sql.tree.Node;
import io.prestosql.sql.tree.NotExpression;
import io.prestosql.sql.tree.NullIfExpression;
import io.prestosql.sql.tree.NullLiteral;
import io.prestosql.sql.tree.Offset;
import io.prestosql.sql.tree.OrderBy;
import io.prestosql.sql.tree.Parameter;
import io.prestosql.sql.tree.PathElement;
import io.prestosql.sql.tree.PathSpecification;
import io.prestosql.sql.tree.Prepare;
import io.prestosql.sql.tree.PrincipalSpecification;
import io.prestosql.sql.tree.Property;
import io.prestosql.sql.tree.QualifiedName;
import io.prestosql.sql.tree.QuantifiedComparisonExpression;
import io.prestosql.sql.tree.Query;
import io.prestosql.sql.tree.QuerySpecification;
import io.prestosql.sql.tree.RenameColumn;
import io.prestosql.sql.tree.RenameSchema;
import io.prestosql.sql.tree.RenameTable;
import io.prestosql.sql.tree.ResetSession;
import io.prestosql.sql.tree.Revoke;
import io.prestosql.sql.tree.RevokeRoles;
import io.prestosql.sql.tree.Rollback;
import io.prestosql.sql.tree.Rollup;
import io.prestosql.sql.tree.Row;
import io.prestosql.sql.tree.Select;
import io.prestosql.sql.tree.SelectItem;
import io.prestosql.sql.tree.SetPath;
import io.prestosql.sql.tree.SetRole;
import io.prestosql.sql.tree.SetSession;
import io.prestosql.sql.tree.ShowCatalogs;
import io.prestosql.sql.tree.ShowColumns;
import io.prestosql.sql.tree.ShowGrants;
import io.prestosql.sql.tree.ShowRoleGrants;
import io.prestosql.sql.tree.ShowRoles;
import io.prestosql.sql.tree.ShowSchemas;
import io.prestosql.sql.tree.ShowSession;
import io.prestosql.sql.tree.ShowStats;
import io.prestosql.sql.tree.ShowTables;
import io.prestosql.sql.tree.SimpleGroupBy;
import io.prestosql.sql.tree.SingleColumn;
import io.prestosql.sql.tree.SortItem;
import io.prestosql.sql.tree.StartTransaction;
import io.prestosql.sql.tree.Statement;
import io.prestosql.sql.tree.StringLiteral;
import io.prestosql.sql.tree.SubqueryExpression;
import io.prestosql.sql.tree.SubscriptExpression;
import io.prestosql.sql.tree.Table;
import io.prestosql.sql.tree.TableSubquery;
import io.prestosql.sql.tree.TimeLiteral;
import io.prestosql.sql.tree.TimestampLiteral;
import io.prestosql.sql.tree.TransactionAccessMode;
import io.prestosql.sql.tree.Union;
import io.prestosql.sql.tree.Unnest;
import io.prestosql.sql.tree.Values;
import io.prestosql.sql.tree.With;
import io.prestosql.sql.tree.WithQuery;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.prestosql.sql.QueryUtil.identifier;
import static io.prestosql.sql.QueryUtil.query;
import static io.prestosql.sql.QueryUtil.quotedIdentifier;
import static io.prestosql.sql.QueryUtil.row;
import static io.prestosql.sql.QueryUtil.selectList;
import static io.prestosql.sql.QueryUtil.simpleQuery;
import static io.prestosql.sql.QueryUtil.subquery;
import static io.prestosql.sql.QueryUtil.table;
import static io.prestosql.sql.QueryUtil.values;
import static io.prestosql.sql.SqlFormatter.formatSql;
import static io.prestosql.sql.parser.IdentifierSymbol.AT_SIGN;
import static io.prestosql.sql.parser.IdentifierSymbol.COLON;
import static io.prestosql.sql.parser.ParsingOptions.DecimalLiteralTreatment.AS_DECIMAL;
import static io.prestosql.sql.testing.TreeAssertions.assertFormattedSql;
import static io.prestosql.sql.tree.ArithmeticUnaryExpression.negative;
import static io.prestosql.sql.tree.ArithmeticUnaryExpression.positive;
import static io.prestosql.sql.tree.ComparisonExpression.Operator.GREATER_THAN;
import static io.prestosql.sql.tree.ComparisonExpression.Operator.LESS_THAN;
import static io.prestosql.sql.tree.SortItem.NullOrdering.UNDEFINED;
import static io.prestosql.sql.tree.SortItem.Ordering.ASCENDING;
import static io.prestosql.sql.tree.SortItem.Ordering.DESCENDING;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class TestSqlParser
{
    private static final SqlParser SQL_PARSER = new SqlParser();

    @Test
    public void testPosition()
    {
        assertExpression("position('a' in 'b')",
                new FunctionCall(QualifiedName.of("strpos"), ImmutableList.of(
                        new StringLiteral("b"),
                        new StringLiteral("a"))));

        assertExpression("position('a' in ('b'))",
                new FunctionCall(QualifiedName.of("strpos"), ImmutableList.of(
                        new StringLiteral("b"),
                        new StringLiteral("a"))));
    }

    @Test
    public void testPossibleExponentialBacktracking()
    {
        SQL_PARSER.createExpression("(((((((((((((((((((((((((((true)))))))))))))))))))))))))))");
    }

    @Test(timeOut = 2_000)
    public void testPotentialUnboundedLookahead()
    {
        SQL_PARSER.createExpression("(\n" +
                "      1 * -1 +\n" +
                "      1 * -2 +\n" +
                "      1 * -3 +\n" +
                "      1 * -4 +\n" +
                "      1 * -5 +\n" +
                "      1 * -6 +\n" +
                "      1 * -7 +\n" +
                "      1 * -8 +\n" +
                "      1 * -9 +\n" +
                "      1 * -10 +\n" +
                "      1 * -11 +\n" +
                "      1 * -12 \n" +
                ")\n");
    }

    @Test
    public void testQualifiedName()
    {
        assertEquals(QualifiedName.of("a", "b", "c", "d").toString(), "a.b.c.d");
        assertEquals(QualifiedName.of("A", "b", "C", "d").toString(), "a.b.c.d");
        assertTrue(QualifiedName.of("a", "b", "c", "d").hasSuffix(QualifiedName.of("b", "c", "d")));
        assertTrue(QualifiedName.of("a", "b", "c", "d").hasSuffix(QualifiedName.of("a", "b", "c", "d")));
        assertFalse(QualifiedName.of("a", "b", "c", "d").hasSuffix(QualifiedName.of("a", "c", "d")));
        assertFalse(QualifiedName.of("a", "b", "c", "d").hasSuffix(QualifiedName.of("z", "a", "b", "c", "d")));
        assertEquals(QualifiedName.of("a", "b", "c", "d"), QualifiedName.of("a", "b", "c", "d"));
    }

    @Test
    public void testGenericLiteral()
    {
        assertGenericLiteral("VARCHAR");
        assertGenericLiteral("BIGINT");
        assertGenericLiteral("DOUBLE");
        assertGenericLiteral("BOOLEAN");
        assertGenericLiteral("DATE");
        assertGenericLiteral("foo");
    }

    @Test
    public void testBinaryLiteral()
    {
        assertExpression("x' '", new BinaryLiteral(""));
        assertExpression("x''", new BinaryLiteral(""));
        assertExpression("X'abcdef1234567890ABCDEF'", new BinaryLiteral("abcdef1234567890ABCDEF"));

        // forms such as "X 'a b' " may look like BinaryLiteral
        // but they do not pass the syntax rule for BinaryLiteral
        // but instead conform to TypeConstructor, which generates a GenericLiteral expression
        assertInvalidExpression("X 'a b'", "Spaces are not allowed.*");
        assertInvalidExpression("X'a b c'", "Binary literal must contain an even number of digits.*");
        assertInvalidExpression("X'a z'", "Binary literal can only contain hexadecimal digits.*");
    }

    public static void assertGenericLiteral(String type)
    {
        assertExpression(type + " 'abc'", new GenericLiteral(type, "abc"));
    }

    @Test
    public void testLiterals()
    {
        assertExpression("TIME" + " 'abc'", new TimeLiteral("abc"));
        assertExpression("TIMESTAMP" + " 'abc'", new TimestampLiteral("abc"));
        assertExpression("INTERVAL '33' day", new IntervalLiteral("33", Sign.POSITIVE, IntervalField.DAY, Optional.empty()));
        assertExpression("INTERVAL '33' day to second", new IntervalLiteral("33", Sign.POSITIVE, IntervalField.DAY, Optional.of(IntervalField.SECOND)));
        assertExpression("CHAR 'abc'", new CharLiteral("abc"));
    }

    @Test
    public void testNumbers()
    {
        assertExpression("9223372036854775807", new LongLiteral("9223372036854775807"));
        assertExpression("-9223372036854775808", new LongLiteral("-9223372036854775808"));

        assertExpression("1E5", new DoubleLiteral("1E5"));
        assertExpression("1E-5", new DoubleLiteral("1E-5"));
        assertExpression(".1E5", new DoubleLiteral(".1E5"));
        assertExpression(".1E-5", new DoubleLiteral(".1E-5"));
        assertExpression("1.1E5", new DoubleLiteral("1.1E5"));
        assertExpression("1.1E-5", new DoubleLiteral("1.1E-5"));

        assertExpression("-1E5", new DoubleLiteral("-1E5"));
        assertExpression("-1E-5", new DoubleLiteral("-1E-5"));
        assertExpression("-.1E5", new DoubleLiteral("-.1E5"));
        assertExpression("-.1E-5", new DoubleLiteral("-.1E-5"));
        assertExpression("-1.1E5", new DoubleLiteral("-1.1E5"));
        assertExpression("-1.1E-5", new DoubleLiteral("-1.1E-5"));

        assertExpression(".1", new DecimalLiteral(".1"));
        assertExpression("1.2", new DecimalLiteral("1.2"));
        assertExpression("-1.2", new DecimalLiteral("-1.2"));
    }

    @Test
    public void testArrayConstructor()
    {
        assertExpression("ARRAY []", new ArrayConstructor(ImmutableList.of()));
        assertExpression("ARRAY [1, 2]", new ArrayConstructor(ImmutableList.of(new LongLiteral("1"), new LongLiteral("2"))));
        assertExpression("ARRAY [1e0, 2.5e0]", new ArrayConstructor(ImmutableList.of(new DoubleLiteral("1.0"), new DoubleLiteral("2.5"))));
        assertExpression("ARRAY ['hi']", new ArrayConstructor(ImmutableList.of(new StringLiteral("hi"))));
        assertExpression("ARRAY ['hi', 'hello']", new ArrayConstructor(ImmutableList.of(new StringLiteral("hi"), new StringLiteral("hello"))));
    }

    @Test
    public void testArraySubscript()
    {
        assertExpression("ARRAY [1, 2][1]", new SubscriptExpression(
                new ArrayConstructor(ImmutableList.of(new LongLiteral("1"), new LongLiteral("2"))),
                new LongLiteral("1")));
        try {
            assertExpression("CASE WHEN TRUE THEN ARRAY[1,2] END[1]", null);
            fail();
        }
        catch (RuntimeException e) {
            // Expected
        }
    }

    @Test
    public void testRowSubscript()
    {
        assertExpression("ROW (1, 'a', true)[1]", new SubscriptExpression(
                new Row(ImmutableList.of(new LongLiteral("1"), new StringLiteral("a"), new BooleanLiteral("true"))),
                new LongLiteral("1")));
    }

    @Test
    public void testDouble()
    {
        assertExpression("123E7", new DoubleLiteral("123E7"));
        assertExpression("123.E7", new DoubleLiteral("123E7"));
        assertExpression("123.0E7", new DoubleLiteral("123E7"));
        assertExpression("123E+7", new DoubleLiteral("123E7"));
        assertExpression("123E-7", new DoubleLiteral("123E-7"));

        assertExpression("123.456E7", new DoubleLiteral("123.456E7"));
        assertExpression("123.456E+7", new DoubleLiteral("123.456E7"));
        assertExpression("123.456E-7", new DoubleLiteral("123.456E-7"));

        assertExpression(".4E42", new DoubleLiteral(".4E42"));
        assertExpression(".4E+42", new DoubleLiteral(".4E42"));
        assertExpression(".4E-42", new DoubleLiteral(".4E-42"));
    }

    @Test
    public void testCast()
    {
        assertCast("foo(42, 55) ARRAY", "ARRAY(foo(42,55))");
        assertCast("varchar");
        assertCast("bigint");
        assertCast("BIGINT");
        assertCast("double");
        assertCast("DOUBLE");
        assertCast("DOUBLE PRECISION", "DOUBLE");
        assertCast("DOUBLE   PRECISION", "DOUBLE");
        assertCast("double precision", "DOUBLE");
        assertCast("boolean");
        assertCast("date");
        assertCast("time");
        assertCast("timestamp");
        assertCast("time with time zone");
        assertCast("timestamp with time zone");
        assertCast("foo");
        assertCast("FOO");

        assertCast("ARRAY<bigint>", "ARRAY(bigint)");
        assertCast("ARRAY<BIGINT>", "ARRAY(BIGINT)");
        assertCast("array<bigint>", "array(bigint)");
        assertCast("array < bigint  >", "ARRAY(bigint)");

        assertCast("ARRAY(bigint)");
        assertCast("ARRAY(BIGINT)");
        assertCast("array(bigint)");
        assertCast("array ( bigint  )", "ARRAY(bigint)");

        assertCast("array<array<bigint>>", "array(array(bigint))");
        assertCast("array(array(bigint))");

        assertCast("foo ARRAY", "ARRAY(foo)");
        assertCast("boolean array  array ARRAY", "ARRAY(ARRAY(ARRAY(boolean)))");
        assertCast("boolean ARRAY ARRAY ARRAY", "ARRAY(ARRAY(ARRAY(boolean)))");
        assertCast("ARRAY<boolean> ARRAY ARRAY", "ARRAY(ARRAY(ARRAY(boolean)))");

        assertCast("map(BIGINT,array(VARCHAR))");
        assertCast("map<BIGINT,array<VARCHAR>>", "map(BIGINT,array(VARCHAR))");

        assertCast("varchar(42)");
        assertCast("foo(42,55)");
        assertCast("foo(BIGINT,array(VARCHAR))");
        assertCast("ARRAY<varchar(42)>", "ARRAY(varchar(42))");
        assertCast("ARRAY<foo(42,55)>", "ARRAY(foo(42,55))");
        assertCast("varchar(42) ARRAY", "ARRAY(varchar(42))");
        assertCast("foo(42, 55) ARRAY", "ARRAY(foo(42,55))");

        assertCast("ROW(m DOUBLE)", "ROW(m DOUBLE)");
        assertCast("ROW(m DOUBLE)");
        assertCast("ROW(x BIGINT,y DOUBLE)");
        assertCast("ROW(x BIGINT, y DOUBLE)", "ROW(x bigint,y double)");
        assertCast("ROW(x BIGINT, y DOUBLE, z ROW(m array<bigint>,n map<double,timestamp>))", "ROW(x BIGINT,y DOUBLE,z ROW(m array(bigint),n map(double,timestamp)))");
        assertCast("array<ROW(x BIGINT, y TIMESTAMP)>", "ARRAY(ROW(x BIGINT,y TIMESTAMP))");

        assertCast("interval year to month", "INTERVAL YEAR TO MONTH");
    }

    @Test
    public void testArithmeticUnary()
    {
        assertExpression("9", new LongLiteral("9"));

        assertExpression("+9", positive(new LongLiteral("9")));
        assertExpression("+ 9", positive(new LongLiteral("9")));

        assertExpression("++9", positive(positive(new LongLiteral("9"))));
        assertExpression("+ +9", positive(positive(new LongLiteral("9"))));
        assertExpression("+ + 9", positive(positive(new LongLiteral("9"))));

        assertExpression("+++9", positive(positive(positive(new LongLiteral("9")))));
        assertExpression("+ + +9", positive(positive(positive(new LongLiteral("9")))));
        assertExpression("+ + + 9", positive(positive(positive(new LongLiteral("9")))));

        assertExpression("-9", new LongLiteral("-9"));
        assertExpression("- 9", new LongLiteral("-9"));

        assertExpression("- + 9", negative(positive(new LongLiteral("9"))));
        assertExpression("-+9", negative(positive(new LongLiteral("9"))));

        assertExpression("+ - + 9", positive(negative(positive(new LongLiteral("9")))));
        assertExpression("+-+9", positive(negative(positive(new LongLiteral("9")))));

        assertExpression("- -9", negative(new LongLiteral("-9")));
        assertExpression("- - 9", negative(new LongLiteral("-9")));

        assertExpression("- + - + 9", negative(positive(negative(positive(new LongLiteral("9"))))));
        assertExpression("-+-+9", negative(positive(negative(positive(new LongLiteral("9"))))));

        assertExpression("+ - + - + 9", positive(negative(positive(negative(positive(new LongLiteral("9")))))));
        assertExpression("+-+-+9", positive(negative(positive(negative(positive(new LongLiteral("9")))))));

        assertExpression("- - -9", negative(negative(new LongLiteral("-9"))));
        assertExpression("- - - 9", negative(negative(new LongLiteral("-9"))));
    }

    @Test
    public void testCoalesce()
    {
        assertInvalidExpression("coalesce()", "The 'coalesce' function must have at least two arguments");
        assertInvalidExpression("coalesce(5)", "The 'coalesce' function must have at least two arguments");
        assertInvalidExpression("coalesce(1, 2) filter (where true)", "FILTER not valid for 'coalesce' function");
        assertInvalidExpression("coalesce(1, 2) OVER ()", "OVER clause not valid for 'coalesce' function");
        assertExpression("coalesce(13, 42)", new CoalesceExpression(new LongLiteral("13"), new LongLiteral("42")));
        assertExpression("coalesce(6, 7, 8)", new CoalesceExpression(new LongLiteral("6"), new LongLiteral("7"), new LongLiteral("8")));
        assertExpression("coalesce(13, null)", new CoalesceExpression(new LongLiteral("13"), new NullLiteral()));
        assertExpression("coalesce(null, 13)", new CoalesceExpression(new NullLiteral(), new LongLiteral("13")));
        assertExpression("coalesce(null, null)", new CoalesceExpression(new NullLiteral(), new NullLiteral()));
    }

    @Test
    public void testIf()
    {
        assertExpression("if(true, 1, 0)", new IfExpression(new BooleanLiteral("true"), new LongLiteral("1"), new LongLiteral("0")));
        assertExpression("if(true, 3, null)", new IfExpression(new BooleanLiteral("true"), new LongLiteral("3"), new NullLiteral()));
        assertExpression("if(false, null, 4)", new IfExpression(new BooleanLiteral("false"), new NullLiteral(), new LongLiteral("4")));
        assertExpression("if(false, null, null)", new IfExpression(new BooleanLiteral("false"), new NullLiteral(), new NullLiteral()));
        assertExpression("if(true, 3)", new IfExpression(new BooleanLiteral("true"), new LongLiteral("3"), null));
        assertInvalidExpression("IF(true)", "Invalid number of arguments for 'if' function");
        assertInvalidExpression("IF(true, 1, 0) FILTER (WHERE true)", "FILTER not valid for 'if' function");
        assertInvalidExpression("IF(true, 1, 0) OVER()", "OVER clause not valid for 'if' function");
    }

    @Test
    public void testNullIf()
    {
        assertExpression("nullif(42, 87)", new NullIfExpression(new LongLiteral("42"), new LongLiteral("87")));
        assertExpression("nullif(42, null)", new NullIfExpression(new LongLiteral("42"), new NullLiteral()));
        assertExpression("nullif(null, null)", new NullIfExpression(new NullLiteral(), new NullLiteral()));
        assertInvalidExpression("nullif(1)", "Invalid number of arguments for 'nullif' function");
        assertInvalidExpression("nullif(1, 2, 3)", "Invalid number of arguments for 'nullif' function");
        assertInvalidExpression("nullif(42, 87) filter (where true)", "FILTER not valid for 'nullif' function");
        assertInvalidExpression("nullif(42, 87) OVER ()", "OVER clause not valid for 'nullif' function");
    }

    @Test
    public void testDoubleInQuery()
    {
        assertStatement("SELECT 123.456E7 FROM DUAL",
                simpleQuery(
                        selectList(new DoubleLiteral("123.456E7")),
                        table(QualifiedName.of("DUAL"))));
    }

    @Test
    public void testIntersect()
    {
        assertStatement("SELECT 123 INTERSECT DISTINCT SELECT 123 INTERSECT ALL SELECT 123",
                new Query(
                        Optional.empty(),
                        new Intersect(ImmutableList.of(
                                new Intersect(ImmutableList.of(createSelect123(), createSelect123()), true),
                                createSelect123()
                        ), false),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testUnion()
    {
        assertStatement("SELECT 123 UNION DISTINCT SELECT 123 UNION ALL SELECT 123",
                new Query(
                        Optional.empty(),
                        new Union(ImmutableList.of(
                                new Union(ImmutableList.of(createSelect123(), createSelect123()), true),
                                createSelect123()
                        ), false),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    private static QuerySpecification createSelect123()
    {
        return new QuerySpecification(
                selectList(new LongLiteral("123")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    @Test
    public void testReservedWordIdentifier()
    {
        assertStatement("SELECT id FROM public.orders",
                simpleQuery(
                        selectList(identifier("id")),
                        new Table(QualifiedName.of("public", "orders"))));

        assertStatement("SELECT id FROM \"public\".\"order\"",
                simpleQuery(
                        selectList(identifier("id")),
                        new Table(QualifiedName.of(ImmutableList.of(
                                new Identifier("public", true),
                                new Identifier("order", true))))));

        assertStatement("SELECT id FROM \"public\".\"order\"\"2\"",
                simpleQuery(
                        selectList(identifier("id")),
                        new Table(QualifiedName.of(ImmutableList.of(
                                new Identifier("public", true),
                                new Identifier("order\"2", true))))));
    }

    @Test
    public void testBetween()
    {
        assertExpression("1 BETWEEN 2 AND 3", new BetweenPredicate(new LongLiteral("1"), new LongLiteral("2"), new LongLiteral("3")));
        assertExpression("1 NOT BETWEEN 2 AND 3", new NotExpression(new BetweenPredicate(new LongLiteral("1"), new LongLiteral("2"), new LongLiteral("3"))));
    }

    @Test
    public void testSelectWithLimit()
    {
        assertStatement("SELECT * FROM table1 LIMIT 2",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new Limit("2"))),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 LIMIT ALL",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new Limit("ALL"))),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        Query valuesQuery = query(values(
                row(new LongLiteral("1"), new StringLiteral("1")),
                row(new LongLiteral("2"), new StringLiteral("2"))));

        assertStatement("SELECT * FROM (VALUES (1, '1'), (2, '2')) LIMIT ALL",
                simpleQuery(selectList(new AllColumns()),
                        subquery(valuesQuery),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new Limit("ALL"))));
    }

    @Test
    public void testValues()
    {
        Query valuesQuery = query(values(
                row(new StringLiteral("a"), new LongLiteral("1"), new DoubleLiteral("2.2")),
                row(new StringLiteral("b"), new LongLiteral("2"), new DoubleLiteral("3.3"))));

        assertStatement("VALUES ('a', 1, 2.2e0), ('b', 2, 3.3e0)", valuesQuery);

        assertStatement("SELECT * FROM (VALUES ('a', 1, 2.2e0), ('b', 2, 3.3e0))",
                simpleQuery(
                        selectList(new AllColumns()),
                        subquery(valuesQuery)));
    }

    @Test
    public void testPrecedenceAndAssociativity()
    {
        assertExpression("1 AND 2 OR 3", new LogicalBinaryExpression(LogicalBinaryExpression.Operator.OR,
                new LogicalBinaryExpression(LogicalBinaryExpression.Operator.AND,
                        new LongLiteral("1"),
                        new LongLiteral("2")),
                new LongLiteral("3")));

        assertExpression("1 OR 2 AND 3", new LogicalBinaryExpression(LogicalBinaryExpression.Operator.OR,
                new LongLiteral("1"),
                new LogicalBinaryExpression(LogicalBinaryExpression.Operator.AND,
                        new LongLiteral("2"),
                        new LongLiteral("3"))));

        assertExpression("NOT 1 AND 2", new LogicalBinaryExpression(LogicalBinaryExpression.Operator.AND,
                new NotExpression(new LongLiteral("1")),
                new LongLiteral("2")));

        assertExpression("NOT 1 OR 2", new LogicalBinaryExpression(LogicalBinaryExpression.Operator.OR,
                new NotExpression(new LongLiteral("1")),
                new LongLiteral("2")));

        assertExpression("-1 + 2", new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.ADD,
                new LongLiteral("-1"),
                new LongLiteral("2")));

        assertExpression("1 - 2 - 3", new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.SUBTRACT,
                new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.SUBTRACT,
                        new LongLiteral("1"),
                        new LongLiteral("2")),
                new LongLiteral("3")));

        assertExpression("1 / 2 / 3", new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.DIVIDE,
                new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.DIVIDE,
                        new LongLiteral("1"),
                        new LongLiteral("2")),
                new LongLiteral("3")));

        assertExpression("1 + 2 * 3", new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.ADD,
                new LongLiteral("1"),
                new ArithmeticBinaryExpression(ArithmeticBinaryExpression.Operator.MULTIPLY,
                        new LongLiteral("2"),
                        new LongLiteral("3"))));
    }

    @Test
    public void testAllowIdentifierColon()
    {
        SqlParser sqlParser = new SqlParser(new SqlParserOptions().allowIdentifierSymbol(COLON));
        sqlParser.createStatement("select * from foo:bar");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAllowIdentifierAtSign()
    {
        SqlParser sqlParser = new SqlParser(new SqlParserOptions().allowIdentifierSymbol(AT_SIGN));
        sqlParser.createStatement("select * from foo@bar");
    }

    @Test
    public void testInterval()
    {
        assertExpression("INTERVAL '123' YEAR", new IntervalLiteral("123", Sign.POSITIVE, IntervalField.YEAR));
        assertExpression("INTERVAL '123-3' YEAR TO MONTH", new IntervalLiteral("123-3", Sign.POSITIVE, IntervalField.YEAR, Optional.of(IntervalField.MONTH)));
        assertExpression("INTERVAL '123' MONTH", new IntervalLiteral("123", Sign.POSITIVE, IntervalField.MONTH));
        assertExpression("INTERVAL '123' DAY", new IntervalLiteral("123", Sign.POSITIVE, IntervalField.DAY));
        assertExpression("INTERVAL '123 23:58:53.456' DAY TO SECOND", new IntervalLiteral("123 23:58:53.456", Sign.POSITIVE, IntervalField.DAY, Optional.of(IntervalField.SECOND)));
        assertExpression("INTERVAL '123' HOUR", new IntervalLiteral("123", Sign.POSITIVE, IntervalField.HOUR));
        assertExpression("INTERVAL '23:59' HOUR TO MINUTE", new IntervalLiteral("23:59", Sign.POSITIVE, IntervalField.HOUR, Optional.of(IntervalField.MINUTE)));
        assertExpression("INTERVAL '123' MINUTE", new IntervalLiteral("123", Sign.POSITIVE, IntervalField.MINUTE));
        assertExpression("INTERVAL '123' SECOND", new IntervalLiteral("123", Sign.POSITIVE, IntervalField.SECOND));
    }

    @Test
    public void testDecimal()
    {
        assertExpression("DECIMAL '12.34'", new DecimalLiteral("12.34"));
        assertExpression("DECIMAL '12.'", new DecimalLiteral("12."));
        assertExpression("DECIMAL '12'", new DecimalLiteral("12"));
        assertExpression("DECIMAL '.34'", new DecimalLiteral(".34"));
        assertExpression("DECIMAL '+12.34'", new DecimalLiteral("+12.34"));
        assertExpression("DECIMAL '+12'", new DecimalLiteral("+12"));
        assertExpression("DECIMAL '-12.34'", new DecimalLiteral("-12.34"));
        assertExpression("DECIMAL '-12'", new DecimalLiteral("-12"));
        assertExpression("DECIMAL '+.34'", new DecimalLiteral("+.34"));
        assertExpression("DECIMAL '-.34'", new DecimalLiteral("-.34"));

        assertInvalidExpression("123.", "Unexpected decimal literal: 123.");
        assertInvalidExpression("123.0", "Unexpected decimal literal: 123.0");
        assertInvalidExpression(".5", "Unexpected decimal literal: .5");
        assertInvalidExpression("123.5", "Unexpected decimal literal: 123.5");
    }

    @Test
    public void testTime()
    {
        assertExpression("TIME '03:04:05'", new TimeLiteral("03:04:05"));
    }

    @Test
    public void testCurrentTimestamp()
    {
        assertExpression("CURRENT_TIMESTAMP", new CurrentTime(CurrentTime.Function.TIMESTAMP));
    }

    @Test
    public void testFormat()
    {
        assertExpression("format('%s', 'abc')", new Format(ImmutableList.of(new StringLiteral("%s"), new StringLiteral("abc"))));
        assertExpression("format('%d %s', 123, 'x')", new Format(ImmutableList.of(new StringLiteral("%d %s"), new LongLiteral("123"), new StringLiteral("x"))));

        assertInvalidExpression("format()", "The 'format' function must have at least two arguments");
        assertInvalidExpression("format('%s')", "The 'format' function must have at least two arguments");
    }

    @Test
    public void testSetSession()
    {
        assertStatement("SET SESSION foo = 'bar'", new SetSession(QualifiedName.of("foo"), new StringLiteral("bar")));
        assertStatement("SET SESSION foo.bar = 'baz'", new SetSession(QualifiedName.of("foo", "bar"), new StringLiteral("baz")));
        assertStatement("SET SESSION foo.bar.boo = 'baz'", new SetSession(QualifiedName.of("foo", "bar", "boo"), new StringLiteral("baz")));

        assertStatement("SET SESSION foo.bar = 'ban' || 'ana'", new SetSession(
                QualifiedName.of("foo", "bar"),
                new FunctionCall(QualifiedName.of("concat"), ImmutableList.of(
                        new StringLiteral("ban"),
                        new StringLiteral("ana")))));
    }

    @Test
    public void testResetSession()
    {
        assertStatement("RESET SESSION foo.bar", new ResetSession(QualifiedName.of("foo", "bar")));
        assertStatement("RESET SESSION foo", new ResetSession(QualifiedName.of("foo")));
    }

    @Test
    public void testShowSession()
    {
        assertStatement("SHOW SESSION", new ShowSession());
    }

    @Test
    public void testShowCatalogs()
    {
        assertStatement("SHOW CATALOGS", new ShowCatalogs(Optional.empty()));
        assertStatement("SHOW CATALOGS LIKE '%'", new ShowCatalogs(Optional.of("%")));
    }

    @Test
    public void testShowSchemas()
    {
        assertStatement("SHOW SCHEMAS", new ShowSchemas(Optional.empty(), Optional.empty(), Optional.empty()));
        assertStatement("SHOW SCHEMAS FROM foo", new ShowSchemas(Optional.of(identifier("foo")), Optional.empty(), Optional.empty()));
        assertStatement("SHOW SCHEMAS IN foo LIKE '%'", new ShowSchemas(Optional.of(identifier("foo")), Optional.of("%"), Optional.empty()));
        assertStatement("SHOW SCHEMAS IN foo LIKE '%$_%' ESCAPE '$'", new ShowSchemas(Optional.of(identifier("foo")), Optional.of("%$_%"), Optional.of("$")));
    }

    @Test
    public void testShowTables()
    {
        assertStatement("SHOW TABLES", new ShowTables(Optional.empty(), Optional.empty(), Optional.empty()));
        assertStatement("SHOW TABLES FROM a", new ShowTables(Optional.of(QualifiedName.of("a")), Optional.empty(), Optional.empty()));
        assertStatement("SHOW TABLES FROM \"awesome schema\"", new ShowTables(Optional.of(QualifiedName.of("awesome schema")), Optional.empty(), Optional.empty()));
        assertStatement("SHOW TABLES IN a LIKE '%$_%' ESCAPE '$'", new ShowTables(Optional.of(QualifiedName.of("a")), Optional.of("%$_%"), Optional.of("$")));
    }

    @Test
    public void testShowColumns()
    {
        assertStatement("SHOW COLUMNS FROM a", new ShowColumns(QualifiedName.of("a")));
        assertStatement("SHOW COLUMNS FROM a.b", new ShowColumns(QualifiedName.of("a", "b")));
        assertStatement("SHOW COLUMNS FROM \"awesome table\"", new ShowColumns(QualifiedName.of("awesome table")));
        assertStatement("SHOW COLUMNS FROM \"awesome schema\".\"awesome table\"", new ShowColumns(QualifiedName.of("awesome schema", "awesome table")));
    }

    @Test
    public void testSubstringBuiltInFunction()
    {
        final String givenString = "ABCDEF";
        assertStatement(format("SELECT substring('%s' FROM 2)", givenString),
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new FunctionCall(QualifiedName.of("substr"), Lists.newArrayList(new StringLiteral(givenString), new LongLiteral("2")))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement(format("SELECT substring('%s' FROM 2 FOR 3)", givenString),
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new FunctionCall(QualifiedName.of("substr"), Lists.newArrayList(new StringLiteral(givenString), new LongLiteral("2"), new LongLiteral("3")))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testSubstringRegisteredFunction()
    {
        final String givenString = "ABCDEF";
        assertStatement(format("SELECT substring('%s', 2)", givenString),
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new FunctionCall(QualifiedName.of("substring"), Lists.newArrayList(new StringLiteral(givenString), new LongLiteral("2")))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement(format("SELECT substring('%s', 2, 3)", givenString),
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new FunctionCall(QualifiedName.of("substring"), Lists.newArrayList(new StringLiteral(givenString), new LongLiteral("2"), new LongLiteral("3")))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testSelectWithRowType()
    {
        assertStatement("SELECT col1.f1, col2, col3.f1.f2.f3 FROM table1",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        new DereferenceExpression(new Identifier("col1"), identifier("f1")),
                                        new Identifier("col2"),
                                        new DereferenceExpression(
                                                new DereferenceExpression(new DereferenceExpression(new Identifier("col3"), identifier("f1")), identifier("f2")), identifier("f3"))),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT col1.f1[0], col2, col3[2].f2.f3, col4[4] FROM table1",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        new SubscriptExpression(new DereferenceExpression(new Identifier("col1"), identifier("f1")), new LongLiteral("0")),
                                        new Identifier("col2"),
                                        new DereferenceExpression(new DereferenceExpression(new SubscriptExpression(new Identifier("col3"), new LongLiteral("2")), identifier("f2")), identifier("f3")),
                                        new SubscriptExpression(new Identifier("col4"), new LongLiteral("4"))),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT CAST(ROW(11, 12) AS ROW(COL0 INTEGER, COL1 INTEGER)).col0",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        new DereferenceExpression(new Cast(new Row(Lists.newArrayList(new LongLiteral("11"), new LongLiteral("12"))), "ROW(COL0 INTEGER,COL1 INTEGER)"), identifier("col0"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testSelectWithOrderBy()
    {
        assertStatement("SELECT * FROM table1 ORDER BY a",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new OrderBy(ImmutableList.of(new SortItem(
                                        new Identifier("a"),
                                        ASCENDING,
                                        UNDEFINED)))),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testSelectWithOffset()
    {
        assertStatement("SELECT * FROM table1 OFFSET 2 ROWS",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new Offset("2")),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 OFFSET 2",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new Offset("2")),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        Query valuesQuery = query(values(
                row(new LongLiteral("1"), new StringLiteral("1")),
                row(new LongLiteral("2"), new StringLiteral("2"))));

        assertStatement("SELECT * FROM (VALUES (1, '1'), (2, '2')) OFFSET 2 ROWS",
                simpleQuery(selectList(new AllColumns()),
                        subquery(valuesQuery),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new Offset("2")),
                        Optional.empty()));

        assertStatement("SELECT * FROM (VALUES (1, '1'), (2, '2')) OFFSET 2",
                simpleQuery(selectList(new AllColumns()),
                        subquery(valuesQuery),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new Offset("2")),
                        Optional.empty()));
    }

    @Test
    public void testSelectWithFetch()
    {
        assertStatement("SELECT * FROM table1 FETCH FIRST 2 ROWS ONLY",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new FetchFirst("2"))),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 FETCH NEXT ROW ONLY",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new FetchFirst(Optional.empty()))),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        Query valuesQuery = query(values(
                row(new LongLiteral("1"), new StringLiteral("1")),
                row(new LongLiteral("2"), new StringLiteral("2"))));

        assertStatement("SELECT * FROM (VALUES (1, '1'), (2, '2')) FETCH FIRST ROW ONLY",
                simpleQuery(selectList(new AllColumns()),
                        subquery(valuesQuery),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new FetchFirst(Optional.empty()))));

        assertStatement("SELECT * FROM (VALUES (1, '1'), (2, '2')) FETCH FIRST ROW WITH TIES",
                simpleQuery(selectList(new AllColumns()),
                        subquery(valuesQuery),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new FetchFirst(Optional.empty(), true))));

        assertStatement("SELECT * FROM table1 FETCH FIRST 2 ROWS WITH TIES",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new FetchFirst("2", true))),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 FETCH NEXT ROW WITH TIES",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(new FetchFirst(Optional.empty(), true))),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testSelectWithGroupBy()
    {
        assertStatement("SELECT * FROM table1 GROUP BY a",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(false, ImmutableList.of(new SimpleGroupBy(ImmutableList.of(new Identifier("a")))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 GROUP BY a, b",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(false, ImmutableList.of(
                                        new SimpleGroupBy(ImmutableList.of(new Identifier("a"))),
                                        new SimpleGroupBy(ImmutableList.of(new Identifier("b")))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 GROUP BY ()",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(false, ImmutableList.of(new SimpleGroupBy(ImmutableList.of())))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 GROUP BY GROUPING SETS (a)",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(false, ImmutableList.of(new GroupingSets(ImmutableList.of(ImmutableList.of(new Identifier("a"))))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT a, b, GROUPING(a, b) FROM table1 GROUP BY GROUPING SETS ((a), (b))",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        DereferenceExpression.from(QualifiedName.of("a")),
                                        DereferenceExpression.from(QualifiedName.of("b")),
                                        new GroupingOperation(
                                                Optional.empty(),
                                                ImmutableList.of(QualifiedName.of("a"), QualifiedName.of("b")))),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(false, ImmutableList.of(new GroupingSets(ImmutableList.of(ImmutableList.of(new Identifier("a")), ImmutableList.of(new Identifier("b"))))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 GROUP BY ALL GROUPING SETS ((a, b), (a), ()), CUBE (c), ROLLUP (d)",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(false, ImmutableList.of(
                                        new GroupingSets(
                                                ImmutableList.of(ImmutableList.of(new Identifier("a"), new Identifier("b")),
                                                        ImmutableList.of(new Identifier("a")),
                                                        ImmutableList.of())),
                                        new Cube(ImmutableList.of(new Identifier("c"))),
                                        new Rollup(ImmutableList.of(new Identifier("d")))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("SELECT * FROM table1 GROUP BY DISTINCT GROUPING SETS ((a, b), (a), ()), CUBE (c), ROLLUP (d)",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(new AllColumns()),
                                Optional.of(new Table(QualifiedName.of("table1"))),
                                Optional.empty(),
                                Optional.of(new GroupBy(true, ImmutableList.of(
                                        new GroupingSets(
                                                ImmutableList.of(ImmutableList.of(new Identifier("a"), new Identifier("b")),
                                                        ImmutableList.of(new Identifier("a")),
                                                        ImmutableList.of())),
                                        new Cube(ImmutableList.of(new Identifier("c"))),
                                        new Rollup(ImmutableList.of(new Identifier("d")))))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testCreateSchema()
    {
        assertStatement("CREATE SCHEMA test",
                new CreateSchema(QualifiedName.of("test"), false, ImmutableList.of()));

        assertStatement("CREATE SCHEMA IF NOT EXISTS test",
                new CreateSchema(QualifiedName.of("test"), true, ImmutableList.of()));

        assertStatement("CREATE SCHEMA test WITH (a = 'apple', b = 123)",
                new CreateSchema(
                        QualifiedName.of("test"),
                        false,
                        ImmutableList.of(
                                new Property(new Identifier("a"), new StringLiteral("apple")),
                                new Property(new Identifier("b"), new LongLiteral("123")))));

        assertStatement("CREATE SCHEMA \"some name that contains space\"",
                new CreateSchema(QualifiedName.of("some name that contains space"), false, ImmutableList.of()));
    }

    @Test
    public void testDropSchema()
    {
        assertStatement("DROP SCHEMA test",
                new DropSchema(QualifiedName.of("test"), false, false));

        assertStatement("DROP SCHEMA test CASCADE",
                new DropSchema(QualifiedName.of("test"), false, true));

        assertStatement("DROP SCHEMA IF EXISTS test",
                new DropSchema(QualifiedName.of("test"), true, false));

        assertStatement("DROP SCHEMA IF EXISTS test RESTRICT",
                new DropSchema(QualifiedName.of("test"), true, false));

        assertStatement("DROP SCHEMA \"some schema that contains space\"",
                new DropSchema(QualifiedName.of("some schema that contains space"), false, false));
    }

    @Test
    public void testRenameSchema()
    {
        assertStatement("ALTER SCHEMA foo RENAME TO bar",
                new RenameSchema(QualifiedName.of("foo"), identifier("bar")));

        assertStatement("ALTER SCHEMA foo.bar RENAME TO baz",
                new RenameSchema(QualifiedName.of("foo", "bar"), identifier("baz")));

        assertStatement("ALTER SCHEMA \"awesome schema\".\"awesome table\" RENAME TO \"even more awesome table\"",
                new RenameSchema(QualifiedName.of("awesome schema", "awesome table"), quotedIdentifier("even more awesome table")));
    }

    @Test
    public void testUnicodeString()
    {
        assertExpression("U&''", new StringLiteral(""));
        assertExpression("U&'' UESCAPE ')'", new StringLiteral(""));
        assertExpression("U&'hello\\6d4B\\8Bd5\\+10FFFFworld\\7F16\\7801'", new StringLiteral("hello\u6d4B\u8Bd5\uDBFF\uDFFFworld\u7F16\u7801"));
        assertExpression("U&'\u6d4B\u8Bd5ABC\\6d4B\\8Bd5'", new StringLiteral("\u6d4B\u8Bd5ABC\u6d4B\u8Bd5"));
        assertExpression("u&'\u6d4B\u8Bd5ABC\\6d4B\\8Bd5'", new StringLiteral("\u6d4B\u8Bd5ABC\u6d4B\u8Bd5"));
        assertExpression("u&'\u6d4B\u8Bd5ABC\\\\'", new StringLiteral("\u6d4B\u8Bd5ABC\\"));
        assertExpression("u&'\u6d4B\u8Bd5ABC###8Bd5' UESCAPE '#'", new StringLiteral("\u6d4B\u8Bd5ABC#\u8Bd5"));
        assertExpression("u&'\u6d4B\u8Bd5''A''B''C##''''#8Bd5' UESCAPE '#'", new StringLiteral("\u6d4B\u8Bd5\'A\'B\'C#\'\'\u8Bd5"));
        assertInvalidExpression("U&  '\u6d4B\u8Bd5ABC\\\\'", ".*mismatched input.*");
        assertInvalidExpression("u&'\u6d4B\u8Bd5ABC\\'", "Incomplete escape sequence: ");
        assertInvalidExpression("u&'\u6d4B\u8Bd5ABC\\+'", "Incomplete escape sequence: ");
        assertInvalidExpression("U&'hello\\6dB\\8Bd5'", "Incomplete escape sequence: 6dB.*");
        assertInvalidExpression("U&'hello\\6D4B\\8Bd'", "Incomplete escape sequence: 8Bd");
        assertInvalidExpression("U&'hello\\K6B\\8Bd5'", "Invalid hexadecimal digit: K");
        assertInvalidExpression("U&'hello\\+FFFFFD\\8Bd5'", "Invalid escaped character: FFFFFD");
        assertInvalidExpression("U&'hello\\DBFF'", "Invalid escaped character: DBFF\\. Escaped character is a surrogate\\. Use \'\\\\\\+123456\' instead\\.");
        assertInvalidExpression("U&'hello\\+00DBFF'", "Invalid escaped character: 00DBFF\\. Escaped character is a surrogate\\. Use \'\\\\\\+123456\' instead\\.");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE '%%'", "Invalid Unicode escape character: %%");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE '\uDBFF'", "Invalid Unicode escape character: \uDBFF");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE '\n'", "Invalid Unicode escape character: \n");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE ''''", "Invalid Unicode escape character: \'");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE ' '", "Invalid Unicode escape character:  ");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE ''", "Empty Unicode escape character");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE '1'", "Invalid Unicode escape character: 1");
        assertInvalidExpression("U&'hello\\8Bd5' UESCAPE '+'", "Invalid Unicode escape character: \\+");
        assertExpression("U&'hello!6d4B!8Bd5!+10FFFFworld!7F16!7801' UESCAPE '!'", new StringLiteral("hello\u6d4B\u8Bd5\uDBFF\uDFFFworld\u7F16\u7801"));
        assertExpression("U&'\u6d4B\u8Bd5ABC!6d4B!8Bd5' UESCAPE '!'", new StringLiteral("\u6d4B\u8Bd5ABC\u6d4B\u8Bd5"));
        assertExpression("U&'hello\\6d4B\\8Bd5\\+10FFFFworld\\7F16\\7801' UESCAPE '!'",
                new StringLiteral("hello\\6d4B\\8Bd5\\+10FFFFworld\\7F16\\7801"));
    }

    @Test
    public void testCreateTable()
    {
        assertStatement("CREATE TABLE foo (a VARCHAR, b BIGINT COMMENT 'hello world', c IPADDRESS)",
                new CreateTable(QualifiedName.of("foo"),
                        ImmutableList.of(
                                new ColumnDefinition(identifier("a"), "VARCHAR", true, emptyList(), Optional.empty()),
                                new ColumnDefinition(identifier("b"), "BIGINT", true, emptyList(), Optional.of("hello world")),
                                new ColumnDefinition(identifier("c"), "IPADDRESS", true, emptyList(), Optional.empty())),
                        false,
                        ImmutableList.of(),
                        Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS bar (c TIMESTAMP)",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(new ColumnDefinition(identifier("c"), "TIMESTAMP", true, emptyList(), Optional.empty())),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));

        // with properties
        assertStatement("CREATE TABLE IF NOT EXISTS bar (c TIMESTAMP WITH (nullable = true, compression = 'LZ4'))",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(new ColumnDefinition(identifier("c"), "TIMESTAMP", true, ImmutableList.of(
                                new Property(new Identifier("nullable"), BooleanLiteral.TRUE_LITERAL),
                                new Property(new Identifier("compression"), new StringLiteral("LZ4"))
                        ), Optional.empty())),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));

        // with LIKE
        assertStatement("CREATE TABLE IF NOT EXISTS bar (LIKE like_table)",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(
                                new LikeClause(QualifiedName.of("like_table"),
                                        Optional.empty())),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS bar (c TIMESTAMP, LIKE like_table)",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(
                                new ColumnDefinition(identifier("c"), "TIMESTAMP", true, emptyList(), Optional.empty()),
                                new LikeClause(QualifiedName.of("like_table"),
                                        Optional.empty())),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS bar (c TIMESTAMP, LIKE like_table, d DATE)",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(
                                new ColumnDefinition(identifier("c"), "TIMESTAMP", true, emptyList(), Optional.empty()),
                                new LikeClause(QualifiedName.of("like_table"),
                                        Optional.empty()),
                                new ColumnDefinition(identifier("d"), "DATE", true, emptyList(), Optional.empty())),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS bar (LIKE like_table INCLUDING PROPERTIES)",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(
                                new LikeClause(QualifiedName.of("like_table"),
                                        Optional.of(LikeClause.PropertiesOption.INCLUDING))),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS bar (c TIMESTAMP, LIKE like_table EXCLUDING PROPERTIES)",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(
                                new ColumnDefinition(identifier("c"), "TIMESTAMP", true, emptyList(), Optional.empty()),
                                new LikeClause(QualifiedName.of("like_table"),
                                        Optional.of(LikeClause.PropertiesOption.EXCLUDING))),
                        true,
                        ImmutableList.of(),
                        Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS bar (c TIMESTAMP, LIKE like_table EXCLUDING PROPERTIES) COMMENT 'test'",
                new CreateTable(QualifiedName.of("bar"),
                        ImmutableList.of(
                                new ColumnDefinition(identifier("c"), "TIMESTAMP", true, emptyList(), Optional.empty()),
                                new LikeClause(QualifiedName.of("like_table"),
                                        Optional.of(LikeClause.PropertiesOption.EXCLUDING))),
                        true,
                        ImmutableList.of(),
                        Optional.of("test")));
    }

    @Test
    public void testCreateTableWithNotNull()
    {
        assertStatement(
                "CREATE TABLE foo (" +
                        "a VARCHAR NOT NULL COMMENT 'column a', " +
                        "b BIGINT COMMENT 'hello world', " +
                        "c IPADDRESS, " +
                        "d DATE NOT NULL)",
                new CreateTable(
                        QualifiedName.of("foo"),
                        ImmutableList.of(
                                new ColumnDefinition(identifier("a"), "VARCHAR", false, emptyList(), Optional.of("column a")),
                                new ColumnDefinition(identifier("b"), "BIGINT", true, emptyList(), Optional.of("hello world")),
                                new ColumnDefinition(identifier("c"), "IPADDRESS", true, emptyList(), Optional.empty()),
                                new ColumnDefinition(identifier("d"), "DATE", false, emptyList(), Optional.empty())),
                        false,
                        ImmutableList.of(),
                        Optional.empty()));
    }

    @Test
    public void testCreateTableAsSelect()
    {
        Query query = simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t")));
        Query querySelectColumn = simpleQuery(selectList(new Identifier("a")), table(QualifiedName.of("t")));
        Query querySelectColumns = simpleQuery(selectList(new Identifier("a"), new Identifier("b")), table(QualifiedName.of("t")));
        QualifiedName table = QualifiedName.of("foo");

        assertStatement("CREATE TABLE foo AS SELECT * FROM t",
                new CreateTableAsSelect(table, query, false, ImmutableList.of(), true, Optional.empty(), Optional.empty()));
        assertStatement("CREATE TABLE foo(x) AS SELECT a FROM t",
                new CreateTableAsSelect(table, querySelectColumn, false, ImmutableList.of(), true, Optional.of(ImmutableList.of(new Identifier("x"))), Optional.empty()));
        assertStatement("CREATE TABLE foo(x,y) AS SELECT a,b FROM t",
                new CreateTableAsSelect(table, querySelectColumns, false, ImmutableList.of(), true, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.empty()));

        assertStatement("CREATE TABLE IF NOT EXISTS foo AS SELECT * FROM t",
                new CreateTableAsSelect(table, query, true, ImmutableList.of(), true, Optional.empty(), Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS foo(x) AS SELECT a FROM t",
                new CreateTableAsSelect(table, querySelectColumn, true, ImmutableList.of(), true, Optional.of(ImmutableList.of(new Identifier("x"))), Optional.empty()));
        assertStatement("CREATE TABLE IF NOT EXISTS foo(x,y) AS SELECT a,b FROM t",
                new CreateTableAsSelect(table, querySelectColumns, true, ImmutableList.of(), true, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.empty()));

        assertStatement("CREATE TABLE foo AS SELECT * FROM t WITH NO DATA",
                new CreateTableAsSelect(table, query, false, ImmutableList.of(), false, Optional.empty(), Optional.empty()));
        assertStatement("CREATE TABLE foo(x) AS SELECT a FROM t WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumn, false, ImmutableList.of(), false, Optional.of(ImmutableList.of(new Identifier("x"))), Optional.empty()));
        assertStatement("CREATE TABLE foo(x,y) AS SELECT a,b FROM t WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumns, false, ImmutableList.of(), false, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.empty()));

        List<Property> properties = ImmutableList.of(
                new Property(new Identifier("string"), new StringLiteral("bar")),
                new Property(new Identifier("long"), new LongLiteral("42")),
                new Property(
                        new Identifier("computed"),
                        new FunctionCall(QualifiedName.of("concat"), ImmutableList.of(new StringLiteral("ban"), new StringLiteral("ana")))),
                new Property(new Identifier("a"), new ArrayConstructor(ImmutableList.of(new StringLiteral("v1"), new StringLiteral("v2")))));

        assertStatement("CREATE TABLE foo " +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT * FROM t",
                new CreateTableAsSelect(table, query, false, properties, true, Optional.empty(), Optional.empty()));
        assertStatement("CREATE TABLE foo(x) " +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a FROM t",
                new CreateTableAsSelect(table, querySelectColumn, false, properties, true, Optional.of(ImmutableList.of(new Identifier("x"))), Optional.empty()));
        assertStatement("CREATE TABLE foo(x,y) " +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a,b FROM t",
                new CreateTableAsSelect(table, querySelectColumns, false, properties, true, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.empty()));

        assertStatement("CREATE TABLE foo " +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT * FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, query, false, properties, false, Optional.empty(), Optional.empty()));
        assertStatement("CREATE TABLE foo(x) " +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumn, false, properties, false, Optional.of(ImmutableList.of(new Identifier("x"))), Optional.empty()));
        assertStatement("CREATE TABLE foo(x,y) " +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a,b FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumns, false, properties, false, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.empty()));

        assertStatement("CREATE TABLE foo COMMENT 'test'" +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT * FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, query, false, properties, false, Optional.empty(), Optional.of("test")));
        assertStatement("CREATE TABLE foo(x) COMMENT 'test'" +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumn, false, properties, false, Optional.of(ImmutableList.of(new Identifier("x"))), Optional.of("test")));
        assertStatement("CREATE TABLE foo(x,y) COMMENT 'test'" +
                        "WITH ( string = 'bar', long = 42, computed = 'ban' || 'ana', a  = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a,b FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumns, false, properties, false, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.of("test")));
        assertStatement("CREATE TABLE foo(x,y) COMMENT 'test'" +
                        "WITH ( \"string\" = 'bar', \"long\" = 42, computed = 'ban' || 'ana', a = ARRAY[ 'v1', 'v2' ] ) " +
                        "AS " +
                        "SELECT a,b FROM t " +
                        "WITH NO DATA",
                new CreateTableAsSelect(table, querySelectColumns, false, properties, false, Optional.of(ImmutableList.of(new Identifier("x"), new Identifier("y"))), Optional.of("test")));
    }

    @Test
    public void testCreateTableAsWith()
    {
        String queryParenthesizedWith = "CREATE TABLE foo " +
                "AS " +
                "( WITH t(x) AS (VALUES 1) " +
                "TABLE t ) " +
                "WITH NO DATA";
        String queryUnparenthesizedWith = "CREATE TABLE foo " +
                "AS " +
                "WITH t(x) AS (VALUES 1) " +
                "TABLE t " +
                "WITH NO DATA";
        String queryParenthesizedWithHasAlias = "CREATE TABLE foo(a) " +
                "AS " +
                "( WITH t(x) AS (VALUES 1) " +
                "TABLE t ) " +
                "WITH NO DATA";
        String queryUnparenthesizedWithHasAlias = "CREATE TABLE foo(a) " +
                "AS " +
                "WITH t(x) AS (VALUES 1) " +
                "TABLE t " +
                "WITH NO DATA";

        QualifiedName table = QualifiedName.of("foo");

        Query query = new Query(Optional.of(new With(false, ImmutableList.of(
                new WithQuery(identifier("t"),
                        query(new Values(ImmutableList.of(new LongLiteral("1")))),
                        Optional.of(ImmutableList.of(identifier("x"))))))),
                new Table(QualifiedName.of("t")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        assertStatement(queryParenthesizedWith, new CreateTableAsSelect(table, query, false, ImmutableList.of(), false, Optional.empty(), Optional.empty()));
        assertStatement(queryUnparenthesizedWith, new CreateTableAsSelect(table, query, false, ImmutableList.of(), false, Optional.empty(), Optional.empty()));
        assertStatement(queryParenthesizedWithHasAlias, new CreateTableAsSelect(table, query, false, ImmutableList.of(), false, Optional.of(ImmutableList.of(new Identifier("a"))), Optional.empty()));
        assertStatement(queryUnparenthesizedWithHasAlias, new CreateTableAsSelect(table, query, false, ImmutableList.of(), false, Optional.of(ImmutableList.of(new Identifier("a"))), Optional.empty()));
    }

    @Test
    public void testDropTable()
    {
        assertStatement("DROP TABLE a", new DropTable(QualifiedName.of("a"), false));
        assertStatement("DROP TABLE a.b", new DropTable(QualifiedName.of("a", "b"), false));
        assertStatement("DROP TABLE a.b.c", new DropTable(QualifiedName.of("a", "b", "c"), false));

        assertStatement("DROP TABLE IF EXISTS a", new DropTable(QualifiedName.of("a"), true));
        assertStatement("DROP TABLE IF EXISTS a.b", new DropTable(QualifiedName.of("a", "b"), true));
        assertStatement("DROP TABLE IF EXISTS a.b.c", new DropTable(QualifiedName.of("a", "b", "c"), true));
    }

    @Test
    public void testDropView()
    {
        assertStatement("DROP VIEW a", new DropView(QualifiedName.of("a"), false));
        assertStatement("DROP VIEW a.b", new DropView(QualifiedName.of("a", "b"), false));
        assertStatement("DROP VIEW a.b.c", new DropView(QualifiedName.of("a", "b", "c"), false));

        assertStatement("DROP VIEW IF EXISTS a", new DropView(QualifiedName.of("a"), true));
        assertStatement("DROP VIEW IF EXISTS a.b", new DropView(QualifiedName.of("a", "b"), true));
        assertStatement("DROP VIEW IF EXISTS a.b.c", new DropView(QualifiedName.of("a", "b", "c"), true));
    }

    @Test
    public void testInsertInto()
    {
        QualifiedName table = QualifiedName.of("a");
        Query query = simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t")));

        assertStatement("INSERT INTO a SELECT * FROM t",
                new Insert(table, Optional.empty(), query));

        assertStatement("INSERT INTO a (c1, c2) SELECT * FROM t",
                new Insert(table, Optional.of(ImmutableList.of(identifier("c1"), identifier("c2"))), query));
    }

    @Test
    public void testDelete()
    {
        assertStatement("DELETE FROM t", new Delete(table(QualifiedName.of("t")), Optional.empty()));
        assertStatement("DELETE FROM \"awesome table\"", new Delete(table(QualifiedName.of("awesome table")), Optional.empty()));

        assertStatement("DELETE FROM t WHERE a = b", new Delete(table(QualifiedName.of("t")), Optional.of(
                new ComparisonExpression(ComparisonExpression.Operator.EQUAL,
                        new Identifier("a"),
                        new Identifier("b")))));
    }

    @Test
    public void testRenameTable()
    {
        assertStatement("ALTER TABLE a RENAME TO b", new RenameTable(QualifiedName.of("a"), QualifiedName.of("b")));
    }

    @Test
    public void testCommentTable()
    {
        assertStatement("COMMENT ON TABLE a IS 'test'", new Comment(Comment.Type.TABLE, QualifiedName.of("a"), Optional.of("test")));
        assertStatement("COMMENT ON TABLE a IS ''", new Comment(Comment.Type.TABLE, QualifiedName.of("a"), Optional.of("")));
        assertStatement("COMMENT ON TABLE a IS NULL", new Comment(Comment.Type.TABLE, QualifiedName.of("a"), Optional.empty()));
    }

    @Test
    public void testRenameColumn()
    {
        assertStatement("ALTER TABLE foo.t RENAME COLUMN a TO b", new RenameColumn(QualifiedName.of("foo", "t"), identifier("a"), identifier("b")));
    }

    @Test
    public void testAnalyze()
    {
        QualifiedName table = QualifiedName.of("foo");
        assertStatement("ANALYZE foo", new Analyze(table, ImmutableList.of()));

        assertStatement("ANALYZE foo WITH ( \"string\" = 'bar', \"long\" = 42, computed = concat('ban', 'ana'), a = ARRAY[ 'v1', 'v2' ] )",
                new Analyze(table, ImmutableList.of(
                        new Property(new Identifier("string"), new StringLiteral("bar")),
                        new Property(new Identifier("long"), new LongLiteral("42")),
                        new Property(
                                new Identifier("computed"),
                                new FunctionCall(QualifiedName.of("concat"), ImmutableList.of(new StringLiteral("ban"), new StringLiteral("ana")))),
                        new Property(new Identifier("a"), new ArrayConstructor(ImmutableList.of(new StringLiteral("v1"), new StringLiteral("v2")))))));

        assertStatement("EXPLAIN ANALYZE foo", new Explain(new Analyze(table, ImmutableList.of()), false, false, ImmutableList.of()));
        assertStatement("EXPLAIN ANALYZE ANALYZE foo", new Explain(new Analyze(table, ImmutableList.of()), true, false, ImmutableList.of()));
    }

    @Test
    public void testAddColumn()
    {
        assertStatement("ALTER TABLE foo.t ADD COLUMN c bigint", new AddColumn(QualifiedName.of("foo", "t"),
                new ColumnDefinition(identifier("c"), "bigint", true, emptyList(), Optional.empty())));
        assertStatement("ALTER TABLE foo.t ADD COLUMN d double NOT NULL", new AddColumn(QualifiedName.of("foo", "t"),
                new ColumnDefinition(identifier("d"), "double", false, emptyList(), Optional.empty())));
    }

    @Test
    public void testDropColumn()
    {
        assertStatement("ALTER TABLE foo.t DROP COLUMN c", new DropColumn(QualifiedName.of("foo", "t"), identifier("c")));
        assertStatement("ALTER TABLE \"t x\" DROP COLUMN \"c d\"", new DropColumn(QualifiedName.of("t x"), quotedIdentifier("c d")));
    }

    @Test
    public void testCreateView()
    {
        Query query = simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t")));

        assertStatement("CREATE VIEW a AS SELECT * FROM t", new CreateView(QualifiedName.of("a"), query, false, Optional.empty()));
        assertStatement("CREATE OR REPLACE VIEW a AS SELECT * FROM t", new CreateView(QualifiedName.of("a"), query, true, Optional.empty()));

        assertStatement("CREATE VIEW a SECURITY DEFINER AS SELECT * FROM t", new CreateView(QualifiedName.of("a"), query, false, Optional.of(CreateView.Security.DEFINER)));
        assertStatement("CREATE VIEW a SECURITY INVOKER AS SELECT * FROM t", new CreateView(QualifiedName.of("a"), query, false, Optional.of(CreateView.Security.INVOKER)));

        assertStatement("CREATE VIEW bar.foo AS SELECT * FROM t", new CreateView(QualifiedName.of("bar", "foo"), query, false, Optional.empty()));
        assertStatement("CREATE VIEW \"awesome view\" AS SELECT * FROM t", new CreateView(QualifiedName.of("awesome view"), query, false, Optional.empty()));
        assertStatement("CREATE VIEW \"awesome schema\".\"awesome view\" AS SELECT * FROM t", new CreateView(QualifiedName.of("awesome schema", "awesome view"), query, false, Optional.empty()));
    }

    @Test
    public void testGrant()
    {
        assertStatement("GRANT INSERT, DELETE ON t TO u",
                new Grant(
                        Optional.of(ImmutableList.of("INSERT", "DELETE")),
                        false,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("u")),
                        false));
        assertStatement("GRANT SELECT ON t TO ROLE PUBLIC WITH GRANT OPTION",
                new Grant(
                        Optional.of(ImmutableList.of("SELECT")),
                        false, QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("PUBLIC")),
                        true));
        assertStatement("GRANT ALL PRIVILEGES ON t TO USER u",
                new Grant(
                        Optional.empty(),
                        false,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("u")),
                        false));
        assertStatement("GRANT DELETE ON \"t\" TO ROLE \"public\" WITH GRANT OPTION",
                new Grant(
                        Optional.of(ImmutableList.of("DELETE")),
                        false,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("public")),
                        true));
    }

    @Test
    public void testRevoke()
    {
        assertStatement("REVOKE INSERT, DELETE ON t FROM u",
                new Revoke(
                        false,
                        Optional.of(ImmutableList.of("INSERT", "DELETE")),
                        false,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("u"))));
        assertStatement("REVOKE GRANT OPTION FOR SELECT ON t FROM ROLE PUBLIC",
                new Revoke(
                        true,
                        Optional.of(ImmutableList.of("SELECT")),
                        false,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("PUBLIC"))));
        assertStatement("REVOKE ALL PRIVILEGES ON TABLE t FROM USER u",
                new Revoke(
                        false,
                        Optional.empty(),
                        true,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("u"))));
        assertStatement("REVOKE DELETE ON TABLE \"t\" FROM \"u\"",
                new Revoke(
                        false,
                        Optional.of(ImmutableList.of("DELETE")),
                        true,
                        QualifiedName.of("t"),
                        new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("u"))));
    }

    @Test
    public void testShowGrants()
    {
        assertStatement("SHOW GRANTS ON TABLE t",
                new ShowGrants(true, Optional.of(QualifiedName.of("t"))));
        assertStatement("SHOW GRANTS ON t",
                new ShowGrants(false, Optional.of(QualifiedName.of("t"))));
        assertStatement("SHOW GRANTS",
                new ShowGrants(false, Optional.empty()));
    }

    @Test
    public void testShowRoles()
    {
        assertStatement("SHOW ROLES",
                new ShowRoles(Optional.empty(), false));
        assertStatement("SHOW ROLES FROM foo",
                new ShowRoles(Optional.of(new Identifier("foo")), false));
        assertStatement("SHOW ROLES IN foo",
                new ShowRoles(Optional.of(new Identifier("foo")), false));

        assertStatement("SHOW CURRENT ROLES",
                new ShowRoles(Optional.empty(), true));
        assertStatement("SHOW CURRENT ROLES FROM foo",
                new ShowRoles(Optional.of(new Identifier("foo")), true));
        assertStatement("SHOW CURRENT ROLES IN foo",
                new ShowRoles(Optional.of(new Identifier("foo")), true));
    }

    @Test
    public void testShowRoleGrants()
    {
        assertStatement("SHOW ROLE GRANTS",
                new ShowRoleGrants(Optional.empty(), Optional.empty()));
        assertStatement("SHOW ROLE GRANTS FROM catalog",
                new ShowRoleGrants(Optional.of(new Identifier("catalog"))));
    }

    @Test
    public void testSetPath()
    {
        assertStatement("SET PATH iLikeToEat.apples, andBananas",
                new SetPath(new PathSpecification(Optional.empty(), ImmutableList.of(
                        new PathElement(Optional.of(new Identifier("iLikeToEat")), new Identifier("apples")),
                        new PathElement(Optional.empty(), new Identifier("andBananas"))))));

        assertStatement("SET PATH \"schemas,with\".\"grammar.in\", \"their!names\"",
                new SetPath(new PathSpecification(Optional.empty(), ImmutableList.of(
                        new PathElement(Optional.of(new Identifier("schemas,with")), new Identifier("grammar.in")),
                        new PathElement(Optional.empty(), new Identifier("their!names"))))));

        try {
            assertStatement("SET PATH one.too.many, qualifiers",
                    new SetPath(new PathSpecification(Optional.empty(), ImmutableList.of(
                            new PathElement(Optional.empty(), new Identifier("dummyValue"))))));
            fail();
        }
        catch (RuntimeException e) {
            //expected - schema can only be qualified by catalog
        }

        try {
            SQL_PARSER.createStatement("SET PATH ", new ParsingOptions());
            fail();
        }
        catch (RuntimeException e) {
            //expected - some form of parameter is required
        }
    }

    @Test
    public void testWith()
    {
        assertStatement("WITH a (t, u) AS (SELECT * FROM x), b AS (SELECT * FROM y) TABLE z",
                new Query(Optional.of(new With(false, ImmutableList.of(
                        new WithQuery(identifier("a"), simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("x"))), Optional.of(ImmutableList.of(identifier("t"), identifier("u")))),
                        new WithQuery(identifier("b"), simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("y"))), Optional.empty())))),
                        new Table(QualifiedName.of("z")),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));

        assertStatement("WITH RECURSIVE a AS (SELECT * FROM x) TABLE y",
                new Query(Optional.of(new With(true, ImmutableList.of(
                        new WithQuery(identifier("a"), simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("x"))), Optional.empty())))),
                        new Table(QualifiedName.of("y")),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testImplicitJoin()
    {
        assertStatement("SELECT * FROM a, b",
                simpleQuery(selectList(new AllColumns()),
                        new Join(Join.Type.IMPLICIT,
                                new Table(QualifiedName.of("a")),
                                new Table(QualifiedName.of("b")),
                                Optional.empty())));
    }

    @Test
    public void testExplain()
    {
        assertStatement("EXPLAIN SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), false, false, ImmutableList.of()));
        assertStatement("EXPLAIN (TYPE LOGICAL) SELECT * FROM t",
                new Explain(
                        simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))),
                        false,
                        false,
                        ImmutableList.of(new ExplainType(ExplainType.Type.LOGICAL))));
        assertStatement("EXPLAIN (TYPE LOGICAL, FORMAT TEXT) SELECT * FROM t",
                new Explain(
                        simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))),
                        false,
                        false,
                        ImmutableList.of(
                                new ExplainType(ExplainType.Type.LOGICAL),
                                new ExplainFormat(ExplainFormat.Type.TEXT))));
    }

    @Test
    public void testExplainVerbose()
    {
        assertStatement("EXPLAIN VERBOSE SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), false, true, ImmutableList.of()));
    }

    @Test
    public void testExplainVerboseTypeLogical()
    {
        assertStatement("EXPLAIN VERBOSE (type LOGICAL) SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), false, true, ImmutableList.of(new ExplainType(ExplainType.Type.LOGICAL))));
    }

    @Test
    public void testExplainAnalyze()
    {
        assertStatement("EXPLAIN ANALYZE SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), true, false, ImmutableList.of()));
    }

    @Test
    public void testExplainAnalyzeTypeDistributed()
    {
        assertStatement("EXPLAIN ANALYZE (type DISTRIBUTED) SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), true, false, ImmutableList.of(new ExplainType(ExplainType.Type.DISTRIBUTED))));
    }

    @Test
    public void testExplainAnalyzeVerbose()
    {
        assertStatement("EXPLAIN ANALYZE VERBOSE SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), true, true, ImmutableList.of()));
    }

    @Test
    public void testExplainAnalyzeVerboseTypeDistributed()
    {
        assertStatement("EXPLAIN ANALYZE VERBOSE (type DISTRIBUTED) SELECT * FROM t",
                new Explain(simpleQuery(selectList(new AllColumns()), table(QualifiedName.of("t"))), true, true, ImmutableList.of(new ExplainType(ExplainType.Type.DISTRIBUTED))));
    }

    @Test
    public void testJoinPrecedence()
    {
        assertStatement("SELECT * FROM a CROSS JOIN b LEFT JOIN c ON true",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.LEFT,
                                new Join(
                                        Join.Type.CROSS,
                                        new Table(QualifiedName.of("a")),
                                        new Table(QualifiedName.of("b")),
                                        Optional.empty()),
                                new Table(QualifiedName.of("c")),
                                Optional.of(new JoinOn(BooleanLiteral.TRUE_LITERAL)))));
        assertStatement("SELECT * FROM a CROSS JOIN b NATURAL JOIN c CROSS JOIN d NATURAL JOIN e",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.INNER,
                                new Join(
                                        Join.Type.CROSS,
                                        new Join(
                                                Join.Type.INNER,
                                                new Join(
                                                        Join.Type.CROSS,
                                                        new Table(QualifiedName.of("a")),
                                                        new Table(QualifiedName.of("b")),
                                                        Optional.empty()),
                                                new Table(QualifiedName.of("c")),
                                                Optional.of(new NaturalJoin())),
                                        new Table(QualifiedName.of("d")),
                                        Optional.empty()),
                                new Table(QualifiedName.of("e")),
                                Optional.of(new NaturalJoin()))));
    }

    @Test
    public void testUnnest()
    {
        assertStatement("SELECT * FROM t CROSS JOIN UNNEST(a)",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.CROSS,
                                new Table(QualifiedName.of("t")),
                                new Unnest(ImmutableList.of(new Identifier("a")), false),
                                Optional.empty())));
        assertStatement("SELECT * FROM t CROSS JOIN UNNEST(a, b) WITH ORDINALITY",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.CROSS,
                                new Table(QualifiedName.of("t")),
                                new Unnest(ImmutableList.of(new Identifier("a"), new Identifier("b")), true),
                                Optional.empty())));
        assertStatement("SELECT * FROM t FULL JOIN UNNEST(a) AS tmp (c) ON true",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.FULL,
                                new Table(QualifiedName.of("t")),
                                new AliasedRelation(new Unnest(ImmutableList.of(new Identifier("a")), false), new Identifier("tmp"), ImmutableList.of(new Identifier("c"))),
                                Optional.of(new JoinOn(BooleanLiteral.TRUE_LITERAL)))));
    }

    @Test
    public void testLateral()
    {
        Lateral lateralRelation = new Lateral(new Query(
                Optional.empty(),
                new Values(ImmutableList.of(new LongLiteral("1"))),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));

        assertStatement("SELECT * FROM t, LATERAL (VALUES 1) a(x)",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.IMPLICIT,
                                new Table(QualifiedName.of("t")),
                                new AliasedRelation(lateralRelation, identifier("a"), ImmutableList.of(identifier("x"))),
                                Optional.empty())));

        assertStatement("SELECT * FROM t CROSS JOIN LATERAL (VALUES 1) ",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.CROSS,
                                new Table(QualifiedName.of("t")),
                                lateralRelation,
                                Optional.empty())));

        assertStatement("SELECT * FROM t FULL JOIN LATERAL (VALUES 1) ON true",
                simpleQuery(
                        selectList(new AllColumns()),
                        new Join(
                                Join.Type.FULL,
                                new Table(QualifiedName.of("t")),
                                lateralRelation,
                                Optional.of(new JoinOn(BooleanLiteral.TRUE_LITERAL)))));
    }

    @Test
    public void testStartTransaction()
    {
        assertStatement("START TRANSACTION",
                new StartTransaction(ImmutableList.of()));
        assertStatement("START TRANSACTION ISOLATION LEVEL READ UNCOMMITTED",
                new StartTransaction(ImmutableList.of(
                        new Isolation(Isolation.Level.READ_UNCOMMITTED))));
        assertStatement("START TRANSACTION ISOLATION LEVEL READ COMMITTED",
                new StartTransaction(ImmutableList.of(
                        new Isolation(Isolation.Level.READ_COMMITTED))));
        assertStatement("START TRANSACTION ISOLATION LEVEL REPEATABLE READ",
                new StartTransaction(ImmutableList.of(
                        new Isolation(Isolation.Level.REPEATABLE_READ))));
        assertStatement("START TRANSACTION ISOLATION LEVEL SERIALIZABLE",
                new StartTransaction(ImmutableList.of(
                        new Isolation(Isolation.Level.SERIALIZABLE))));
        assertStatement("START TRANSACTION READ ONLY",
                new StartTransaction(ImmutableList.of(
                        new TransactionAccessMode(true))));
        assertStatement("START TRANSACTION READ WRITE",
                new StartTransaction(ImmutableList.of(
                        new TransactionAccessMode(false))));
        assertStatement("START TRANSACTION ISOLATION LEVEL READ COMMITTED, READ ONLY",
                new StartTransaction(ImmutableList.of(
                        new Isolation(Isolation.Level.READ_COMMITTED),
                        new TransactionAccessMode(true))));
        assertStatement("START TRANSACTION READ ONLY, ISOLATION LEVEL READ COMMITTED",
                new StartTransaction(ImmutableList.of(
                        new TransactionAccessMode(true),
                        new Isolation(Isolation.Level.READ_COMMITTED))));
        assertStatement("START TRANSACTION READ WRITE, ISOLATION LEVEL SERIALIZABLE",
                new StartTransaction(ImmutableList.of(
                        new TransactionAccessMode(false),
                        new Isolation(Isolation.Level.SERIALIZABLE))));
    }

    @Test
    public void testCommit()
    {
        assertStatement("COMMIT", new Commit());
        assertStatement("COMMIT WORK", new Commit());
    }

    @Test
    public void testRollback()
    {
        assertStatement("ROLLBACK", new Rollback());
        assertStatement("ROLLBACK WORK", new Rollback());
    }

    @Test
    public void testAtTimeZone()
    {
        assertStatement("SELECT timestamp '2012-10-31 01:00 UTC' AT TIME ZONE 'America/Los_Angeles'",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        new AtTimeZone(new TimestampLiteral("2012-10-31 01:00 UTC"), new StringLiteral("America/Los_Angeles"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testLambda()
    {
        assertExpression("() -> x",
                new LambdaExpression(
                        ImmutableList.of(),
                        new Identifier("x")));
        assertExpression("x -> sin(x)",
                new LambdaExpression(
                        ImmutableList.of(new LambdaArgumentDeclaration(identifier("x"))),
                        new FunctionCall(QualifiedName.of("sin"), ImmutableList.of(new Identifier("x")))));
        assertExpression("(x, y) -> mod(x, y)",
                new LambdaExpression(
                        ImmutableList.of(new LambdaArgumentDeclaration(identifier("x")), new LambdaArgumentDeclaration(identifier("y"))),
                        new FunctionCall(
                                QualifiedName.of("mod"),
                                ImmutableList.of(new Identifier("x"), new Identifier("y")))));
    }

    @Test
    public void testNonReserved()
    {
        assertStatement("SELECT zone FROM t",
                simpleQuery(
                        selectList(new Identifier("zone")),
                        table(QualifiedName.of("t"))));
        assertStatement("SELECT INCLUDING, EXCLUDING, PROPERTIES FROM t",
                simpleQuery(
                        selectList(
                                new Identifier("INCLUDING"),
                                new Identifier("EXCLUDING"),
                                new Identifier("PROPERTIES")),
                        table(QualifiedName.of("t"))));
        assertStatement("SELECT ALL, SOME, ANY FROM t",
                simpleQuery(
                        selectList(
                                new Identifier("ALL"),
                                new Identifier("SOME"),
                                new Identifier("ANY")),
                        table(QualifiedName.of("t"))));

        assertExpression("stats", new Identifier("stats"));
        assertExpression("nfd", new Identifier("nfd"));
        assertExpression("nfc", new Identifier("nfc"));
        assertExpression("nfkd", new Identifier("nfkd"));
        assertExpression("nfkc", new Identifier("nfkc"));
    }

    @Test
    public void testBinaryLiteralToHex()
    {
        // note that toHexString() always outputs in upper case
        assertEquals(new BinaryLiteral("ab 01").toHexString(), "AB01");
    }

    @Test
    public void testCall()
    {
        assertStatement("CALL foo()", new Call(QualifiedName.of("foo"), ImmutableList.of()));
        assertStatement("CALL foo(123, a => 1, b => 'go', 456)", new Call(QualifiedName.of("foo"), ImmutableList.of(
                new CallArgument(new LongLiteral("123")),
                new CallArgument("a", new LongLiteral("1")),
                new CallArgument("b", new StringLiteral("go")),
                new CallArgument(new LongLiteral("456")))));
    }

    @Test
    public void testPrepare()
    {
        assertStatement("PREPARE myquery FROM select * from foo",
                new Prepare(identifier("myquery"), simpleQuery(
                        selectList(new AllColumns()),
                        table(QualifiedName.of("foo")))));
    }

    @Test
    public void testPrepareWithParameters()
    {
        assertStatement("PREPARE myquery FROM SELECT ?, ? FROM foo",
                new Prepare(identifier("myquery"), simpleQuery(
                        selectList(new Parameter(0), new Parameter(1)),
                        table(QualifiedName.of("foo")))));
    }

    @Test
    public void testDeallocatePrepare()
    {
        assertStatement("DEALLOCATE PREPARE myquery", new Deallocate(identifier("myquery")));
    }

    @Test
    public void testExecute()
    {
        assertStatement("EXECUTE myquery", new Execute(identifier("myquery"), emptyList()));
    }

    @Test
    public void testExecuteWithUsing()
    {
        assertStatement("EXECUTE myquery USING 1, 'abc', ARRAY ['hello']",
                new Execute(identifier("myquery"), ImmutableList.of(new LongLiteral("1"), new StringLiteral("abc"), new ArrayConstructor(ImmutableList.of(new StringLiteral("hello"))))));
    }

    @Test
    public void testExists()
    {
        assertStatement("SELECT EXISTS(SELECT 1)", simpleQuery(selectList(exists(simpleQuery(selectList(new LongLiteral("1")))))));

        assertStatement(
                "SELECT EXISTS(SELECT 1) = EXISTS(SELECT 2)",
                simpleQuery(
                        selectList(new ComparisonExpression(
                                ComparisonExpression.Operator.EQUAL,
                                exists(simpleQuery(selectList(new LongLiteral("1")))),
                                exists(simpleQuery(selectList(new LongLiteral("2"))))))));

        assertStatement(
                "SELECT NOT EXISTS(SELECT 1) = EXISTS(SELECT 2)",
                simpleQuery(
                        selectList(
                                new NotExpression(
                                        new ComparisonExpression(
                                                ComparisonExpression.Operator.EQUAL,
                                                exists(simpleQuery(selectList(new LongLiteral("1")))),
                                                exists(simpleQuery(selectList(new LongLiteral("2")))))))));

        assertStatement(
                "SELECT (NOT EXISTS(SELECT 1)) = EXISTS(SELECT 2)",
                simpleQuery(
                        selectList(
                                new ComparisonExpression(
                                        ComparisonExpression.Operator.EQUAL,
                                        new NotExpression(exists(simpleQuery(selectList(new LongLiteral("1"))))),
                                        exists(simpleQuery(selectList(new LongLiteral("2"))))))));
    }

    private static ExistsPredicate exists(Query query)
    {
        return new ExistsPredicate(new SubqueryExpression(query));
    }

    @Test
    public void testShowStats()
    {
        final String[] tableNames = {"t", "s.t", "c.s.t"};

        for (String fullName : tableNames) {
            QualifiedName qualifiedName = makeQualifiedName(fullName);
            assertStatement(format("SHOW STATS FOR %s", qualifiedName), new ShowStats(new Table(qualifiedName)));
        }
    }

    @Test
    public void testShowStatsForQuery()
    {
        final String[] tableNames = {"t", "s.t", "c.s.t"};

        for (String fullName : tableNames) {
            QualifiedName qualifiedName = makeQualifiedName(fullName);
            assertStatement(format("SHOW STATS FOR (SELECT * FROM %s)", qualifiedName),
                    createShowStats(qualifiedName, ImmutableList.of(new AllColumns()), Optional.empty()));
            assertStatement(format("SHOW STATS FOR (SELECT * FROM %s WHERE field > 0)", qualifiedName),
                    createShowStats(qualifiedName,
                            ImmutableList.of(new AllColumns()),
                            Optional.of(
                                    new ComparisonExpression(GREATER_THAN,
                                            new Identifier("field"),
                                            new LongLiteral("0")))));
            assertStatement(format("SHOW STATS FOR (SELECT * FROM %s WHERE field > 0 or field < 0)", qualifiedName),
                    createShowStats(qualifiedName,
                            ImmutableList.of(new AllColumns()),
                            Optional.of(
                                    new LogicalBinaryExpression(LogicalBinaryExpression.Operator.OR,
                                            new ComparisonExpression(GREATER_THAN,
                                                    new Identifier("field"),
                                                    new LongLiteral("0")),
                                            new ComparisonExpression(LESS_THAN,
                                                    new Identifier("field"),
                                                    new LongLiteral("0"))))));
        }
    }

    private ShowStats createShowStats(QualifiedName name, List<SelectItem> selects, Optional<Expression> where)
    {
        return new ShowStats(
                new TableSubquery(simpleQuery(new Select(false, selects),
                        new Table(name),
                        where,
                        Optional.empty())));
    }

    @Test
    public void testDescribeOutput()
    {
        assertStatement("DESCRIBE OUTPUT myquery", new DescribeOutput(identifier("myquery")));
    }

    @Test
    public void testDescribeInput()
    {
        assertStatement("DESCRIBE INPUT myquery", new DescribeInput(identifier("myquery")));
    }

    @Test
    public void testAggregationFilter()
    {
        assertStatement("SELECT SUM(x) FILTER (WHERE x > 4)",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        new FunctionCall(
                                                Optional.empty(),
                                                QualifiedName.of("SUM"),
                                                Optional.empty(),
                                                Optional.of(new ComparisonExpression(
                                                        ComparisonExpression.Operator.GREATER_THAN,
                                                        new Identifier("x"),
                                                        new LongLiteral("4"))),
                                                Optional.empty(),
                                                false,
                                                ImmutableList.of(new Identifier("x")))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testQuantifiedComparison()
    {
        assertExpression("col1 < ANY (SELECT col2 FROM table1)",
                new QuantifiedComparisonExpression(
                        LESS_THAN,
                        QuantifiedComparisonExpression.Quantifier.ANY,
                        identifier("col1"),
                        new SubqueryExpression(simpleQuery(selectList(new SingleColumn(identifier("col2"))), table(QualifiedName.of("table1"))))));
        assertExpression("col1 = ALL (VALUES ROW(1), ROW(2))",
                new QuantifiedComparisonExpression(
                        ComparisonExpression.Operator.EQUAL,
                        QuantifiedComparisonExpression.Quantifier.ALL,
                        identifier("col1"),
                        new SubqueryExpression(query(values(row(new LongLiteral("1")), row(new LongLiteral("2")))))));
        assertExpression("col1 >= SOME (SELECT 10)",
                new QuantifiedComparisonExpression(
                        ComparisonExpression.Operator.GREATER_THAN_OR_EQUAL,
                        QuantifiedComparisonExpression.Quantifier.SOME,
                        identifier("col1"),
                        new SubqueryExpression(simpleQuery(selectList(new LongLiteral("10"))))));
    }

    @Test
    public void testAggregationWithOrderBy()
    {
        assertExpression("array_agg(x ORDER BY x DESC)",
                new FunctionCall(
                        Optional.empty(),
                        QualifiedName.of("array_agg"),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(new OrderBy(ImmutableList.of(new SortItem(identifier("x"), DESCENDING, UNDEFINED)))),
                        false,
                        ImmutableList.of(identifier("x"))));
        assertStatement("SELECT array_agg(x ORDER BY t.y) FROM t",
                new Query(
                        Optional.empty(),
                        new QuerySpecification(
                                selectList(
                                        new FunctionCall(
                                                Optional.empty(),
                                                QualifiedName.of("array_agg"),
                                                Optional.empty(),
                                                Optional.empty(),
                                                Optional.of(new OrderBy(ImmutableList.of(new SortItem(new DereferenceExpression(new Identifier("t"), identifier("y")), ASCENDING, UNDEFINED)))),
                                                false,
                                                ImmutableList.of(new Identifier("x")))),
                                Optional.of(table(QualifiedName.of("t"))),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty()),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty()));
    }

    @Test
    public void testCreateRole()
    {
        assertStatement("CREATE ROLE role", new CreateRole(new Identifier("role"), Optional.empty()));
        assertStatement("CREATE ROLE role1 WITH ADMIN admin",
                new CreateRole(
                        new Identifier("role1"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("admin")))))));
        assertStatement("CREATE ROLE \"role\" WITH ADMIN \"admin\"",
                new CreateRole(
                        new Identifier("role"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("admin")))))));
        assertStatement("CREATE ROLE \"ro le\" WITH ADMIN \"ad min\"",
                new CreateRole(
                        new Identifier("ro le"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("ad min")))))));
        assertStatement("CREATE ROLE \"!@#$%^&*'\" WITH ADMIN \"ад\"\"мін\"",
                new CreateRole(
                        new Identifier("!@#$%^&*'"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("ад\"мін")))))));
        assertStatement("CREATE ROLE role2 WITH ADMIN USER admin1",
                new CreateRole(
                        new Identifier("role2"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("admin1")))))));
        assertStatement("CREATE ROLE role2 WITH ADMIN ROLE role1",
                new CreateRole(
                        new Identifier("role2"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role1")))))));
        assertStatement("CREATE ROLE role2 WITH ADMIN CURRENT_USER",
                new CreateRole(
                        new Identifier("role2"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.CURRENT_USER,
                                Optional.empty()))));
        assertStatement("CREATE ROLE role2 WITH ADMIN CURRENT_ROLE",
                new CreateRole(
                        new Identifier("role2"),
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.CURRENT_ROLE,
                                Optional.empty()))));
    }

    @Test
    public void testDropRole()
    {
        assertStatement("DROP ROLE role", new DropRole(new Identifier("role")));
        assertStatement("DROP ROLE \"role\"", new DropRole(new Identifier("role")));
        assertStatement("DROP ROLE \"ro le\"", new DropRole(new Identifier("ro le")));
        assertStatement("DROP ROLE \"!@#$%^&*'ад\"\"мін\"", new DropRole(new Identifier("!@#$%^&*'ад\"мін")));
    }

    @Test
    public void testGrantRoles()
    {
        assertStatement("GRANT role1 TO user1",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("user1"))),
                        false,
                        Optional.empty()));
        assertStatement("GRANT role1, role2, role3 TO user1, USER user2, ROLE role4 WITH ADMIN OPTION",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1"), new Identifier("role2"), new Identifier("role3")),
                        ImmutableSet.of(
                                new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("user1")),
                                new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("user2")),
                                new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role4"))),
                        true,
                        Optional.empty()));
        assertStatement("GRANT role1 TO user1 WITH ADMIN OPTION GRANTED BY admin",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("user1"))),
                        true,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("admin")))))));
        assertStatement("GRANT role1 TO USER user1 WITH ADMIN OPTION GRANTED BY USER admin",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("user1"))),
                        true,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("admin")))))));
        assertStatement("GRANT role1 TO ROLE role2 WITH ADMIN OPTION GRANTED BY ROLE admin",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role2"))),
                        true,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("admin")))))));
        assertStatement("GRANT role1 TO ROLE role2 GRANTED BY ROLE admin",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role2"))),
                        false,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("admin")))))));
        assertStatement("GRANT \"role1\" TO ROLE \"role2\" GRANTED BY ROLE \"admin\"",
                new GrantRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role2"))),
                        false,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("admin")))))));
    }

    @Test
    public void testRevokeRoles()
    {
        assertStatement("REVOKE role1 FROM user1",
                new RevokeRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("user1"))),
                        false,
                        Optional.empty()));
        assertStatement("REVOKE ADMIN OPTION FOR role1, role2, role3 FROM user1, USER user2, ROLE role4",
                new RevokeRoles(
                        ImmutableSet.of(new Identifier("role1"), new Identifier("role2"), new Identifier("role3")),
                        ImmutableSet.of(
                                new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("user1")),
                                new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("user2")),
                                new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role4"))),
                        true,
                        Optional.empty()));
        assertStatement("REVOKE ADMIN OPTION FOR role1 FROM user1 GRANTED BY admin",
                new RevokeRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("user1"))),
                        true,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.UNSPECIFIED, new Identifier("admin")))))));
        assertStatement("REVOKE ADMIN OPTION FOR role1 FROM USER user1 GRANTED BY USER admin",
                new RevokeRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("user1"))),
                        true,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.USER, new Identifier("admin")))))));
        assertStatement("REVOKE role1 FROM ROLE role2 GRANTED BY ROLE admin",
                new RevokeRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role2"))),
                        false,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("admin")))))));
        assertStatement("REVOKE \"role1\" FROM ROLE \"role2\" GRANTED BY ROLE \"admin\"",
                new RevokeRoles(
                        ImmutableSet.of(new Identifier("role1")),
                        ImmutableSet.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("role2"))),
                        false,
                        Optional.of(new GrantorSpecification(
                                GrantorSpecification.Type.PRINCIPAL,
                                Optional.of(new PrincipalSpecification(PrincipalSpecification.Type.ROLE, new Identifier("admin")))))));
    }

    @Test
    public void testSetRole()
    {
        assertStatement("SET ROLE ALL", new SetRole(SetRole.Type.ALL, Optional.empty()));
        assertStatement("SET ROLE NONE", new SetRole(SetRole.Type.NONE, Optional.empty()));
        assertStatement("SET ROLE role", new SetRole(SetRole.Type.ROLE, Optional.of(new Identifier("role"))));
        assertStatement("SET ROLE \"role\"", new SetRole(SetRole.Type.ROLE, Optional.of(new Identifier("role"))));
    }

    private QualifiedName makeQualifiedName(String tableName)
    {
        List<Identifier> parts = Arrays.stream(tableName.split("\\."))
                .map(Identifier::new)
                .collect(Collectors.toList());
        return QualifiedName.of(parts);
    }

    private static void assertCast(String type)
    {
        assertCast(type, type);
    }

    private static void assertCast(String type, String expected)
    {
        assertExpression("CAST(null AS " + type + ")", new Cast(new NullLiteral(), expected));
    }

    private static void assertStatement(String query, Statement expected)
    {
        assertParsed(query, expected, SQL_PARSER.createStatement(query));
        assertFormattedSql(SQL_PARSER, expected);
    }

    private static void assertExpression(String expression, Expression expected)
    {
        assertParsed(expression, expected, SQL_PARSER.createExpression(expression, new ParsingOptions(AS_DECIMAL)));
    }

    private static void assertParsed(String input, Node expected, Node parsed)
    {
        if (!parsed.equals(expected)) {
            fail(format("expected\n\n%s\n\nto parse as\n\n%s\n\nbut was\n\n%s\n",
                    indent(input),
                    indent(formatSql(expected)),
                    indent(formatSql(parsed))));
        }
    }

    private static void assertInvalidExpression(String expression, String expectedErrorMessageRegex)
    {
        try {
            Expression result = SQL_PARSER.createExpression(expression);
            fail("Expected to throw ParsingException for input:[" + expression + "], but got: " + result);
        }
        catch (ParsingException e) {
            if (!e.getErrorMessage().matches(expectedErrorMessageRegex)) {
                fail(format("Expected error message to match '%s', but was: '%s'", expectedErrorMessageRegex, e.getErrorMessage()));
            }
        }
    }

    private static String indent(String value)
    {
        String indent = "    ";
        return indent + value.trim().replaceAll("\n", "\n" + indent);
    }
}
