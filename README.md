# FLISPCC - Flexible Instruction Set Processor C Compiler
Flispcc is a C (subset) compiler written in Java for the FLISP processor, which is an 8-bit processor used in introductionary courses
to computer engineering on Chalmers University of Technology. This compiler make use of the [BNFC tool](https://github.com/BNFC/bnfc),
which generates a compiler front-end from a labeled BNF grammar.

# Why use BNFC?
The FLISP processor is very similar to the Motorola 68HC11 microcontroller since both are accumulator-based (one-address)
CPUs with similar instructions and architechture. This led me to believe that it was easier to translate an [exisiting compiler](https://www.gnu.org/software/m68hc11/m68hc11_gcc.html) for the 68HC11
into FLISP instructions. After some research I realized that it was harder than what I first thought. I would have to invest 
many hours learning how GCC works.
In the course DAT151 "Programming Language Technology" I recently took, we learned about context-free grammars and some tools that produces compiler front-ends, and this is  where I learned about BNFC.
The laborations for the course recommended BNFC to handle the parsing of files. So instead of translating GCC code I choose to write a compiler myself.

# The C language
The precidence and associativity is directly derived from "C--", 
which is a small fragment of C++ used in the course I mentioned above:

| Level | Expression forms     | Assoc | Explanation             |
|-------|----------------------|-------|-------------------------|
| 6     | literal              | \-    | literal                 |
| 6     | identifier x         | \-    | variable                |
| 6     | x\(e,\.\.\.,e\)      | none  | function call           |
| 6     | x\+\+, x\-\-         | none  | post in/decrement       |
| 6     | \+\+x, \-\-x         | none  | pre in/decrement        |
| 5     | e\*e, e/e            | left  | mult, div               |
| 4     | e\+e, e\-e           | left  | add, sub                |
| 3     | e<e, e>e, e>=e, e<=e | none  | comparison              |
| 3     | e==e, e\!=e          | none  | \(in\)equality          |
| 2     | e&&e                 | left  | conjunction             |
| 1     | e\|\|e               | left  | disjunction             |
| 0     | x=e                  | right | assignment              |


Note: Only a subset of C is implemented, since all the features of C11 can not be fully utilized with the FLISP.
# Datatypes
  TODO

# Limitations
  TODO

# Installation
- Linux
  1. Download binary from git or make from source. (Making requires the dependencies that BNFC requires).
  2. Download digiflisp from [here](http://www.gbgmv.se/html/digiflisp.html). 
  3. Add QAflisp to PATH:  ```export PATH=$PATH:/usr/share/digiflisp/``` (to the folder where you install digiflisp).
  4. Now the compiler chain should call the assembler after compilation.

- Windows
  Same as above but add QAflisp to Path environment variable, different procedure but should give same result.


Note: I have only tested installing the compiler and running the compiler on Linux.
