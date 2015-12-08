/**
 * CMSC330P1.java
 * 
 * CMSC 330
 * Project 1
 * 
 * Alan Johnson
 * 2 February 15
 * NetBeans IDE 8.0.2
 */

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;


/**
 *
 * @author Alan Johnson
 */
public class CMSC330P1 {
    
    final boolean debug = false;
    
    enum TokenType {NOT_FOUND, SEMICOLON, COMMA, STRING, WIDGET, END, WINDOW,
                LAYOUT, FLOW, BUTTON, LABEL, PANEL, TEXTFIELD, GRID, GROUP,
                RADIO, QUOTATION, OPEN_PARENTHESIS, CLOSE_PARENTHESIS, COLON,
                PERIOD, NUMBER, END_OF_FILE}
    
    enum Status {IN_PARENTHESIS, IN_QUOTATION, DEFAULT}
    
    Lexer lexer;
    
    Parser parser;
    
    public static void main(String[] args) {
        
        //                       ----  Setup  ----
        
        System.out.println("Alan Johnson, CMSC 330, Project 1\n");
        
        //                       ----  Action  ----
        
        CMSC330P1 test = new CMSC330P1();
        
        test.startOptions();
        
    }  //  end main() method
    
    
    
    /**
     * Displays to the user the option to load an existing file, which is parsed
     * by <code>parser</code>
     */
    private void startOptions() {
        
        JFileChooser fc = new JFileChooser();
        
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        int fcOption;
        
        do {
                        
            fcOption = fc.showOpenDialog(null);
                
        } while (fcOption != JFileChooser.APPROVE_OPTION && fcOption != JFileChooser.CANCEL_OPTION);
        
        if (fcOption == JFileChooser.CANCEL_OPTION) return;
        
        File file = fc.getSelectedFile();
        
        lexer = new Lexer(file); lexer.analyzeFile(); lexer.printTokens();
        
        parser = new Parser();
        
        if (parser.parseFile()) System.out.println("\n\nParse Pass!");
        
        return;
        
    }  // end startOptions() method
    
    
    
    /**
     * Designed based on the Lexer class example in CMSC 330, Module 1, Part C.
     * This example can be found at:
     * https://learn.umuc.edu/d2l/le/content/47340/viewContent/2303098/View
     * 
     */
    private class Lexer {
        
        private File file;
        
        private char character;
        
        private int i;
        
        private TokenType lastToken;
        
        private Status status;
        
        private String line = "", currentLexeme;
        
        private ArrayList<Token> tokens;
        
       
        /**
         * Constructor initializes private data members and opens the input
         * file.
         * 
         * @param _file 
         */  
        public Lexer (File _file) {
            
            if (_file != null) {
                
                file = _file;
                
            }
            
            tokens = new ArrayList<>();
            
        }  //  end Lexer constructor
        
        
        
        /**
         * Adds given token to <code>tokens</code>, after checking some contextual
         * information.  Even if the lexeme is marked as a special token, verify
         * that it's not in a parenthesis (a number literal) or in a quotation
         * (a string literal):
         * 
         * @param token
         * @param lexeme 
         */
        private void addToken(TokenType token, String lexeme) {
            
            if (status == Status.IN_PARENTHESIS) {
                
                if (token == TokenType.CLOSE_PARENTHESIS) {
                    
                    //  Closing a parenthesis:
                    lastToken = token;
                    tokens.add(new Token(token, lexeme));
                    status = Status.DEFAULT;
                    return;
                    
                }  //  end if closing a parenthesis
                    
                //  Otherwise, its a Number:
                lastToken = token;
                tokens.add(new Token(token, lexeme));
                return;
                
            } else if (status == Status.IN_QUOTATION) {
                
                if (token == TokenType.QUOTATION) {  //  Closing a Quotation:
                    
                    if (lastToken == TokenType.QUOTATION) {
                        
                        // Quotation was closed out with an empty String:
                        tokens.add(new Token(TokenType.STRING, ""));
                        
                        tokens.add(new Token(token, lexeme));
                        status = Status.DEFAULT;
                        return;
                        
                    }  //  else, closing a quotation after String added:
                    
                    lastToken = token;
                    tokens.add(new Token(token, lexeme));
                    status = Status.DEFAULT;
                    return;
                    
                }  //  else, it is a string, to be appended to previous String:
                
                if (lastToken == TokenType.STRING) {
                    
                    int x = tokens.size() - 1;
                    tokens.get(x).lexeme += " " + lexeme;
                    return;
                    
                }  //  else, it is the first portion of the String:
                
                token = TokenType.STRING;
                lastToken = token;
                tokens.add(new Token(token, lexeme));
                return;
                
            }  else if (token == TokenType.QUOTATION) {

                //  Opening a quoted String:
                lastToken = token;
                tokens.add(new Token(token, lexeme));
                status = Status.IN_QUOTATION;
                return;
                
            } else if (token == TokenType.OPEN_PARENTHESIS) {
                
                //  Opening a parenthesised Number:
                lastToken = token;
                tokens.add(new Token(token, lexeme));
                status = Status.IN_PARENTHESIS;
                return;
            }
            
            lastToken = token;
            tokens.add(new Token(token, lexeme));
            
        }  //  end method addToken()
        
        
        
