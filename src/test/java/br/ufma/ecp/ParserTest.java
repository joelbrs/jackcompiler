package br.ufma.ecp;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class ParserTest extends TestSupport{
    @Test
    public void testParseLetSimple() {
        var input = "let var1 = 10+20;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseLet();
        var expectedResult =  """
	     <letStatement>
        <keyword> let </keyword>
        <identifier> var1 </identifier>
        <symbol> = </symbol>
        <expression>
          <term>
          <integerConstant> 10 </integerConstant>
          </term>
          <symbol> + </symbol>
          <term>
          <integerConstant> 20 </integerConstant>
          </term>
          </expression>
        <symbol> ; </symbol>
      </letStatement> 
				""";
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseClassVarDecSimple() {
        var input = "static Parse parse;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseClassVarDec();
        var expectedResult =  """
	     <classVarDec>
            <keyword> static </keyword>
            <identifier> Parse </identifier>
            <identifier> parse </identifier>
            <symbol> ; </symbol>
        </classVarDec> 
				""";
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseClassVarDecMultiples() {
        var input = "static Parse parse, parseTwo, parseThree;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseClassVarDec();
        var expectedResult =  """
	     <classVarDec>
            <keyword> static </keyword>
            <identifier> Parse </identifier>
            <identifier> parse </identifier>
            <symbol> , </symbol>
            <identifier> parseTwo </identifier>
            <symbol> , </symbol>
            <identifier> parseThree </identifier>
            <symbol> ; </symbol>
        </classVarDec> 
				""";
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseVarDecSimple() {
        var input = "var boolean simple;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseVarDec();
        var expectedResult =  """
	     <varDec>
            <keyword> var </keyword>
            <keyword> boolean </keyword>
            <identifier> simple </identifier>
            <symbol> ; </symbol>
        </varDec> 
				""";
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseVarDecMultiples() {
        var input = "var boolean isBoolean, isLiteral, isKeyword;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseVarDec();
        var expectedResult =  """
	     <varDec>
            <keyword> var </keyword>
            <keyword> boolean </keyword>
            <identifier> isBoolean </identifier>
            <symbol> , </symbol>
            <identifier> isLiteral </identifier>
            <symbol> , </symbol>
            <identifier> isKeyword </identifier>
            <symbol> ; </symbol>
        </varDec> 
				""";
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseIfSimple() {
        var input = "if (statement) {}";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseIf();
        var expectedResult =  """
                  <ifStatement>
                        <keyword> if </keyword>
                        <symbol> ( </symbol>
                        <expression>
                        <term>
                        <identifier> statement </identifier>
                        </term>
                        </expression>
                        <symbol> ) </symbol>
                        <symbol> { </symbol>
                        <symbol> } </symbol>
                    </ifStatement> 
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseIfElseSimple() {
        var input = "if (statement) {} else {}";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseIf();
        var expectedResult =  """
                  <ifStatement>
                        <keyword> if </keyword>
                        <symbol> ( </symbol>
                        <expression>
                        <term>
                        <identifier> statement </identifier>
                        </term>
                        </expression>
                        <symbol> ) </symbol>
                        <symbol> { </symbol>
                        <symbol> } </symbol>
                        <keyword> else </keyword>
                        <symbol> { </symbol>
                        <symbol> } </symbol>
                    </ifStatement> 
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }
    @Test
    public void testParseIfWithExpression() {
        var input = "if (statement) { let x = 10; }";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseIf();
        var expectedResult =  """
                  <ifStatement>
                        <keyword> if </keyword>
                        <symbol> ( </symbol>
                        <expression>
                        <term>
                        <identifier> statement </identifier>
                        </term>
                        </expression>
                        <symbol> ) </symbol>
                        <symbol> { </symbol>
                        <letStatement>
                        <keyword> let </keyword>
                        <identifier> x </identifier>
                        <symbol> = </symbol>
                        <expression>
                        <term>
                        <integerConstant> 10 </integerConstant>
                        </term>
                        </expression> 
                        <symbol> ; </symbol>
                        </letStatement>
                        <symbol> } </symbol>
                    </ifStatement> 
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseIfElseWithExpression() {
        var input = "if (statement) { let x = 10; } else { let x = 20; }";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseIf();
        var expectedResult =  """
                  <ifStatement>
                        <keyword> if </keyword>
                        <symbol> ( </symbol>
                        <expression>
                        <term>
                        <identifier> statement </identifier>
                        </term>
                        </expression>
                        <symbol> ) </symbol>
                        <symbol> { </symbol>
                        <letStatement>
                        <keyword> let </keyword>
                        <identifier> x </identifier>
                        <symbol> = </symbol>
                        <expression>
                        <term>
                        <integerConstant> 10 </integerConstant>
                        </term>
                        </expression> 
                        <symbol> ; </symbol>
                        </letStatement>
                        <symbol> } </symbol>
                        <keyword> else </keyword>
                        <symbol> { </symbol>
                        <letStatement>
                        <keyword> let </keyword>
                        <identifier> x </identifier>
                        <symbol> = </symbol>
                        <expression>
                        <term>
                        <integerConstant> 20 </integerConstant>
                        </term>
                        </expression> 
                        <symbol> ; </symbol>
                        </letStatement>
                        <symbol> } </symbol>
                    </ifStatement> 
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseWhileSimple() {
        var input = "while (statement) {}";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseWhile();
        var expectedResult =  """
                  <whileStatement>
                        <keyword> while </keyword>
                        <symbol> ( </symbol>
                        <expression>
                        <term>
                        <identifier> statement </identifier>
                        </term>
                        </expression>
                        <symbol> ) </symbol>
                        <symbol> { </symbol>
                        <symbol> } </symbol>
                        </whileStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }
    @Test
    public void testParseWhileWithExpression() {
        var input = "while (statement) { let x = 10; }";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseWhile();
        var expectedResult =  """
                  <whileStatement>
                        <keyword> while </keyword>
                        <symbol> ( </symbol>
                        <expression>
                        <term>
                        <identifier> statement </identifier>
                        </term>
                        </expression>
                        <symbol> ) </symbol>
                        <symbol> { </symbol>
                        <letStatement>
                        <keyword> let </keyword>
                        <identifier> x </identifier>
                        <symbol> = </symbol>
                        <expression>
                        <term>
                        <integerConstant> 10 </integerConstant>
                        </term>
                        </expression> 
                        <symbol> ; </symbol>
                        </letStatement>
                        <symbol> } </symbol>
                        </whileStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseVoidReturn() {
        var input = "return;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseReturn();
        var expectedResult =  """
                  <returnStatement>
                        <keyword> return </keyword>
                        <symbol> ; </symbol>
                  </returnStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }

    @Test
    public void testParseReturnExpression() {
        var input = "return x;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseReturn();
        var expectedResult =  """
                  <returnStatement>
                        <keyword> return </keyword>
                        <expression>
                        <term>
                        <identifier> x </identifier>
                        </term>
                        </expression>
                        <symbol> ; </symbol>
                  </returnStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }
    @Test
    public void testParseDo() {
        var input = "do Sys.wait(5);";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseDo();

        var expectedResult = """
            <doStatement>
            <keyword> do </keyword>
            <subRoutineCall>
            <identifier> Sys </identifier>
            <symbol> . </symbol>
            <identifier> wait </identifier>
            <symbol> ( </symbol>
            <expressionList>
              <expression>
                <term>
                  <integerConstant> 5 </integerConstant>
                </term>
              </expression>
            </expressionList>
            <symbol> ) </symbol>
            </subRoutineCall>
            <symbol> ; </symbol>
          </doStatement>
                """;
        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);
    }
}
