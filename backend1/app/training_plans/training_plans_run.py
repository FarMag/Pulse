import os

from flask import Flask
from backend1.app.training_plans.routes import (refresh_token_bp, check_token_bp, get_user_personal_training_plan_bp, get_all_training_plan_bp, get_name_training_plan_bp)

app = Flask(__name__)
# app = Flask(__name__, static_url_path='/static', static_folder=os.path.join(os.getcwd(), 'food_images'))

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.register_blueprint(get_user_personal_training_plan_bp, url_prefix='/api')
app.register_blueprint(get_all_training_plan_bp, url_prefix='/api')
app.register_blueprint(get_name_training_plan_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host=db_config.host, port=8006, debug=True)