        /**
         * Calls the openFile() and analyzeInput() methods.
         * 
         * The Scanner object returned by openFile() is given to analyzeInput().
         */
        private void analyzeFile() {
            
            Scanner scanner = openFile(file);
        
            if (scanner != null) { analyzeInput(scanner);}
            
        }  //  end method analyzeFile()
        
        
        
        /**
        * Is called by <code>analyzeFile()</code>
        * 
        * Calls <code>analyzeLine()</code> on each line in the <code>Scanner</code> parameter.
        * 
        * @param scanner      Contains the data from the <code>File</code> object supplied to <code>analyzeFile()</code> method
        */
        private void analyzeInput (Scanner scanner) {
        
            while (scanner.hasNextLine()) {      //  Grab lines from the input
            
                String nextLine = scanner.nextLine();
                
                if (!(nextLine.startsWith("/") || nextLine.isEmpty()))
                    analyzeLine(nextLine);
            
            }  //  Stepping through the input file
        
        }  //  end analyzeInput() method
        
        
        
        /**
        * Is called by <code>analyzeInput()</code>.  Takes a single <code>String</code>
        * (one line from the input file) and analyzes it into <code>Token</code>
        * objects into <code>tokens</code>
        * 
        * @param nextLine      The <code>String</code> to be analyzed
        */
        private void analyzeLine (String nextLine) {
            
            line = nextLine; i = 0; character = nextChar();
            String punctuations = "\"(),:;.";
            
            do {
                
                currentLexeme = "";
                
                while (character != 0 && (Character.isWhitespace(character) ||
                        !(punctuations.contains(String.valueOf(character)) ||
                        Character.isAlphabetic(character) || Character.isDigit(character)))) {
                    
                    character = nextChar();
                    
                }  //  while 'character' is not valid, move right
                
                if (character == 0) return;  //  End of line, return
                
                if (Character.isLetterOrDigit(character)) {

                    //  Found Upper-Case Identifier, Number or String:
                    currentLexeme += character; character = nextChar();
                    
                    while (Character.isLetterOrDigit(character)) {
                        currentLexeme += character;
                        character = nextChar();
                        
                    }  //  while 'character' is a letter
                    
                    TokenType token = testToken(currentLexeme);
                    
                    addToken(token, currentLexeme);
                    
                }  //  if 'character' is Upper Case
                
                
                else if (punctuations.contains(String.valueOf(character))) {
                    
                    currentLexeme = String.valueOf(character);
                    
                    TokenType token = testPunctuation(currentLexeme);
                    
                    addToken(token, currentLexeme);
                    
                    character = nextChar();
                    
                }
                
            } while (character != 0);
        
        }  //  end analyzeLine() method
        
        
        
        /**
         * Returns the next character in <code>line</code>, or <code>0</code> if
         * the end of <code>line</code> is reached.
         * 
         * @return 
         */
        private char nextChar() {
            
            if (i == line.length()) { i = 0; return 0; }
           
            else return line.charAt(i++);
            
        }  //  end method nextChar()
        
        
        
        /**
        * Is called by analyzeFile()
        * 
        * @param file     File to be opened into a <code>Scanner</code> object
        * @return         <code>Scanner</code> from parameter
        */
        private Scanner openFile(File file) {
            
            Scanner scanner;
            
            try { scanner = new Scanner(file); }
            
            catch (FileNotFoundException e) {
            
                System.out.println("File input fail!");
            
                return null;
            
            }  //  catch
        
            return scanner;
        
        }  //  end method readFile()
        
        
        
