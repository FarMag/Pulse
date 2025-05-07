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
        "calories_burned": progress_log.calories_burned
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

# @user_progress_bp.route('/userProgress', methods=['POST'])
# def user_progress():
#     db = DataBase()
#     if request.form['access_token']:
#         if db.db_connect():
#             try:
#                 id = get_user_id(request.form.get('access_token'))
#                 # progress_logs = db.session.query(ProgressLog).filter(user_id = id).all()
#                 all_progress_logs = db.session.query(ProgressLog).filter(ProgressLog.user_id == id).all()
#                 first_data_user = db.session.query(ProgressLog).filter_by(user_id = id).first()
#
#                 # print(id)
#                 # for log in progress_logs:
#                 #     print(log)
#
#                 return jsonify({
#                     "all_progress_logs": all_progress_logs,
#                     "first_data_user": first_data_user
#                 }), 200
#
#             except SQLAlchemyError as e:
#                 print(f"Error: {e}")
#                 db.session.rollback()  # Откатить изменения в случае ошибки
#                 return {"answer": "Database error"}, 500
#             except ValueError:
#                 return {"answer": "Invalid input data"}, 400
#     return {"answer": "Bad error"}, 400

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


# @reset_weight_bp.route('/resetWeight', methods=['POST'])
# def reset_weight():
#     db = DataBase()
#     if request.form.get('access_token'):
#         if db.db_connect():
#             try:
#                 print("test")
#                 id = get_user_id(request.form.get('access_token'))
#                 print("id = ", id)
#
#                 # Получаем все записи пользователя
#                 # progress_logs = db.session.query(ProgressLog).filter_by(user_id=id).all()
#                 progress_logs = db.session.query(ProgressLog).filter(ProgressLog.user_id == id).all()
#                 print("ахахахах - ", len(progress_logs))
#
#                 if len(progress_logs) > 1:
#                     # Оставляем только последнюю запись
#                     db.session.query(ProgressLog).filter_by(user_id=id).delete(synchronize_session=False)
#                     # Добавляем обратно только последнюю запись
#                     last_progress_log = progress_logs[-1]
#                     db.session.add(last_progress_log)
#
#                 db.session.commit()  # Применяем изменения
#
#                 return {"answer": "Success"}, 200
#
#             except SQLAlchemyError as e:
#                 print(f"Error: {e}")
#                 db.session.rollback()  # Откатить изменения в случае ошибки
#                 return {"answer": "Database error"}, 500
#             except ValueError:
#                 return {"answer": "Invalid input data"}, 400
#     return {"answer": "Bad error"}, 400

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
                print(today)

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

# @add_user_first_weight_bp.route('/addUserFirstWeight', methods=['POST'])


