## Создание таблиц:

```sql
CREATE TABLE shots (
    id SERIAL PRIMARY KEY,
    owner_login VARCHAR(20) NOT NULL,
    x_coordinate DOUBLE PRECISION NOT NULL,
    y_coordinate DOUBLE PRECISION NOT NULL,
    scope DOUBLE PRECISION NOT NULL,
    hit BOOLEAN NOT NULL,
    datetime TIMESTAMP NOT NULL,
    processing_time_nano BIGINT NOT NULL
);
```

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL
);
```

# Какой апи?

- /login - возвращает результат входа
- /logout - возвращает результат выхода
- /register - возвращает результат регистрации
- /shot - возвращает результат выстрела, записывая его данные в базу
- /shots - возвращает все выстрелы как json массив

# Перед подробным описанием опишу общие моменты, касающиеся ошибок:

Об ошибке можно догадаться по коду ответа, так что флага error в json не будет.
Однако же будет описание ошибки:

Ключ "error\_type", значения пока: 
- "absent\_key", к нему добавляется "absent\_keys" со списком отсутствующих ключей

Олсо при попытке вызова /shot или /shots (методы для реганых пользователей) с отсутствующией сессией получишь ошибку 403, поясняющие ключи не нужны 

Но не все ошибки должны помечаться определенным кодом ответа, ведь некоторые "отказы" могут быть в рамках бизнес-логики, не только связанные с валидацией приходящих данных.

# Подробное описание:

## Название:

/login

### Отправляется форма с:

- name="login", строковое значение длиной от 5 до 20


### Ответ:

- "login\_state", значения "logon", "wrong\_login" или "wrong\_password" 

## Название:

/logout


### Отправляется:

Пусто

### Ответ:

- "not\_entered" с ```true``` если сессии даже нет

## Название:

/register


### Отправляется форма с:

- name="login", строковое значение длиной от 5 до 20, только латинские буквы в обоих регистрах + подчеркивания
- name="password", строковое значение длиной от 8 до 30

### Ответ:

- "register\_state", значения "registered", "duplicate\_login" или "bad\_content" (следующие два)
- "wrong\_length_params", массив с ["login", "password"] - смотря что имеет неправильную длину
- "wrong\_login\_character" с ```true```

## Название:

/shot


### Отправляется форма с:

- name="x", Double
- name="y", Double
- name="R", Double

### Ответ:

- "hit", ```true``` или ```false```
- "datetime", в формате ISO 8601 
- "processing\_time\_nano", в наносекундах
- "wrong\_type", массив с ["x", "y", "R"] - смотря что имеет неверный тип (не Double) 

## Название:

/shots


### Отправляется:

Пусто

### Ответ:

Массив по ключу "shots" со словарями с ключами:
- "x", Double
- "y", Double
- "R", Double
- "hit", ```true``` или ```false```
- "datetime", в формате ISO 8601 
- "processing\_time", в наносекундах
