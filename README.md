Пока плагин в Альфа-версии, поэтому я знаю только следующий вариант запуска:

1) Клонируем репозиторий
2) Открываем "KotlinPsi" в Idea
3) Тыкаем task Gradle "Intelliji[runIde]"
4) Открываем проект JBTestProject
5а) Открываем JBTestTask/JBTestProject/src/main/kotlin/testDsl/specialTests/largeClassTestLocal.kt
Это тест когда все классы в одном файле
5б) Открываем JBTestTask/JBTestProject/src/main/kotlin/testDsl/specialTests/largeClassTest/usage.kt
Это тест когда классы раскиданы по разным пакетам. 
класс "Person" присутствует в 2 экземплярах
6) Тыкаем на "желтенький" "Crowd", применяем "quickFix"
7) ????
8) PROFIT
9) Наслаждаемся сгенерированным wrapper'ом

P.S. Пример из файла с таском тоже есть, но закомменчен, иначе код, юзающий индексацию бузотёрит
P.S.S. да, как я писал в письме, - пока автоимпортов сгенерированных билдеров нет.

