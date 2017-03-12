# OpenWifiLocalizator

Rémy Detobel - *000408013*  
Denis Hoornaert - *000413326*  
Nathan Licardo - *000408278*  
Robin Petit - *000408282*


## Conventions

### Generalities

+ Every block **shall** be delimited by braces (`{` and `}`), even if block is only one line long.
+ Indentation **shall** be made with four spaces.
+ Inline comments **shall** start to spaces after the end of the line.

### Naming Conventions

#### camelCase

Variables, attributes, methods, **shall** be written in camelCase (starting with a lower case, and starting every
new word with an upper case), example: `int myIntegerVariable`, `void myMethod(int myParam1, String myparam2)`, etc.

#### PascalCase

On the other hand, classes **shall** be written in PascalCase (same as camelCase but starting with an upper case),
example: `class ExampleClass;`

#### UPPER_CASE

Any constant object (which is then `final`) **shall** be written in UPPER_CASE with underscores to separate words.

### To summarize

```java
static final Object CONSTANT_OBJECT;

class ExampleClass {
    public void fooMethod(Object param) {
        if(param == CONSTANT) {
            // Code
        } else {
            oneLineInstruction(param);  // inline comment
        }
    }
};
```
