from flask import Blueprint, request, jsonify
from sqlalchemy import null

from db.DataBase import DataBase
from token_manager import send_tokens  # Импортируем функцию send_tokens

# Создаем Blueprint
login_bp = Blueprint('login', __name__)

@login_bp.route('/login', methods=['POST'])
def login():
    db = DataBase()
    if request.form['email'] and request.form['password_hash']:
        if db.db_connect():
            result = db.log_in(request.form['email'], request.form['password_hash'])
            if result:
                if result.height is not None:
                    return send_tokens(result.id, "success")  # Генерируем и отправляем токены
                else:
                    return send_tokens(result.id, "incomplete data")
            else:
                return {"answer": "Email or Password wrong"}, 401
        else:
            return {"answer": "Error: Database connection"}, 500
    else:
        return {"answer": "All fields are required"}, 400


