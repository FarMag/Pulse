from flask import Blueprint, request, jsonify
from datetime import datetime, timedelta
import jwt
from sqlalchemy.exc import SQLAlchemyError
from backend1.app.progress.models import ProgressLog
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.db.DataBase import DataBase

refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
user_progress_bp = Blueprint('user_progress', __name__)
reset_weight_bp = Blueprint('reset_weight', __name__)
update_user_current_weight_bp = Blueprint('update_user_current_weight', __name__)
get_user_water_bp = Blueprint('get_user_water', __name__)
add_user_water_bp = Blueprint('add_user_water', __name__)
add_today_progress_bp = Blueprint('add_today_progress', __name__)



SECRET_KEY = secretKey
REFRESH_SECRET_KEY = refreshSecretKey
ACCESS_TOKEN_EXPIRY = accessTokenExpiry
REFRESH_TOKEN_EXPIRY = refreshTokenExpiry

def get_user_id(access_token):
    try:
        payload = jwt.decode(access_token, SECRET_KEY, algorithms=['HS256'])
        return payload['sub']
    except jwt.ExpiredSignatureError:
        raise ValueError("Token has expired")
    except jwt.InvalidTokenError:
        raise ValueError("Invalid token")

def progress_log_to_dict(progress_log):
    return {
        "id": progress_log.id,
        "user_id": progress_log.user_id,
        "date": progress_log.date,
        "weight": progress_log.weight,
        "body_fat_percentage": progress_log.body_fat_percentage,
        "steps": progress_log.steps,
        "calories_burned": progress_log.calories_burned,
        "water": progress_log.water
        }

def generate_token(user_id):
    token = jwt.encode({
        'sub': user_id,
        'exp': datetime.utcnow() + timedelta(seconds=ACCESS_TOKEN_EXPIRY)
    }, SECRET_KEY, algorithm='HS256')
    return token

