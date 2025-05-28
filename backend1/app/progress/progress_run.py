from flask import Flask
from backend1.app.progress.routes import (refresh_token_bp, check_token_bp, get_user_weight_bp, reset_weight_bp,
                                          update_user_current_weight_bp, get_user_water_bp, add_user_water_bp,
                                          add_today_progress_bp)

app = Flask(__name__)

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.register_blueprint(get_user_weight_bp, url_prefix='/api')
app.register_blueprint(reset_weight_bp, url_prefix='/api')
app.register_blueprint(update_user_current_weight_bp, url_prefix='/api')
app.register_blueprint(get_user_water_bp, url_prefix='/api')
app.register_blueprint(add_user_water_bp, url_prefix='/api')
app.register_blueprint(add_today_progress_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host=db_config.host, port=8003, debug=True)