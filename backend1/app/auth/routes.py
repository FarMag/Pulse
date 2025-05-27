from flask import Blueprint, request, jsonify
from datetime import datetime, timedelta
import bcrypt
import jwt
import hashlib
from sqlalchemy.exc import SQLAlchemyError
from backend1.app.auth.models import User
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.db.DataBase import DataBase
from cryptography.fernet import Fernet
from backend1.SecretData import emailEncryptionKey

register_bp = Blueprint('register', __name__)
login_bp = Blueprint('login', __name__)
get_user_data_bp = Blueprint('get_user_data', __name__)
get_user_data_and_age_bp = Blueprint('get_user_data_and_age', __name__)
refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
add_full_information_user_bp = Blueprint('add_full_information_user', __name__)
update_user_information_bp = Blueprint('update_user_information', __name__)
update_user_xp_bp = Blueprint('update_user_xp', __name__)
check_user_authorization_bp = Blueprint('check_user_authorization', __name__)
add_user_note_bp = Blueprint('add_user_note', __name__)

SECRET_KEY = secretKey
REFRESH_SECRET_KEY = refreshSecretKey
ACCESS_TOKEN_EXPIRY = accessTokenExpiry
REFRESH_TOKEN_EXPIRY = refreshTokenExpiry

fernet = Fernet(emailEncryptionKey.encode())

def encrypt_email(email: str) -> str:
    return fernet.encrypt(email.encode()).decode()

def decrypt_email(encrypted_email: str) -> str:
    return fernet.decrypt(encrypted_email.encode()).decode()

def hash_email(email: str) -> str:
    return hashlib.sha256(email.encode()).hexdigest()

def get_user_id(access_token):
    try:
        # print(f"Access Token: {access_token}")  # Для отладки
        # payload = jwt.decode(access_token, REFRESH_SECRET_KEY, algorithms=['HS256'])
        # print('test1')
        payload = jwt.decode(access_token, SECRET_KEY, algorithms=['HS256'])
        # if not isinstance(payload['sub'], str):
        #     raise ValueError("Subject is not a string")
        # payload = jwt.decode(access_token, SECRET_KEY, algorithm='HS256')
        # print('test2')
        # print(f'payload sub = {payload["sub"]}')
        return payload['sub']
    except jwt.ExpiredSignatureError:
        raise ValueError("Token has expired")
    except jwt.InvalidTokenError:
        raise ValueError("Invalid token")

def generate_token(user_id):
    token = jwt.encode({
        'sub': user_id,
        # 'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds=ACCESS_TOKEN_EXPIRY)
        'exp': datetime.utcnow() + timedelta(seconds=ACCESS_TOKEN_EXPIRY)
    }, SECRET_KEY, algorithm='HS256')
    return token

def generate_refresh_token(user_id):
    refresh_token = jwt.encode({
        'sub': user_id,
        # 'exp': datetime.datetime.utcnow() + datetime.timedelta(seconds=ACCESS_TOKEN_EXPIRY)
        'exp': datetime.utcnow() + timedelta(seconds=ACCESS_TOKEN_EXPIRY)
    }, REFRESH_SECRET_KEY, algorithm='HS256')
    return refresh_token

def send_tokens(user_id, message):
    if not isinstance(user_id, str):
        user_id = str(user_id)

    access_token = generate_token(user_id)
    refresh_token = generate_refresh_token(user_id)
    return jsonify({
        "access_token": access_token,
        "refresh_token": refresh_token,
        "message": message
    }), 200

