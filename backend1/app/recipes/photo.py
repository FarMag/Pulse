# import os
#
# # Путь к папке с изображениями
# folder_path = r"C:\Users\manan\PycharmProjects\VKR_Pulse\food_images"
#
# # Перебираем все файлы в папке
# for filename in os.listdir(folder_path):
#     # Проверяем, что файл имеет нужный формат
#     if filename.endswith('.jpg'):
#         # Извлекаем номер файла без расширения
#         try:
#             file_number = int(os.path.splitext(filename)[0])
#             new_number = file_number - 15
#             new_filename = f"{new_number}.jpg"
#
#             # Формируем полные пути
#             old_file_path = os.path.join(folder_path, filename)
#             new_file_path = os.path.join(folder_path, new_filename)
#
#             # Переименовываем файл
#             os.rename(old_file_path, new_file_path)
#             print(f"Переименовано: {filename} -> {new_filename}")
#         except ValueError:
#             print(f"Пропущен файл: {filename} (некорректный формат номера)")
#
# print("Переименование завершено.")






# import os
#
# # Статичное значение ID
# id = 840
#
# # Путь к папке с изображениями (относительно файла routes.py)
# # images_folder = os.path.join('..', '..', 'food_images')
# images_folder = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'food_images')
#
# # Формируем имя файла
# image_filename = f'{id}.jpg'
# image_path = os.path.join(images_folder, image_filename)
#
# # Проверяем, существует ли файл
# if os.path.isfile(image_path):
#     print(f"Путь к изображению: {image_path}")
# else:
#     print("Изображение не найдено")







import os
import base64
from PIL import Image
from io import BytesIO
import json

# Статичное значение ID
id = 840

# Путь к папке с изображениями
images_folder = os.path.join(os.path.dirname(__file__), '..', '..', '..', 'food_images')

# Формируем имя файла
image_filename = f'{id}.jpg'
image_path = os.path.join(images_folder, image_filename)

# Проверяем, существует ли файл
if os.path.isfile(image_path):
    print(f"Путь к изображению: {image_path}")

    # Преобразуем изображение в base64
    with Image.open(image_path) as img:
        buffered = BytesIO()
        img.save(buffered, format="JPEG")  # сохраняем изображение во временный буфер
        img_base64 = base64.b64encode(buffered.getvalue()).decode('utf-8')  # байты → base64 → str

    # Создаём JSON-ответ с изображением
    data = {
        "id": id,
        "image": img_base64
    }

    # Пример вывода JSON
    json_output = json.dumps(data, ensure_ascii=False)
    print(json_output)

else:
    print("Изображение не найдено")

