from flask import Blueprint, request, jsonify, json
from datetime import datetime, timedelta, date
import jwt
from sqlalchemy.exc import SQLAlchemyError

from backend1.app.recipes.models import Recipes
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.db.DataBase import DataBase

refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
show_product_data_bp = Blueprint('show_product_data', __name__)



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








