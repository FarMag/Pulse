import bcrypt
from datetime import datetime
from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy.exc import SQLAlchemyError
from .DataBaseConfig import DataBaseConfig

Base = declarative_base()

# Определение модели User
# class User(Base):
#     __tablename__ = 'users'
#     id = Column(String)
#     username = Column(String)
#     email = Column(String, primary_key=True)
#     password_hash = Column(String)
#     gender = Column(String)
#     birth_date = Column(String)
#     height = Column(Integer)
#     weight = Column(Integer)
#     target_weight = Column(Integer)
#     phis_train = Column(String)


class DataBase:
    def __init__(self):
        self.session = None
        db_config = DataBaseConfig()
        self.database_url = f"mysql+pymysql://{db_config.username}:{db_config.password}@{db_config.servername}/{db_config.databasename}"

    def db_connect(self):
        try:
            engine = create_engine(self.database_url)
            Session = sessionmaker(bind=engine)
            self.session = Session()
        except SQLAlchemyError as e:
            print(f"Error: {e}")
        return self.session
