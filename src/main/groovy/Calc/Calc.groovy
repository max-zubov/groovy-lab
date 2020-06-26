package Calc

class Calc {

    public static void main(String[] args) {
        //println "Вычисление выражения, унарные операции не реализованы"

        def clc = new Calculator()
        String exprStr = ""

        if(args.length>0) 
            exprStr = args[0]
        else
            exprStr = " 20 / ( (2*.5 + (5.1-3.1 )**(6?:15/0?:3) ) ) = +7"

            println "Input string: " + exprStr
            println "Result: " + clc.resolve(exprStr)
    }
}


class Calculator {

    GString resolve(String instr) {

        def start = new startItem()
        def itog = start.compile(new express(), instr)

        print("Postfix string: ")
        itog.postfixList.each { print("${it.lexemma} ")}
        println " "
        itog.stackList.each { print(it.lexemma)}
        println " "
        itog.postfixList.each {it.calculate(itog)}

        return "${itog.stackList[-1].lexemma}"
    }
}

class express {
    calcItem[] postfixList = []
    calcItem[] stackList = []
    calcItem[] item = [new valueItem(), \
                      new stepItem(),  \
                      new elvisItem(),  \
                      new plusItem(), new minusItem(), \
                      new multyItem(), new divideItem(),  \
                      new equalItem(),  \
                      new op_bracketItem(),  \
                      new cl_bracketItem(),  \
                      new blankItem(), \
                      new errorItem()
    ]
}

/*  основной класс для всех элементов калькулятора: числа, операторы, скобки элементы управления и т.п. */
abstract class calcItem {

    String lexemma = "@"  //лексемма элемента - если она имеется
    int prioritet = 0     //приоритет для элементов при разборе выражения
    double digit = 0      //числовое значение операндов в соответствии с лексеммой

    //поиск лексеммы в начале не разобранной части выражения
    //для части элементов переопределяется
    int compare(String s) {
        if( s.startsWith(lexemma))
            return lexemma.length()
        return 0
    }

    //работа элемента со стеком при преобразовании в постфиксную запись
    def perform(express expr) {
        return expr
    }

    //перевод элементом своей части выражения в постфиксную запись,
    // поиск и передача управления следующему элементу
    // !!!!!!!!!!!!!! не переопределяется при наследовании
    def compile(express expr, String infix){
        def next_expr = perform(expr)    //перевод в постфиксную
        int i, c
        //если выражение закончилось прередаем управление элементу окончания
        if(infix.length()==0) return new endItem().perform(next_expr)
        //определяем следующий элемент
        for (i = c = 0; ( i < next_expr.item.length )&&(c==0); i++) {
            c = next_expr.item[i].compare(infix)
        }
        // и передаем ему управление
        return next_expr.item[i-1].compile(next_expr, infix.drop(c))
    }
    //выполнение опреандами своих действий при вычислении выражения
    //переопределено ниже по иерархии для операндов
    def calculate(express expr){
        return expr
    }

    //выполнение операторами своих действий при вычислении выражения
    //переопределено ниже по иерархии для операторов
    def calculate(express expr, Closure oper){
        return expr
    }
}


/* стартовый элемент
* !!!!!!!!!!!!!! в item[] не помещается */
class startItem extends calcItem {
}

/*
abstract  class operandItem extends calcItem{
}
*/

/* числовое значение */
class valueItem extends calcItem {
    valueItem() {lexemma="0123456789"; prioritet=0; digit=1;}
    valueItem(String l) { lexemma=l; prioritet=0; digit=l.toDouble();}

    int compare(String s) {
        lexemma=""
        int  i,d, l = s.length()
        for(i = d =0; i<l; ++i) {
            if(( ('0'<=s[i]) && (s[i]<='9') ) || (s[i]=='.') ){
                lexemma = lexemma + s[i]
                if(s[i]=='.') d++
            }
            else break
        }
        l = lexemma.size()
        if(d > 1) return -l
        else      return l
    }

    def perform(express expr){
        expr.postfixList=expr.postfixList+new valueItem(lexemma)
        return expr
    }

    def calculate(express expr){
        expr.stackList = expr.stackList + this
        return expr
    }
}

