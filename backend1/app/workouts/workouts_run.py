from flask import Flask
from backend1.app.workouts.routes import (refresh_token_bp, check_token_bp, get_user_date_training_bp,
                                          get_user_selected_date_training_bp, update_user_workout_note_bp,
                                          add_user_today_training_bp, get_user_today_training_bp)

app = Flask(__name__)

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

app.register_blueprint(get_user_date_training_bp, url_prefix='/api')
app.register_blueprint(get_user_selected_date_training_bp, url_prefix='/api')
app.register_blueprint(update_user_workout_note_bp, url_prefix='/api')
app.register_blueprint(add_user_today_training_bp, url_prefix='/api')
app.register_blueprint(get_user_today_training_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host=db_config.host, port=8007, debug=True)