# @login_bp.route('/login', methods=['POST'])
# def login():
#     db = DataBase()
#     if 'email' in request.form and 'password_hash' in request.form:
#         if db.db_connect():
#             try:
#                 user = db.session.query(User).filter_by(email=request.form['email']).first()
#                 if user:  # Проверяем, найден ли пользователь
#                     print("user password_hash = ", user.password_hash)
#                     dbpassword = user.password_hash.encode('utf-8')  # Кодируем хэшированный пароль
#                     print("user password_hash = ", user.password_hash)
#                     if bcrypt.checkpw(request.form['password_hash'].encode('utf-8'), dbpassword):  # Проверяем пароль
#                         if user.height is not None:
#                             return send_tokens(user.id, "success")  # Генерируем и отправляем токены
#                         else:
#                             return send_tokens(user.id, "incomplete data")
#                 else:
#                     return {"answer": "Email or Password wrong"}, 401
#             except Exception as e:
#                 print(f"Error: {e}")
#                 return {"answer": "An error occurred during authentication"}, 500
#         else:
#             return {"answer": "Error: Database connection"}, 500
#     else:
#         return {"answer": "All fields are required"}, 400














# @login_bp.route('/login', methods=['POST'])
# def login():
#     db = DataBase()
#     if 'email' in request.form and 'password_hash' in request.form:
#         print(request.form['password_hash'])
#         if db.db_connect():
#             try:
#                 user = db.session.query(User).filter_by(email=request.form['email']).first()
#
#                 # if user:  # Проверяем, найден ли пользователь
#                 #     print("1")
#                 #     # dbpassword = user.password_hash.encode('utf-8')  # Кодируем хэшированный пароль
#                 #     dbpassword = user.password_hash
#                 #     print("2")
#                 #     # Важно: здесь убираем лишнюю кодировку
#                 #     password_attempt = request.form['password_hash']  # Получаем пароль из формы
#                 #     print("3")
#                 #     print("пароль из базы данных", dbpassword)
#                 #     print("пароль из приложения", password_attempt.encode('utf-8'))
#                 #     if bcrypt.checkpw(password_attempt.encode('utf-8'), dbpassword):  # Проверяем пароль
#                 #         if user.height is not None:
#                 #             return send_tokens(user.id, "success")  # Генерируем и отправляем токены
#                 #         else:
#                 #             return send_tokens(user.id, "incomplete data")
#                 # else:
#                 #     return {"answer": "Email or Password wrong"}, 401
#
#                 if user:  # Проверяем, найден ли пользователь
#                     dbpassword = user.password_hash
#                     password_attempt = request.form['password_hash']  # Получаем пароль из формы
#
#                     print("пароль из базы данных", dbpassword)
#                     print("пароль из приложения", password_attempt.encode('utf-8'))
#
#                     if bcrypt.checkpw(password_attempt.encode('utf-8'), dbpassword):  # Проверяем пароль
#                         if user.height is not None:
#                             return send_tokens(user.id, "success")  # Генерируем и отправляем токены
#                         else:
#                             return send_tokens(user.id, "incomplete data")
#                     else:
#                         return {"answer": "Email or Password wrong"}, 401
#
#             except Exception as e:
#                 print(f"Error: {e}")
#                 return {"answer": "An error occurred during authentication"}, 500
#         else:
#             return {"answer": "Error: Database connection"}, 500
#     else:
#         return {"answer": "All fields are required"}, 400










# @login_bp.route('/login', methods=['POST'])
# def login():
#     db = DataBase()
#     if 'email' in request.form and 'password_hash' in request.form:
#         password_attempt = request.form['password_hash'].encode('utf-8')
#         if db.db_connect():
#             try:
#                 user = db.session.query(User).filter_by(email=request.form['email']).first()
#
#                 if user:  # Проверяем, найден ли пользователь
#
#                     dbpassword = user.password_hash.encode('utf-8')
#                     print("пароль из базы данных", dbpassword)
#                     print("пароль из приложения", password_attempt)
#                     if bcrypt.checkpw(password_attempt, dbpassword):  # Проверяем пароль
#                         if user.height is not None:
#                             return send_tokens(user.id, "success")  # Генерируем и отправляем токены
#                         else:
#                             return send_tokens(user.id, "incomplete data")
#                     else:
#                         return {"answer": "Email or Password wrong"}, 401
#
#             except Exception as e:
#                 print(f"Error: {e}")
#                 return {"answer": "An error occurred during authentication"}, 500
#         else:
#             return {"answer": "Error: Database connection"}, 500
#     else:
#         return {"answer": "All fields are required"}, 400





