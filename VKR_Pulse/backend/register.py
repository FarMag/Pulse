import uuid
from flask import Blueprint, request, jsonify
import jwt
import datetime
from db.DataBase import DataBase
from token_manager import send_tokens
from SecretData import secretKey

# Создаем Blueprint
register_bp = Blueprint('register', __name__)
SECRET_KEY = secretKey

@register_bp.route('/register', methods=['POST'])
def register():
    db = DataBase()

    # name = request.form.get('name')
    email = request.form.get('email')
    password = request.form.get('password_hash')

    if email and password:
        if db.db_connect():
            # user_id = str(uuid.uuid4())  # Генерация уникального идентификатора
            # result = db.register_user(name, email, password, user_id)
            # result = db.register_user(request.form['name'], request.form['email'], request.form['password_hash'], user_id)
            result = db.register_user(request.form['username'], request.form['email'], request.form['password_hash'], request.form['birth_date'], request.form['gender'])

            # if result is True:
            if result:
                # return {"answer": "Success"}
                # return send_tokens(result.id, "Success")
                user = db.log_in(email, password)
                if user:
                    return send_tokens(user.id, "Success")
                else:
                    return jsonify({"message": "Error"})
            else:
                # return {"error": "This email is already exists"}  # Возвращаем сообщение об ошибке, если email существует
                # return send_tokens(result.id, "This email is already exists")
                return jsonify({"message": "This email is already exists"})
        else:
            return {"error": "Error: Database connection"}
    else:
        return {"error": "All fields are required"}