        /**
         * 
         */
        private void printTokens() {
            
            for (int x = 0; x < tokens.size(); x++) {
                
                if (x % 5 == 0) System.out.println("\n");
                
                System.out.print(tokens.get(x) + " ");
                
            }  //  For all tokens
            
        }  //  end method printTokens()
        
        
         
        /**
         * Tests punctuation of the given lexeme and returns the appropriate <code>TokenType</code>
         * 
         * @param lexeme     lexeme to be analyzed
         * @return           <code>TokenType</code> of the lexeme
         */
        private TokenType testPunctuation(String lexeme) {
            
            switch(lexeme) {
                
                case "\"":
                    return TokenType.QUOTATION;
                    
                case "(":
                    return TokenType.OPEN_PARENTHESIS;
                    
                case ")":
                    return TokenType.CLOSE_PARENTHESIS;
                    
                case ",":
                    return TokenType.COMMA;
                    
                case ":":
                    return TokenType.COLON;
                    
                case ";":
                    return TokenType.SEMICOLON;
                    
                case ".":
                    return TokenType.PERIOD;
                    
                default:
                    return TokenType.NOT_FOUND;
                
            }  //  switch
            
        }  //  end method testPunctuation()
        
        
        
        /**
         * Tests non-punctuation lexemes for token type; returns that type
         * 
         * @param lexeme     lexeme to be analyzed
         * @return           <code>TokenType</code> of that lexeme
         */
        private TokenType testToken(String lexeme) {
            
            if (status == Status.IN_QUOTATION) {
                
                return TokenType.STRING;
                
            } else if (status == Status.IN_PARENTHESIS) {
                
                boolean flag = true;
                
                for (int x = 0; x < lexeme.length(); x++) {
                    
                    char c = lexeme.charAt(x);
                    
                    if (!Character.isDigit(c))
                        flag = false;
                    
                }  //  check that all characters are digits
                
                if (flag) {
                    
                    return TokenType.NUMBER;
                    
                } else return TokenType.NOT_FOUND;
                
            } else switch(lexeme.charAt(0)) {
                
                case 'B':
                    if (lexeme.equals("Button")) {
                        
                        return TokenType.BUTTON;
                        
                    }
                    
                case 'E':
                    if (lexeme.equals("End")) {
                        
                        return TokenType.END;
                        
                    }
                
                case 'F':
                    if (lexeme.equals("Flow")) {
                        
                        return TokenType.FLOW;
                        
                    }
                    
                case 'G':
                    if (lexeme.equals("Grid")) {
                        
                        return TokenType.GRID;
                        
                    } else if (lexeme.equals("Group")) {
                        
                        return TokenType.GROUP;
                        
                    }
                    
                case 'L':
                    if (lexeme.equals("Label")) {
                        
                        return TokenType.LABEL;
                        
                    } else if (lexeme.equals("Layout")) {
                        
                        return TokenType.LAYOUT;
                        
                    }
                    
                case 'P':
                    if (lexeme.equals("Panel")) {
                        
                        return TokenType.PANEL;
                        
                    }
                    
                case 'R':
                    if (lexeme.equals("Radio")) {
                        
                        return TokenType.RADIO;
                        
                    }
                    
                case 'T':
                    if (lexeme.equals("Textfield")) {
                        
                        return TokenType.TEXTFIELD;
                        
                    }
                   
                case 'W':
                    if (lexeme.equals("Window")) {
                        
                        return TokenType.WINDOW;
                        
                    }
                    
                default:
                    return TokenType.NUMBER;
                
            }  //  switch
            
        }  //  end method testToken()
        
    }  //  end class Lexer
    
    
    
    private class Token {
        
        TokenType type;
        String lexeme;
        
        public Token(TokenType _type, String _lexeme) {
            type = _type;
            lexeme = _lexeme;
        }
        
        
        /**
         * 
         * @return 
         */
        @Override public String toString() {
           return String.format("[%s, \"%s\"]", type, lexeme); 
        }
        
        
    }  //  end class Token
    
    
    
    /**
     * 
     * 
     */
    private class Parser {
        
        ArrayList<Token> tokens;
        JFrame window;
        Container currentContainer;
        ButtonGroup group;
        int i = 0;
        TokenType token;
        String error = "";
        
