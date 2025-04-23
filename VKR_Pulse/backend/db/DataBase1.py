import bcrypt
from datetime import datetime
from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy.exc import SQLAlchemyError
from .DataBaseConfig import DataBaseConfig

Base = declarative_base()

# Определение модели User
class User(Base):
    __tablename__ = 'users'
    id = Column(String)
    username = Column(String)
    email = Column(String, primary_key=True)
    password_hash = Column(String)
    gender = Column(String)
    birth_date = Column(String)
    height = Column(Integer)
    weight = Column(Integer)
    target_weight = Column(Integer)
    phis_train = Column(String)


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

    def log_in(self, email, password):
        if self.session:
            try:
                user = self.session.query(User).filter_by(email=email).first()
                if user:
                    dbpassword = user.password_hash.encode('utf-8')
                    if bcrypt.checkpw(password.encode('utf-8'), dbpassword):
                        return user
            except SQLAlchemyError as e:
                print(f"Error: {e}")
        return None

    def register_user(self, username, email, password, birth_date_str, gender):
        if self.session:
            try:
                existing_user = self.session.query(User).filter_by(email=email).first()
                if existing_user:
                    return False

                birth_date = datetime.strptime(birth_date_str, "%d.%m.%Y")
                hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
                new_user = User(username=username, email=email, password_hash=hashed_password.decode('utf-8'), birth_date=birth_date.strftime("%Y-%m-%d"), gender=gender)

                self.session.add(new_user)
                self.session.commit()
                return True
            except SQLAlchemyError as e:
                self.session.rollback()
        return False


    def add_full_information_user(self, email, height, weight, target_weight):
        if self.session:
            try:
                # Находим пользователя по email
                user = self.session.query(User).filter_by(email=email).first()
                if user:
                    # Обновляем данные пользователя
                    user.height = height
                    user.weight = weight
                    user.target_weight = target_weight

                    # Сохраняем изменения в базе данных
                    self.session.commit()
                    return True
                else:
                    print("Пользователь не найден.")
                    return False
            except SQLAlchemyError as e:
                print(f"Error: {e}")
                self.session.rollback()  # Откатываем изменения в случае ошибки
        return False




