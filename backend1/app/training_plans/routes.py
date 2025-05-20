import base64
import os

from flask import Blueprint, request, jsonify, json, current_app
from datetime import datetime, timedelta, date
import jwt

from sqlalchemy.exc import SQLAlchemyError

from backend1.app.recipes.models import Recipes
from backend1.SecretData import secretKey, refreshSecretKey, accessTokenExpiry, refreshTokenExpiry
from backend1.app.training_plans.models import Training_plans
from backend1.db.DataBase import DataBase

refresh_token_bp = Blueprint('refresh', __name__)
check_token_bp = Blueprint('check_token', __name__)
get_user_personal_training_plan_bp = Blueprint('get_user_personal_training_plan', __name__)
get_all_training_plan_bp = Blueprint('get_all_training_plan', __name__)


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

def training_plans_log_to_dict(training_plans_log):
    return {
        "id": training_plans_log.id,
        "name": training_plans_log.name,
        "description": training_plans_log.description,
        "goal": training_plans_log.goal,
        "exercises": training_plans_log.exercises
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

@get_all_training_plan_bp.route('/getAllTrainingPlan', methods=['POST'])
def get_user_training_plan():
    db = DataBase()
    if db.db_connect():
        try:
            training_plans = db.session.query(Training_plans).all()
            training_plan_list = [training_plans_log_to_dict(training_plan) for training_plan in training_plans]

            response = jsonify(training_plan_list)
            response.data = json.dumps(training_plan_list, ensure_ascii=False)
            response.headers.add('Content-Type', 'application/json; charset=utf-8')

            return response, 200
        except Exception as e:
            return jsonify({"error": str(e)}), 500
    else:
        return jsonify({"error": "Database connection failed"}), 500

@get_user_personal_training_plan_bp.route('/getUserPersonalTrainingPlan', methods=['POST'])
def get_user_training_plan():
    db = DataBase()
    if db.db_connect():
        try:
            search_training_plan = request.form.get('goal', "")

            training_plans = db.session.query(Training_plans).filter(
                Training_plans.goal.like(f"%{search_training_plan}%")).all()

            training_plan_list = [training_plans_log_to_dict(training_plan) for training_plan in training_plans]

            response = jsonify(training_plan_list)
            response.data = json.dumps(training_plan_list, ensure_ascii=False)
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
#             # search_name_product = request.form.get('search_name_product', "")
#             user_goal = request.form.get('goal', "mass")  # например: mass/keeping/losing/longevity
#
#             products = db.session.query(Recipes) \
#                 .filter(
#                     # Recipes.name.like(f"%{search_name_product}%"),
#                     Recipes.ingredient_name.isnot(None),
#                     Recipes.description == user_goal
#                 ) \
#                 .limit(20).all()
#
#             product_list = [recipes_log_to_dict(product) for product in products]
#
#             response = jsonify(product_list)
#             response.data = json.dumps(product_list, ensure_ascii=False)
#             response.headers.add('Content-Type', 'application/json; charset=utf-8')
#             return response, 200
#         except Exception as e:
#             return jsonify({"error": str(e)}), 500
#     else:
#         return jsonify({"error": "Database connection failed"}), 500


