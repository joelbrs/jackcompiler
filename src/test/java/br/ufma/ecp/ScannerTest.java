package br.ufma.ecp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import br.ufma.ecp.token.Token;
import br.ufma.ecp.token.TokenType;



public class ScannerTest extends TestSupport {
    @Test
    public void testSimple () {
        String input = "45  + if + \"ola mundo\" - 876";
        Scanner scan = new Scanner (input.getBytes());
        for (Token tk = scan.nextToken(); tk.getType() != TokenType.EOF; tk = scan.nextToken()) {
            System.out.println(tk);
        }
    }
    @Test
    public void testScannerWithSquareGame() throws IOException {
        var input = fromFile("Square/SquareGame.jack");
        var expectedResult =  fromFile("Square/SquareGameT.xml");

        var scanner = new Scanner(input.getBytes(StandardCharsets.UTF_8));
        var result = new StringBuilder();
        
        result.append("<tokens>\r\n");

        for (Token tk = scanner.nextToken(); tk.getType() != TokenType.EOF; tk = scanner.nextToken()) {
            result.append(String.format("%s\r\n",tk));
        }

        result.append("</tokens>\r\n");
        
        assertEquals(expectedResult, result.toString());
    }
    @Test
    public void testScannerWithSquare() throws IOException {
        var input = fromFile("Square/Square.jack");
        var expectedResult =  fromFile("Square/SquareT.xml");

        var scanner = new Scanner(input.getBytes(StandardCharsets.UTF_8));
        var result = new StringBuilder();
        
        result.append("<tokens>\r\n");

        for (Token tk = scanner.nextToken(); tk.getType() !=TokenType.EOF; tk = scanner.nextToken()) {
            result.append(String.format("%s\r\n",tk));
        }
        
        result.append("</tokens>\r\n");
        System.out.println(result);
        assertEquals(expectedResult, result.toString());
    }
    @Test
    public void testParseTermInteger () {
        var input = "10;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseTerm();
        var expectedResult =  """
        <term>
        <integerConstant> 10 </integerConstant>
        </term>
        """;

        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);

    }
    @Test
    public void testParseTermIdentifer() {
        var input = "varName;";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseTerm();

        var expectedResult =  """
          <term>
          <identifier> varName </identifier>
          </term>
          """;

        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);

    }
    @Test
    public void testParseTermString() {
        var input = "\"Hello World\"";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseTerm();

        var expectedResult =  """
          <term>
          <stringConstant> Hello World </stringConstant>
          </term>
          """;

        var result = parser.XMLOutput();
        expectedResult = expectedResult.replaceAll("  ", "");
        result = result.replaceAll("\r", ""); // no codigo em linux não tem o retorno de carro
        assertEquals(expectedResult, result);

    }
    @Test
    public void testParseExpressionSimple() {
        var input = "10+20";
        var parser = new Parser(input.getBytes(StandardCharsets.UTF_8));
        parser.parseExpression();

        var expectedResult =  """
          <expression>
          <term>
          <integerConstant> 10 </integerConstant>
          </term>
          <symbol> + </symbol>
          <term>
          <integerConstant> 20 </integerConstant>
          </term>
          </expression>
          """;

        var result = parser.XMLOutput();
        result = result.replaceAll("\r", "");
        expectedResult = expectedResult.replaceAll("  ", "");
        assertEquals(expectedResult, result);
    }
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

}
