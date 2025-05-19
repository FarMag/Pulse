import os

from flask import Flask
from backend1.app.recipes.routes import (refresh_token_bp, check_token_bp, show_product_data_bp, show_product_data_goal_bp)

app = Flask(__name__)
# app = Flask(__name__, static_url_path='/static', static_folder=os.path.join(os.getcwd(), 'food_images'))

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.register_blueprint(show_product_data_bp, url_prefix='/api')
app.register_blueprint(show_product_data_goal_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host=db_config.host, port=8005, debug=True)