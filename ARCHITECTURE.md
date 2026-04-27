# Архітектура шахового додатку

```mermaid
graph TD
    %% Глобальні стилі для вузлів: більший шрифт, заокруглення
    classDef default font-size:18px, rx:8px, ry:8px;
    classDef note fill:#fff5ad,stroke:#d6b656,color:#333,font-size:16px;

    %% Основні входи та ініціалізації
    Main["MainActivity<br/>Lifecycle"] --> Bridge["C++ Рушій:<br/>Engine81Bridge"]
    Main --> Settings["Налаштування:<br/>SettingsManager"]
    Main --> Media["Медіа:<br/>SoundPool / Vibration"]
    
    %% Глобальний стан
    Settings --> GlobalState["Глобальний UI Стан:<br/>CompositionLocal"]
    
    %% Навігація та Екрани
    Main --> Nav["Навігатор:<br/>Compose NavHost"]
    Nav --> ScreenHome["Головне<br/>меню"]
    Nav --> ScreenFactory["Універсальна<br/>Фабрика Екранів"]
    
    %% Компактна нотатка з ручним розривом
    ScreenFactory -.-> FactoryNote["Визначає режим гри<br/>через параметри<br/>навігації та вмикає<br/>відповідну модель"]
    class FactoryNote note;
    
    subgraph "Моделі вигляду (Режими)"
        ScreenFactory --> VM_Standard["ViewModel:<br/>Стандарт"]
        ScreenFactory --> VM_Symmetric["ViewModel:<br/>Симетричний<br/>(2 гравці)"]
        ScreenFactory --> VM_Puzzle["ViewModel:<br/>Пазли /<br/>Мініігри"]
    end
    
    VM_Standard --> UniversalBoardUI["Універсальний<br/>UI Дошки"]
    VM_Symmetric --> UniversalBoardUI
    VM_Puzzle --> UniversalBoardUI
    
    GlobalState -.-> UniversalBoardUI
    
    %% Концепт: Логіка гри
    UniversalBoardUI --> GameState["Стан Партії /<br/>Моделі"]
    
    %% Концепт: Взаємодія та Хід
    UniversalBoardUI --> UserInput["Ввід:<br/>Клік / Свайп"]
    UserInput --> MoveValidation["Валідатор<br/>Ходів"]
    MoveValidation --> GameState
    
    %% Концепт: Робота рушія
    GameState --> EngineReq["Запит<br/>до JNI"]
    Bridge --> EngineReq
    EngineReq --> EngineRes["Відповідь:<br/>Хід / Оцінка"]
    EngineRes --> GameState
    
    %% Концепт: Анімація
    GameState --> AnimState["Компоненти Анімації:<br/>Offset / Coroutines"]
    AnimState --> UniversalBoardUI
    
    %% Концепт: Фідбек
    GameState --> Feedback["Виклик звуку<br/>чи вібрації"]
    Media --> Feedback
