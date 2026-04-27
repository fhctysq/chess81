# Архітектура

```mermaid
graph TD
    %% Глобальні стилі для вузлів: більший шрифт, заокруглення
    classDef default font-size:20px, rx:8px, ry:8px;
    classDef note fill:#fff5ad,stroke:#d6b656,color:#333,font-size:18px;

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
    
    %% Моделі вигляду
    subgraph "Моделі вигляду (Режими)"
        VM_Standard["ViewModel:<br/>Стандарт / Бот"]
        VM_Online["ViewModel:<br/>Онлайн (Firebase)"]
        VM_Symmetric["ViewModel:<br/>Симетричний<br/>(2 гравці)"]
        VM_Puzzle["ViewModel:<br/>Пазли /<br/>Мініігри"]
        
        %% Хак: невидимі ребра для вертикального вирівнювання блоку
        VM_Standard ~~~ VM_Online
        VM_Online ~~~ VM_Symmetric
        VM_Symmetric ~~~ VM_Puzzle
    end
    
    ScreenFactory --> VM_Standard
    ScreenFactory --> VM_Online
    ScreenFactory --> VM_Symmetric
    ScreenFactory --> VM_Puzzle
    
    %% Ієрархія UI
    subgraph "Композиція UI дошки"
        BoardLayout["BoardElementsLayout<br/>(або DualGameScreen)"] --> Wrapper["ChessBoardWrapper<br/>(Масштаб/Осі)"]
        Wrapper --> Board["ChessBoard<br/>(Сітка)"]
        Board --> Square["ChessSquare<br/>(Клітинка)"]
    end
    
    VM_Standard --> BoardLayout
    VM_Online --> BoardLayout
    VM_Symmetric --> BoardLayout
    VM_Puzzle --> BoardLayout
    
    GlobalState -.-> BoardLayout
    
    %% Концепт: Логіка гри
    Square --> GameState["Стан Партії /<br/>Моделі"]
    
    %% Концепт: Взаємодія та Хід
    Square --> UserInput["Ввід:<br/>Клік / Свайп"]
    UserInput --> MoveValidation["Валідатор<br/>Ходів"]
    MoveValidation --> GameState
    
    %% Концепт: Робота рушія
    GameState --> EngineReq["Запит<br/>до JNI"]
    Bridge --> EngineReq
    EngineReq --> EngineRes["Відповідь:<br/>Хід / Оцінка"]
    EngineRes --> GameState
    
    %% Концепт: Анімація
    GameState --> AnimState["Компоненти Анімації:<br/>Offset / Coroutines"]
    AnimState --> Square
    
    %% Концепт: Фідбек
    GameState --> Feedback["Виклик звуку<br/>чи вібрації"]
    Media --> Feedback