        /**
        * Called by readFile().
        * 
        * Steps through each line in the ArrayList of ArrayLists of String objects, 
        * the input parameter, and verifies syntactical correctness.
        * 
        * @param tokens      List of tokens
        */
        private boolean parseFile() {
            
            tokens = lexer.tokens;
            token = nextToken();
            
            if (gui()){
                window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                window.setLocationRelativeTo(null);
                window.setVisible(true);
                return true;
            } else {
                System.out.printf("Fail on token %d: %s\n", i + 1, error);
                return false;
            }
        
        }  // end checkFile() method
        
        
        
        /**
         * 
         * @return 
         */
        private String getToken() {
            return tokens.get(i - 1).lexeme;
        }
        
        
        
        private boolean gui() {
            
            if (token == TokenType.WINDOW) {
                
                token = nextToken();
                
                if (token == TokenType.QUOTATION) {
                    
                    token = nextToken();
                
                    if (token == TokenType.STRING) {
                        
                        currentContainer = window = new JFrame(getToken());

                        token = nextToken();
                        
                        if (token == TokenType.QUOTATION) {
                            
                            token = nextToken();
                
                            if (token == TokenType.OPEN_PARENTHESIS) {

                                token = nextToken();

                                if (token == TokenType.NUMBER) {
                                    
                                    int width = Integer.parseInt(getToken());
                            
                                    token = nextToken();
                            
                                    if (token == TokenType.COMMA) {
                                
                                        token = nextToken();
                                
                                        if (token == TokenType.NUMBER) {
                                            
                                            int height = Integer.parseInt(getToken());
                                    
                                            token = nextToken();
                                    
                                            if (token == TokenType.CLOSE_PARENTHESIS) {
                                        
                                                token = nextToken();
                                        
                                                if (layout()) {
                                            
                                                    token = nextToken();
                                                    
                                                    if (widgets()) {
                                                    
                                                        if (token == TokenType.END) {
                                                    
                                                            token = nextToken();
                                                    
                                                            if (token == TokenType.PERIOD) {
                                                                
                                                                window.setMinimumSize(new Dimension(width, height));
                                                                    
                                                                return true;
                                                        
                                                            } else error = "GUI: \"Period\" token not found.";
                                                    
                                                        } else error += "  GUI: \"End\" token not found.";
                                                
                                                    } else error += "  GUI: \"Widgets\" pattern not found.";
                                            
                                                } else error += "  (GUI: \"Layout\" pattern not found.)";
                                        
                                            } else error = "GUI: \"Close_Parenthesis\" token not found.";
                                    
                                        } else error += "GUI: \"Number\" token not found.";
                                
                                    } else error = "GUI: \"Comma\" token not found.";
                            
                                } else error += "GUI: \"Number\" token not found.";
                        
                            } else error = "GUI: \"Open_Parenthesis\" token not found.";
                        
                        } else error = "GUI: \"Quotation\" token not found.";
                    
                    }  else error += "  (GUI: \"String\" token not found.)";
                
                } else error = "GUI: \"Quotation\" token not found.";
                
            }  else error = "GUI: \"Window\" token not found.";
            
            return false;
            
        }
        
        
        
        /**
        * Called by gui() or widget()
        * 
        * @param tokens
        * @return 
        */
        private boolean layout() {
            
            if (token == TokenType.LAYOUT) {
                
                token = nextToken();
                
                if (layout_type()) {
                    
                    token = nextToken();
                    
                    if (token == TokenType.COLON) {
                        
                        return true;
                        
                    } else error = "(\"Colon\" token not found.)";
                    
                } else error += "  \"Layout_Type\" pattern not found.";
                
            } else error += "  (\"Layout\" token not found.)";
        
            return false;
        
        }  //  end method layout()
    
        
         
