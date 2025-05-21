from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base


Base = declarative_base()

# Определение модели User
class User(Base):
    __tablename__ = 'users'
    id = Column(String)
    username = Column(String)
    email = Column(String)
    email_hash = Column(String, primary_key=True)
    password_hash = Column(String)
    gender = Column(String)
    birth_date = Column(String)
    height = Column(Integer)
    weight = Column(Integer)
    xp = Column(Integer)
    target_weight = Column(Integer)
    target_phis = Column(String)
    phis_train = Column(String)
    notes = Column(String)