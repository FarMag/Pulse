# from flask import Flask
# from ...app.auth import login_bp
#
# app = Flask(__name__)
#
# app.register_blueprint(login_bp)
#
# if __name__ == '__main__':
#     app.run(host='192.168.1.4', port=8080, debug=True)



from flask import Flask
from backend1.app.auth.routes import (register_bp, login_bp, refresh_token_bp, check_token_bp,
                                      add_full_information_user_bp, get_user_data_bp, update_user_information_bp, update_user_xp_bp)

app = Flask(__name__)

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Привязываем db к текущему приложению

# Регистрируем маршруты из get_data и login
app.register_blueprint(login_bp, url_prefix='/api')
app.register_blueprint(register_bp, url_prefix='/api')
app.register_blueprint(get_user_data_bp, url_prefix='/api')
app.register_blueprint(add_full_information_user_bp, url_prefix='/api')
app.register_blueprint(update_user_information_bp, url_prefix='/api')
app.register_blueprint(refresh_token_bp, url_prefix='/api')
app.register_blueprint(check_token_bp, url_prefix='/api')
app.register_blueprint(update_user_xp_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host=db_config.host, port=8001, debug=True)
    # app.run(host='0.0.0.0', port=8001, debug=True)
    # app.run(host='192.168.0.15', port=8001, debug=True)