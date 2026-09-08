import requests
import json

files = {'file': open('app/build/outputs/apk/debug/app-debug.apk', 'rb')}
response = requests.post('https://file.io', files=files)
try:
    print(response.json().get('link'))
except Exception as e:
    print(response.text)
