# Overview
   Описание: Вендинговый аппарат, TCP-клиент (далее клиент); 
   Сервис - сервер. 
   
## Interface specifications
   Соединение клиент-сервер происходит по постоянному TCP соединению. Для приема/отдачи сообщений используется Socket. 
   Пример сообщений описан ниже:

   | Head                  | Body |
   |-----------------------|------|
   | Length &#124; Padding | JSON |
   

### Head：
   length: 4 байта, определяет длину `всего` сообщения (включая заголовок) в Ascii
   code (decimal), unit: byte. Чтобы корректно прочитать заголовок нужно вычесть из каждого байта ASCII код нуля (48) и использовать LittleEndian. 
   padding: It is identified by Ascii code, не был замечен в использовании 
   Для генерации собственного хедера 
### Body: 
   JSON формат, подробное описание пакета данных каждого интерфейса показано в API документации. Кодировка: UTF-8

____
   
## Service specifications
   Сервис (Spring-PostgreSQL-ServerSocket-Maven, в будущем +Rabbit/HTTP json).
   JDK 22
   input: массив байтов, описанный выше (12 байтов - length тела + padding)
   ouput: массив байтов, согласно шаблону, отправляется на вендинговый аппарат. Нужные JSON данные идут в брокер сообщений 
   Rabbit, а также в БД