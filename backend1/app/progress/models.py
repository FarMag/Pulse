from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base


Base = declarative_base()

# Определение модели User
class ProgressLog(Base):
    __tablename__ = 'progress_logs'
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String)
    date = Column(String)
    weight = Column(String)
    body_fat_percentage = Column(String)
    steps = Column(String)
    calories_burned = Column(String)




