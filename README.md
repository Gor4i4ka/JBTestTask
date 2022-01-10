Пока плагин в Альфа-версии, поэтому я знаю только следующий вариант запуска:

1) Клонируем репозиторий
2) Открываем "KotlinPsi" в Idea
3) Тыкаем task Gradle "Intelliji[runIde]"
4) Открываем проект JBTestProject
5) Открываем JBTestTask/JBTestProject/src/main/kotlin/testDsl/specialTests/largeClassTest/usage.kt
7) Тыкаем на "желтенький" "Crowd", применяем "quickFix"
7) ????
8) PROFIT
9) Наслаждаемся сгенерированным wrapper'ом

P.S. Пример из файла с таском тоже есть, но закомменчен, иначе код, юзающий индексацию бузотёрит

upd:
Как я писал в письме, автоимпортов необходимых пакетов нет. Класс Person присутствует в 2 экземплярах. LargeClassTest.kt который был раньше,
присутствует так же в specialTests. Теперь он LargeClassTestLocal.kt

