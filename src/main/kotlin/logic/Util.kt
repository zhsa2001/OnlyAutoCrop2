package logic

/**
 * Функция проверки строки на то, что она содержит только числа
 * @param num проверяемая строка
 * @return true, если строка не содержит в себе символы, не являющиеся цифрами
 */
fun checkIsNum(num: String):Boolean{
    var res = true
    for(c in num){
        if(!c.isDigit()){
            res = false
            break
        }
    }
    return res
}