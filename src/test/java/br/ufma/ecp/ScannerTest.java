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
}
