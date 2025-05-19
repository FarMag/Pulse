from app.auth.auth_run import app as auth_app
from app.test.test_run import app as test_app
from app.progress.progress_run import app as progress_app
from app.nutrition.nutrition_run import app as nutrition_app
from app.recipes.recipes_run import app as recipes_app
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

if __name__ == '__main__':
    auth_process = Process(target=run_auth)
    test_process = Process(target=run_test)
    progress_process = Process(target=run_progress)
    nutrition_process = Process(target=run_nutrition)
    recipes_process = Process(target=run_recipes)

    auth_process.start()
    test_process.start()
    progress_process.start()
    nutrition_process.start()
    recipes_process.start()

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
        auth_process.join()
        test_process.join()
        progress_process.join()
        nutrition_process.join()
        recipes_process.join()


