# Expressioner2
#### A new version of the [Expressioner](https://github.com/iamsubhranil/Expressioner) project with regular grammer, syntax trees and visitor pattern
This program solves a given expression containing arbitrary number of unknown variables, with arbitrary precision output. It supports all common trigonometric functions : `sin`, `cos` and `tan` family of functions. It also supports several other common functions like `sqrt`, `log` and `log10`, `exp` etc. Finally, the program itself provides a set of common constants like `PI`, `E` etc. This program also carefully upscales the output datatype of an operation, with a strictly only-if-needed policy. Even so, any operation involving trigonometric functions, `sqrt`, `log` or such type of functions, a mixed mode operation, or the division operation will always result the datatype to be upscaled to `BigDecimal`. However, any simple arithmetic operation involving one or two `BigInteger`s, like addition, summation, multiplication, factorization will be performed and generated as integer expressions only. It can also save a generated expression in binary form, and can recalculate the binary expression later by reading it from a file. This should (however theoritically) be faster than reading from standard input.
##### Building
1. Clone the repo or download the source code as a zip file.
2. Do either a in directory or an out of directory build, I prefer doing the later because it keeps the tree clean.
```
$ mkdir build
$ cd build
$ cmake ..
$ make all
```
##### Running
1. Arguments

a) `--precision` or `-p` : Set the output precision of the program for the running instance

b) `--rounding_mode` or `-r` : Set the rounding mode from one of Java's valid [RoundingMode](https://docs.oracle.com/javase/8/docs/api/java/math/RoundingMode.html)s ( Note : Either specifying the exact mode name or specifying its value as an integer will do)

c) `--save` or `-s` : Save an expression in binary from to a file. It will only happen if the given expression parses successfully.

d) `--load` or `-l` : Load and recalculate a previously saved expression from a binary file.

e) `--display` or `-d` : Display the present expression before solving. Particularly useful in cojunction with `--load`. Valid display modes are `prefix`,`infix` or `postfix`.

2. Example

You can run the program with any combination of the arguments and even without any of them at all. If no argument is specified, the programs uses Java provided IEEE754 complaint DECIMAL128 precision, i.e. precision upto 34 digits after the decimal point, and HALF_EVEN rounding mode.

a) Run the program with an output precision upto 100 digits after the decimal point and rounding mode [FLOOR](https://docs.oracle.com/javase/8/docs/api/java/math/RoundingMode.html#FLOOR)
```
$ java -jar Expressioner.jar --precision=100 --rounding_mode=3
$ java -jar Expressioner.jar --precision 100 --rounding_mode 3
$ java -jar Expressioner.jar -p=100 -r=3
$ java -jar Expressioner.jar -p 100 -r 3
```
b) Run the program with default precision and rounding mode [HALF_UP](https://docs.oracle.com/javase/8/docs/api/java/math/RoundingMode.html#HALF_UP)
```
$ java -jar Expressioner.jar -r HALF_UP
```
c) Run the program with default rounding mode and output precision upto 50 digits after the decimal point
```
$ java -jar Expressioner.jar -p 50
```
d) Run the program and save the resulting expression as `myexpression.expr`
```
$ java -jar Expressioner.jar --save=myexpression.expr
$ java -jar Expressioner.jar --save myexpression.expr
$ java -jar Expressioner.jar -s=myexpression.expr
$ java -jar Expressioner.jar -s myexpression.expr
```
e) Load and recalculate an expression saved in `myexpression.expr` with precision 100 and show its infix form
```
$ java -jar Expressioner.jar -l=myexpression.expr -p=100 -d=infix
```
