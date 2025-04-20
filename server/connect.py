from flask import Flask, request, jsonify, Response
from flask_cors import CORS
import pymysql.cursors
import bcrypt
from datetime import datetime
import json

app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False  # Важно для кириллицы!
CORS(app)

# Конфигурация БД с явным указанием кодировки
DB_CONFIG = {
    'host': 'localhost',
    'user': 'ваш_пользователь',
    'password': 'ваш_пароль',
    'db': 'ваша_база',
    'charset': 'utf8mb4',  # Обязательно!
    'cursorclass': pymysql.cursors.DictCursor
}

def get_db_connection():
    return pymysql.connect(**DB_CONFIG)

@app.route('/register', methods=['POST'])
def register():
    try:
        request.get_data(cache=True, as_text=True, parse_form_data=False)
        data = request.get_json(force=True)
        if not all(field in data for field in required_fields):
            return jsonify({
                'status': 'error',
                'message': 'Отсутствуют обязательные поля'
            }), 400

        username = data['username']
        email = data['email']
        password = data['password'].encode('utf-8')
        birth_date = data['birth_date']
        gender = data['gender']

        # Валидация длины имени пользователя
        if len(username) < 3:
            return jsonify({
                'status': 'error',
                'message': 'Имя пользователя должно содержать минимум 3 символа'
            }), 400

        # Валидация формата даты
        try:
            parsed_date = datetime.strptime(birth_date, "%d.%m.%Y")
            sql_date = parsed_date.strftime("%Y-%m-%d")
        except ValueError:
            return jsonify({
                'status': 'error',
                'message': 'Неверный формат даты (требуется ДД.ММ.ГГГГ)'
            }), 400

        # Хеширование пароля
        hashed_password = bcrypt.hashpw(password, bcrypt.gensalt())

        connection = get_db_connection()
        try:
            with connection.cursor() as cursor:
                # SQL-запрос с учетом структуры вашей таблицы
                sql = """INSERT INTO users
                        (username, email, password_hash, birth_date, gender)
                        VALUES (%s, %s, %s, %s, %s)"""
                cursor.execute(sql, (
                    username,
                    email,
                    hashed_password.decode('utf-8'),
                    sql_date,
                    gender
                ))
                connection.commit()

            return jsonify({
                'status': 'success',
                'message': 'Пользователь успешно зарегистрирован'
            }), 201

        except pymysql.err.IntegrityError as e:
            if 'email' in str(e):
                return jsonify({
                    'status': 'error',
                    'message': 'Email уже используется'
                }), 409
            elif 'username' in str(e):
                return jsonify({
                    'status': 'error',
                    'message': 'Имя пользователя уже занято'
                }), 409
            else:
                return jsonify({
                    'status': 'error',
                    'message': 'Ошибка базы данных'
                }), 500

        finally:
            connection.close()

        response.headers['Content-Type'] = 'application/json; charset=utf-8'
        return response, 201

    except Exception as e:
        error_message = f"Ошибка: {str(e)}"
        # Логируем ошибку в консоли
        print(error_message)
        return Response(
            json.dumps({'status': 'error', 'message': error_message}, ensure_ascii=False),
            status=500,
            mimetype='application/json; charset=utf-8'
        )

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)