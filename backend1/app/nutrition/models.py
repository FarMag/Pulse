from sqlalchemy import create_engine, Column, String, Integer
from sqlalchemy.orm import sessionmaker, declarative_base

Base = declarative_base()

class NutritionLog(Base):
    __tablename__ = 'nutrition_logs'
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(String)
    recipe_id = Column(String)
    date = Column(String)
    meal_type = Column(String)
    food_name = Column(String)
    calories = Column(String)
    protein = Column(String)
    fat = Column(String)
    carbohydrates = Column(String)
    grams = Column(String)

