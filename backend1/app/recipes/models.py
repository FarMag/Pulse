from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base

Base = declarative_base()

class Recipes(Base):
    __tablename__ = 'recipes'
    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String)
    description = Column(String)
    calories = Column(String)
    protein = Column(String)
    fat = Column(String)
    carbohydrates = Column(String)
    ingredient_name = Column(String)