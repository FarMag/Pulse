from flask import Flask
from backend1.app.nutrition.routes import (refresh_token_bp, check_token_bp, get_nutrition_data_bp, add_product_user_bp)

app = Flask(__name__)

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.register_blueprint(get_nutrition_data_bp, url_prefix='/api')
app.register_blueprint(add_product_user_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host=db_config.host, port=8004, debug=True)