/* общий класс для всех операторов,
* отдельно для каждого оператора определяются только лексемма, приоритет и действие в виде Closure.
* в item[] составные операторы помещаются ранее простых*/
abstract  class operatorItem extends calcItem{

    def perform(express expr){
        while (expr.stackList.length!=0) {
            if( this.prioritet <= (expr.stackList[-1].prioritet) ){
                expr.postfixList = expr.postfixList + expr.stackList[-1]
                expr.stackList = expr.stackList.dropRight(1)
            }else break
        }
        expr.stackList=expr.stackList+this
        return expr
    }

    def calculate(express expr, Closure oper){
        expr.stackList[-2].digit=oper(expr.stackList[-2].digit, expr.stackList[-1].digit)
        expr.stackList[-2].lexemma = expr.stackList[-2].digit.toString()
        expr.stackList = expr.stackList.dropRight(1)
        return expr
    }
}


class plusItem extends operatorItem {
    plusItem() {lexemma="+"; prioritet=10}

    def calculate(express expr){
        calculate(expr, {i,j -> return(i+j)})
    }
}


class minusItem extends operatorItem {
    minusItem() {lexemma="-"; prioritet=10}

    def calculate(express expr){
        calculate(expr, {i,j -> return(i-j)})
    }
}


class multyItem extends operatorItem {
    multyItem() {lexemma="*"; prioritet=20}

    def calculate(express expr){
        calculate(expr, {i,j -> return(i*j)})
    }
}


class divideItem extends operatorItem  {
    divideItem() {lexemma="/"; prioritet=20}

    def calculate(express expr) {
        if (expr.stackList[-1].digit != 0)
            calculate(expr, {i,j -> return(i/j)})
        else
            return expr
    }
}


class stepItem extends operatorItem {
    stepItem() {lexemma="**"; prioritet=30}

    def calculate(express expr){
        calculate(expr, {i,j -> return(i**j)})
    }
}


class elvisItem extends operatorItem {
    elvisItem() {lexemma="?:"; prioritet=40}

    def calculate(express expr){
        calculate(expr, {i,j -> if(i==0) i=j; return i;})
    }
}


class equalItem extends operatorItem  {
    equalItem() {lexemma="="; prioritet=5}
}


class op_bracketItem extends calcItem {
    op_bracketItem() {lexemma="("; prioritet=0}

    def perform(express expr){
        expr.stackList=expr.stackList+this
        return expr
    }
}


class cl_bracketItem extends operatorItem {
    cl_bracketItem() {lexemma=")"; prioritet=0}

    def perform(express expr){
        while (expr.stackList.length!=0) {
            if( expr.stackList[-1].lexemma != "(" ){
                expr.postfixList = expr.postfixList + expr.stackList[-1]
                expr.stackList = expr.stackList.dropRight(1)
            }else{
                expr.stackList = expr.stackList.dropRight(1)
                break
            }
        }
        return expr
    }
}

/* пробелы и табуляции удаляются из выражения */
class blankItem extends calcItem {
    blankItem() {lexemma="@"; prioritet=0}

    int compare(String s) {
        lexemma=""
        def l = s.length()
        for( int i=0; (i<l)&&((" "==s[i]) || (s[i]=="\t")); i++) {
            lexemma = lexemma + s[i]
        }
        lexemma.length()
    }
}

/* определение ошибки в лексеммах и отмена выполнения
* !!!!!!!!!!!!!! помещается последним в item[] */
class errorItem extends calcItem {
    errorItem() {lexemma="error "; prioritet=0}

    int compare(String s) {
        lexemma="Error from that --> ${s}"
        return s.length()
    }

    def perform(express expr){
        calcItem[] errList = []
        expr.stackList  = errList
        errList = errList  + this
        expr.postfixList = errList
        return expr
    }

    def calculate(express expr){
        expr.stackList = expr.stackList + this
        return expr
    }
}

/* конечные действия закрытия стека
* !!!!!!!!!!! в item[] не помещается */
class endItem extends  calcItem {

    def perform(express expr){
        while (expr.stackList.length!=0) {
            expr.postfixList = expr.postfixList + expr.stackList[-1]
            expr.stackList = expr.stackList.dropRight(1)
        }
        return expr
    }
}