# @login_bp.route('/login', methods=['POST'])
# def login():
#     db = DataBase()
#
#     email = request.form.get('email')
#     password = request.form.get('password_hash')
#
#     if all([email, password]):
#         if db.db_connect():
#             try:
#                 existing_user = db.session.query(User).filter_by(email=email).first()
#                 if not existing_user:
#                     return jsonify({"message": "User not found"}), 404  # Пользователь не найден
#
#                 print("password hash", existing_user.password_hash)
#                 hashed_password = existing_user.password_hash.encode('utf-8')
#                 if bcrypt.checkpw(password.encode('utf-8'), hashed_password):
#                     return send_tokens(existing_user.id, "Success")
#                 else:
#                     return jsonify({"message": "Invalid password"}), 401  # Неправильный пароль
#             except SQLAlchemyError as e:
#                 db.session.rollback()  # Откат транзакции в случае ошибки
#                 return jsonify({"message": "An error occurred during login"}), 500
#         else:
#             return jsonify({"error": "Error: Database connection"}), 500
#     else:
#         return jsonify({"error": "All fields are required"}), 400

@register_bp.route('/register', methods=['POST'])
def register():
    db = DataBase()

    username = request.form.get('username')
    email = request.form.get('email')
    password = request.form.get('password_hash')
    birth_date_str = request.form.get('birth_date')
    gender = request.form.get('gender')

    if all([email, password, username, birth_date_str, gender]):
        if db.db_connect():
            try:
                email_hash = hash_email(email)
                encrypted_email = encrypt_email(email)

                # Проверка по хешу email (стабильный)
                existing_user = db.session.query(User).filter_by(email_hash=email_hash).first()
                if existing_user:
                    return jsonify({"message": "This email already exists"}), 409

                birth_date = datetime.strptime(birth_date_str, "%d.%m.%Y")
                hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

                new_user = User(
                    username=username,
                    email=encrypted_email,            # Зашифрованный email
                    email_hash=email_hash,            # Хэш email для поиска
                    password_hash=hashed_password.decode('utf-8'),
                    birth_date=birth_date.strftime("%Y-%m-%d"),
                    gender=gender,
                    xp=0
                )

                db.session.add(new_user)
                db.session.commit()
                return send_tokens(new_user.id, "Success")

            except SQLAlchemyError as e:
                db.session.rollback()
                return jsonify({"message": "An error occurred during registration"}), 500
        else:
            return jsonify({"error": "Error: Database connection"}), 500
    else:
        return jsonify({"error": "All fields are required"}), 400


@login_bp.route('/login', methods=['POST'])
def login():
    db = DataBase()
    if 'email' in request.form and 'password_hash' in request.form:
        if db.db_connect():
            try:
                email = request.form['email']
                password = request.form['password_hash']

                email_hash = hash_email(email)
                user = db.session.query(User).filter_by(email_hash=email_hash).first()

                if user:
                    db_password = user.password_hash.encode('utf-8')
                    if bcrypt.checkpw(password.encode('utf-8'), db_password):
                        if user.height is not None:
                            return send_tokens(user.id, "success")
                        else:
                            return send_tokens(user.id, "incomplete data")

                return jsonify({"answer": "Email or Password is incorrect"}), 401

            except Exception as e:
                print(f"Error: {e}")
                return jsonify({"answer": "An error occurred during authentication"}), 500
        else:
            return jsonify({"answer": "Error: Database connection"}), 500
    else:
        return jsonify({"answer": "All fields are required"}), 400

