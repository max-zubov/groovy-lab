package Calc

import spock.lang.Specification

class CalculatorTest extends spock.lang.Specification {
    Calculator calculator = new Calculator(metaClass: null)

    def "test resolve"(String instr, String result) {

       expect:
        result == calculator.resolve(instr)


        where:
        instr      |      result
        "2+2"      | "4.0"
        "3-2"      | "1.0"
        "3*22"     | "66.0"
        "11 /10"   | "1.1"
        "5.3=+7"   | "12.3"
        "3**2"     | "9.0"
        "0?:10"    | "10.0"
        "5?:10"    | "5.0"
        ".3+5*4"   | "20.3"
        "30/0?:10" | "3.0"
        "(1+4)**2" | "25.0"

    }
}

/*
class CalculatorTest extends Specification {
    Calculator calculator = new Calculator(metaClass: null)

    def "test resolve"() {
        when:
        GString result = calculator.resolve("2*2")

        then:
        result == "4"
    }
}

*/

