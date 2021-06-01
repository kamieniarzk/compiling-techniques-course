# Compiling techniques course project
## Task 
Write a program reading a subset of a selected language C, C++, C# ,Java, Python code and showing all variables, which are not used. Specify appropriate code limitations.

## Test cases
The functional test cases (input files) are in the directory `src/main/resources`. Running the tests by the instruction below will generate the output. The output is printed on the console and additionally at the end of every line at which there is unused variable in the input file, a comment is added: **`//unused variable`**

## Run instructions
This directory contains the source code, to run the tests, execute
`mvn test`  
The test cases in the `src/main/test` directory are for valid cases (with no errors). To run the test cases with errors, open the project in the IDE (tested in IntelliJ IDEA 2020.3), delete or comment the `@Disabled` annotation above error test methods and run the test class using the IDE. 