@add_full_information_user_bp.route('/addFullInformationUser', methods=['POST'])
def add_full_information_user():
    db = DataBase()
    # Проверяем, что все необходимые параметры присутствуют
    if (request.form['access_token'] and request.form['phis_train'] and request.form['height']
            and request.form['weight'] and request.form['target_phis'] and request.form['target_weight']):
        if db.db_connect():
            try:
                user_id = get_user_id(request.form.get('access_token'))
                phis_train = request.form['phis_train']
                height = int(request.form['height'])
                weight = int(request.form['weight'])
                target_phis = request.form['target_phis']
                target_weight = int(request.form['target_weight'])

                # Находим пользователя по id и обновляем его данные
                user = db.session.query(User).filter_by(id=user_id).first()
                if user:
                    user.phis_train = phis_train
                    user.height = height
                    user.weight = weight
                    user.target_phis = target_phis
                    user.target_weight = target_weight
                    db.session.commit()  # Сохраняем изменения
                    return {"answer": "Success"}, 200
                else:
                    return {"answer": "User not found"}, 404
            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400

@update_user_xp_bp.route('/updateUserXp', methods=['POST'])
def update_user_xp():
    db = DataBase()
    # Проверяем, что все необходимые параметры присутствуют
    if request.form['access_token'] and request.form['current_xp']:
        if db.db_connect():
            try:
                # Находим пользователя по id и обновляем его вес
                user_id = get_user_id(request.form.get('access_token'))
                user = db.session.query(User).filter_by(id=user_id).first()
                if user:
                    xp = request.form.get('current_xp')
                    user.xp = xp
                    db.session.commit()  # Сохраняем изменения
                    return {"answer": "Success"}, 200
                else:
                    return {"answer": "User not found"}, 404

            except SQLAlchemyError as e:
                print(f"Database error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400

@get_user_data_bp.route('/getUserData', methods=['POST'])
def get_full_information_user():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                user_id = get_user_id(request.form.get('access_token'))
                user = db.session.query(User).filter_by(id=user_id).first()
                if user:
                    # Дешифруем email перед отправкой
                    decrypted_email = decrypt_email(user.email)

                    user_data = {
                        'username': user.username,
                        'email': decrypted_email,
                        'birth_date': user.birth_date,
                        'height': user.height,
                        'gender': user.gender,
                        'target_weight': user.target_weight,
                        'phis_train': user.phis_train,
                        'target_phis': user.target_phis,
                        'weight': user.weight,
                        'xp': user.xp,
                        'notes': user.notes if user.notes is not None else ''  # Проверка на NULL
                    }

                    today = datetime.today().date()
                    age = today.year - user.birth_date.year - (
                            (today.month, today.day) < (user.birth_date.month, user.birth_date.day))

                    # Добавляем возраст в user_data
                    user_data['age'] = age

                    return jsonify(user_data), 200
                else:
                    return jsonify({'error': 'The user was not found'}), 404
            except SQLAlchemyError as e:
                return jsonify({'error': str(e)}), 500
    return jsonify({'error': 'ID not provided'}), 400



@add_user_note_bp.route('/addUserNote', methods=['POST'])
def add_user_note():
    db = DataBase()
    # Проверяем, что access_token присутствует
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                user_id = get_user_id(request.form.get('access_token'))
                user = db.session.query(User).filter_by(id=user_id).first()
                if user:
                    # Получаем notes
                    notes = request.form.get('notes')

                    # # Проверяем длину notes
                    # if notes and len(notes) > 1023:
                    #     return {"answer": "Error: Notes must not exceed 1023 characters"}, 400

                    # Устанавливаем значение в None, если notes пустое
                    user.notes = notes if notes else None
                    db.session.commit()  # Сохраняем изменения
                    return {"answer": "Success"}, 200
                else:
                    return {"answer": "User not found"}, 404

            except SQLAlchemyError as e:
                print(f"Database error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    else:
        return {"answer": "Access token is required"}, 400


# @get_user_data_and_age_bp.route('/getUserDataAndAge', methods=['POST'])
# def get_full_information_user():
#     db = DataBase()
#     if request.form.get('access_token'):
#         # get_user_id(request.form.get('access_token'))
#         if db.db_connect():
#             try:
#                 user_id = get_user_id(request.form.get('access_token'))
#                 user = db.session.query(User).filter_by(id=user_id).first()
#                 if user:
#                     user_data = {
#                         'username': user.username,
#                         'email': user.email,
#                         'birth_date': user.birth_date,
#                         'gender': user.gender,
#                         'height': user.height,
#                         'target_weight': user.target_weight,
#                         'phis_train': user.phis_train,
#                         'target_phis': user.target_phis,
#                         'weight': user.weight,
#                         'xp': user.xp,
#                         'notes': user.notes if user.notes is not None else ''  # Проверка на NULL
#                     }
#
#                     # Рассчитываем возраст
#                     # birth_date = datetime.strptime(user.birth_date, '%Y-%m-%d')
#                     # Рассчитываем возраст
#                     today = datetime.today().date()
#                     age = today.year - user.birth_date.year - (
#                                 (today.month, today.day) < (user.birth_date.month, user.birth_date.day))
#
#                     # Добавляем возраст в user_data
#                     user_data['age'] = age
#
#                     return jsonify(user_data), 200
#                 else:
#                     return jsonify({'error': 'The user was not found'}), 404
#             except SQLAlchemyError as e:
#                 return jsonify({'error': str(e)}), 500
#             # finally:
#             #     db.session.close()
#     return jsonify({'error': 'ID not provided'}), 400


# @update_user_information_bp.route('/updateUserInformation', methods=['POST'])
# def update_user_information():
#     db = DataBase()
#     if (request.form['id'] and request.form['username'] and request.form['height'] and request.form['birth_date']
#             and request.form['target_weight'] and request.form['phis_train'] and request.form['target_phis']):
#         if db.db_connect():
#             try:
#                 user_id = request.form['id']
#                 username = request.form['username']
#                 height = int(request.form['height'])
#                 birth_date_str = request.form.get('birth_date')
#                 # birth_date = datetime.strptime(birth_date_str, "%d.%m.%Y")
#                 birth_date = datetime.strptime(birth_date_str, "%Y.%m.%d")
#                 target_weight = int(request.form['target_weight'])
#                 phis_train = request.form['phis_train']
#                 target_phis = request.form['target_phis']
#
#
#
#                 # Находим пользователя по id и обновляем его данные
#                 user = db.session.query(User).filter_by(id=user_id).first()
#                 if user:
#                     user.username = username
#                     user.height = height
#                     user.birth_date = birth_date.strftime("%Y-%m-%d"),
#                     user.target_weight = target_weight
#                     user.phis_train = phis_train
#                     user.target_phis = target_phis
#
#                     db.session.commit()  # Сохраняем изменения
#                     return {"answer": "Success"}, 200
#                 else:
#                     return {"answer": "User not found"}, 404
#             except SQLAlchemyError as e:
#                 print(f"Error: {e}")
#                 db.session.rollback()  # Откатить изменения в случае ошибки
#                 return {"answer": "Database error"}, 500
#             except ValueError:
#                 return {"answer": "Invalid input data"}, 400
#             return {"answer": "Bad error"}, 400



@update_user_information_bp.route('/updateUserInformation', methods=['POST'])
def update_user_information():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            user_id = get_user_id(request.form.get('access_token'))
            if all(key in request.form for key in ['username', 'height', 'birth_date', 'target_weight', 'phis_train', 'target_phis']):
                # Полное обновление данных пользователя
                return update_full_user_information(db, user_id)
            elif 'weight' in request.form:
                # Обновление только веса пользователя
                return update_user_weight(db, user_id)
            else:
                return {"answer": "Invalid parameters"}, 400
        else:
            return {"answer": "Database connection failed"}, 500
    else:
        return {"answer": "ID is required"}, 400

def update_full_user_information(db, user_id):
    try:
        username = request.form['username']
        height = int(request.form['height'])
        birth_date_str = request.form.get('birth_date')
        birth_date = datetime.strptime(birth_date_str, "%d.%m.%Y")
        target_weight = int(request.form['target_weight'])
        phis_train = request.form['phis_train']
        target_phis = request.form['target_phis']

        # Находим пользователя по id и обновляем его данные
        user = db.session.query(User).filter_by(id=user_id).first()
        if user:
            user.username = username
            user.height = height
            user.birth_date = birth_date.strftime("%Y-%m-%d")
            user.target_weight = target_weight
            user.phis_train = phis_train
            user.target_phis = target_phis

            db.session.commit()  # Сохраняем изменения
            return {"answer": "Success"}, 200
        else:
            return {"answer": "User not found"}, 404
    except SQLAlchemyError as e:
        print(f"Database error: {e}")
        db.session.rollback()  # Откатить изменения в случае ошибки
        return {"answer": "Database error"}, 500
    except ValueError:
        return {"answer": "Invalid input data"}, 400

def update_user_weight(db, user_id):
    try:
        weight = int(request.form['weight'])

        # Находим пользователя по id и обновляем его вес
        user = db.session.query(User).filter_by(id=user_id).first()
        if user:
            user.weight = weight  # Предполагается, что есть атрибут weight в модели User
            db.session.commit()  # Сохраняем изменения
            return {"answer": "Success"}, 200
        else:
            return {"answer": "User not found"}, 404
    except SQLAlchemyError as e:
        print(f"Database error: {e}")
        db.session.rollback()  # Откатить изменения в случае ошибки
        return {"answer": "Database error"}, 500
    except ValueError:
        return {"answer": "Invalid input data"}, 400



@check_user_authorization_bp.route('/checkUserAuthorization', methods=['POST'])
def check_user_authorization():
    db = DataBase()
    access_token = request.form.get('access_token')
    if not access_token:
        return jsonify({"error": "Отсутствует access_token"}), 400

    if db.db_connect():
        try:
            user_id = get_user_id(access_token)
            user = db.session.query(User).filter_by(id=user_id).first()
            if user:
                return jsonify({"isValid": True, "user_id": user_id}), 200
            else:
                return jsonify({"isValid": False}), 401

        except ValueError as e:
            if str(e) == "Token has expired":
                return jsonify({"isValid": False}), 401
            return jsonify({"error": "Ошибка при обработке запроса"}), 500

        except Exception as e:
            print(f"Ошибка при проверке токена: {e}")
            return jsonify({"error": "Ошибка при обработке запроса"}), 500
    else:
        return jsonify({"error": "Ошибка подключения к базе данных"}), 500



@refresh_token_bp.route('/refresh', methods=['POST'])
def refresh():
    refresh_token = request.form.get('refresh_token')
    if not refresh_token:
        return jsonify({"error": "Refresh token is required"}), 400
    return refresh_access_token(refresh_token)

def refresh_access_token(refresh_token):
    try:
        payload = jwt.decode(refresh_token, REFRESH_SECRET_KEY, algorithms=['HS256'])
        user_id = payload['sub']
        new_access_token = generate_token(user_id)
        return jsonify({"access_token": new_access_token}), 200
    except jwt.ExpiredSignatureError:
        return jsonify({"error": "Refresh token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"error": "Invalid refresh token"}), 401

@check_token_bp.route('/check_token', methods=['POST'])
def check_token():
    access_token = request.form.get('access_token')
    if not access_token:
        return jsonify({"error": "Access token is required"}), 400

    try:
        jwt.decode(access_token, SECRET_KEY, algorithms=['HS256'])
        return jsonify({"valid": True}), 200
    except jwt.ExpiredSignatureError:
        return jsonify({"valid": False, "error": "Token expired"}), 401
    except jwt.InvalidTokenError:
        return jsonify({"valid": False, "error": "Invalid token"}), 401