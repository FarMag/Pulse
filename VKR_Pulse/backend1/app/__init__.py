# from flask import Flask
# from flask_sqlalchemy import SQLAlchemy
# from config import Config
#
# db = SQLAlchemy()
#
#
# def create_app():
#     app = Flask(__name__)
#     app.config.from_object(Config)
#     db.init_app(app)
#
#     from .auth.routes import auth as auth_blueprint
#     from .product.routes import product as product_blueprint
#
#     app.register_blueprint(auth_blueprint)
#     app.register_blueprint(product_blueprint)
#
#     return app
