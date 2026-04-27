# Архітектура шахового додатку

```mermaid
graph TD
    Main[MainActivity Lifecycle] --> Bridge[C++ Рушій: Engine81Bridge]
    Main --> Settings[Налаштування: SettingsManager]
    Main --> Media[Медіа: SoundPool / Vibrator]
    
    Settings --> GlobalState[Глобальний UI Стан: CompositionLocal]
    
    Main --> Nav[Навігатор: Compose NavHost]
    Nav --> ScreenHome[Головне меню]
    Nav --> ScreenGame[Екрани Гри: Локальна / Онлайн]
    Nav --> ScreenTrain[Екран Редактора / Тренувань]
    
    GlobalState -.-> ScreenGame
    
    ScreenGame --> BoardUI[UI Дошки та Фігур]
    ScreenGame --> GameState[Стан Партії: ChessState / MoveRecord]
    
    BoardUI --> UserInput[Клік по клітинці]
    UserInput --> MoveValidation[Валідатор Ходів]
    MoveValidation --> GameState
    
    GameState --> EngineReq[Запит до JNI]
    Bridge --> EngineReq
    EngineReq --> EngineRes[Відповідь: Хід / Оцінка]
    EngineRes --> GameState
    
    GameState --> AnimState[Компоненти Анімації: Offset / Coroutines]
    AnimState --> AnimAction[Анімація працює: Переміщення]
    AnimAction --> BoardUI
    
    GameState --> Feedback[Виклик звуку / вібрації]
    Media --> Feedback
