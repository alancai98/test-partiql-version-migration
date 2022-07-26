package examples

import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionelement.api.ionInt
import org.partiql.lang.ast.DateLiteral
import org.partiql.lang.ast.TimeLiteral
import org.partiql.lang.ast.emptyMetaContainer
import org.partiql.lang.domains.PartiqlAst
import org.partiql.lang.eval.Environment
import org.partiql.lang.eval.ExprFunction
import org.partiql.lang.eval.ExprValue
import org.partiql.lang.syntax.SqlParser
import org.partiql.lang.types.FunctionSignature
import org.partiql.lang.types.StaticType
import kotlin.test.Test
import kotlin.test.assertEquals

class BreakingChanges {
    @Test
    fun `api change - modeling of DateTimeType in AST`() {
        // In v0.5.0 and before, date value constructor uses `DateLiteral` without an outer sealed class
        val date = DateLiteral(year = 2022, month = 1, day = 1, metas = emptyMetaContainer)

        // Similarly, for the time value constructor uses `TimeLiteral` without an outer sealed class
        val time = TimeLiteral(
            hour = 12,
            minute = 34,
            second = 56,
            nano = 78,
            precision = 2,
            with_time_zone = false,
            metas = emptyMetaContainer
        )
    }

    @Test
    fun `api change - refactor of ExprFunction interface`() {
        // Starting in v0.5.0, the `ExprFunction` interface has been refactored and `ExprFunction` implementations will
        // need to define the `FunctionSignature` of the function which specifies the name, return type, and any
        // required, optional, and variadic arguments. The implementation will need to override `callWith*` depending
        // on the permitted arguments (in this example, overriding `callWithRequired` and `callWithOptional`).
        class SomeExprFunction(): ExprFunction {
            override val signature = FunctionSignature(
                name = "some_expr_function",
                requiredParameters = listOf(StaticType.ANY),
                optionalParameter = StaticType.ANY,
                returnType = StaticType.ANY
            )

            override fun callWithRequired(env: Environment, required: List<ExprValue>): ExprValue {
                TODO("Implementation details without optional argument")
            }

            override fun callWithOptional(env: Environment, required: List<ExprValue>, opt: ExprValue): ExprValue {
                TODO("Implementation details with optional argument")
            }
        }
    }

    @Test
    fun `api change - modeling of NULLIF and COALESCE as AST nodes`() {
        val ion = IonSystemBuilder.standard().build()
        val parser = SqlParser(ion)

        // In v0.5.0 onwards, NULLIF is modeled as a separate AST node
        val nullIfQuery = "NULLIF(1, 2)"
        val nullIfParsedAst = parser.parseAstStatement(nullIfQuery)
        val nullIfExpectedAst = PartiqlAst.build {
            query(
                nullIf(
                    lit(ionInt(1)),
                    lit(ionInt(2))
                )
            )
        }
        assertEquals(nullIfExpectedAst, nullIfParsedAst)

        // In v0.5.0 onwards, COALESCE is modeled as a separate AST node
        val coalesceQuery = "COALESCE(1, 2)"
        val coalesceParsedAst = parser.parseAstStatement(coalesceQuery)
        val coalesceExpectedAst = PartiqlAst.build {
            query(
                coalesce(
                    args = listOf(lit(ionInt(1)), lit(ionInt(2))),
                )
            )
        }
        assertEquals(coalesceExpectedAst, coalesceParsedAst)
    }
}
