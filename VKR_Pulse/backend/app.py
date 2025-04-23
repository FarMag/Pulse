from flask import Flask
from login import login_bp
from backend.register import register_bp
from backend.addFullInformationUser import addFullInformationUser_bp
from backend.token_manager import refresh_token_bp, check_token_bp

app = Flask(__name__)

from db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Привязываем db к текущему приложению

# Регистрируем маршруты из get_data и login
app.register_blueprint(login_bp, url_prefix='/api')
app.register_blueprint(register_bp, url_prefix='/api')
app.register_blueprint(refresh_token_bp, url_prefix='/api')
app.register_blueprint(check_token_bp, url_prefix='/api')
app.register_blueprint(addFullInformationUser_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host='192.168.1.4', port=8080, debug=True)
    # app.run(host='192.168.0.15', port=8080, debug=True)