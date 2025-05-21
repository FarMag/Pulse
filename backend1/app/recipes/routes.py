import base64
import os

from flask import Blueprint, request, jsonify, json, current_app
from datetime import datetime, timedelta, date
import jwt

from sqlalchemy.exc import SQLAlchemyError

from backend1.app.recipes.models import Recipes
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.db.DataBase import DataBase

refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
show_product_data_bp = Blueprint('show_product_data', __name__)
show_product_data_goal_bp = Blueprint('show_product_data_goal', __name__)


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

def recipes_log_to_dict(recipes_log):
    return {
        "id": recipes_log.id,
        "name": recipes_log.name,
        "description": recipes_log.description,
        "calories": recipes_log.calories,
        "protein": recipes_log.protein,
        "fat": recipes_log.fat,
        "carbohydrates": recipes_log.carbohydrates,
        "ingredient_name": recipes_log.ingredient_name
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


# @show_product_data_bp.route('/showProductData', methods=['POST'])
# def show_product_data():
#     db = DataBase()
#     if db.db_connect():
#         try:
#             # Получаем имя продукта, по которому будем искать
#             search_name_product = request.form.get('search_name_product', "")  # По умолчанию - пустая строка
#
#             # Выполняем запрос к базе данных
#             products = db.session.query(Recipes).filter(Recipes.name.like(f"%{search_name_product}%")).all()
#
#             # Преобразуем результаты в словарь
#             product_list = [recipes_log_to_dict(product) for product in products]
#
#             # Указываем правильную кодировку для JSON
#             response = jsonify(product_list)
#             response.headers.add('Content-Type', 'application/json; charset=utf-8')
#             return response, 200  # Возвращаем список продуктов в формате JSON
#         except Exception as e:
#             # Обрабатываем исключения и возвращаем сообщение об ошибке
#             return jsonify({"error": str(e)}), 500
#     else:
#         return jsonify({"error": "Database connection failed"}), 500

@show_product_data_bp.route('/showProductData', methods=['POST'])
def show_product_data():
    db = DataBase()
    if db.db_connect():
        try:
            search_name_product = request.form.get('search_name_product', "")

            products = db.session.query(Recipes).filter(Recipes.name.like(f"%{search_name_product}%")).limit(10).all()
            product_list = [recipes_log_to_dict(product) for product in products]

            response = jsonify(product_list)
            # Декодируем JSON в читаемом виде
            response.data = json.dumps(product_list, ensure_ascii=False)
            response.headers.add('Content-Type', 'application/json; charset=utf-8')
            return response, 200
        except Exception as e:
            return jsonify({"error": str(e)}), 500
    else:
        return jsonify({"error": "Database connection failed"}), 500




@show_product_data_goal_bp.route('/showProductDataGoal', methods=['POST'])
def show_product_data_goal():
    db = DataBase()
    if db.db_connect():
        try:
            # search_name_product = request.form.get('search_name_product', "")
            user_goal = request.form.get('goal', "keeping")  # например: mass/keeping/losing/longevity
            # user_goal = request.form.get('goal')  # например: mass/keeping/losing/longevity

            products = db.session.query(Recipes) \
                .filter(
                    # Recipes.name.like(f"%{search_name_product}%"),
                    Recipes.ingredient_name.isnot(None),
                    Recipes.description == user_goal
                ) \
                .limit(20).all()

            product_list = [recipes_log_to_dict(product) for product in products]

            response = jsonify(product_list)
            response.data = json.dumps(product_list, ensure_ascii=False)
            response.headers.add('Content-Type', 'application/json; charset=utf-8')
            return response, 200
        except Exception as e:
            return jsonify({"error": str(e)}), 500
    else:
        return jsonify({"error": "Database connection failed"}), 500



# @show_product_data_goal_bp.route('/showProductDataGoal', methods=['POST'])
# def show_product_data_goal():
#     db = DataBase()
#     if db.db_connect():
#         try:
#             user_goal = request.form.get('goal', "mass")  # например: mass/keeping/losing/longevity
#
#             # Получаем продукты на основе цели пользователя
#             products = db.session.query(Recipes) \
#                 .filter(
#                 Recipes.ingredient_name.isnot(None),
#                 Recipes.description == user_goal
#             ) \
#                 .limit(5).all()
#
#             product_list = []
#             for product in products:
#                 product_data = recipes_log_to_dict(product)
#
#                 # Путь к изображению
#                 image_path = os.path.join('food_images', f"{product.id}.jpg")
#                 if os.path.exists(image_path):
#                     print(1)
#                     # product_data['image'] = f".../{product.id}.jpg"  # Укажите правильный путь к изображению
#                     product_data['image'] = f"/food_images/{product.id}.jpg"
#                     print("Фото ", product_data['image'])
#
#                 product_list.append(product_data)
#
#             print(2)
#             response = jsonify(product_list)
#             response.data = json.dumps(product_list, ensure_ascii=False)
#             response.headers.add('Content-Type', 'application/json; charset=utf-8')
#             return response, 200
#
#         except Exception as e:
#             return jsonify({"error": str(e)}), 500
#     else:
#         return jsonify({"error": "Database connection failed"}), 500



# @show_product_data_goal_bp.route('/showProductDataGoal', methods=['POST'])
# def show_product_data_goal():
#     db = DataBase()
#     if db.db_connect():
#         try:
#             user_goal = request.form.get('goal', "mass")
#
#             products = db.session.query(Recipes) \
#                 .filter(
#                     Recipes.ingredient_name.isnot(None),
#                     Recipes.description == user_goal
#                 ) \
#                 .limit(1).all()
#
#             product_list = []
#             for product in products:
#                 product_data = recipes_log_to_dict(product)
#
#                 # Абсолютный путь к файлу
#                 image_filename = f"{product.id}.jpg"
#                 image_path = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'food_images')
#
#                 # Проверка, существует ли файл
#                 if os.path.exists(image_path):
#                     # Относительный путь, доступный клиенту
#                     # product_data['image'] = f"/static/food_images/{image_filename}
#                     product_data['image'] = os.path.join(image_path, image_filename)
#
#                 product_list.append(product_data)
#
#             response = jsonify(product_list)
#             response.data = json.dumps(product_list, ensure_ascii=False)
#             response.headers.add('Content-Type', 'application/json; charset=utf-8')
#             return response, 200
#
#         except Exception as e:
#             return jsonify({"error": str(e)}), 500
#     else:
#         return jsonify({"error": "Database connection failed"}), 500








# import base64
# import os
# import json
# from flask import jsonify, request, Blueprint, Response
#
# @show_product_data_goal_bp.route('/showProductDataGoal', methods=['POST'])
# def show_product_data_goal():
#     db = DataBase()
#     if db.db_connect():
#         try:
#             # Получаем цель пользователя, поддерживая русский язык
#             user_goal = request.form.get('goal', "масса")
#
#             # Получаем список продуктов, поддерживая русский язык
#             products = db.session.query(Recipes) \
#                 .filter(
#                     Recipes.ingredient_name.isnot(None),
#                     Recipes.description == user_goal
#                 ) \
#                 .limit(20).all()
#
#             product_list = []
#             for product in products:
#                 product_data = recipes_log_to_dict(product)
#
#                 image_filename = f"{product.id}.jpg"
#                 image_path = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'food_images', image_filename)
#
#                 if os.path.exists(image_path):
#                     with open(image_path, "rb") as image_file:
#                         encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
#                         product_data['image_base64'] = encoded_string
#                 else:
#                     product_data['image_base64'] = None
#
#                 product_list.append(product_data)
#
#             # Преобразование в JSON с поддержкой русских символов
#             json_data = json.dumps(product_list, ensure_ascii=False)
#
#             # Создаём корректный HTTP-ответ с нужной кодировкой
#             return Response(json_data, content_type='application/json; charset=utf-8', status=200)
#
#         except Exception as e:
#             # Возвращаем ошибку с русским текстом
#             return jsonify({"error": str(e)}), 500
#     else:
#         # Возвращаем ошибку с русским текстом
#         return jsonify({"error": "Ошибка подключения к базе данных"}), 500




