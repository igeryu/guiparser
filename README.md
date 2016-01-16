# Java-based Recursive-Descent Parser

This application parses an artificial GUI definition language defined in an input file and generates the GUI that it defines. The grammar for this language is defined below:

    gui ::=
        Window STRING '(' NUMBER ',' NUMBER ')' layout widgets End '.'
    layout ::=
        Layout layout_type ':'
    layout_type ::=
            Flow |
        Grid '(' NUMBER ',' NUMBER [',' NUMBER ',' NUMBER] ')'
    widgets ::=
        widget widgets |
        widget
    widget ::=
        Button STRING ';' |
        Group radio_buttons End ';' |
        Label STRING ';' |
        Panel layout widgets End ';' |
        Textfield NUMBER ';'
    radio_buttons ::=
        radio_button radio_buttons |
        radio_button
    radio_button ::=
        Radio STRING ';'

After parsing, it then creates a GUI window using Swing and AWT components.