        /**
        * Called by layout()
        * 
        * @param tokens
        * @return 
        */
        private boolean layout_type() {
            
            if (token == TokenType.FLOW) {
                
                currentContainer.setLayout(new FlowLayout());
                
                return true;
                
            } else if (token == TokenType.GRID) {
                
                token = nextToken();
                
                if (token == TokenType.OPEN_PARENTHESIS) {
                    
                    token = nextToken();
                    
                    if (token == TokenType.NUMBER) {
                        
                        int rows = Integer.parseInt(getToken());
                        
                        token = nextToken();
                        
                        if (token == TokenType.COMMA) {
                            
                            token = nextToken();
                            
                            if (token == TokenType.NUMBER) {
                                
                                int columns = Integer.parseInt(getToken());
                                
                                token = nextToken();
                                
                                if (token == TokenType.CLOSE_PARENTHESIS) {
                                    
                                    currentContainer.setLayout(new GridLayout(rows, columns));
                                    return true;
                                    
                                } else if (token == TokenType.COMMA) {
                                    
                                    token = nextToken();
                                    
                                    if (token == TokenType.NUMBER) {
                                        
                                        int hgap = Integer.parseInt(getToken());
                                        
                                        token = nextToken();
                                        
                                        if (token == TokenType.COMMA) {
                                            
                                            token = nextToken();
                                            
                                            if (token == TokenType.NUMBER) {
                                                
                                                int vgap = Integer.parseInt(getToken());
                                                
                                                token = nextToken();
                                                
                                                if (token == TokenType.CLOSE_PARENTHESIS) {
                                                    
                                                    currentContainer.setLayout(new GridLayout(rows, columns, hgap, vgap));
                                                    return true;
                                                    
                                                } else error = "\"Close_Parenthesis\" token not found.";
                                                
                                            } else error += "  (\"Number\" token not found.)";
                                            
                                        } else error = "\"Comma\" token not found.";
                                        
                                    } else error += "  (\"Number\" token not found.)";
                                    
                                } else error = "\"Comma\" or \"Close_Parenthesis\" token not found.";
                                
                            } else error += "  (\"Number\" token not found.)";
                            
                        } else error = "\"Comma\" token not found.";
                        
                    } else error += "  (\"Number\" token not found.)";
                    
                } else error = "\"Open_Parenthesis\" token not found.";
                
            } else error = "\"Flow\" or \"Grid\" token not found.";
        
            return false;
            
        }  //  end method layout_type()
        
        
        
        /**
         * 
         * @return 
         */
        private TokenType nextToken() {
            
            if (i == tokens.size()) { return TokenType.END_OF_FILE; }
            else                    { return tokens.get(i++).type; }
            
        }  //  end method nextToken()
        
        
        
        private boolean radioButton() {
            
            if (token == TokenType.RADIO) {
                
                token = nextToken();
                
                if (token == TokenType.QUOTATION) {
                    
                    token = nextToken();
                
                    if (token == TokenType.STRING) {
                        
                        JRadioButton button = new JRadioButton(getToken());
                        
                        token = nextToken();
                        
                        if (token == TokenType.QUOTATION) {
                            
                            token = nextToken();
                    
                            if (token == TokenType.SEMICOLON) {
                                
                                currentContainer.add(button);
                                group.add(button);
                                return true;
                        
                            } else error = "\"Semicolon\" token not found.";
                        
                        } else error += "  (\"Quotation\" token not found.)";
                    
                    } else error += "  (\"String\" token not found.)";
                
                } else error += "  (\"Quotation\" token not found.)";
                
            } else error += "  (\"Radio_Button\" pattern not found.)";
            
            return false;
            
        }  //  end method radioButton()
        
        
        
        private boolean radioButtons() {
            
            boolean flag = false; int temp = i;
             
            if (radioButton()) {
                
                flag = true;
                token = nextToken();
                temp = i;
                radioButtons();
                
            } else {i = temp;}
            
            return flag;
            
        }  //  end method radioButtons()
        
        
        
