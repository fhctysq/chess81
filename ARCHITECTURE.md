# Архітектура шахового додатку

```mermaid
graph TD
    %% Основні точки входу та ініціалізації
    Main[MainActivity Lifecycle] --> Bridge[C++ Рушій: Engine81Bridge]
    Main --> Settings[Налаштування: SettingsManager]
    Main --> Media[Медіа: SoundPool / Vibrator]
    
    %% Глобальний стан
    Settings --> GlobalState[Глобальний UI Стан: CompositionLocal]
    
    %% Навігація та Екрани (ОНОВЛЕНИЙ КОНЦЕПТ ФАБРИКИ)
    Main --> Nav[Навігатор: Compose NavHost]
    Nav --> ScreenHome[Головне меню]
    
    Nav --> ScreenFactory[Універсальна Фабрика Екранів]
    note right of ScreenFactory: Визначає режим гри\n(через параметри навігації)\nта підключає потрібну модель
    
    subgraph "Моделі вигляду (Режими)"
        ScreenFactory --> VM_Standard[ViewModel: Стандарт]
        ScreenFactory --> VM_Symmetric[ViewModel: Симетричний\n(для 2 гравців)]
        ScreenFactory --> VM_Puzzle[ViewModel: Пазли / Мініігри]
    end
    
    VM_Standard --> UniversalBoardUI[Універсальний UI Дошки]
    VM_Symmetric --> UniversalBoardUI
    VM_Puzzle --> UniversalBoardUI
    
    GlobalState -.-> UniversalBoardUI
    
    %% Концепт: Логіка гри
    UniversalBoardUI --> GameState[Стан Партії / Моделі]
    
    %% Концепт: Взаємодія та Хід
    UniversalBoardUI --> UserInput[Клік по клітинці / Свайп]
    UserInput --> MoveValidation[Валідатор Ходів]
    MoveValidation --> GameState
    
    %% Концепт: Робота рушія (Бота / Підказок)
    GameState --> EngineReq[Запит до JNI]
    Bridge --> EngineReq
    EngineReq --> EngineRes[Відповідь: Хід / Оцінка]
    EngineRes --> GameState
    
    %% Концепт: Анімація
    GameState --> AnimState[Компоненти Анімації: Offset / Coroutines]
    AnimState --> AnimAction[Анімація працює: Переміщення]
    AnimAction --> UniversalBoardUI
    
    %% Концепт: Фідбек
    GameState --> Feedback[Виклик звуку / вібрації]
    Media --> Feedback
