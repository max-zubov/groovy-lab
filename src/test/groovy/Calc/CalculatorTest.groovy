package Calc

import spock.lang.Specification

class CalculatorTest extends spock.lang.Specification {
    Calculator calculator = new Calculator(metaClass: null)

    def "test resolve"(String instr, String result) {

       expect:
        result == calculator.resolve(instr)


        where:
        instr      |      result
        "2+2"      | "4"
        "3-2"      | "1"
        "3*22"     | "65"
        "11/10"    | "1.1"

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

