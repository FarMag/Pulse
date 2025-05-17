from flask import Blueprint, request, jsonify
from datetime import datetime, timedelta, date
import jwt
from sqlalchemy.exc import SQLAlchemyError

from backend1.app.nutrition.models import NutritionLog
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.db.DataBase import DataBase

refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
get_nutrition_data_bp = Blueprint('get_nutrition_data', __name__)
add_product_user_bp = Blueprint('add_product_user', __name__)



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

def nutrition_log_to_dict(progress_log):
    return {
        "id": progress_log.id,
        "user_id": progress_log.user_id,
        "date": progress_log.date,
        "meal_type": progress_log.meal_type,
        "food_name": progress_log.food_name,
        "calories": progress_log.calories,
        "protein": progress_log.protein,
        "fat": progress_log.fat,
        "carbohydrates": progress_log.carbohydrates
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


# @get_nutrition_data_bp.route('/getNutritionData', methods=['POST'])
# def get_nutrition_data():
#     db = DataBase()
#     if request.form.get('access_token'):
#         if db.db_connect():
#             try:
#                 user_id = get_user_id(request.form.get('access_token'))
#                 today = date.today()  # Получаем сегодняшнюю дату
#                 nutrition = db.session.query(NutritionLog).filter_by(user_id=user_id, date=today).first()
#
#                 if nutrition:
#                     # Если данные найдены, возвращаем их
#                     response = {
#                         'calories': nutrition.calories,
#                         'protein': nutrition.protein,
#                         'fat': nutrition.fat,
#                         'carbohydrates': nutrition.carbohydrates,
#                         'water': nutrition.water,
#                         'food_name': nutrition.food_name,
#                         'meal_type': nutrition.meal_type
#                     }
#                 else:
#                     # Если данные не найдены, возвращаем значения по умолчанию
#                     response = {
#                         'calories': 0,
#                         'protein': 0,
#                         'fat': 0,
#                         'carbohydrates': 0,
#                         'water': 0,
#                         'food_name': "",
#                         'meal_type': ""
#                     }
#
#                 return jsonify(response)  # Возвращаем ответ в формате JSON
#
#             except Exception as e:
#                 return jsonify({'error': str(e)}), 500
#     return jsonify({'error': 'Access token not provided'}), 401



# @get_nutrition_data_bp.route('/getNutritionData', methods=['POST'])
# def get_nutrition_data():
#     db = DataBase()
#     if request.form.get('access_token'):
#         # Получаем user_id по access_token
#         user_id = get_user_id(request.form.get('access_token'))
#
#         if db.db_connect():
#             try:
#                 # Получаем сегодняшнюю дату
#                 today = datetime.now().date()
#
#                 # Запрашиваем данные о питании для пользователя и сегодняшней даты
#                 nutrition_logs = db.session.query(NutritionLog).filter_by(user_id=user_id, date=today).all()
#
#                 print("1")
#
#                 # Если записи не найдены, возвращаем значения по умолчанию
#                 if not nutrition_logs:
#                     print("2")
#                     return jsonify({
#                         "breakfast": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
#                         "lunch": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
#                         "dinner": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
#                         "snack": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0}
#                     })
#
#                 # Формируем ответ
#                 nutrition_data = {
#                     "breakfast": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
#                     "lunch": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
#                     "dinner": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
#                     "snack": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0}
#                 }
#
#                 # Обрабатываем найденные записи и заполняем соответствующие массивы
#                 for log in nutrition_logs:
#                     meal_type = log.meal_type
#                     nutrition_data[meal_type] = {
#                         "calories": log.calories,
#                         "protein": log.protein,
#                         "fat": log.fat,
#                         "carbohydrates": log.carbohydrates
#                     }
#
#                 print("success")
#                 return jsonify(nutrition_data), 200
#
#             except Exception as e:
#                 return jsonify({"error": str(e)}), 500
#
#     return jsonify({"error": "Invalid access token."}), 401


@get_nutrition_data_bp.route('/getNutritionData', methods=['POST'])
def get_nutrition_data():
    db = DataBase()
    if request.form.get('access_token'):
        # Получаем user_id по access_token
        user_id = get_user_id(request.form.get('access_token'))
        if db.db_connect():
            try:
                # Получаем сегодняшнюю дату
                today = datetime.now().date()

                # Запрашиваем данные о питании для пользователя и сегодняшней даты
                nutrition_logs = db.session.query(NutritionLog).filter_by(user_id=user_id, date=today).all()

                # Если записи не найдены, возвращаем значения по умолчанию
                if not nutrition_logs:
                    return jsonify({
                        "breakfast": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                        "lunch": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                        "dinner": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                        "snack": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                        "total_calories": 0,
                        "total_protein": 0,
                        "total_fat": 0,
                        "total_carbohydrates": 0,
                    })

                # Формируем ответ
                nutrition_data = {
                    "breakfast": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                    "lunch": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                    "dinner": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                    "snack": {"calories": 0, "protein": 0, "fat": 0, "carbohydrates": 0},
                    "total_calories": 0,
                    "total_protein": 0,         # Добавлено
                    "total_fat": 0,             # Добавлено
                    "total_carbohydrates": 0     # Добавлено
                }

                # Обрабатываем найденные записи и заполняем соответствующие данные
                for log in nutrition_logs:
                    meal_type = log.meal_type
                    nutrition_data[meal_type]['calories'] += log.calories
                    nutrition_data[meal_type]['protein'] += log.protein
                    nutrition_data[meal_type]['fat'] += log.fat
                    nutrition_data[meal_type]['carbohydrates'] += log.carbohydrates
                    nutrition_data['total_calories'] += log.calories
                    nutrition_data['total_protein'] += log.protein  # Подсчет общего белка
                    nutrition_data['total_fat'] += log.fat          # Подсчет общего жира
                    nutrition_data['total_carbohydrates'] += log.carbohydrates  # Подсчет общего углеводов

                return jsonify(nutrition_data), 200

            except Exception as e:
                return jsonify({"error": str(e)}), 500

    return jsonify({"error": "Invalid access token."}), 401


@add_product_user_bp.route('/addProductUser', methods=['POST'])
def add_product_user():
    db = DataBase()
    if request.form.get('access_token'):
        # Получаем user_id по access_token
        user_id = get_user_id(request.form.get('access_token'))
        # user_id = request.form.get('access_token')
        if db.db_connect():
            try:
                # Получаем данные из запроса
                # Получаем значения из формы
                recipe_id = request.form.get('product_id')
                meal_type = request.form.get('meal_type')
                food_name = request.form.get('product_name')
                calories = request.form.get('calories')
                protein = request.form.get('protein')
                fat = request.form.get('fats')
                carbohydrates = request.form.get('carbs')
                grams = request.form.get('grams')

                # Создаем новую запись в базе данных
                new_log = NutritionLog(
                    user_id=user_id,
                    recipe_id=recipe_id,
                    # date=datetime.now().isoformat(),  # Записываем текущее время
                    date=date.today(),
                    meal_type=meal_type,
                    food_name=food_name,
                    calories=calories,
                    protein=protein,
                    fat=fat,
                    carbohydrates=carbohydrates,
                    grams=grams
                )
                db.session.add(new_log)
                db.session.commit()
                return {"message": "Success"}, 201

            except Exception as e:
                db.session.rollback()  # Откат транзакции в случае ошибки
                return jsonify({"error": str(e)}), 500
    else:
        return jsonify({"error": "Invalid access token."}), 401

