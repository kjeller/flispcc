# FLISPCC - Flexible Instruction Set Processor C Compiler
- is a C (subset) compiler written in Java for the FLISP processor, which is an 8-bit processor used in introductionary courses
to computer engineering for Chalmers University of Technology. This compiler make use of the [BNFC tool](https://github.com/BNFC/bnfc),
which generates a compiler front-end from a labeled BNF grammar.

# Why use BNFC?
The FLISP processor is very similar to the Motorola 68HC11 microcontroller since both are accumulator-based (one-address)
CPUs with similar instructions and architechture. This led me to believe that it was easier to translate an [exisiting compiler](https://www.gnu.org/software/m68hc11/m68hc11_gcc.html) for the 68HC11
into FLISP instructions. After some research I realized that it was harder than I thought, learning how GCC works.
In the course DAT151 "Programming Language Technology" I recently took, we used context-free grammars where I learned BNF.

# The C language
Note: No C standard is used, since all the features of C11 can not be fully utilized with the FLISP, and thus It is not 
technically correct to call it a "C Compiler".

The precidence and associativity is directly derived from "C--", 
which is a small fragment of C++ used in the course I mentioned above:

| Level | Expression forms     | Assoc | Explanation             |
|-------|----------------------|-------|-------------------------|
| 6     | literal              | \-    | literal \(2\)           |
| 6     | identifier x         | \-    | variable \(3\)          |
| 6     | x\(e,\.\.\.,e\)      | none  | function call \(3\)     |
| 6     | x\+\+, x\-\-         | none  | post in/decrement \(3\) |
| 6     | \+\+x, \-\-x         | none  | pre in/decrement \(3\)  |
| 5     | e\*e, e/e            | left  | mult, div \(3\)         |
| 4     | e\+e, e\-e           | left  | add, sub \(3\)          |
| 3     | e<e, e>e, e>=e, e<=e | none  | comparison \(3\)        |
| 3     | e==e, e\!=e          | none  | \(in\)equality \(3\)    |
| 2     | e&&e                 | left  | conjunction \(3\)       |
| 1     | e\|\|e               | left  | disjunction \(3\)       |
| 0     | x=e                  | right | assignment \(3\)        |

# Installation
....TO BE ADDED
  Linux
    Add QAflisp to PATH..

  Windows
    Add QAflisp to PATH

Make it yourself from source:
....TO BE ADDED

The Assembler QAflisp can be downloaded from [here](http://www.gbgmv.se/html/digiflisp.html) (it is included with digiflisp). 

# Limitations
- String type and String literals are not yet supported
- Double type will not be supported

