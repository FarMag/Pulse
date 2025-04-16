from flask import Blueprint, request, jsonify
from sqlalchemy import null

from db.DataBase import DataBase

# Создаем Blueprint
addFullInformationUser_bp = Blueprint('addFullInformationUser', __name__)

@addFullInformationUser_bp.route('/addFullInformationUser', methods=['POST'])
def addFullInformationUser():
    db = DataBase()
    if (request.form['id'] and request.form['phis_train'] and request.form['height']
            and request.form['weight'] and request.form['target_phis'] and request.form['target_weight']):
        if db.db_connect():
            result = db.add_full_information_user(request.form['id'], request.form['phis_train'],
                                                  request.form['height'], request.form['weight'],
                                                  request.form['target_phis'], request.form['target_weight'])
            if result:
                return {"answer": "Success"}, 200
            else:
                return {"answer": "Error"}, 400
    else:
        return {"answer": "Bad error"}, 400