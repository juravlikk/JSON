JSON
====

Task 1

Задание.
Разработйте, пожалуйста, процесс генерации коротких аннотаций для
статей. Исходные данные
̃
1500 статей. Данные упакованы в
файл
, содер-
жащий на каждой строчке json-объект с тремя атрибутами:
url
- адрес документа;
title - plaintext
- заголовок статьи;
content - html
- фрагмент документа, содержащий тело статьи.
Пример (json отформатирован для читаемости):
{
"url":"http://someresource.ru/article/81
"content":«divitemprop=
̈
definition
̈
>Дедукция (от лат. deductio
- выведение) – процесс логического вывода на основани иперехода
от общих положений к частным.</div>
"title":"Дедукция"
}
Для каждой статьи необходимо сгенерировать plaintext-аннотацию,
включающую в себя заголовок и начало статьи, общая длина аннотации
не должна превышать 300 символов. В случае необходимости, текст ста-
тьи можно обрезать, добавив к последнему предложению, попадающему
в аннотацию "..."Следует обратить внимание, что тело статьи может со-
держать только часть заголовка или же не содержать его вовсе.
Результат работы программы:
Файл, на каждой строчке которого должен находиться json-объект с дву-
мя атрибутами:
url
- адрес документа.
snippet
- аннотация.
Пример (
json
отформатирован для читаемости):
{
"url":"http://someresource.ru/article/81
"snippet":"Дедукция (от лат. deductio – выведение) – процесс
логического вывода на основании перехода от общих положений к
частным."
}