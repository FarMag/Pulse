from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base

Base = declarative_base()

class Training_plans(Base):
    __tablename__ = 'training_plans'
    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String)
    description = Column(String)
    goal = Column(String)
    exercises = Column(String)