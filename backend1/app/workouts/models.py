from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base


Base = declarative_base()

# Определение модели User
class Workouts(Base):
    __tablename__ = 'workouts'
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String)
    plan_id = Column(String)
    date = Column(String)
    duration_time = Column(String)



