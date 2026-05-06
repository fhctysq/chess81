Українською:
chess 81 — це Android-реалізація шахового варіанту на дошці 9×9, написана на Kotlin з використанням Jetpack Compose.
в основі застосунку власний рушій на C++, підімкнений через JNI:
він відповідає за правила гри, генерацію ходів, валідацію легальних ходів, та пошук кращого ходу шахового бота.
UI-шар залишається медіумом між гравцем і рушієм. доступні кілька режимів гри: локальна гра на одному пристрої,
партії проти бота з налаштовуваною глибиною пошуку.
в розробці онлайн-мультиплеєр через Firebase Realtime Database (пізніше планується перехід на сервери GooglePlay або Lichess),
задачі та вільна тренувальна дошка для аналізу позицій.

інтерфейс підтримує кілька тем дошки, плавні анімації ходів, панель нотації, ігрові таймери та систему скасування ходів.
завершені партії автоматично зберігаються в архів, а незавершені відновлюються при наступному запуску.
застосунок адаптується до портретної та альбомної орієнтацій з підтримкою edge-to-edge і керуванням системними панелями.

це рання версія, в котрій може щось не працювати чи не бути звичних функцій. напишіть, якщо виявите баги.

English:
chess 81 is an Android implementation of a 9×9 chess variant, built with Kotlin and Jetpack Compose.
at its core is a custom C++ engine connected via JNI:
it handles the rules of the game, move generation, legal move validation, and best-move search for the chess bot.
the UI layer acts as a medium between the player and the engine. several game modes are available: local hotseat on a single device,
and games against the bot with adjustable search depth.
online multiplayer via Firebase Realtime Database is in development (with a later planned migration to GooglePlay or Lichess servers),
along with puzzles and a freeform training board for position analysis.

the interface supports multiple board themes, smooth move animations, a notation panel, game timers, and an undo system.
completed games are automatically saved to an archive, and unfinished games are restored on the next session.
the app adapts to both portrait and landscape orientations with edge-to-edge support and system bar handling.

this is an early release — some features may be missing or not working as expected. feel free to report any bugs you find.
