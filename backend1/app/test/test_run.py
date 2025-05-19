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
from backend1.app.test.routes import test_bp

app = Flask(__name__)

from backend1.db.DataBaseConfig import DataBaseConfig
db_config = DataBaseConfig()
app.config['SQLALCHEMY_DATABASE_URI'] = f'mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

# Привязываем db к текущему приложению

# Регистрируем маршруты из get_data и login
app.register_blueprint(test_bp, url_prefix='/api')

if __name__ == '__main__':
    app.run(host='192.168.0.19', port=8002, debug=True)
    # app.run(host='0.0.0.0', port=8001, debug=True)
    # app.run(host='192.168.0.15', port=8001, debug=True)