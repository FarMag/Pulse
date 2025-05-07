# from backend1.app.auth.auth_run import app as auth_app
# # from app.product.product_run import app as product_app
# from threading import Thread
#
# def run_auth():
#     auth_app.run(host='192.168.1.4', port=8001, debug=False)
#     # auth_app.run(host='0.0.0.0', port=8001, debug=True)
#
# # def run_product():
# #     product_app.run(host='192.168.1.4', port=8002, debug=True)
#
# def run_all():
#     auth_thread = Thread(target=run_auth)
#     # product_thread = Thread(target=run_product)
#
#     auth_thread.start()
#     # product_thread.start()
#
#     auth_thread.join()
#     # product_thread.join()
#
# if __name__ == '__main__':
#     run_all()  # Запускаем сразу все модули при запуске run.py









# from app.auth.auth_run import app as auth_app
# from multiprocessing import Process
#
# def run_auth():
#     auth_app.run(host='192.168.1.4', port=8001, debug=True)
#
# if __name__ == '__main__':
#     auth_process = Process(target=run_auth)
#     auth_process.start()
#     auth_process.join()  # Это ожидает завершения процесса, если необходимо







from app.auth.auth_run import app as auth_app
from app.test.test_run import app as test_app
from app.progress.progress_run import app as progress_app
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

if __name__ == '__main__':
    auth_process = Process(target=run_auth)
    test_process = Process(target=run_test)
    progress_process = Process(target=run_progress)

    auth_process.start()
    test_process.start()
    progress_process.start()

    # Ожидаем завершения процессов
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        auth_process.terminate()
        test_process.terminate()
        progress_process.terminate()
        auth_process.join()
        test_process.join()
        progress_process.join()



