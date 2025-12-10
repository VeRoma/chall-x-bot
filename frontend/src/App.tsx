import { useEffect, useState } from 'react';
import './App.css';

// --- ТИПЫ ДАННЫХ ---

// Состояние пользователя
type UserState = {
  id: number;
  username: string;
  firstName: string;
} | null;

// Тип Темы (Topic) из БД
type Topic = {
  id: number;
  name: string;
  // Поддержка и 'isActive' (Java поле), и 'active' (если Jackson сократит)
  isActive?: boolean;
  active?: boolean;
};

// Тип Урока (Lesson) из БД
type LessonData = {
  id: number;
  title: string;
  content: string;
};

// Возможные экраны приложения
type ViewState = 'login' | 'topics' | 'lesson';

function App() {
  // --- СОСТОЯНИЕ (STATE) ---
  const [user, setUser] = useState<UserState>(null);
  const [status, setStatus] = useState<string>('Загрузка...');

  // Навигация и данные
  const [view, setView] = useState<ViewState>('login');
  const [topics, setTopics] = useState<Topic[]>([]);
  const [currentLesson, setCurrentLesson] = useState<string | null>(null);

  // --- ИНИЦИАЛИЗАЦИЯ ---
  useEffect(() => {
    // Проверяем Telegram WebApp
    const tg = window.Telegram?.WebApp;
    if (tg) {
      tg.ready();

      // ИСПРАВЛЕНИЕ: Используем 'as any', чтобы TypeScript не ругался на expand
      try {
        (tg as any).expand();
      } catch (e) {
        console.log('Expand failed or not supported', e);
      }

      const tgUser = tg.initDataUnsafe?.user;
      if (tgUser) {
        setUser({
          id: tgUser.id,
          username: tgUser.username || 'Аноним',
          firstName: tgUser.first_name
        });
        setStatus('Готово к входу');
        return;
      }
    }

    // Режим разработки (в браузере)
    console.log("Telegram не найден. Режим разработки.");
    setUser({ id: 12345, username: 'developer', firstName: 'Dev' });
    setStatus('Dev Mode (Localhost)');
  }, []);

  // --- ЛОГИКА ---

  // 1. Вход в систему
  const handleRegister = async () => {
    if (!user) return;
    setStatus('Вход в систему...');

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          tgId: user.id,
          username: user.username,
          firstName: user.firstName
        }),
      });

      if (response.ok) {
        // Если вход успешен -> сразу грузим темы
        await fetchTopicsAndRedirect();
      } else {
        setStatus('Ошибка при входе на сервер');
      }
    } catch (e) {
      console.error(e);
      setStatus('Ошибка сети. Бэкенд запущен?');
    }
  };

  // 2. Загрузка списка тем
  const fetchTopicsAndRedirect = async () => {
    setStatus('Загрузка тем курса...');

    try {
      const response = await fetch('/api/topics');

      if (!response.ok) {
        throw new Error('Ошибка при загрузке тем');
      }

      const realTopics = await response.json() as Topic[];

      // Если база пуста (бывает при первом запуске)
      if (realTopics.length === 0) {
        setTopics([{ id: 0, name: "Нет тем в базе", isActive: true }]);
      } else {
        setTopics(realTopics);
      }

      // Переключаем экран
      setView('topics');
    } catch (e) {
      console.error(e);
      setStatus('Не удалось загрузить темы');
    }
  };

  // 3. Загрузка урока при клике на тему
  const handleTopicClick = async (topic: Topic) => {
    setStatus(`Загрузка урока: ${topic.name}...`);

    try {
      // Идем на наш новый контроллер LessonController
      const response = await fetch(`/api/lessons/by-topic/${topic.id}`);

      if (!response.ok) {
        throw new Error('Урок не найден');
      }

      const lessonData = await response.json() as LessonData;

      // Формируем текст для отображения
      setCurrentLesson(`📖 ${lessonData.title}\n\n${lessonData.content}`);
      setView('lesson');
      setStatus('Урок открыт');
    } catch (e) {
      console.error(e);
      setCurrentLesson(`⚠ Упс! Урок для темы "${topic.name}" еще не готов.\n\nВозможно, Gemini не успел его сгенерировать или произошла ошибка на сервере.`);
      setView('lesson');
    }
  };

  // 4. Кнопка "Назад"
  const handleBackToTopics = () => {
    setView('topics');
    setCurrentLesson(null);
    setStatus('Список тем');
  };

  // --- РЕНДЕР (ИНТЕРФЕЙС) ---

  // Экран 1: Вход
  if (view === 'login') {
    return (
      <div className="card">
        <h1>Chall X Bot</h1>
        <div style={{ fontSize: '40px', margin: '20px' }}>🤖</div>
        <p>Статус: {status}</p>

        {user && (
          <div style={{ marginTop: '20px' }}>
            <p>Привет, {user.firstName}!</p>
            <button onClick={handleRegister} className="primary-btn">
              Начать обучение 🚀
            </button>
          </div>
        )}
      </div>
    );
  }

  // Экран 2: Список тем
  if (view === 'topics') {
    return (
      <div className="container">
        <h2>🇬🇧 English Course</h2>
        <p className="subtitle">Выберите тему для изучения:</p>

        <div className="topics-grid">
          {topics.map(topic => {
            // Проверка обоих полей (на случай разного JSON)
            const isTopicActive = topic.active !== undefined ? topic.active : topic.isActive;

            return (
              <button
                key={topic.id}
                className="topic-card"
                onClick={() => handleTopicClick(topic)}
                disabled={!isTopicActive}
              >
                <span className="topic-icon">{isTopicActive ? '📚' : '🔒'}</span>
                <span className="topic-name">{topic.name}</span>
              </button>
            );
          })}
        </div>
      </div>
    );
  }

  // Экран 3: Просмотр урока
  if (view === 'lesson') {
    return (
      <div className="container">
        <button onClick={handleBackToTopics} className="back-btn">⬅ Назад к темам</button>

        <div className="lesson-content">
          {/* whiteSpace: 'pre-wrap' сохраняет переносы строк из Gemini */}
          <div style={{ whiteSpace: 'pre-wrap', textAlign: 'left', lineHeight: '1.6' }}>
            {currentLesson}
          </div>

          <button className="primary-btn" style={{ marginTop: '30px', width: '100%' }}>
            ✅ Пройти тест (Quiz)
          </button>
        </div>
      </div>
    );
  }

  return null;
}

export default App;