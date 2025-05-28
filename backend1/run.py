from app.auth.auth_run import app as auth_app
from app.test.test_run import app as test_app
from app.progress.progress_run import app as progress_app
from app.nutrition.nutrition_run import app as nutrition_app
from app.recipes.recipes_run import app as recipes_app
from app.training_plans.training_plans_run import app as training_plans_app
from app.workouts.workouts_run import app as workouts_app
from multiprocessing import Process
from backend1.db.DataBaseConfig import DataBaseConfig
import time

db_config = DataBaseConfig()

def run_auth():
    auth_app.run(host=db_config.host, port=8001, debug=True, use_reloader=False)  # Отключаем авто-перезапуск

def run_test():
    test_app.run(host=db_config.host, port=8002, debug=True, use_reloader=False)  # Отключаем авто-перезапуск

def run_progress():
    progress_app.run(host=db_config.host, port=8003, debug=True, use_reloader=False)

def run_nutrition():
    nutrition_app.run(host=db_config.host, port=8004, debug=True, use_reloader=False)

def run_recipes():
    recipes_app.run(host=db_config.host, port=8005, debug=True, use_reloader=False)

def run_training_plans():
    training_plans_app.run(host=db_config.host, port=8006, debug=True, use_reloader=False)

def run_workouts():
    workouts_app.run(host=db_config.host, port=8007, debug=True, use_reloader=False)

if __name__ == '__main__':
    auth_process = Process(target=run_auth)
    test_process = Process(target=run_test)
    progress_process = Process(target=run_progress)
    nutrition_process = Process(target=run_nutrition)
    recipes_process = Process(target=run_recipes)
    training_plans_process = Process(target=run_training_plans)
    workouts_process = Process(target=run_workouts)

    auth_process.start()
    test_process.start()
    progress_process.start()
    nutrition_process.start()
    recipes_process.start()
    training_plans_process.start()
    workouts_process.start()

    # Ожидаем завершения процессов
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        auth_process.terminate()
        test_process.terminate()
        progress_process.terminate()
        nutrition_process.terminate()
        recipes_process.terminate()
        training_plans_process.terminate()
        workouts_process.terminate()

        auth_process.join()
        test_process.join()
        progress_process.join()
        nutrition_process.join()
        recipes_process.join()
        training_plans_process.join()
        workouts_process.join()


