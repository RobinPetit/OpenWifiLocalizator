# OpenWifiLocalizator (OWL)

## Conventions

When writing python code, please follow the given rules:

- class and exceptions names are in `PascalCase`
- variable and method names are in `lower_case` (refer to PEP8)
- constants are `ALL_UPPER_CASE`
- `mixedCase` is **not** welcome in Python code


## Configuration

To configure GraphMaker you must copy `app/Config.py.init` to `app/Config.py`.
`cp app/Config.py.init app/Config.py`
Here you have an example of config:
```

class Config:

    # Change this according to your network device id
    NETWORK_INTERFACE = 'wlo1'
    # Debug Mode
    DEBUG = True
    # Path to the images map
    MAPS_PATH = '../OWL/app/src/main/assets/'
    # Path to the database
    DB_PATH = '../OWL/app/src/main/assets/OWL-DB.db'


```

If you don't configure GraphMaker, you will get this error:
´´´
ImportError: No module named 'app.Config'
´´´




## Start

To start the application run:
```
sudo python3 GraphMaker.py
```
**in this folder** (`GraphMaker`)