        private boolean widget() {
            
            switch (token) {
                
                case BUTTON:
                    
                    token = nextToken();
                    
                    if (token == TokenType.QUOTATION) {
                        
                        token = nextToken();
                    
                        if (token == TokenType.STRING) {
                            
                            JButton button = new JButton(getToken());
                            token = nextToken();
                            
                            if (token == TokenType.QUOTATION) {
                                
                                token = nextToken();
                        
                                if (token == TokenType.SEMICOLON) {
                                    
                                    currentContainer.add(button);
                                    return true;
                            
                                } else error = "Widget: \"Semicolon\" token not found.";
                            
                            } else error += "  (Widget: \"Quotation\" token not found.)";
                        
                        } else error += "  (Widget: \"String\" token not found.)";
                    
                    } else error += "  (Widget: \"Quotation\" token not found.)";
                    
                    
                    
                case GROUP:
                    token = nextToken();
                    
                    group = new ButtonGroup();
                    
                    if (radioButtons()) {
                        
                        if (token == TokenType.END) {
                            
                            token = nextToken();
                            
                            if (token == TokenType.SEMICOLON) {
                                
                                System.out.printf(debug ? "\nend widget(%s)" : "", tokens.get(i - 1).lexeme);
                                
                                return true;
                                
                            } else error = "Widget: Group: \"Semicolon\" token not found.";
                            
                        } else error += "  (Widget: Group: \"End\" token not found.)";
                        
                    } else error += "  (Widget: Group: \"Radio_Buttons\" pattern not found.)";
                    
                    
                    
                case LABEL:
                    token = nextToken();
                    
                    if (token == TokenType.QUOTATION) {
                        
                        token = nextToken();
                    
                        if (token == TokenType.STRING) {
                            
                            JLabel label = new JLabel(getToken());
                        
                            token = nextToken();
                            
                            if (token == TokenType.QUOTATION) {
                                
                                token = nextToken();
                        
                                if (token == TokenType.SEMICOLON) {
                                    
                                    currentContainer.add(label);
                                    
                                    System.out.printf(debug ? "\nend widget(%s)" : "", tokens.get(i - 1).lexeme);
                            
                                    return true;
                            
                                } else error = "Widget: \"Semicolon\" token not found.";
                            
                            } else error += "  (Widget: \"Quotation\" token not found.)";
                        
                        } else error += "  (Widget: \"String\" token not found.)";
                    
                    } else error += "  (Widget: \"Quotation\" token not found.)";
                    
                    
                    
                case PANEL:
                    System.out.printf(debug ? "\nPanel Starting(%d) on \"%s\"\n" : "", i - 1, tokens.get(i - 1));
                    
                    token = nextToken();
                    JPanel panel;
                    Container parentContainer = panel = new JPanel();
                    currentContainer.add(panel);
                    currentContainer = panel;
                    if (layout()) {
                        
                        token = nextToken();
                        
                        if (widgets()) {
                            
                            currentContainer = parentContainer;
                            
                            if (token == TokenType.END) {
                                
                                System.out.printf(debug ? "\nPanel : END found(%d) on \"%s\"\n" : "", i - 1, tokens.get(i - 1));
                                
                                token = nextToken();
                                
                                if (token == TokenType.SEMICOLON) {
                                    
                                    System.out.printf(debug ? "\nPanel Complete(%d) on \"%s\"\n" : "", i - 1, tokens.get(i - 1));
                                    
                                    Border border = BorderFactory.createLineBorder(Color.black);
                                    panel.setSize(500, 500);
                                    panel.setBorder(border);
                                    if (panel.getBorder() == null) System.out.printf("\nNo Border!\n");
                                    
                                    System.out.printf(debug ? "\nend widget(%s)" : "", tokens.get(i - 1).lexeme);
                                    return true;
                                    
                                } else error = "Widget: Panel: \"Semicolon\" token not found.";
                                
                            } else error = "Widget: Panel: \"End\" token not found.";
                            
                        } else error += "  Widget: Panel: \"Widget\" pattern not found.";
                        
                    } else error += "  (Widget: Panel: \"Layout\" pattern not found.)";
                    
                    
                    
                case TEXTFIELD:
                    token = nextToken();
                    
                    if (token == TokenType.NUMBER) {
                        
                        JTextField textField = new JTextField(Integer.parseInt(getToken()));
                        
                        token = nextToken();
                        
                        if (token == TokenType.SEMICOLON) {
                            
                            currentContainer.add(textField);
                            
                            System.out.printf(true ? "\nend widget(%s)" : "", tokens.get(i - 1).lexeme);
                            
                            return true;
                            
                        } else error = "Widget: \"Semicolon\" token not found.";
                        
                    } else error += "  (Widget: \"Number\" token not found.)";
                    
                    
                default:
                    error += "  Widget: \"Widgets\" pattern not found.";
                    return false;
                
            }
            
        }
        
        
         private boolean widgets() {
             
            boolean flag = false; int temp = i;
             
            if (widget()) {
                
                flag = true;
                token = nextToken();
                temp = i;
                
                Container parentContainer = currentContainer;
                
                widgets();
                
                currentContainer = parentContainer;
                
            } else {i = temp;}
            
            return flag;
            
        }  //  end method widgets()
        
        
        
    }  //  end class Parser
    
}
