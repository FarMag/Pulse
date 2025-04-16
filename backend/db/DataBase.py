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
                if user:  # Проверяем, найден ли пользователь
                    # dbpassword = user.Password.encode('utf-8')  # Кодируем хэшированный пароль
                    dbpassword = user.password_hash.encode('utf-8')  # Кодируем хэшированный пароль
                    if bcrypt.checkpw(password.encode('utf-8'), dbpassword):  # Проверяем пароль
                        # print(user)
                        return user  # Вернуть данные пользователя
            except SQLAlchemyError as e:
                print(f"Error: {e}")
        return None  # Вернуть None, если аутентификация не удалась

    # def register_user(self, name, email, password, user_id):
    def register_user(self, username, email, password, birth_date_str, gender):
        if self.session:
            try:
                # Проверяем на наличие дубликата email
                existing_user = self.session.query(User).filter_by(email=email).first()
                if existing_user:
                    # print(f"Ошибка: данная почта '{email}' уже существует.")
                    return False  # Возвращаем False, если email уже существует

                # Добавляем дату рождения
                birth_date = datetime.strptime(birth_date_str, "%d.%m.%Y")
                # Хешируем пароль
                hashed_password = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
                new_user = User(username=username, email=email, password_hash=hashed_password.decode('utf-8'), birth_date=birth_date.strftime("%Y-%m-%d"), gender=gender)

                self.session.add(new_user)
                self.session.commit()

                # user = self.log_in(email, password)
                # print("user = ", user.email)
                # return True  # Возвращаем True, если регистрация успешна
                return new_user
            except SQLAlchemyError as e:
                self.session.rollback()  # Откат транзакции в случае ошибки
                # print(f"Error: {e}")
        # return False  # Возвращаем False, если регистрация не удалась
        return None

    # def get_id_by_email(self, email, password):
    #     if self.session:
    #         try:
    #             self.log_in(email, password)

    def add_full_information_user(self, id, phis_train, height, weight, target_phis, target_weight):
        if self.session:
            try:
                # Находим пользователя по email
                user = self.session.query(User).filter_by(id=id).first()
                if user:
                    # Обновляем данные пользователя
                    user.phis_train = phis_train
                    user.height = int(height)
                    user.weight = int(weight)
                    user.target_phis = target_phis
                    user.target_weight = int(target_weight)

                    # Сохраняем изменения в базе данных
                    self.session.commit()
                    return True
                else:
                    # print("Пользователь не найден.")
                    return False
            except SQLAlchemyError as e:
                print(f"Error: {e}")
                self.session.rollback()  # Откатываем изменения в случае ошибки
        return False