def generate_refresh_token(user_id):
    refresh_token = jwt.encode({
        'sub': user_id,
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


@add_today_progress_bp.route('/addTodayProgress', methods=['POST'])
def add_today_progress():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                id = get_user_id(request.form.get('access_token'))
                today = datetime.now().date()

                # Проверяем, существует ли уже запись с сегодняшней датой
                existing_progress = db.session.query(ProgressLog).filter_by(user_id=id, date=today).first()

                if existing_progress is not None:
                    return {'message': 'Progress for today already exists'}, 200  # Если запись уже существует, то ничего не надо делать

                # Получаем последнее значение веса из базы данных
                last_progress = db.session.query(ProgressLog).filter_by(user_id=id).order_by(
                    ProgressLog.date.desc()).first()
                last_weight = last_progress.weight if last_progress else None

                # Создаем новую запись для сегодняшнего прогресса
                new_log = ProgressLog(user_id=id, date=today)
                new_log.previous_weight = last_weight  # Убедитесь, что поле previous_weight существует в модели ProgressLog

                # Если вес не передан в запрос, используем значение последнего веса
                new_log.weight = last_weight if last_weight is not None else None

                # Здесь можно добавить другие поля, если необходимо
                new_log.body_fat_percentage = 0  # Или другое значение
                new_log.steps = 0  # Или другое значение
                new_log.calories_burned = 0  # Или другое значение
                new_log.water = 0  # Или другое значение

                db.session.add(new_log)
                db.session.commit()  # Применяем изменения
                return {'message': 'Success'}, 201  # Статус 201 при создании нового

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad request"}, 400


@user_progress_bp.route('/userProgress', methods=['POST'])
def user_progress():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                id = get_user_id(request.form.get('access_token'))
                all_progress_logs = db.session.query(ProgressLog).filter(ProgressLog.user_id == id).all()
                first_data_user = db.session.query(ProgressLog).filter_by(user_id=id).first()

                # Преобразуем объекты в словари
                all_progress_logs_dict = [progress_log_to_dict(log) for log in all_progress_logs]
                first_data_user_dict = progress_log_to_dict(first_data_user) if first_data_user else None

                return jsonify({
                    "all_progress_logs": all_progress_logs_dict,
                    "first_data_user": first_data_user_dict
                }), 200

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400


@reset_weight_bp.route('/resetWeight', methods=['POST'])
def user_progress():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                id = get_user_id(request.form.get('access_token'))

                # Получаем все записи пользователя
                progress_logs = db.session.query(ProgressLog).filter_by(user_id=id).all()

                if len(progress_logs) > 1:
                    # Удаляем все записи кроме последней
                    last_progress_log = progress_logs[-1]
                    db.session.query(ProgressLog).filter(ProgressLog.id != last_progress_log.id,
                                                         ProgressLog.user_id == id).delete(synchronize_session=False)

                db.session.commit()  # Применяем изменения

                return jsonify({"answer": "Success"}), 200

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400

@update_user_current_weight_bp.route('/updateUserCurrentWeight', methods=['POST'])
def update_user_current_weight():
    db = DataBase()
    if request.form.get('access_token') and request.form.get('weight'):
        if db.db_connect():
            try:
                id = get_user_id(request.form.get('access_token'))
                weight = request.form.get('weight')

                # Получаем сегодняшнюю дату
                today = datetime.now().date()
                # print(today)

                # Проверяем, существует ли запись на сегодня
                current_log = db.session.query(ProgressLog).filter_by(user_id=id, date=today).first()

                if current_log:
                    # Если запись существует, обновляем вес
                    current_log.weight = weight
                else:
                    # Если записи нет, создаем новую
                    new_log = ProgressLog(user_id=id, date=today, weight=weight)
                    # Убедитесь, что все обязательные поля заполнены
                    new_log.body_fat_percentage = None  # или другое значение, если нужно
                    new_log.steps = None  # или другое значение, если нужно
                    new_log.calories_burned = None  # или другое значение, если нужно
                    new_log.water = 0
                    db.session.add(new_log)

                db.session.commit()  # Применяем изменения

                return {"answer": "Success"}, 200

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400


@get_user_water_bp.route('/getUserWater', methods=['POST'])
def user_progress():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                id = get_user_id(request.form.get('access_token'))
                today = datetime.now().date()
                current_log = db.session.query(ProgressLog).filter_by(user_id=id, date=today).first()

                if current_log:
                    # Если запись существует, просто возвращаем количество воды
                    water = current_log.water  # Получаем количество воды из записи
                    return jsonify({'water': water}), 200
                else:
                    # Если записи нет, возвращаем 0
                    return jsonify({'water': '0'}), 200  # Статус 201 при создании нового

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400


@add_user_water_bp.route('/addUserWater', methods=['POST'])
def add_user_water():
    db = DataBase()
    if request.form.get('access_token') and request.form.get('current_water'):
        if db.db_connect():
            try:
                id = get_user_id(request.form.get('access_token'))
                today = datetime.now().date()
                current_water = float(request.form.get('current_water'))  # Получаем текущее количество воды как число
                current_log = db.session.query(ProgressLog).filter_by(user_id=id, date=today).first()

                if current_log:
                    # Если запись существует, обновляем количество воды
                    current_log.water = current_water  # Добавляем новую порцию воды
                    db.session.commit()  # Применяем изменения
                    return jsonify({'water': current_log.water}), 200  # Возвращаем обновленное количество воды
                else:
                    # Если записи нет, создаем новую запись с текущим количеством воды
                    new_log = ProgressLog(user_id=id, date=today, water=current_water)
                    # Убедитесь, что все обязательные поля заполнены
                    new_log.body_fat_percentage = None  # или другое значение, если нужно
                    new_log.steps = None  # или другое значение, если нужно
                    new_log.calories_burned = None  # или другое значение, если нужно
                    new_log.weight = None
                    db.session.add(new_log)
                    db.session.commit()  # Применяем изменения
                    return jsonify({'water': new_log.water}), 201  # Статус 201 при создании нового

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad request"}, 400


