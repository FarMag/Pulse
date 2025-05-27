from flask import Blueprint, request, jsonify
from datetime import datetime, timedelta
import jwt
from sqlalchemy.exc import SQLAlchemyError
from backend1.app.workouts.models import Workouts
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.db.DataBase import DataBase

refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
get_user_date_training_bp = Blueprint('get_user_date_training', __name__)
get_user_selected_date_training_bp = Blueprint('get_user_selected_date_training', __name__)
update_user_workout_note_bp = Blueprint('update_user_workout_note', __name__)


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

# def workouts_to_dict(workouts):
#     return {
#         "id": workouts.id,
#         "user_id": workouts.user_id,
#         "plan_id": workouts.user_id,
#         "date": workouts.date,
#         "duration_time": workouts.duration_time
#         }

def workouts_to_dict(workout):
    return {
        "id": workout.id,
        "user_id": workout.user_id,
        "plan_id": workout.plan_id,
        "date": workout.date.strftime("%Y-%m-%d"),  # Приведение даты к строковому формату
        "duration_time": workout.duration_time,
        "notes": workout.notes
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


@get_user_date_training_bp.route('/getUserDateTraining', methods=['POST'])
def get_user_date_training():
    db = DataBase()
    if request.form.get('access_token'):
        if db.db_connect():
            try:
                user_id = get_user_id(request.form.get('access_token'))
                # user_id = request.form.get('access_token')

                # Получение массива тренировок для конкретного пользователя
                workouts = db.session.query(Workouts).filter(Workouts.user_id == user_id).all()

                if workouts:
                    # Преобразование массива тренировок в список словарей
                    workouts_list = [workouts_to_dict(workout) for workout in workouts]
                    return {"workouts": workouts_list}, 200
                else:
                    return {"answer": "No workouts found for this user"}, 404

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400

@get_user_selected_date_training_bp.route('/getUserSelectedDateTraining', methods=['POST'])
def get_user_selected_date_training():
    db = DataBase()
    if request.form.get('access_token') and request.form.get('date'):
        if db.db_connect():
            try:
                user_id = get_user_id(request.form.get('access_token'))
                workout_date = request.form.get('date')  # Получаем дату из запроса

                # Получение первой тренировки для конкретного пользователя по дате
                workout = db.session.query(Workouts).filter(
                    Workouts.user_id == user_id,
                    Workouts.date == workout_date  # Предполагается, что в модели есть поле `date`
                ).first()

                if workout:
                    # Преобразование тренировки в словарь
                    workout_dict = workouts_to_dict(workout)
                    return {"workout": workout_dict}, 200
                else:
                    return {"answer": "No workouts found for this user on the specified date"}, 404

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400

@update_user_workout_note_bp.route('/updateUserWorkoutNote', methods=['POST'])
def update_user_workout_note():
    db = DataBase()
    if request.form.get('access_token') and request.form.get('date'):
        if db.db_connect():
            try:
                user_id = get_user_id(request.form.get('access_token'))
                # user_id = request.form.get('access_token')
                workout_date = request.form.get('date')  # Получаем дату из запроса
                new_note = request.form.get('notes')  # Получаем новую заметку из запроса

                # Получение тренировки для конкретного пользователя по дате
                workout = db.session.query(Workouts).filter(
                    Workouts.user_id == user_id,
                    Workouts.date == workout_date  # Предполагается, что в модели есть поле `date`
                ).first()

                if workout:
                    # Обновление заметки
                    # workout.notes = new_note  # Предполагается, что в модели есть поле `note`
                    workout.notes = new_note if new_note else None
                    db.session.commit()  # Сохранение изменений в базе данных
                    return {"answer": "Workout note updated successfully"}, 200
                else:
                    return {"answer": "No workouts found for this user on the specified date"}, 404

            except SQLAlchemyError as e:
                print(f"Error: {e}")
                db.session.rollback()  # Откатить изменения в случае ошибки
                return {"answer": "Database error"}, 500
            except ValueError:
                return {"answer": "Invalid input data"}, 400
    return {"answer": "Bad error"}, 400



